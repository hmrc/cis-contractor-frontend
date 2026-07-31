/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import connectors.ConstructionIndustrySchemeConnector
import models.agent.AgentClientData
import models.{EmployerReference, Subcontractor, UserAnswers}
import models.requests.*
import models.response.{ChrisPollResponse, ChrisSubmissionResponse, CreateSubmissionForVerificationResponse}
import models.verify.*
import pages.verify.*
import play.api.libs.json.*
import play.api.mvc.AnyContent
import queries.CisIdQuery
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.SubmissionUtils

import java.time.{Clock, LocalDateTime, ZoneId}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerificationService @Inject() (
  cisConnector: ConstructionIndustrySchemeConnector,
  cisManageService: CisManageService,
  chrisVerificationRequestBuilder: ChrisVerificationRequestBuilder,
  sessionRepository: SessionRepository,
  submissionUtils: SubmissionUtils,
  clock: Clock
)(implicit ec: ExecutionContext) {

  private val ukZone = ZoneId.of("Europe/London")

  def refreshNewestVerificationBatch(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers] =
    for {
      instanceId <- userAnswers
                      .get(CisIdQuery)
                      .map(Future.successful)
                      .getOrElse(Future.failed(new RuntimeException("InstanceIdQuery not found in session data")))

      response  <- cisConnector.getNewestVerificationBatch(instanceId)
      unverified = unverifiedSubcontractors(response.subcontractors)

      updated <- Future.fromTry(
                   userAnswers
                     .set(NewestVerificationBatchResponsePage, response)
                     .flatMap(_.set(UnverifiedSubcontractorsPage, unverified))
                 )

      _ <- sessionRepository.set(updated)
    } yield updated

  def getCurrentVerificationBatch(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers] =
    for {
      instanceId <- userAnswers
                      .get(CisIdQuery)
                      .map(Future.successful)
                      .getOrElse(Future.failed(new RuntimeException("InstanceIdQuery not found in session data")))

      response <- cisConnector.getCurrentVerificationBatch(instanceId)
      updated  <- Future.fromTry(userAnswers.set(CurrentVerificationBatchResponsePage, response))
      _        <- sessionRepository.set(updated)
    } yield updated

  def createVerificationBatchAndVerifications(
    userAnswers: UserAnswers,
    selectedSubcontractorIds: Seq[Long],
    actionIndicator: Option[String] = None
  )(implicit hc: HeaderCarrier): Future[UserAnswers] =
    for {
      instanceId <- userAnswers
                      .get(CisIdQuery)
                      .map(Future.successful)
                      .getOrElse(Future.failed(new RuntimeException("InstanceIdQuery not found in session data")))

      _ <- if (selectedSubcontractorIds.nonEmpty) Future.successful(())
           else Future.failed(new RuntimeException("No subcontractors selected"))

      current <-
        userAnswers
          .get(CurrentVerificationBatchResponsePage)
          .map(Future.successful)
          .getOrElse(
            Future.failed(new RuntimeException("CurrentVerificationBatchResponsePage not found in session data"))
          )

      idToRef = current.subcontractors.flatMap(s => s.subbieResourceRef.map(ref => s.subcontractorId -> ref)).toMap

      verificationResourceRefs <- Future.fromTry {
                                    scala.util.Try {
                                      selectedSubcontractorIds.distinct.map { id =>
                                        idToRef.getOrElse(
                                          id,
                                          throw new RuntimeException(
                                            s"Missing subbieResourceRef for subcontractorId=$id in current verification batch"
                                          )
                                        )
                                      }
                                    }
                                  }

      _ <- cisConnector.createVerificationBatchAndVerifications(
             CreateVerificationBatchAndVerificationsRequest(
               instanceId = instanceId,
               verificationResourceReferences = verificationResourceRefs,
               actionIndicator = actionIndicator
             )
           )

      afterCurrent <- getCurrentVerificationBatch(userAnswers)
      afterNewest  <- refreshNewestVerificationBatch(afterCurrent)
      _            <- sessionRepository.set(afterNewest)
    } yield afterNewest

  private def unverifiedSubcontractors(
    subcontractors: Seq[Subcontractor]
  ): Seq[Subcontractor] =
    subcontractors.filter(isUnverified)

  private def isUnverified(sub: Subcontractor): Boolean =
    !sub.verified.contains("Y")

  def modifyVerificationBatchAndVerifications(
    userAnswers: UserAnswers,
    request: ModifyVerificationsRequest
  )(implicit hc: HeaderCarrier): Future[UserAnswers] =
    for {
      _            <- cisConnector.modifyVerificationBatch(request)
      afterCurrent <- getCurrentVerificationBatch(userAnswers)
      afterNewest  <- refreshNewestVerificationBatch(afterCurrent)
      _            <- sessionRepository.set(afterNewest)
    } yield afterNewest

  def createSubmitAndPersistVerificationSubmission(implicit
    request: DataRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[ChrisSubmissionResponse] =
    for {
      latestUa      <- getCurrentVerificationBatch(request.userAnswers)
      createRequest <- buildCreateSubmissionRequest(latestUa)
      submissionId  <- createSubmissionForVerification(createRequest).map(_.submissionId)
      response      <- submitVerificationToChris(submissionId, latestUa)
      updatedUa     <- saveVerificationSubmissionDetailsToSession(latestUa, response)
    } yield response

  def pollStatusAndPersist(
    ua: UserAnswers,
    submissionDetails: VerificationSubmissionDetails
  )(implicit hc: HeaderCarrier): Future[ChrisPollResponse] =
    for {
      pollUrl          <- required(submissionDetails.pollUrl, "Poll URL missing in submission details")
      response         <- cisConnector.getSubmissionStatus(pollUrl, submissionDetails.submissionId)
      effectiveResponse = response.copy(status = effectivePollStatus(response, submissionDetails.submittedAt))
      updatedDetails    = VerificationSubmissionDetailsBuilder.updateFromPollResponse(submissionDetails, effectiveResponse)
      updatedUa        <- saveVerificationPollDetailsToSession(ua, updatedDetails)
    } yield effectiveResponse

  // F18: while ChRIS is still processing (or its poll endpoint is erroring) the backend keeps
  // reporting ACCEPTED/PENDING; once the polling window is exhausted the user must be routed
  // to "send error" (SM-06) if polls were failing with timeOut errors, or "in progress" otherwise.
  private def effectivePollStatus(
    response: ChrisPollResponse,
    submittedAt: LocalDateTime
  ): SubmissionStatus =
    response.status match {
      case SubmissionStatus.ACCEPTED | SubmissionStatus.PENDING if pollingWindowExhausted(submittedAt) =>
        if (isTimeoutError(response)) SubmissionStatus.SEND_ERROR else SubmissionStatus.TIMED_OUT
      case other                                                                                       =>
        other
    }

  private def pollingWindowExhausted(submittedAt: LocalDateTime): Boolean =
    !LocalDateTime.now(clock.withZone(ukZone)).isBefore(submissionUtils.calculateTimeoutDateTime(submittedAt))

  private def isTimeoutError(response: ChrisPollResponse): Boolean = {
    val hasTimeoutGovTalkError = response.error.exists { error =>
      (error \ "type").asOpt[String].exists(_.equalsIgnoreCase("timeOut"))
    }
    val hasNoResponseStatus    = response.govTalkErrorStatus.exists {
      case GovTalkErrorStatus.ServerError(_) | GovTalkErrorStatus.NoResponse => true
      case _                                                                 => false
    }
    hasTimeoutGovTalkError || hasNoResponseStatus
  }

  private def createSubmissionForVerification(
    request: CreateSubmissionForVerificationRequest
  )(implicit hc: HeaderCarrier): Future[CreateSubmissionForVerificationResponse] =
    cisConnector.createSubmissionForVerification(request)

  private def submitVerificationToChris(
    submissionId: Long,
    ua: UserAnswers
  )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[ChrisSubmissionResponse] =
    for {
      employerReference <- resolveEmployerReference(request.userId, request.isAgent, request.employerReference)
      chrisRequest      <- chrisVerificationRequestBuilder.build(ua, request.isAgent, employerReference)
      result            <- cisConnector.submitVerificationToChris(submissionId, chrisRequest)
    } yield result

  private def buildCreateSubmissionRequest(
    ua: UserAnswers
  ): Future[CreateSubmissionForVerificationRequest] =
    CreateSubmissionForVerificationRequestBuilder
      .build(ua)
      .fold(
        error => Future.failed(new RuntimeException(error)),
        request => Future.successful(request)
      )

  private def resolveEmployerReference(
    userId: String,
    isAgent: Boolean,
    employerReference: Option[EmployerReference]
  )(implicit hc: HeaderCarrier): Future[EmployerReference] =
    if (isAgent) {
      cisManageService.getAgentClient(userId).flatMap {
        case Some(data) => Future.successful(EmployerReference(data.taxOfficeNumber, data.taxOfficeReference))
        case None       => Future.failed(new RuntimeException("Employer reference missing for agent user"))
      }
    } else {
      employerReference match {
        case Some(er) => Future.successful(er)
        case None     => Future.failed(new RuntimeException("Employer reference missing for non-agent user"))
      }
    }

  private def saveVerificationSubmissionDetailsToSession(
    ua: UserAnswers,
    response: ChrisSubmissionResponse
  ): Future[UserAnswers] = {
    val details = VerificationSubmissionDetailsBuilder.fromSubmissionResponse(response)

    ua.set(VerificationSubmissionDetailsPage, details)
      .fold(
        error => Future.failed(error),
        updatedUa => sessionRepository.set(updatedUa).map(_ => updatedUa)
      )
  }

  private def saveVerificationPollDetailsToSession(
    ua: UserAnswers,
    details: VerificationSubmissionDetails
  ): Future[UserAnswers] =
    ua.set(VerificationSubmissionDetailsPage, details)
      .fold(
        error => Future.failed(error),
        updatedUa => sessionRepository.set(updatedUa).map(_ => updatedUa)
      )

  private def required[A](value: Option[A], errorMsg: String): Future[A] =
    value match {
      case Some(v) => Future.successful(v)
      case None    => Future.failed(new RuntimeException(errorMsg))
    }
}

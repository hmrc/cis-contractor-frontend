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

package controllers.verify

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, MultiTabAction}
import models.{Mode, UserAnswers}
import models.response.GetCurrentVerificationBatchResponse
import models.validation.SubcontractorValidationFailure
import pages.validation.SubcontractorValidationFailuresPage
import pages.verify.{CurrentVerificationBatchResponsePage, SelectSubcontractorPage, SelectSubcontractorsToReverifyPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, RequestHeader, Result}
import repositories.SessionRepository
import services.{SubcontractorDetailsValidator, SubcontractorPartnershipValidator, VerificationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CurrentVerificationBatchController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  val controllerComponents: MessagesControllerComponents,
  verificationBatchService: VerificationService,
  subcontractorDetailsValidator: SubcontractorDetailsValidator,
  subcontractorPartnershipValidator: SubcontractorPartnershipValidator,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      loadCurrentBatch(mode, None)
    }

  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)).async {
      implicit request =>
        loadCurrentBatch(mode, Some(flowId))
    }

  private def loadCurrentBatch(mode: Mode, flowId: Option[UUID])(implicit
    request: models.requests.DataRequest[?]
  ): Future[Result] = {
    val id        = flowId.map(_.toString)
    val refreshed =
      id.fold(verificationBatchService.refreshNewestVerificationBatch(request.userAnswers))(nestedId =>
        verificationBatchService.refreshNewestVerificationBatch(request.userAnswers, Some(nestedId))
      )
    refreshed
      .flatMap { updatedAnswers =>
        val canModify =
          id.fold(verificationBatchService.latestBatchCanBeModified(updatedAnswers))(nestedId =>
            verificationBatchService.latestBatchCanBeModified(updatedAnswers, Some(nestedId))
          )
        if (canModify) {
          val current =
            id.fold(verificationBatchService.getCurrentVerificationBatch(updatedAnswers))(nestedId =>
              verificationBatchService.getCurrentVerificationBatch(updatedAnswers, Some(nestedId))
            )
          current
            .flatMap { currentAnswers =>
              val currentPage =
                id.map(CurrentVerificationBatchResponsePage.apply).getOrElse(CurrentVerificationBatchResponsePage)
              currentAnswers
                .get(currentPage)
                .map { response =>
                  val validationFailures =
                    SubcontractorValidationFailure.merge(
                      subcontractorDetailsValidator.validate(
                        response.subcontractors
                      ),
                      subcontractorPartnershipValidator.validate(
                        response.subcontractors
                      )
                    )

                  for {
                    answersWithFailures <-
                      Future.fromTry(
                        currentAnswers.set(
                          SubcontractorValidationFailuresPage,
                          validationFailures
                        )
                      )

                    _ <- sessionRepository.set(
                           answersWithFailures
                         )
                  } yield redirectFor(response, mode, flowId)
                }
                .getOrElse {
                  logger.error(
                    "[CurrentVerificationBatchController][onPageLoad] - CurrentVerificationBatchResponsePage missing from userAnswers"
                  )
                  Future.successful(
                    Redirect(
                      controllers.routes.JourneyRecoveryController
                        .onPageLoad()
                    )
                  )
                }
            }
        } else {
          createVerificationBatch(updatedAnswers, mode, id)
        }
      }
      .recover { case throwable =>
        logger.error(
          "[CurrentVerificationBatchController.onPageLoad] Failed to refresh, validate or persist the current verification batch",
          throwable
        )

        Redirect(
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
        )
      }
  }

  private def redirectFor(
    response: GetCurrentVerificationBatchResponse,
    mode: Mode,
    flowId: Option[UUID]
  ): Result =
    if (
      response.verificationBatch.nonEmpty ||
      response.verifications.nonEmpty
    ) {
      Redirect(
        flowId
          .map(id =>
            controllers.verify.routes.ModifyVerificationBatchAndVerificationsController
              .modifyVerificationBatchWithId(id, mode)
          )
          .getOrElse(
            controllers.verify.routes.ModifyVerificationBatchAndVerificationsController
              .modifyVerificationBatch(mode)
          )
      )
    } else {
      logger.error(
        "[CurrentVerificationBatchController][redirectFor] - current batch has no verification batch or verifications"
      )
      Redirect(
        controllers.routes.JourneyRecoveryController
          .onPageLoad()
      )
    }

  private def createVerificationBatch(
    userAnswers: UserAnswers,
    mode: Mode,
    subJourneyId: Option[String]
  )(implicit request: RequestHeader): Future[Result] = {
    val selectPage   = subJourneyId.map(SelectSubcontractorPage.apply).getOrElse(SelectSubcontractorPage)
    val reverifyPage =
      subJourneyId.map(SelectSubcontractorsToReverifyPage.apply).getOrElse(SelectSubcontractorsToReverifyPage)

    val verifyIdsRaw: Seq[String] =
      userAnswers
        .get(selectPage)
        .map(_.toSeq.map(_.id))
        .getOrElse(Seq.empty)

    val reverifyIdsRaw: Seq[String] =
      userAnswers
        .get(reverifyPage)
        .map(_.toSeq.map(_.id))
        .getOrElse(Seq.empty)

    val selectedIdsEither =
      for {
        verifyIds   <- parseIds("SelectSubcontractorPage", verifyIdsRaw)
        reverifyIds <- parseIds("SelectSubcontractorsToReverifyPage", reverifyIdsRaw)
      } yield (verifyIds ++ reverifyIds).distinct

    selectedIdsEither match {
      case Left(msg) =>
        logger.error(s"[CurrentVerificationBatchController.createVerificationBatch] $msg")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

      case Right(selectedIds) =>
        val created =
          subJourneyId.fold(
            verificationBatchService.createVerificationBatchAndVerifications(
              userAnswers = userAnswers,
              selectedSubcontractorIds = selectedIds,
              actionIndicator = None
            )
          )(nestedId =>
            verificationBatchService.createVerificationBatchAndVerifications(
              userAnswers = userAnswers,
              selectedSubcontractorIds = selectedIds,
              actionIndicator = None,
              subJourneyId = Some(nestedId)
            )
          )
        created
          .map(_ =>
            Redirect(
              subJourneyId
                .map(id =>
                  controllers.verify.routes.CheckVerificationBatchReadinessController
                    .checkVerificationBatchReadinessWithId(UUID.fromString(id), mode)
                )
                .getOrElse(
                  controllers.verify.routes.CheckVerificationBatchReadinessController
                    .checkVerificationBatchReadiness(mode)
                )
            )
          )
    }
  }

  private def parseIds(label: String, ids: Iterable[String]): Either[String, Seq[Long]] = {
    val parsed = ids.toSeq.distinct.map(_.trim).map(_.toLongOption)
    if (parsed.forall(_.isDefined)) Right(parsed.flatten)
    else Left(s"Invalid subcontractor id(s) found in $label")
  }
}

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
import models.finalvalidation.*
import models.UserAnswers
import uk.gov.hmrc.http.HeaderCarrier
import pages.finalvalidation.{FinalValidationContextPage, FinalValidationHandoffPage}
import javax.inject.{Inject, Singleton}
import repositories.SessionRepository
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FinalValidationHandoffService @Inject() (
  connector: ConstructionIndustrySchemeConnector,
  sessionRepository: SessionRepository,
  subcontractorService: SubcontractorService
)(using ec: ExecutionContext)
    extends Logging {

  def prepareMonthlyReturnJourney(
    userAnswers: UserAnswers,
    handoffId: String
  )(implicit hc: HeaderCarrier): Future[Option[(UserAnswers, FinalValidationHandoffPayload)]] =
    connector
      .getFinalValidationJourneyHandoff(
        JourneyHandoffTypes.FinalValidation,
        handoffId
      )
      .flatMap {

        case None =>
          Future.successful(None)

        case Some(payload) =>
          logger.info(
            s"[FinalValidationHandoffService] " +
              s"handoff payload: " +
              s"subcontractorId=${payload.subcontractorId}, " +
              s"subbieResourceRef=${payload.subbieResourceRef}, " +
              s"field=${payload.field.key}"
          )
          for {
            subcontractor    <- subcontractorService.getSubcontractor(payload.instanceId, payload.subbieResourceRef)
            populatedAnswers <- Future.fromTry(
                                  subcontractorService.populateFinalValidationUserAnswers(
                                    userAnswers = userAnswers,
                                    instanceId = payload.instanceId,
                                    response = subcontractor,
                                    changeTarget = payload.changeTarget
                                  )
                                )
            withContext      <-
              Future.fromTry(populatedAnswers.set(FinalValidationContextPage, FinalValidationContext.MonthlyReturn))
            updatedAnswers   <- Future.fromTry(withContext.set(FinalValidationHandoffPage, handoffId))
            stored           <- sessionRepository.set(updatedAnswers)
            _                <- if (stored) {
                                  Future.unit
                                } else {
                                  Future.failed(new RuntimeException(s"Failed to store updated UserAnswers for handoffId: $handoffId"))
                                }
          } yield Some((updatedAnswers, payload))
      }
}

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

package controllers.finalvalidations

import controllers.actions.*
import models.finalvalidation.*
import navigation.finalvalidation.FinalValidationNavigator
import pages.finalvalidation.*
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.{FinalValidationSubcontractorService, SubcontractorService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FinalValidationChangeController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  requireCisId: CisIdRequiredAction,
  subcontractorService: SubcontractorService,
  finalValidationSubcontractorService: FinalValidationSubcontractorService,
  finalValidationNavigator: FinalValidationNavigator,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with Logging {
  
  def onPageLoad(
    subcontractorId: Long,
    fieldKey: String,
    targetKey: String
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireCisId).async { implicit request =>

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      val payload =
        for {
          failure           <- request.userAnswers
                                 .get(FinalValidationErrorPage)
                                 .flatMap(_.find(_.subcontractorId == subcontractorId))
          issue             <- failure.issues.find(_.field.key == fieldKey)
          target            <- FinalValidationChangeTarget.fromKey(targetKey)
          subbieResourceRef <- failure.subbieResourceRef
        } yield FinalValidationHandoffPayload(
          instanceId = request.cisId,
          subcontractorId = subcontractorId,
          subbieResourceRef = subbieResourceRef,
          field = issue.field,
          changeTarget = target
        )
        
      payload match {
        case Some(finalValidationPayload) =>
          subcontractorService.getSubcontractor(request.cisId, finalValidationPayload.subbieResourceRef).flatMap { response =>
            val updatedAnswers =
              for {
                populated <- finalValidationSubcontractorService.populateFinalValidationUserAnswers(
                              request.userAnswers,
                              request.cisId,
                              response,
                              finalValidationPayload.changeTarget
                            )
                withPayload <- populated.set(VerifyFinalValidationPayloadPage, finalValidationPayload)
                withContext <- withPayload.set(FinalValidationContextPage, FinalValidationContext.VerifySubcontractor)
              } yield withContext

            Future.fromTry(updatedAnswers).flatMap { answers =>
              val nextPage =
                finalValidationNavigator.startPage(finalValidationPayload.changeTarget, answers)
              sessionRepository.set(answers).map { _ =>
                Redirect(nextPage)
              }
            }
          }.recover { case exception =>
            logger.error(s"[onPageLoad] Unable to start FinalValidation correction for " +
              s"subcontractorId=${finalValidationPayload.subcontractorId}", exception)
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

}

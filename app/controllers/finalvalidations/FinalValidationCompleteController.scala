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

import config.FrontendAppConfig
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import services.{FinalValidationHandoffService, FinalValidationSubcontractorService}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.finalvalidation.{FinalValidationContext, FinalValidationUpdateRequestBuilder}
import models.requests.DataRequest
import pages.finalvalidation.{FinalValidationContextPage, FinalValidationHandoffPage, VerifyFinalValidationPayloadPage}
import play.api.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FinalValidationCompleteController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  finalValidationHandoffService: FinalValidationHandoffService,
  finalValidationSubcontractorService: FinalValidationSubcontractorService,
  updateRequestBuilder: FinalValidationUpdateRequestBuilder,
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.get(FinalValidationContextPage) match {
      case Some(FinalValidationContext.VerifySubcontractor) =>
        request.userAnswers.get(VerifyFinalValidationPayloadPage) match {

          case Some(payload) =>
            Future.fromTry(updateRequestBuilder.build(request.userAnswers, payload))
              .flatMap {
                case Some(updateRequest) =>
                  finalValidationSubcontractorService.updateSubcontractorForFinalValidation(updateRequest)
                case None =>
                  Future.unit
              }
            .map { _ =>
              Redirect(
                controllers.finalvalidations.routes.UpdateSubcontractorDetailsController.onPageLoad(payload.subcontractorId)
              )
            }

          case None =>
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }

      case _ =>
        completeMonthlyReturn()
    }
  }
  
  private def completeMonthlyReturn()(implicit request: DataRequest[?]): Future[Result] = {
    request.userAnswers.get(FinalValidationHandoffPage) match {

      case Some(handoffId) =>
        finalValidationHandoffService.getPayload(handoffId).flatMap {
          case Some(payload) =>
            Future.fromTry(updateRequestBuilder.build(request.userAnswers, payload))
              .flatMap {
                case Some(updateRequest) =>
                  finalValidationSubcontractorService.updateSubcontractorForFinalValidation(updateRequest)
                case None =>
                  Future.unit
              }
              .map { _ =>
                Redirect(appConfig.cisFrontendFinalValidationReturnUrl(handoffId))
              }

          case None =>
            Future.successful(
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            )
        }

      case None =>
        Future.successful(
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        )
    }
  }

}

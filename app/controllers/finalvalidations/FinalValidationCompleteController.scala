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
import models.finalvalidation.FinalValidationContext.{MonthlyReturn, VerifySubcontractors}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import services.SubcontractorService
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.finalvalidation.{FinalValidationContextPage, FinalValidationHandoffPage}
import play.api.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FinalValidationCompleteController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subcontractorService: SubcontractorService,
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    (request.userAnswers.get(FinalValidationContextPage), request.userAnswers.get(FinalValidationHandoffPage)) match {

      case (Some(MonthlyReturn), Some(handoffId)) =>
//        subcontractorService.createAndUpdateSubcontractor(request.userAnswers) // TODO: need to create a new method dedicated to update Subcontractor
//          .map { _ =>
//          Redirect(appConfig.cisFrontendFinalValidationReturnUrl(handoffId))
//          }
//        .recover { case NonFatal(ex) =>
//          logger.error(s"[FinalValidationCompleteController] Error creating subcontractor", ex)
//          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
//        }
        Future.successful(Redirect(appConfig.cisFrontendFinalValidationReturnUrl(handoffId)))

      case (Some(VerifySubcontractors), _) => // TODO: to be updated for verify subcontractor journey
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

      case _ =>
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  }

}

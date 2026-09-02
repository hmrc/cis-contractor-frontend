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

import controllers.actions.*
import models.NormalMode
import models.contractordetails.ContractorDetailsValidationTarget
import pages.verify.{CurrentVerificationBatchResponsePage, VerificationBatchReadinessPage}
import play.api.i18n.I18nSupport
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ContractorDetailsFinalValidationService, ReviewInsufficientInfoService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.ReviewInsufficientInfoSubcontractorsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReviewInsufficientInfoSubcontractorsController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reviewInsufficientInfoService: ReviewInsufficientInfoService,
  contractorDetailsFinalValidationService: ContractorDetailsFinalValidationService,
  val controllerComponents: MessagesControllerComponents,
  sessionRepository: SessionRepository,
  view: ReviewInsufficientInfoSubcontractorsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    contractorDetailsFinalValidationService
      .refreshAndValidate(request.userAnswers, ContractorDetailsValidationTarget.VerifySubcontractors)
      .flatMap { case (validatedAnswers, validation) =>
        if (!validation.allComplete) {
          Future.successful(
            Redirect(controllers.finalvalidations.routes.ContractorDetailsFinalValidationController.onPageLoad())
          )
        } else {
          validatedAnswers.get(CurrentVerificationBatchResponsePage) match {
            case Some(batch) =>
              val viewModel = reviewInsufficientInfoService.buildViewModel(batch)
              if (viewModel.hasMissing || viewModel.hasReady) {
                for {
                  updatedAnswers <-
                    Future.fromTry(validatedAnswers.set(VerificationBatchReadinessPage, viewModel.allReady))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Ok(view(viewModel))
              } else {
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
              }

            case None =>
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
        }
      }
      .recover { case t =>
        logger.error(
          "[ReviewInsufficientInfoSubcontractorsController.onPageLoad] Failed final contractor validation",
          t
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  // TODO: This is a temporary redirect until DTR-6949 is implemented to handle the next step in the journey
  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) { _ =>
    Redirect(controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoad(NormalMode))
  }
}

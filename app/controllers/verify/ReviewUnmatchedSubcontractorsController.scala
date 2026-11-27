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
import models.finalvalidation.{FinalValidationContext, VerifyFinalValidationSource}
import pages.finalvalidation.{FinalValidationContextPage, VerifyFinalValidationSourcePage}
import pages.verify.CurrentVerificationBatchResponsePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.ReviewUnmatchedSubcontractorsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.ReviewUnmatchedSubcontractorsView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ReviewUnmatchedSubcontractorsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  val controllerComponents: MessagesControllerComponents,
  sessionRepository: SessionRepository,
  reviewUnmatchedSubcontractorsService: ReviewUnmatchedSubcontractorsService,
  view: ReviewUnmatchedSubcontractorsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      showPage(
        request.userAnswers.get(CurrentVerificationBatchResponsePage),
        controllers.verify.routes.ReviewUnmatchedSubcontractorsController.onSubmit(),
        controllers.verify.routes.VerificationResultsController.onPageLoad().url
      )
    }

  def onPageLoadWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)) { implicit request =>
      showPage(
        request.userAnswers.get(CurrentVerificationBatchResponsePage(flowId.toString)),
        controllers.verify.routes.ReviewUnmatchedSubcontractorsController.onSubmitWithId(flowId),
        controllers.verify.routes.VerificationResultsController.onPageLoadWithId(flowId).url
      )
    }

  def onSubmit: Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      persistAndContinue(
        request.userAnswers,
        controllers.verify.routes.ContinueVerificationSubmissionController.onSubmit()
      )
    }

  def onSubmitWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)).async {
      implicit request =>
        persistAndContinue(
          request.userAnswers,
          controllers.verify.routes.ContinueVerificationSubmissionController.onSubmitWithId(flowId)
        )
    }

  private def showPage(
    currentBatch: Option[models.response.GetCurrentVerificationBatchResponse],
    submitAction: Call,
    backToResultsUrl: String
  )(implicit request: play.api.mvc.Request[?], messages: play.api.i18n.Messages) =
    currentBatch match {
      case Some(batch) =>
        reviewUnmatchedSubcontractorsService.buildViewModel(batch) match {
          case Success(viewModel) =>
            Ok(view(viewModel, submitAction, backToResultsUrl))

          case Failure(e) =>
            logger.error(
              "[ReviewUnmatchedSubcontractorsController] Failed to build review unmatched subcontractors view model",
              e
            )
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }

      case None =>
        logger.error(
          "[ReviewUnmatchedSubcontractorsController][onPageLoad] - CurrentVerificationBatchResponsePage missing from userAnswers"
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }

  private def persistAndContinue(userAnswers: models.UserAnswers, continue: Call): Future[play.api.mvc.Result] =
    for {
      withContext <- Future.fromTry(
                       userAnswers.set(
                         FinalValidationContextPage,
                         FinalValidationContext.VerifySubcontractor
                       )
                     )
      withSource  <- Future.fromTry(
                       withContext.set(
                         VerifyFinalValidationSourcePage,
                         VerifyFinalValidationSource.ReviewUnmatchedSubcontractors
                       )
                     )
      _           <- sessionRepository.set(withSource)
    } yield Redirect(continue)
}

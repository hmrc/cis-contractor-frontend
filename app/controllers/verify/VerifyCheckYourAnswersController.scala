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
import models.verify.ValidatedVerify
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.verify.*
import viewmodels.govuk.summarylist.*
import views.html.verify.VerifyCheckYourAnswersView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future

class VerifyCheckYourAnswersController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  val controllerComponents: MessagesControllerComponents,
  view: VerifyCheckYourAnswersView
) extends FrontendBaseController
    with I18nSupport
    with Logging
    with PendingVerificationRequestGuard {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    renderCya(None)
  }

  def onPageLoadWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)) { implicit request =>
      renderCya(Some(flowId))
    }

  private def renderCya(flowId: Option[UUID])(implicit request: models.requests.DataRequest[?]) = {
    val id = flowId.map(_.toString)
    redirectIfVerificationRequestInProgress(request.userAnswers, id).getOrElse {
      val ua     = request.userAnswers
      val built  = id.map(ValidatedVerify.build(ua, _)).getOrElse(ValidatedVerify.build(ua))
      val submit = cyaSubmitAction(flowId)
      built match {
        case Right(_)    =>
          val list = SummaryListViewModel(
            rows = Seq(
              SelectSubcontractorSummary.row(ua, id),
              ReverifyExistingSubcontractorsYesNoSummary.row(ua, id),
              SelectSubcontractorsToReverifySummary.row(ua, id),
              ContractorEmailConfirmationStoredSummary.row(ua, id),
              ContractorEmailConfirmationNotStoredSummary.row(ua, id),
              EmailAddressSummary.row(ua, id)
            ).flatten
          )
          Ok(view(list, submit))
        case Left(error) =>
          logger.error(s"[VerifyCheckYourAnswersController.onPageLoad] Validation failed: $error")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
  }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      submitCya(None)
    }

  def onSubmitWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)).async {
      implicit request =>
        submitCya(Some(flowId))
    }

  private def submitCya(flowId: Option[UUID])(implicit request: models.requests.DataRequest[?]) = {
    val id = flowId.map(_.toString)
    redirectIfVerificationRequestInProgress(request.userAnswers, id) match {
      case Some(redirectResult) =>
        Future.successful(redirectResult)

      case None =>
        val built =
          id.map(ValidatedVerify.build(request.userAnswers, _)).getOrElse(ValidatedVerify.build(request.userAnswers))
        built match {
          case Right(_)    =>
            Future.successful(
              Redirect(
                flowId
                  .map(fid => controllers.verify.routes.SubmissionSendingController.onPageLoadWithId(fid))
                  .getOrElse(controllers.verify.routes.SubmissionSendingController.onPageLoad())
              )
            )
          case Left(error) =>
            logger.error(
              s"[VerifyCheckYourAnswersController.onSubmit] Validation failed: $error"
            )
            Future.successful(
              Redirect(
                controllers.routes.JourneyRecoveryController.onPageLoad()
              )
            )
        }
    }
  }

  private def cyaSubmitAction(flowId: Option[UUID]): Call =
    flowId
      .map(id => routes.VerifyCheckYourAnswersController.onSubmitWithId(id))
      .getOrElse(routes.VerifyCheckYourAnswersController.onSubmit())
}

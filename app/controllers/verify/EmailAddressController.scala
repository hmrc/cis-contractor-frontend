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
import forms.verify.EmailAddressFormProvider
import models.Mode
import models.verify.ContractorEmailConfirmationStored.DifferentEmail
import navigation.Navigator
import pages.verify.{ContractorEmailConfirmationNotStoredPage, ContractorEmailConfirmationStoredPage, EmailAddressPage, NewestVerificationBatchResponsePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.EmailAddressView
import models.UserAnswers

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EmailAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  formProvider: EmailAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: EmailAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  private def hintKey(answers: UserAnswers, subJourneyId: Option[String] = None): String = {
    val newestPage =
      subJourneyId.map(NewestVerificationBatchResponsePage.apply).getOrElse(NewestVerificationBatchResponsePage)

    val hasStoredEmail =
      answers
        .get(newestPage)
        .exists { response =>
          response.scheme.exists(_.emailAddress.exists(_.nonEmpty))
        }

    if (hasStoredEmail) "verify.emailAddress.hint"
    else "verify.emailAddress.hint.notStored"
  }

  private def submitAction(mode: Mode, flowId: Option[UUID]): Call =
    flowId
      .map(id => routes.EmailAddressController.onSubmitWithId(id, mode))
      .getOrElse(routes.EmailAddressController.onSubmit(mode))

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    renderEmailPage(mode, None)
  }

  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)) { implicit request =>
      renderEmailPage(mode, Some(flowId))
    }

  private def renderEmailPage(mode: Mode, flowId: Option[UUID])(implicit
    request: models.requests.DataRequest[?]
  ) = {
    val id                = flowId.map(_.toString)
    val storedPage        =
      id.map(ContractorEmailConfirmationStoredPage.apply).getOrElse(ContractorEmailConfirmationStoredPage)
    val notStoredPage     =
      id.map(ContractorEmailConfirmationNotStoredPage.apply).getOrElse(ContractorEmailConfirmationNotStoredPage)
    val emailPage         = id.map(EmailAddressPage.apply).getOrElse(EmailAddressPage)
    val emailPageRequired =
      request.userAnswers.get(storedPage).contains(DifferentEmail) ||
        request.userAnswers.get(notStoredPage).contains(true)

    if (!emailPageRequired) {
      Redirect(
        flowId
          .map(fid => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoadWithId(fid))
          .getOrElse(controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad())
      )
    } else {
      val preparedForm = request.userAnswers.get(emailPage).fold(form)(form.fill)
      Ok(view(preparedForm, mode, hintKey(request.userAnswers, id), submitAction(mode, flowId)))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      submitEmail(mode, None)
  }

  def onSubmitWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)).async {
      implicit request =>
        submitEmail(mode, Some(flowId))
    }

  private def submitEmail(mode: Mode, flowId: Option[UUID])(implicit request: models.requests.DataRequest[?]) = {
    val id        = flowId.map(_.toString)
    val emailPage = id.map(EmailAddressPage.apply).getOrElse(EmailAddressPage)
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(
            BadRequest(view(formWithErrors, mode, hintKey(request.userAnswers, id), submitAction(mode, flowId)))
          ),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(emailPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(emailPage, mode, updatedAnswers))
      )
  }
}

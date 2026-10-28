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

package controllers.add.partnership

import controllers.actions.*
import controllers.helpers.SubcontractorNameDisplayHelper
import forms.add.partnership.PartnershipMobileNumberFormProvider
import models.{FinalValidationMode, Mode}
import models.contact.ContactMethodOptions
import models.requests.CisPath
import navigation.Navigator
import pages.add.partnership.{PartnershipContactMethodOptionsPage, PartnershipMobileNumberPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.partnership.PartnershipMobileNumberView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipMobileNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  resolveScheme: SchemeAction,
  getJourney: JourneyAction,
  formProvider: PartnershipMobileNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnershipMobileNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val contactOption   = request.userAnswers.get(PartnershipContactMethodOptionsPage)
    val partnershipName = SubcontractorNameDisplayHelper.getPartnershipDisplayName(request.userAnswers, mode)
    val submitAction    = routes.PartnershipMobileNumberController.onSubmit(mode)

    val mobileIsAvailable =
      mode == FinalValidationMode ||
        contactOption.exists(_.contains(ContactMethodOptions.Mobile))

    (partnershipName, mobileIsAvailable) match {
      case (Some(partnershipName), true) =>
        val preparedForm = request.userAnswers.get(PartnershipMobileNumberPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(view(preparedForm, mode, partnershipName, submitAction))

      case (Some(_), false) =>
        Redirect(controllers.add.partnership.routes.AddPartnershipContactMethodsYesNoController.onPageLoad(mode))
      case _                =>
        logger.error(s"[PartnershipMobileNumberController][onPageLoad] - PartnershipNamePage missing from userAnswers")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val contactOption   = request.userAnswers.get(PartnershipContactMethodOptionsPage)
      val partnershipName = SubcontractorNameDisplayHelper.getPartnershipDisplayName(request.userAnswers, mode)
      val submitAction    = routes.PartnershipMobileNumberController.onSubmit(mode)

      val mobileIsAvailable =
        mode == FinalValidationMode ||
          contactOption.exists(_.contains(ContactMethodOptions.Mobile))

      (for {
        partnershipName <- partnershipName
        if mobileIsAvailable
      } yield form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, partnershipName, submitAction))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnershipMobileNumberPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(PartnershipMobileNumberPage, mode, updatedAnswers))
        ))
        .getOrElse {
          logger.error(s"[PartnershipMobileNumberController][onSubmit] - partnership name or mobile option missing")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
  }

  def onPageLoadWithId(cisPath: CisPath, uuid: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen resolveScheme(cisPath) andThen getJourney(uuid)) { implicit request =>
      val contactOption     = request.userAnswers.get(PartnershipContactMethodOptionsPage(request.subJourneyId))
      val partnershipName   =
        SubcontractorNameDisplayHelper.getPartnershipDisplayName(request.userAnswers, mode, request.subJourneyId)
      val mobileIsAvailable = contactOption.exists(_.contains(ContactMethodOptions.Mobile))

      (partnershipName, mobileIsAvailable) match {
        case (Some(partnershipName), true) =>
          val page         = PartnershipMobileNumberPage(request.subJourneyId)
          val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)
          Ok(
            view(
              preparedForm,
              mode,
              partnershipName,
              routes.PartnershipMobileNumberController.onSubmitWithId(cisPath, uuid, mode)
            )
          )
        case _                             =>
          logger.error(
            s"[PartnershipMobileNumberController][onPageLoadWithId] - partnership name or mobile option missing"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmitWithId(cisPath: CisPath, uuid: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen resolveScheme(cisPath) andThen getJourney(uuid)).async { implicit request =>
      val submitAction      = routes.PartnershipMobileNumberController.onSubmitWithId(cisPath, uuid, mode)
      val contactOption     = request.userAnswers.get(PartnershipContactMethodOptionsPage(request.subJourneyId))
      val partnershipName   =
        SubcontractorNameDisplayHelper.getPartnershipDisplayName(request.userAnswers, mode, request.subJourneyId)
      val mobileIsAvailable = contactOption.exists(_.contains(ContactMethodOptions.Mobile))

      (for {
        partnershipName <- partnershipName
        if mobileIsAvailable
      } yield form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, partnershipName, submitAction))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(PartnershipMobileNumberPage(request.subJourneyId), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(cisPath, uuid, PartnershipMobileNumberPage, mode, updatedAnswers))
        ))
        .getOrElse {
          logger.error(
            s"[PartnershipMobileNumberController][onSubmitWithId] - partnership name or mobile option missing"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }
}

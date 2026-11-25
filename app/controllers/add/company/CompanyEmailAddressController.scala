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

package controllers.add.company

import controllers.actions.*
import forms.add.company.CompanyEmailAddressFormProvider
import models.{FinalValidationMode, Mode}
import models.contact.ContactMethodOptions
import navigation.Navigator
import pages.add.company.{CompanyContactMethodOptionsPage, CompanyEmailAddressPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import controllers.helpers.SubcontractorNameDisplayHelper
import views.html.add.company.CompanyEmailAddressView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyEmailAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  formProvider: CompanyEmailAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CompanyEmailAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>

      val contactOption = request.userAnswers.get(CompanyContactMethodOptionsPage)
      val companyName   =
        SubcontractorNameDisplayHelper.getCompanyDisplayName(request.userAnswers, mode)
      val submitAction  =
        routes.CompanyEmailAddressController.onSubmit(mode)

      val emailIsAvailable =
        mode == FinalValidationMode ||
          contactOption.exists(_.contains(ContactMethodOptions.Email))

      (companyName, emailIsAvailable) match {
        case (Some(companyName), true) =>
          val preparedForm = request.userAnswers.get(CompanyEmailAddressPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(
            view(
              preparedForm,
              mode,
              companyName,
              submitAction
            )
          )

        case (Some(_), false) =>
          Redirect(
            controllers.add.company.routes.AddCompanyContactMethodsYesNoController
              .onPageLoad(mode)
          )

        case _ =>
          logger.error(
            "[CompanyEmailAddressController][onPageLoad] - CompanyNamePage missing from userAnswers"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val contactOption = request.userAnswers.get(CompanyContactMethodOptionsPage)
      val companyName   =
        SubcontractorNameDisplayHelper.getCompanyDisplayName(request.userAnswers, mode)
      val submitAction  =
        routes.CompanyEmailAddressController.onSubmit(mode)

      val emailIsAvailable =
        mode == FinalValidationMode ||
          contactOption.exists(_.contains(ContactMethodOptions.Email))

      (for {
        companyName <- companyName
        if emailIsAvailable
      } yield form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  mode,
                  companyName,
                  submitAction
                )
              )
            ),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers.set(
                    CompanyEmailAddressPage,
                    value
                  )
                )
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(
                CompanyEmailAddressPage,
                mode,
                updatedAnswers
              )
            )
        ))
        .getOrElse {
          logger.error(
            "[CompanyEmailAddressController][onSubmit] - CompanyNamePage missing or email contact method not selected"
          )
          Future.successful(
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          )
        }
    }

  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val id               = flowId.toString
      val contactOption    = request.userAnswers.get(CompanyContactMethodOptionsPage(id))
      val companyName      = SubcontractorNameDisplayHelper.getCompanyDisplayName(request.userAnswers, mode, id)
      val emailIsAvailable = contactOption.exists(_.contains(ContactMethodOptions.Email))

      (companyName, emailIsAvailable) match {
        case (Some(companyName), true) =>
          val page         = CompanyEmailAddressPage(id)
          val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)
          Ok(
            view(
              preparedForm,
              mode,
              companyName,
              routes.CompanyEmailAddressController.onSubmitWithId(flowId, mode)
            )
          )
        case _                         =>
          logger.error(
            "[CompanyEmailAddressController][onPageLoadWithId] - company name or email option missing"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmitWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val id               = flowId.toString
      val submitAction     = routes.CompanyEmailAddressController.onSubmitWithId(flowId, mode)
      val contactOption    = request.userAnswers.get(CompanyContactMethodOptionsPage(id))
      val companyName      = SubcontractorNameDisplayHelper.getCompanyDisplayName(request.userAnswers, mode, id)
      val emailIsAvailable = contactOption.exists(_.contains(ContactMethodOptions.Email))

      (for {
        companyName <- companyName
        if emailIsAvailable
      } yield form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, companyName, submitAction))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(CompanyEmailAddressPage(id), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(CompanyEmailAddressPage(id), mode, updatedAnswers))
        ))
        .getOrElse {
          logger.error(
            "[CompanyEmailAddressController][onSubmitWithId] - company name or email option missing"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }
}

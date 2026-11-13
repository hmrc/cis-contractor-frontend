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
import forms.add.company.CompanyMobileNumberFormProvider
import models.{FinalValidationMode, Mode}
import models.contact.ContactMethodOptions
import navigation.Navigator
import pages.add.company.{CompanyContactMethodOptionsPage, CompanyMobileNumberPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import controllers.helpers.SubcontractorNameDisplayHelper
import views.html.add.company.CompanyMobileNumberView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyMobileNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  formProvider: CompanyMobileNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CompanyMobileNumberView
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
        routes.CompanyMobileNumberController.onSubmit(mode)

      val mobileIsAvailable =
        mode == FinalValidationMode ||
          contactOption.exists(_.contains(ContactMethodOptions.Mobile))

      (companyName, mobileIsAvailable) match {
        case (Some(companyName), true) =>
          val preparedForm = request.userAnswers.get(CompanyMobileNumberPage) match {
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
            "[CompanyMobileNumberController][onPageLoad] - CompanyNamePage missing from userAnswers"
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
        routes.CompanyMobileNumberController.onSubmit(mode)

      val mobileIsAvailable =
        mode == FinalValidationMode ||
          contactOption.exists(_.contains(ContactMethodOptions.Mobile))

      (for {
        companyName <- companyName
        if mobileIsAvailable
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
                    CompanyMobileNumberPage,
                    value
                  )
                )
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(
                CompanyMobileNumberPage,
                mode,
                updatedAnswers
              )
            )
        ))
        .getOrElse {
          logger.error(
            "[CompanyMobileNumberController][onSubmit] - CompanyNamePage missing or mobile contact method not selected"
          )
          Future.successful(
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          )
        }
    }

  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val id                = flowId.toString
      val contactOption     = request.userAnswers.get(CompanyContactMethodOptionsPage(id))
      val companyName       =
        SubcontractorNameDisplayHelper.getCompanyDisplayName(request.userAnswers, mode, id)
      val mobileIsAvailable = contactOption.exists(_.contains(ContactMethodOptions.Mobile))

      (companyName, mobileIsAvailable) match {
        case (Some(companyName), true) =>
          val page         = CompanyMobileNumberPage(id)
          val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)
          Ok(
            view(
              preparedForm,
              mode,
              companyName,
              routes.CompanyMobileNumberController.onSubmitWithId(flowId, mode)
            )
          )
        case _                         =>
          logger.error(
            "[CompanyMobileNumberController][onPageLoadWithId] - company name or mobile option missing"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmitWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val id                = flowId.toString
      val submitAction      = routes.CompanyMobileNumberController.onSubmitWithId(flowId, mode)
      val contactOption     = request.userAnswers.get(CompanyContactMethodOptionsPage(id))
      val companyName       =
        SubcontractorNameDisplayHelper.getCompanyDisplayName(request.userAnswers, mode, id)
      val mobileIsAvailable = contactOption.exists(_.contains(ContactMethodOptions.Mobile))

      (for {
        companyName <- companyName
        if mobileIsAvailable
      } yield form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, companyName, submitAction))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(CompanyMobileNumberPage(id), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(CompanyMobileNumberPage(id), mode, updatedAnswers))
        ))
        .getOrElse {
          logger.error(
            "[CompanyMobileNumberController][onSubmitWithId] - company name or mobile option missing"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }
}

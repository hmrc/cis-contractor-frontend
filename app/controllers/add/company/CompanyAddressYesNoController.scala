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
import forms.add.company.CompanyAddressYesNoFormProvider
import models.Mode
import navigation.Navigator
import pages.add.company.CompanyAddressYesNoPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import controllers.helpers.SubcontractorNameDisplayHelper
import views.html.add.company.CompanyAddressYesNoView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyAddressYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  formProvider: CompanyAddressYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CompanyAddressYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      SubcontractorNameDisplayHelper
        .getCompanyDisplayName(request.userAnswers, mode)
        .map { companyName =>
          val preparedForm = request.userAnswers.get(CompanyAddressYesNoPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(
            view(
              preparedForm,
              mode,
              companyName,
              routes.CompanyAddressYesNoController.onSubmit(mode)
            )
          )
        }
        .getOrElse {
          logger.error(
            "[CompanyAddressYesNoController][onPageLoad] - CompanyNamePage missing from userAnswers"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      SubcontractorNameDisplayHelper
        .getCompanyDisplayName(request.userAnswers, mode)
        .map { companyName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      mode,
                      companyName,
                      routes.CompanyAddressYesNoController.onSubmit(mode)
                    )
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(CompanyAddressYesNoPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(CompanyAddressYesNoPage, mode, updatedAnswers))
            )
        }
        .getOrElse {
          logger.error(
            "[CompanyAddressYesNoController][onSubmit] - CompanyNamePage missing from userAnswers"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
  }

  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val id = flowId.toString

      SubcontractorNameDisplayHelper
        .getCompanyDisplayName(request.userAnswers, mode, id)
        .map { companyName =>
          val page         = CompanyAddressYesNoPage(id)
          val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)

          Ok(
            view(
              preparedForm,
              mode,
              companyName,
              routes.CompanyAddressYesNoController.onSubmitWithId(flowId, mode)
            )
          )
        }
        .getOrElse {
          logger.error(
            "[CompanyAddressYesNoController][onPageLoadWithId] - CompanyNamePage missing from userAnswers"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmitWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val id           = flowId.toString
      val submitAction = routes.CompanyAddressYesNoController.onSubmitWithId(flowId, mode)

      SubcontractorNameDisplayHelper
        .getCompanyDisplayName(request.userAnswers, mode, id)
        .map { companyName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, companyName, submitAction))),
              value =>
                for {
                  updatedAnswers <-
                    Future.fromTry(request.userAnswers.set(CompanyAddressYesNoPage(id), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(CompanyAddressYesNoPage(id), mode, updatedAnswers))
            )
        }
        .getOrElse {
          logger.error(
            "[CompanyAddressYesNoController][onSubmitWithId] - CompanyNamePage missing from userAnswers"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }
}

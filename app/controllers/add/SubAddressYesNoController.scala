/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.add

import controllers.actions.*
import forms.add.SubAddressYesNoFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.add.SubAddressYesNoPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.SubcontractorNameExtractor
import controllers.helpers.SubcontractorNameDisplayHelper
import views.html.add.SubAddressYesNoView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubAddressYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SubAddressYesNoFormProvider,
  subcontractorNameExtractor: SubcontractorNameExtractor,
  multiTabAction: MultiTabAction,
  val controllerComponents: MessagesControllerComponents,
  view: SubAddressYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form = formProvider()

  private def recoveryRedirect =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  private def preparedForm(implicit request: DataRequest[?]) =
    request.userAnswers.get(SubAddressYesNoPage).fold(form)(form.fill)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      SubcontractorNameDisplayHelper
        .getSubcontractorDisplayName(request.userAnswers, mode)
        .fold {
          logger.error("[SubAddressYesNoController][onPageLoad] - subcontractor name missing or invalid")
          recoveryRedirect
        } { subcontractorName =>
          Ok(view(preparedForm, mode, subcontractorName, routes.SubAddressYesNoController.onSubmit(mode)))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      SubcontractorNameDisplayHelper
        .getSubcontractorDisplayName(request.userAnswers, mode)
        .fold {
          logger.error("[SubAddressYesNoController][onSubmit] - subcontractor name missing or invalid")
          Future.successful(recoveryRedirect)
        } { subcontractorName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(formWithErrors, mode, subcontractorName, routes.SubAddressYesNoController.onSubmit(mode))
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(SubAddressYesNoPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(SubAddressYesNoPage, mode, updatedAnswers))
            )
        }
    }

  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val id           = flowId.toString
      val page         = SubAddressYesNoPage(id)
      val submitAction = routes.SubAddressYesNoController.onSubmitWithId(flowId, mode)
      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers, mode, id)
        .fold {
          logger.error("[SubAddressYesNoController][onPageLoadWithId] - subcontractor name missing or invalid")
          recoveryRedirect
        } { subcontractorName =>
          val filledForm = request.userAnswers.get(page).fold(form)(form.fill)
          Ok(view(filledForm, mode, subcontractorName, submitAction))
        }
    }

  def onSubmitWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val id           = flowId.toString
      val page         = SubAddressYesNoPage(id)
      val submitAction = routes.SubAddressYesNoController.onSubmitWithId(flowId, mode)
      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers, mode, id)
        .fold {
          logger.error("[SubAddressYesNoController][onSubmitWithId] - subcontractor name missing or invalid")
          Future.successful(recoveryRedirect)
        } { subcontractorName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, subcontractorName, submitAction))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(page, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(page, mode, updatedAnswers))
            )
        }
    }
}

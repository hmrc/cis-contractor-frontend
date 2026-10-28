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
import forms.add.partnership.AddPartnershipContactMethodsYesNoFormProvider
import models.Mode
import models.requests.CisPath
import navigation.Navigator
import pages.add.partnership.AddPartnershipContactMethodsYesNoPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.partnership.AddPartnershipContactMethodsYesNoView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddPartnershipContactMethodsYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  resolveScheme: SchemeAction,
  getJourney: JourneyAction,
  formProvider: AddPartnershipContactMethodsYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddPartnershipContactMethodsYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      SubcontractorNameDisplayHelper
        .getPartnershipDisplayName(request.userAnswers, mode)
        .map { partnershipName =>
          val preparedForm = request.userAnswers.get(AddPartnershipContactMethodsYesNoPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(
            view(
              preparedForm,
              mode,
              partnershipName,
              routes.AddPartnershipContactMethodsYesNoController.onSubmit(mode)
            )
          )
        }
        .getOrElse {
          logger.error(
            s"[AddPartnershipContactMethodsYesNoController][onPageLoad] - PartnershipNamePage missing from userAnswers"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val submitAction = routes.AddPartnershipContactMethodsYesNoController.onSubmit(mode)
      SubcontractorNameDisplayHelper
        .getPartnershipDisplayName(request.userAnswers, mode)
        .map { partnershipName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, partnershipName, submitAction))),
              value =>
                for {
                  updatedAnswers <-
                    Future.fromTry(request.userAnswers.set(AddPartnershipContactMethodsYesNoPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(AddPartnershipContactMethodsYesNoPage, mode, updatedAnswers))
            )
        }
        .getOrElse {
          logger.error(
            s"[AddPartnershipContactMethodsYesNoController][onSubmit] - PartnershipNamePage missing from userAnswers"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
  }

  def onPageLoadWithId(cisPath: CisPath, uuid: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen resolveScheme(cisPath) andThen getJourney(uuid)) { implicit request =>
      SubcontractorNameDisplayHelper
        .getPartnershipDisplayName(request.userAnswers, mode, request.subJourneyId)
        .map { partnershipName =>
          val page         = AddPartnershipContactMethodsYesNoPage(request.subJourneyId)
          val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)
          Ok(
            view(
              preparedForm,
              mode,
              partnershipName,
              routes.AddPartnershipContactMethodsYesNoController.onSubmitWithId(cisPath, uuid, mode)
            )
          )
        }
        .getOrElse {
          logger.error(
            s"[AddPartnershipContactMethodsYesNoController][onPageLoadWithId] - PartnershipNamePage missing from userAnswers"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmitWithId(cisPath: CisPath, uuid: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen resolveScheme(cisPath) andThen getJourney(uuid)).async { implicit request =>
      val submitAction = routes.AddPartnershipContactMethodsYesNoController.onSubmitWithId(cisPath, uuid, mode)
      SubcontractorNameDisplayHelper
        .getPartnershipDisplayName(request.userAnswers, mode, request.subJourneyId)
        .map { partnershipName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, partnershipName, submitAction))),
              value =>
                for {
                  updatedAnswers <-
                    Future.fromTry(
                      request.userAnswers.set(AddPartnershipContactMethodsYesNoPage(request.subJourneyId), value)
                    )
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator.nextPage(cisPath, uuid, AddPartnershipContactMethodsYesNoPage, mode, updatedAnswers)
                )
            )
        }
        .getOrElse {
          logger.error(
            s"[AddPartnershipContactMethodsYesNoController][onSubmitWithId] - PartnershipNamePage missing from userAnswers"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }
}

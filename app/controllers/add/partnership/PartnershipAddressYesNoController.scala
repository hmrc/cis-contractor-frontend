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
import forms.add.partnership.PartnershipAddressYesNoFormProvider
import models.Mode
import navigation.Navigator
import pages.add.partnership.PartnershipAddressYesNoPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.partnership.PartnershipAddressYesNoView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipAddressYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  formProvider: PartnershipAddressYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnershipAddressYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    SubcontractorNameDisplayHelper
      .getPartnershipDisplayName(request.userAnswers, mode)
      .map { partnershipName =>
        val preparedForm = request.userAnswers.get(PartnershipAddressYesNoPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, mode, partnershipName, routes.PartnershipAddressYesNoController.onSubmit(mode)))
      }
      .getOrElse {
        logger.error(s"[PartnershipAddressYesNoController][onPageLoad] - PartnershipNamePage missing from userAnswers")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      SubcontractorNameDisplayHelper
        .getPartnershipDisplayName(request.userAnswers, mode)
        .map { partnershipName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(formWithErrors, mode, partnershipName, routes.PartnershipAddressYesNoController.onSubmit(mode))
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnershipAddressYesNoPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(PartnershipAddressYesNoPage, mode, updatedAnswers))
            )
        }
        .getOrElse {
          logger.error(s"[PartnershipAddressYesNoController][onSubmit] - PartnershipNamePage missing from userAnswers")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
  }

  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val id = flowId.toString
      SubcontractorNameDisplayHelper
        .getPartnershipDisplayName(request.userAnswers, mode, id)
        .map { partnershipName =>
          val page         = PartnershipAddressYesNoPage(id)
          val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)
          Ok(
            view(
              preparedForm,
              mode,
              partnershipName,
              routes.PartnershipAddressYesNoController.onSubmitWithId(flowId, mode)
            )
          )
        }
        .getOrElse {
          logger.error(
            s"[PartnershipAddressYesNoController][onPageLoadWithId] - PartnershipNamePage missing from userAnswers"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmitWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val id           = flowId.toString
      val submitAction = routes.PartnershipAddressYesNoController.onSubmitWithId(flowId, mode)
      SubcontractorNameDisplayHelper
        .getPartnershipDisplayName(request.userAnswers, mode, id)
        .map { partnershipName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, partnershipName, submitAction))),
              value =>
                for {
                  updatedAnswers <-
                    Future.fromTry(request.userAnswers.set(PartnershipAddressYesNoPage(id), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(PartnershipAddressYesNoPage(id), mode, updatedAnswers))
            )
        }
        .getOrElse {
          logger.error(
            s"[PartnershipAddressYesNoController][onSubmitWithId] - PartnershipNamePage missing from userAnswers"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }
}

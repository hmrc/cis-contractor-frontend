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
import forms.add.partnership.PartnershipNominatedPartnerCrnYesNoFormProvider
import models.Mode
import models.requests.CisPath
import navigation.Navigator
import pages.add.partnership.PartnershipNominatedPartnerCrnYesNoPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.partnership.PartnershipNominatedPartnerCrnYesNoView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipNominatedPartnerCrnYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  resolveScheme: SchemeAction,
  getJourney: JourneyAction,
  formProvider: PartnershipNominatedPartnerCrnYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnershipNominatedPartnerCrnYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    SubcontractorNameDisplayHelper
      .getPartnerDisplayName(request.userAnswers, mode)
      .map { nominatedPartnershipName =>
        val preparedForm = request.userAnswers.get(PartnershipNominatedPartnerCrnYesNoPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(
          view(
            preparedForm,
            mode,
            nominatedPartnershipName,
            routes.PartnershipNominatedPartnerCrnYesNoController.onSubmit(mode)
          )
        )
      }
      .getOrElse {
        logger.error(
          s"[PartnershipNominatedPartnerCrnYesNoController][onPageLoad] - partner name missing from userAnswers"
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val submitAction = routes.PartnershipNominatedPartnerCrnYesNoController.onSubmit(mode)
      SubcontractorNameDisplayHelper
        .getPartnerDisplayName(request.userAnswers, mode)
        .map { nominatedPartnershipName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, nominatedPartnershipName, submitAction))),
              value =>
                for {
                  updatedAnswers <-
                    Future.fromTry(request.userAnswers.set(PartnershipNominatedPartnerCrnYesNoPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(PartnershipNominatedPartnerCrnYesNoPage, mode, updatedAnswers))
            )
        }
        .getOrElse {
          logger.error(
            s"[PartnershipNominatedPartnerCrnYesNoController][onSubmit] - partner name missing from userAnswers"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
  }

  def onPageLoadWithId(cisPath: CisPath, uuid: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen resolveScheme(cisPath) andThen getJourney(uuid)) { implicit request =>
      SubcontractorNameDisplayHelper
        .getPartnerDisplayName(request.userAnswers, mode, request.subJourneyId)
        .map { nominatedPartnershipName =>
          val page         = PartnershipNominatedPartnerCrnYesNoPage(request.subJourneyId)
          val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)
          Ok(
            view(
              preparedForm,
              mode,
              nominatedPartnershipName,
              routes.PartnershipNominatedPartnerCrnYesNoController.onSubmitWithId(cisPath, uuid, mode)
            )
          )
        }
        .getOrElse {
          logger.error(
            s"[PartnershipNominatedPartnerCrnYesNoController][onPageLoadWithId] - partner name missing from userAnswers"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmitWithId(cisPath: CisPath, uuid: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen resolveScheme(cisPath) andThen getJourney(uuid)).async { implicit request =>
      val submitAction = routes.PartnershipNominatedPartnerCrnYesNoController.onSubmitWithId(cisPath, uuid, mode)
      SubcontractorNameDisplayHelper
        .getPartnerDisplayName(request.userAnswers, mode, request.subJourneyId)
        .map { nominatedPartnershipName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, nominatedPartnershipName, submitAction))),
              value =>
                for {
                  updatedAnswers <-
                    Future.fromTry(
                      request.userAnswers.set(PartnershipNominatedPartnerCrnYesNoPage(request.subJourneyId), value)
                    )
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator.nextPage(cisPath, uuid, PartnershipNominatedPartnerCrnYesNoPage, mode, updatedAnswers)
                )
            )
        }
        .getOrElse {
          logger.error(
            s"[PartnershipNominatedPartnerCrnYesNoController][onSubmitWithId] - partner name missing from userAnswers"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }
}

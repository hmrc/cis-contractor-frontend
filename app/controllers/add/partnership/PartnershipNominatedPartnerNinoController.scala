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
import forms.add.partnership.PartnershipNominatedPartnerNinoFormProvider
import models.Mode
import navigation.Navigator
import pages.add.partnership.{PartnershipNominatedPartnerNinoPage, PartnershipNominatedPartnerNinoYesNoPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.YesOrNoPageGuardService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.partnership.PartnershipNominatedPartnerNinoView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipNominatedPartnerNinoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  formProvider: PartnershipNominatedPartnerNinoFormProvider,
  yesOrNoPageGuardService: YesOrNoPageGuardService,
  val controllerComponents: MessagesControllerComponents,
  view: PartnershipNominatedPartnerNinoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val yesOrNoPage       = PartnershipNominatedPartnerNinoYesNoPage
      val yesOrNoPageOption = request.userAnswers.get(PartnershipNominatedPartnerNinoYesNoPage)
      val submitAction      = routes.PartnershipNominatedPartnerNinoController.onSubmit(mode)

      SubcontractorNameDisplayHelper
        .getPartnerDisplayName(request.userAnswers, mode)
        .map { nominatedPartnerName =>
          val preparedForm =
            request.userAnswers.get(PartnershipNominatedPartnerNinoPage).fold(form)(form.fill)
          val result       = Ok(view(preparedForm, mode, nominatedPartnerName, submitAction))
          yesOrNoPageGuardService.yesOrNoPageRoute(result, yesOrNoPageOption, yesOrNoPage, mode)
        }
        .getOrElse {
          logger.error(
            s"[PartnershipNominatedPartnerNinoController][onPageLoad] - partner name missing from userAnswers"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val submitAction = routes.PartnershipNominatedPartnerNinoController.onSubmit(mode)
      SubcontractorNameDisplayHelper
        .getPartnerDisplayName(request.userAnswers, mode)
        .map { nominatedPartnerName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, nominatedPartnerName, submitAction))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnershipNominatedPartnerNinoPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(PartnershipNominatedPartnerNinoPage, mode, updatedAnswers))
            )
        }
        .getOrElse {
          logger.error(
            s"[PartnershipNominatedPartnerNinoController][onSubmit] - partner name missing from userAnswers"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }

  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val id = flowId.toString
      if (!request.userAnswers.get(PartnershipNominatedPartnerNinoYesNoPage(id)).contains(true)) {
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      } else {
        SubcontractorNameDisplayHelper
          .getPartnerDisplayName(request.userAnswers, mode, id)
          .map { nominatedPartnerName =>
            val page         = PartnershipNominatedPartnerNinoPage(id)
            val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)
            Ok(
              view(
                preparedForm,
                mode,
                nominatedPartnerName,
                routes.PartnershipNominatedPartnerNinoController.onSubmitWithId(flowId, mode)
              )
            )
          }
          .getOrElse {
            logger.error(
              s"[PartnershipNominatedPartnerNinoController][onPageLoadWithId] - partner name missing from userAnswers"
            )
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
      }
    }

  def onSubmitWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val id           = flowId.toString
      val submitAction = routes.PartnershipNominatedPartnerNinoController.onSubmitWithId(flowId, mode)
      if (!request.userAnswers.get(PartnershipNominatedPartnerNinoYesNoPage(id)).contains(true)) {
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      } else {
        SubcontractorNameDisplayHelper
          .getPartnerDisplayName(request.userAnswers, mode, id)
          .map { nominatedPartnerName =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(BadRequest(view(formWithErrors, mode, nominatedPartnerName, submitAction))),
                value =>
                  for {
                    updatedAnswers <-
                      Future.fromTry(
                        request.userAnswers.set(PartnershipNominatedPartnerNinoPage(id), value)
                      )
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(
                    navigator.nextPage(PartnershipNominatedPartnerNinoPage(id), mode, updatedAnswers)
                  )
              )
          }
          .getOrElse {
            logger.error(
              s"[PartnershipNominatedPartnerNinoController][onSubmitWithId] - partner name missing from userAnswers"
            )
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
      }
    }
}

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
import forms.add.partnership.PartnershipContactMethodOptionsFormProvider
import models.Mode
import models.requests.CisPath
import navigation.Navigator
import pages.add.partnership.{AddPartnershipContactMethodsYesNoPage, PartnershipContactMethodOptionsPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.YesOrNoPageGuardService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.partnership.PartnershipContactMethodOptionsView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipContactMethodOptionsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  resolveScheme: SchemeAction,
  getJourney: JourneyAction,
  formProvider: PartnershipContactMethodOptionsFormProvider,
  yesOrNoPageGuardService: YesOrNoPageGuardService,
  val controllerComponents: MessagesControllerComponents,
  view: PartnershipContactMethodOptionsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val yesOrNoPage       = AddPartnershipContactMethodsYesNoPage
    val yesOrNoPageOption = request.userAnswers.get(AddPartnershipContactMethodsYesNoPage)
    val submitAction      = routes.PartnershipContactMethodOptionsController.onSubmit(mode)

    SubcontractorNameDisplayHelper
      .getPartnershipDisplayName(request.userAnswers, mode)
      .map { partnershipName =>
        val preparedForm = request.userAnswers.get(PartnershipContactMethodOptionsPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        val result = Ok(view(preparedForm, mode, partnershipName, submitAction))
        yesOrNoPageGuardService.yesOrNoPageRoute(result, yesOrNoPageOption, yesOrNoPage, mode)
      }
      .getOrElse {
        logger.error(
          s"[PartnershipContactMethodOptionsController][onPageLoad] - PartnershipNamePage missing from userAnswers"
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val submitAction = routes.PartnershipContactMethodOptionsController.onSubmit(mode)
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
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnershipContactMethodOptionsPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(PartnershipContactMethodOptionsPage, mode, updatedAnswers))
            )
        }
        .getOrElse {
          logger.error(
            s"[PartnershipContactMethodOptionsController][onSubmit] - PartnershipNamePage missing from userAnswers"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
  }

  def onPageLoadWithId(cisPath: CisPath, uuid: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen resolveScheme(cisPath) andThen getJourney(uuid)) { implicit request =>
      if (!request.userAnswers.get(AddPartnershipContactMethodsYesNoPage(request.subJourneyId)).contains(true)) {
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      } else {
        SubcontractorNameDisplayHelper
          .getPartnershipDisplayName(request.userAnswers, mode, request.subJourneyId)
          .map { partnershipName =>
            val page         = PartnershipContactMethodOptionsPage(request.subJourneyId)
            val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)
            Ok(
              view(
                preparedForm,
                mode,
                partnershipName,
                routes.PartnershipContactMethodOptionsController.onSubmitWithId(cisPath, uuid, mode)
              )
            )
          }
          .getOrElse {
            logger.error(
              s"[PartnershipContactMethodOptionsController][onPageLoadWithId] - PartnershipNamePage missing from userAnswers"
            )
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
      }
    }

  def onSubmitWithId(cisPath: CisPath, uuid: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen resolveScheme(cisPath) andThen getJourney(uuid)).async { implicit request =>
      val submitAction = routes.PartnershipContactMethodOptionsController.onSubmitWithId(cisPath, uuid, mode)
      if (!request.userAnswers.get(AddPartnershipContactMethodsYesNoPage(request.subJourneyId)).contains(true)) {
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      } else {
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
                        request.userAnswers.set(PartnershipContactMethodOptionsPage(request.subJourneyId), value)
                      )
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(
                    navigator.nextPage(cisPath, uuid, PartnershipContactMethodOptionsPage, mode, updatedAnswers)
                  )
              )
          }
          .getOrElse {
            logger.error(
              s"[PartnershipContactMethodOptionsController][onSubmitWithId] - PartnershipNamePage missing from userAnswers"
            )
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
      }
    }
}

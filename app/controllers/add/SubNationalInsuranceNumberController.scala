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
import forms.add.SubNationalInsuranceNumberFormProvider
import models.Mode
import models.JourneyNamespace
import models.requests.DataRequest
import navigation.Navigator
import pages.add.{NationalInsuranceNumberYesNoPage, SubNationalInsuranceNumberPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.YesOrNoPageGuardService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.SubcontractorNameExtractor
import views.html.add.SubNationalInsuranceNumberView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubNationalInsuranceNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sharedSubcontractorJourneyAction: SharedSubcontractorJourneyAction,
  formProvider: SubNationalInsuranceNumberFormProvider,
  subcontractorNameExtractor: SubcontractorNameExtractor,
  val controllerComponents: MessagesControllerComponents,
  yesOrNoPageGuardService: YesOrNoPageGuardService,
  view: SubNationalInsuranceNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form = formProvider()

  private def recoveryRedirect =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  private def preparedForm(implicit request: DataRequest[?]) =
    request.userAnswers.get(SubNationalInsuranceNumberPage).fold(form)(form.fill)

  // Multi-tab
  private def preparedForm(id: String, namespace: JourneyNamespace)(implicit request: DataRequest[?]) =
    request.userAnswers.get(SubNationalInsuranceNumberPage(id, namespace)).fold(form)(form.fill)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>

      val yesOrNoPage       = NationalInsuranceNumberYesNoPage
      val yesOrNoPageOption = request.userAnswers.get(NationalInsuranceNumberYesNoPage)

      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers, mode)
        .fold {
          logger.error("[SubNationalInsuranceNumberController][onPageLoad] - subcontractor name missing or invalid")
          recoveryRedirect
        } { subcontractorName =>
          val result = Ok(view(preparedForm, mode, subcontractorName))
          yesOrNoPageGuardService.yesOrNoPageRoute(result, yesOrNoPageOption, yesOrNoPage, mode)
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers, mode)
        .fold {
          logger.error("[SubNationalInsuranceNumberController][onSubmit] - subcontractor name missing or invalid")
          Future.successful(recoveryRedirect)
        } { subcontractorName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, subcontractorName))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(SubNationalInsuranceNumberPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(SubNationalInsuranceNumberPage, mode, updatedAnswers))
            )
        }
    }

  // Multi-tab
  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    sharedSubcontractorJourneyAction(flowId, mode) { namespace => implicit request =>

      val id                = flowId.toString
      val yesOrNoPage       = NationalInsuranceNumberYesNoPage(id, namespace)
      val yesOrNoPageOption = request.userAnswers.get(yesOrNoPage)

      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers, mode, id, namespace)
        .fold {
          logger.error(
            "[SubNationalInsuranceNumberController][onPageLoadWithId] - subcontractor name missing or invalid"
          )
          recoveryRedirect
        } { subcontractorName =>
          val result = Ok(view(preparedForm(id, namespace), mode, subcontractorName, Some(flowId)))

          yesOrNoPageGuardService.yesOrNoPageRoute(
            result,
            yesOrNoPageOption,
            yesOrNoPage,
            mode
          )
        }
    }

  def onSubmitWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    sharedSubcontractorJourneyAction.async(flowId, mode) { namespace => implicit request =>

      val id   = flowId.toString
      val page = SubNationalInsuranceNumberPage(id, namespace)

      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers, mode, id, namespace)
        .fold {
          logger.error("[SubNationalInsuranceNumberController][onSubmitWithId] - subcontractor name missing or invalid")
          Future.successful(recoveryRedirect)
        } { subcontractorName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, subcontractorName, Some(flowId)))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(page, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(page, mode, updatedAnswers))
            )
        }
    }
}

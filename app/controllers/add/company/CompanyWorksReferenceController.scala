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
import forms.add.company.CompanyWorksReferenceFormProvider
import models.Mode
import navigation.Navigator
import pages.add.company.{CompanyWorksReferencePage, CompanyWorksReferenceYesNoPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.YesOrNoPageGuardService
import controllers.helpers.SubcontractorNameDisplayHelper
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.company.CompanyWorksReferenceView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyWorksReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  formProvider: CompanyWorksReferenceFormProvider,
  val controllerComponents: MessagesControllerComponents,
  yesOrNoPageGuardService: YesOrNoPageGuardService,
  view: CompanyWorksReferenceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val yesOrNoPage       = CompanyWorksReferenceYesNoPage
      val yesOrNoPageOption = request.userAnswers.get(CompanyWorksReferenceYesNoPage)
      val submitAction      = routes.CompanyWorksReferenceController.onSubmit(mode)

      SubcontractorNameDisplayHelper
        .getCompanyDisplayName(request.userAnswers, mode)
        .map { companyName =>
          val preparedForm = request.userAnswers.get(CompanyWorksReferencePage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          val result = Ok(view(preparedForm, mode, companyName, submitAction))
          yesOrNoPageGuardService.yesOrNoPageRoute(result, yesOrNoPageOption, yesOrNoPage, mode)
        }
        .getOrElse {
          logger.error("[CompanyWorksReferenceController][onPageLoad] - CompanyNamePage missing from userAnswers")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val submitAction = routes.CompanyWorksReferenceController.onSubmit(mode)

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
                      submitAction
                    )
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(CompanyWorksReferencePage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(CompanyWorksReferencePage, mode, updatedAnswers))
            )
        }
        .getOrElse {
          logger.error("[CompanyWorksReferenceController][onSubmit] - CompanyNamePage missing from userAnswers")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }

  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val id = flowId.toString
      if (!request.userAnswers.get(CompanyWorksReferenceYesNoPage(id)).contains(true)) {
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      } else {
        SubcontractorNameDisplayHelper
          .getCompanyDisplayName(request.userAnswers, mode, id)
          .map { companyName =>
            val page         = CompanyWorksReferencePage(id)
            val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)
            Ok(
              view(
                preparedForm,
                mode,
                companyName,
                routes.CompanyWorksReferenceController.onSubmitWithId(flowId, mode)
              )
            )
          }
          .getOrElse {
            logger.error(
              s"[CompanyWorksReferenceController][onPageLoadWithId] - CompanyNamePage missing from userAnswers"
            )
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
      }
    }

  def onSubmitWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val id           = flowId.toString
      val submitAction = routes.CompanyWorksReferenceController.onSubmitWithId(flowId, mode)
      if (!request.userAnswers.get(CompanyWorksReferenceYesNoPage(id)).contains(true)) {
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      } else {
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
                      Future.fromTry(
                        request.userAnswers.set(CompanyWorksReferencePage(id), value)
                      )
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(
                    navigator.nextPage(CompanyWorksReferencePage(id), mode, updatedAnswers)
                  )
              )
          }
          .getOrElse {
            logger.error(
              s"[CompanyWorksReferenceController][onSubmitWithId] - CompanyNamePage missing from userAnswers"
            )
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
      }
    }
}

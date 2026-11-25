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
import forms.add.SubcontractorNameFormProvider
import models.{FinalValidationMode, Mode}
import models.add.IndividualNamesOptions.SubcontractorName
import models.add.SubcontractorName.format
import navigation.Navigator
import pages.add.{IndividualNamesOptionsPage, SubcontractorNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.SubcontractorNameView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubcontractorNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sharedSubcontractorJourneyAction: SharedSubcontractorJourneyAction,
  formProvider: SubcontractorNameFormProvider,
  redirectVerifiedSubcontractor: RedirectVerifiedSubcontractorAction,
  val controllerComponents: MessagesControllerComponents,
  view: SubcontractorNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen redirectVerifiedSubcontractor) { implicit request =>
      mode match {
        case FinalValidationMode =>
          val preparedForm =
            request.userAnswers.get(SubcontractorNamePage) match {
              case Some(subcontractorName) => form.fill(subcontractorName)
              case None                    => form
            }

          Ok(view(preparedForm, mode))

        case _ =>
          val namesOptions =
            request.userAnswers.get(IndividualNamesOptionsPage)

          namesOptions match {
            case Some(namesOptions) if namesOptions.contains(SubcontractorName) =>
              val preparedForm =
                request.userAnswers.get(SubcontractorNamePage) match {
                  case Some(subcontractorName) => form.fill(subcontractorName)
                  case None                    => form
                }

              Ok(view(preparedForm, mode))

            case _ =>
              Redirect(
                controllers.add.routes.IndividualNamesOptionsController.onPageLoad(mode)
              )
          }
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen redirectVerifiedSubcontractor).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(SubcontractorNamePage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(SubcontractorNamePage, mode, updatedAnswers)
            )
        )
    }

  // Multi-tab
  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    sharedSubcontractorJourneyAction(flowId, mode) { namespace =>
      implicit request =>

        val id = flowId.toString
        val page = SubcontractorNamePage(id, namespace)

        val preparedForm = request.userAnswers.get(page) match {
          case Some(subcontractorName) => form.fill(subcontractorName)
          case None => form
        }

        mode match {
          case FinalValidationMode =>
            Ok(view(preparedForm, mode, Some(flowId)))

          case _ =>
            request.userAnswers.get(IndividualNamesOptionsPage(id, namespace)) match {
              case Some(namesOptions) if namesOptions.contains(SubcontractorName) =>
                Ok(view(preparedForm, mode, Some(flowId)))

              case _ =>
                // TODO: Restore scoped Add Subcontractor navigation once Individual Add routes are integrated.
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
        }
    }

  def onSubmitWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    sharedSubcontractorJourneyAction.async(flowId, mode) { namespace =>
      implicit request =>

        val id = flowId.toString
        val page = SubcontractorNamePage(id, namespace)

        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, Some(flowId)))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(page, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(
                navigator.nextPage(page, mode, updatedAnswers)
              )
          )
    }
}

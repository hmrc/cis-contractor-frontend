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
import forms.add.TypeOfSubcontractorFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.add.TypeOfSubcontractorPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.CisIdQuery
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DefaultSubcontractorCleanupService
import views.html.add.TypeOfSubcontractorView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class TypeOfSubcontractorController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  cleanupService: DefaultSubcontractorCleanupService,
  formProvider: TypeOfSubcontractorFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: TypeOfSubcontractorView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(TypeOfSubcontractorPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode, routes.TypeOfSubcontractorController.onSubmit(mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val submitAction = routes.TypeOfSubcontractorController.onSubmit(mode)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, submitAction))),
          value =>
            cleanupService.clean(request.userAnswers) match {
              case Success(cleanedAnswers) =>
                Future
                  .fromTry(
                    cleanedAnswers.set(TypeOfSubcontractorPage, value)
                  )
                  .flatMap { updatedAnswers =>
                    sessionRepository
                      .set(updatedAnswers)
                      .map { _ =>
                        Redirect(
                          navigator.nextPage(
                            TypeOfSubcontractorPage,
                            mode,
                            updatedAnswers
                          )
                        )
                      }
                  }

              case Failure(exception) =>
                logger.error(
                  "[TypeOfSubcontractorController][onSubmit] Failed to clean previous subcontractor answers",
                  exception
                )

                Future.successful(
                  Redirect(
                    controllers.routes.JourneyRecoveryController.onPageLoad()
                  )
                )
            }
        )
    }

  def onPageLoadStart(cisId: String, mode: Mode): Action[AnyContent] =
    (identify andThen multiTabAction.forStart(cisId) andThen getData) { implicit request =>
      Ok(view(form, mode, routes.TypeOfSubcontractorController.onSubmitStart(cisId, mode)))
    }

  def onSubmitStart(cisId: String, mode: Mode): Action[AnyContent] =
    (identify andThen multiTabAction.forStart(cisId) andThen getData).async { implicit request =>
      val submitAction = routes.TypeOfSubcontractorController.onSubmitStart(cisId, mode)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, submitAction))),
          value => {
            val answers = request.userAnswers.getOrElse(UserAnswers(request.userId))
            cleanupService.clean(answers) match {
              case Failure(exception)      =>
                logger.error(
                  "[TypeOfSubcontractorController][onSubmitStart] Failed to clean previous subcontractor answers",
                  exception
                )
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
              case Success(cleanedAnswers) =>
                val id = UUID.randomUUID().toString
                Future
                  .fromTry(
                    cleanedAnswers
                      .set(TypeOfSubcontractorPage(id), value)
                      .flatMap(_.set(CisIdQuery(id), cisId))
                  )
                  .flatMap { updatedAnswers =>
                    sessionRepository.set(updatedAnswers).map { _ =>
                      Redirect(navigator.nextPage(TypeOfSubcontractorPage(id), mode, updatedAnswers))
                    }
                  }
            }
          }
        )
    }

  def onPageLoadWithId(id: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(id)) { implicit request =>
      val page         = TypeOfSubcontractorPage(id.toString)
      val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)
      Ok(view(preparedForm, mode, routes.TypeOfSubcontractorController.onSubmitWithId(id, mode)))
    }

  def onSubmitWithId(id: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(id)).async { implicit request =>
      val submitAction = routes.TypeOfSubcontractorController.onSubmitWithId(id, mode)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, submitAction))),
          value =>
            cleanupService.clean(request.userAnswers) match {
              case Failure(exception)      =>
                logger.error(
                  "[TypeOfSubcontractorController][onSubmitWithId] Failed to clean previous subcontractor answers",
                  exception
                )
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
              case Success(cleanedAnswers) =>
                Future
                  .fromTry(cleanedAnswers.set(TypeOfSubcontractorPage(id.toString), value))
                  .flatMap { updatedAnswers =>
                    sessionRepository.set(updatedAnswers).map { _ =>
                      Redirect(navigator.nextPage(TypeOfSubcontractorPage(id.toString), mode, updatedAnswers))
                    }
                  }
            }
        )
    }
}

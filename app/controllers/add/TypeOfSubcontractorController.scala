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
import models.requests.CisPath
import models.{JourneyScope, Mode, UserAnswers}
import navigation.Navigator
import pages.add.TypeOfSubcontractorPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.CisIdQuery
import repositories.SessionRepository
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DefaultSubcontractorCleanupService
import views.html.add.TypeOfSubcontractorView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class TypeOfSubcontractorController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  resolveScheme: SchemeAction,
  journeyService: JourneyService,
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
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, mode, routes.TypeOfSubcontractorController.onSubmit(mode)))
            ),
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

  def onPageLoadWithCisPath(cisPath: CisPath, mode: Mode): Action[AnyContent] =
    (identify andThen resolveScheme(cisPath)) { implicit request =>
      Ok(view(form, mode, routes.TypeOfSubcontractorController.onSubmitWithCisPath(cisPath, mode)))
    }

  def onSubmitWithCisPath(cisPath: CisPath, mode: Mode): Action[AnyContent] =
    (identify andThen resolveScheme(cisPath)).async { implicit request =>
      val submitAction = routes.TypeOfSubcontractorController.onSubmitWithCisPath(cisPath, mode)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, submitAction))),
          value =>
            journeyService
              .create(
                request,
                (ua, id) =>
                  ua.set(TypeOfSubcontractorPage(id), value)
                    .flatMap(_.set(CisIdQuery, request.cisId))
              )
              .map { flowId =>
                val id             = JourneyScope.subJourneyId(cisPath, flowId)
                val updatedAnswers = UserAnswers(request.identifier.userId)
                  .initialiseSubJourney(id)
                  .flatMap(_.set(TypeOfSubcontractorPage(id), value))
                  .flatMap(_.set(CisIdQuery, request.cisId))
                  .get
                Redirect(navigator.nextPage(cisPath, flowId, TypeOfSubcontractorPage, mode, updatedAnswers))
              }
              .recover { case NonFatal(exception) =>
                logger.error("[TypeOfSubcontractorController] Failed to create journey", exception)
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }
        )
    }
}

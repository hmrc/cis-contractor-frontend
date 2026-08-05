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

package controllers.amend.trust

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.UserAnswers
import pages.add.trust.TrustNamePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Reads
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{CisIdQuery, OriginalTrustAnswersQuery}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DefaultSubcontractorCleanupService
import viewmodels.amend.trust.TrustAmendConfirmationViewModel
import views.html.amend.AmendConfirmationView

import scala.util.{Failure, Success}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendTrustConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  cleanupService: DefaultSubcontractorCleanupService,
  sessionRepository: SessionRepository,
  view: AmendConfirmationView,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val recoveryRedirect =
        Redirect(routes.JourneyRecoveryController.onPageLoad())

      val ua = request.userAnswers

      ua.get(OriginalTrustAnswersQuery) match {

        case None =>
          logger.error("[AmendTrustConfirmationController] Missing OriginalTrustAnswersQuery")
          Future.successful(recoveryRedirect)

        case Some(originalTrustAnswers) =>
          ua.get(CisIdQuery) match {

            case None =>
              logger.error("[AmendTrustConfirmationController] Missing CisIdQuery")
              Future.successful(recoveryRedirect)

            case Some(_) =>
              val tableRows = TrustAmendConfirmationViewModel.rows(originalTrustAnswers, ua)
              val trustName = trustDisplayName(ua)

              cleanupService.cleanAmend(ua) match {

                case Success(cleanedUa) =>
                  sessionRepository.set(cleanedUa).map { _ =>
                    Ok(
                      view(
                        tableRows,
                        trustName,
                        appConfig.retrieveSubcontractorListUrl
                      )
                    )
                  }

                case Failure(exception) =>
                  logger.warn(
                    "[AmendTrustConfirmationController] Failed to clean user answers",
                    exception
                  )
                  Future.successful(recoveryRedirect)
              }
          }
      }
    }

  private def trustDisplayName(ua: UserAnswers): String =
    ua.get(TrustNamePage).getOrElse("")
}

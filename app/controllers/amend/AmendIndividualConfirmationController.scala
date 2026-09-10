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

package controllers.amend

import controllers.actions.*
import controllers.routes
import pages.amend.AmendCheckYourAnswersSubmittedPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Reads
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{CisIdQuery, OriginalIndividualAnswersQuery}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{DefaultSubcontractorCleanupService, SubcontractorNameExtractor}
import viewmodels.amend.IndividualAmendedViewModel
import views.html.amend.AmendConfirmationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AmendIndividualConfirmationController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: AmendConfirmationView,
  cleanupService: DefaultSubcontractorCleanupService,
  sessionRepository: SessionRepository,
  subcontractorNameExtractor: SubcontractorNameExtractor
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val recoveryRedirect = Redirect(routes.JourneyRecoveryController.onPageLoad())

      val ua = request.userAnswers

      if (!ua.get(AmendCheckYourAnswersSubmittedPage).contains(true)) {
        logger.warn("[AmendIndividualConfirmationController] Accessed without prior CYA submission")
        Future.successful(recoveryRedirect)
      } else {
        ua.get(OriginalIndividualAnswersQuery) match {
          case None =>
            logger.error("[AmendIndividualConfirmationController] Missing OriginalIndividualAnswersQuery")
            Future.successful(recoveryRedirect)

          case Some(originalIndividualAnswers) =>
            ua.get(CisIdQuery) match {

              case None =>
                logger.error("[AmendIndividualConfirmationController] Missing CisIdQuery")
                Future.successful(recoveryRedirect)

              case Some(cisId) =>
                val tableRows      = IndividualAmendedViewModel.rows(originalIndividualAnswers, ua)
                val individualName = subcontractorNameExtractor.displaySubcontractorName(ua)

                cleanupService.cleanAmend(ua) match {

                  case Success(cleanedUa) =>
                    sessionRepository.set(cleanedUa).map { _ =>
                      Ok(
                        view(
                          tableRows,
                          individualName
                        )
                      )
                    }

                  case Failure(exception) =>
                    logger.warn(
                      "[AmendIndividualConfirmationController] Failed to clean user answers",
                      exception
                    )
                    Future.successful(recoveryRedirect)
                }
            }
        }
      }
    }
}

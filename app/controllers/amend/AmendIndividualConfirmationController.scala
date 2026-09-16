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

import config.FrontendAppConfig
import controllers.actions.*
import controllers.routes
import models.UserAnswers
import models.amend.AmendJourneyType
import pages.amend.{AmendCheckYourAnswersSubmittedPage, AmendJourneyTypePage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.{CisIdQuery, OriginalIndividualAnswersQuery}
import repositories.SessionRepository
import services.VerificationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{DefaultSubcontractorCleanupService, SubcontractorNameExtractor}
import viewmodels.amend.{AmendConfirmationLinks, IndividualAmendedViewModel}
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
  verificationService: VerificationService,
  sessionRepository: SessionRepository,
  appConfig: FrontendAppConfig,
  subcontractorNameExtractor: SubcontractorNameExtractor
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val userAnswers =
        request.userAnswers

      if (
        !userAnswers
          .get(AmendCheckYourAnswersSubmittedPage)
          .contains(true)
      ) {
        logger.warn(
          "[AmendIndividualConfirmationController.onPageLoad] " +
            "Accessed without prior CYA submission"
        )

        Future.successful(journeyRecoveryRedirect)
      } else {
        renderConfirmation(userAnswers)
      }
    }

  private def renderConfirmation(
    userAnswers: UserAnswers
  )(implicit request: play.api.mvc.Request[?]): Future[Result] =
    (
      userAnswers.get(OriginalIndividualAnswersQuery),
      userAnswers.get(CisIdQuery),
      userAnswers.get(AmendJourneyTypePage)
    ) match {

      case (
            Some(originalIndividualAnswers),
            Some(cisId),
            Some(journeyType)
          ) =>
        val tableRows =
          IndividualAmendedViewModel.rows(
            originalIndividualAnswers,
            userAnswers
          )

        val individualName =
          subcontractorNameExtractor.displaySubcontractorName(userAnswers)

        val link =
          AmendConfirmationLinks.build(
            journeyType,
            cisId,
            appConfig
          )
        cleanupService.cleanAmend(userAnswers) match {

          case Success(cleanedUserAnswers) =>
            val persistFinalUserAnswers =
              journeyType match {

                case AmendJourneyType.Standard =>
                  sessionRepository
                    .set(cleanedUserAnswers)
                    .map(_ => cleanedUserAnswers)

                case AmendJourneyType.InsufficientInfo | AmendJourneyType.UnmatchedInfo =>
                  verificationService.refreshVerificationBatches(
                    cleanedUserAnswers
                  )
              }

            persistFinalUserAnswers
              .map { _ =>
                Ok(
                  view(
                    rows = tableRows,
                    subcontractorName = individualName,
                    confirmationLink = link
                  )
                )
              }
              .recover { case exception =>
                logger.error(
                  "[AmendIndividualConfirmationController.onPageLoad] " +
                    "Failed to persist confirmation session data",
                  exception
                )

                journeyRecoveryRedirect
              }

          case Failure(exception) =>
            logger.warn(
              "[AmendIndividualConfirmationController.onPageLoad] " +
                "Failed to clean user answers",
              exception
            )

            Future.successful(journeyRecoveryRedirect)
        }

      case (None, _, _) =>
        logger.error(
          "[AmendIndividualConfirmationController.onPageLoad] " +
            "Missing OriginalIndividualAnswersQuery"
        )

        Future.successful(journeyRecoveryRedirect)

      case (_, None, _) =>
        logger.error(
          "[AmendIndividualConfirmationController.onPageLoad] " +
            "Missing CisIdQuery"
        )

        Future.successful(journeyRecoveryRedirect)

      case (_, _, None) =>
        logger.error(
          "[AmendIndividualConfirmationController.onPageLoad] " +
            "Missing AmendJourneyTypePage"
        )

        Future.successful(journeyRecoveryRedirect)
    }

  private def journeyRecoveryRedirect: Result =
    Redirect(
      routes.JourneyRecoveryController.onPageLoad()
    )
}

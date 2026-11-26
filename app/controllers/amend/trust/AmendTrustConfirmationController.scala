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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, MultiTabAction}
import controllers.helpers.SubcontractorNameDisplayHelper
import controllers.routes
import models.{AmendMode, UserAnswers}
import models.amend.AmendJourneyType
import pages.amend.{AmendCheckYourAnswersSubmittedPage, AmendJourneyTypePage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Reads
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{CisIdQuery, OriginalTrustAnswersQuery}
import repositories.SessionRepository
import services.VerificationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.amend.AmendConfirmationLinks
import viewmodels.amend.trust.TrustAmendConfirmationViewModel
import views.html.amend.AmendConfirmationView

import javax.inject.Inject
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AmendTrustConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  val controllerComponents: MessagesControllerComponents,
  verificationService: VerificationService,
  sessionRepository: SessionRepository,
  view: AmendConfirmationView,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val recoveryRedirect = Redirect(routes.JourneyRecoveryController.onPageLoad())

      val ua = request.userAnswers

      if (!ua.get(AmendCheckYourAnswersSubmittedPage).contains(true)) {
        logger.error("[AmendTrustConfirmationController] Accessed without prior CYA submission")
        Future.successful(recoveryRedirect)
      } else {
        ua.get(OriginalTrustAnswersQuery) match {
          case None                       =>
            logger.error("[AmendTrustConfirmationController] Missing OriginalTrustAnswersQuery")
            Future.successful(recoveryRedirect)
          case Some(originalTrustAnswers) =>
            ua.get(CisIdQuery) match {
              case None =>
                logger.error("[AmendTrustConfirmationController] Missing CisIdQuery")
                Future.successful(recoveryRedirect)

              case Some(cisId) =>
                ua.get(AmendJourneyTypePage) match {

                  case Some(journeyType) =>
                    val tableRows =
                      TrustAmendConfirmationViewModel.rows(
                        originalTrustAnswers,
                        ua
                      )

                    val trustName =
                      trustDisplayName(ua)

                    val confirmationLink =
                      AmendConfirmationLinks.build(
                        journeyType,
                        cisId,
                        appConfig
                      )

                    val persistFinalUserAnswers =
                      journeyType match {

                        case AmendJourneyType.Standard =>
                          sessionRepository
                            .set(ua)
                            .map(_ => ua)

                        case AmendJourneyType.InsufficientInfo | AmendJourneyType.UnmatchedInfo =>
                          verificationService.refreshVerificationBatches(ua)
                      }

                    persistFinalUserAnswers
                      .map { _ =>
                        Ok(
                          view(
                            tableRows,
                            trustName,
                            confirmationLink
                          )
                        )
                      }
                      .recover { case exception =>
                        logger.error(
                          "[AmendTrustConfirmationController.onPageLoad] " +
                            "Failed to persist confirmation session data",
                          exception
                        )

                        recoveryRedirect
                      }

                  case None =>
                    logger.error(
                      "[AmendTrustConfirmationController] Missing AmendJourneyTypePage"
                    )

                    Future.successful(recoveryRedirect)
                }
            }
        }
      }
    }

  def onPageLoadWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val subJourneyId     = flowId.toString
      val recoveryRedirect = Redirect(routes.JourneyRecoveryController.onPageLoad())

      val ua = request.userAnswers

      if (!ua.get(AmendCheckYourAnswersSubmittedPage(subJourneyId)).contains(true)) {
        logger.error("[AmendTrustConfirmationController] Accessed without prior CYA submission")
        Future.successful(recoveryRedirect)
      } else {
        ua.get(OriginalTrustAnswersQuery) match { // TODO DTR-9463: Do we need to update queries with id?
          case None                       =>
            logger.error("[AmendTrustConfirmationController] Missing OriginalTrustAnswersQuery")
            Future.successful(recoveryRedirect)
          case Some(originalTrustAnswers) =>
            ua.get(CisIdQuery(subJourneyId)) match {
              case None =>
                logger.error("[AmendTrustConfirmationController] Missing CisIdQuery")
                Future.successful(recoveryRedirect)

              case Some(cisId) =>
                ua.get(AmendJourneyTypePage(subJourneyId)) match {

                  case Some(journeyType) =>
                    val tableRows =
                      TrustAmendConfirmationViewModel.rows(
                        originalTrustAnswers,
                        ua,
                        subJourneyId
                      )

                    val trustName =
                      trustDisplayName(ua, subJourneyId)

                    val confirmationLink =
                      AmendConfirmationLinks.build(
                        journeyType,
                        cisId,
                        appConfig,
                        flowId
                      )

                    val persistFinalUserAnswers =
                      journeyType match {

                        case AmendJourneyType.Standard =>
                          sessionRepository
                            .set(ua)
                            .map(_ => ua)

                        case AmendJourneyType.InsufficientInfo | AmendJourneyType.UnmatchedInfo =>
                          verificationService.refreshVerificationBatches(ua)
                      }

                    persistFinalUserAnswers
                      .map { _ =>
                        Ok(
                          view(
                            tableRows,
                            trustName,
                            confirmationLink
                          )
                        )
                      }
                      .recover { case exception =>
                        logger.error(
                          "[AmendTrustConfirmationController.onPageLoadWithId] " +
                            "Failed to persist confirmation session data",
                          exception
                        )

                        recoveryRedirect
                      }

                  case None =>
                    logger.error(
                      "[AmendTrustConfirmationController] Missing AmendJourneyTypePage"
                    )

                    Future.successful(recoveryRedirect)
                }
            }
        }
      }
    }

  private def trustDisplayName(ua: UserAnswers, subJourneyId: Option[String] = None)(implicit
    messages: Messages
  ): String =
    subJourneyId match {
      case Some(id) => SubcontractorNameDisplayHelper.trustDisplayName(ua, AmendMode, id)
      case None     => SubcontractorNameDisplayHelper.trustDisplayName(ua, AmendMode)
    }

}

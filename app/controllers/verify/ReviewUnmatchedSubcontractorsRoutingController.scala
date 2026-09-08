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

package controllers.verify

import controllers.actions.*
import pages.verify.LastSubmittedVerificationBatchResponsePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{CheckUnmatchedSubcontractorsService, VerificationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReviewUnmatchedSubcontractorsRoutingController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  cisIdRequired: CisIdRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  verificationService: VerificationService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen cisIdRequired).async { implicit request =>
      request.userAnswers.get(LastSubmittedVerificationBatchResponsePage) match {

        case Some(response) =>
          val hasUnmatched =
            response.verifications.exists(
              CheckUnmatchedSubcontractorsService.isUnmatched
            )

          if (!hasUnmatched) {
            Future.successful(
              Redirect(
                controllers.verify.routes.VerificationResultsController.onPageLoad()
              )
            )
          } else {
            verificationService
              .anyUnmatchedResourceRefsStillPresent(
                request.cisId,
                response
              )
              .recoverWith { case t =>
                logger.error(
                  "[ReviewUnmatchedSubcontractorsRoutingController.onPageLoad] " +
                    "Failed to check whether unmatched subcontractors are still present",
                  t
                )

                Future.failed(t)
              }
              .flatMap {
                case true =>
                  verificationService
                    .recreateCurrentBatchFromUnmatchedVerifications(
                      request.cisId,
                      request.userAnswers
                    )
                    .map { _ =>
                      Redirect(
                        controllers.routes.UnmatchedSubcontractorsController
                          .onPageLoad()
                      )
                    }
                    .recover { case t =>
                      logger.error(
                        "[ReviewUnmatchedSubcontractorsRoutingController.onPageLoad] " +
                          "Failed to recreate the current batch from unmatched verifications",
                        t
                      )

                      Redirect(
                        controllers.routes.SystemErrorController.onPageLoad()
                      )
                    }

                case false =>
                  Future.successful(
                    Redirect(
                      controllers.routes.NoUnmatchedSubcontractorsController
                        .onPageLoad()
                    )
                  )
              }
              .recover { case t =>
                logger.error(
                  "[ReviewUnmatchedSubcontractorsRoutingController.onPageLoad] " +
                    "Unexpected failure routing unmatched subcontractors",
                  t
                )

                Redirect(
                  controllers.routes.SystemErrorController.onPageLoad()
                )
              }
          }

        case None =>
          Future.successful(
            Redirect(
              controllers.routes.JourneyRecoveryController.onPageLoad()
            )
          )
      }
    }
}

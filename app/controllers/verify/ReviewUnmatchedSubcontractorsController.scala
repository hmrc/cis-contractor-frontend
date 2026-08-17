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
import queries.CisIdQuery
import services.VerificationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.verify.VerificationResultsViewModel

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReviewUnmatchedSubcontractorsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  verificationService: VerificationService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      (
        request.userAnswers.get(LastSubmittedVerificationBatchResponsePage),
        request.userAnswers.get(CisIdQuery)
      ) match {
        case (Some(response), Some(cisId)) =>
          val unmatchedIds = VerificationResultsViewModel.unmatchedSubcontractorIds(response)

          if (unmatchedIds.isEmpty) {
            Future.successful(Redirect(controllers.verify.routes.VerificationResultsController.onPageLoad()))
          } else {
            verificationService
              .anyUnmatchedSubcontractorsStillPresent(cisId, unmatchedIds)
              .map { stillPresent =>
                if (stillPresent) {
                  Redirect(controllers.routes.UnmatchedSubcontractorsController.onPageLoad())
                } else {
                  Redirect(controllers.routes.NoUnmatchedSubcontractorsController.onPageLoad())
                }
              }
              .recover { case t =>
                logger.error(
                  "[ReviewUnmatchedSubcontractorsController.onPageLoad] Failed to check live subcontractors",
                  t
                )
                Redirect(controllers.routes.SystemErrorController.onPageLoad())
              }
          }

        case _ =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}

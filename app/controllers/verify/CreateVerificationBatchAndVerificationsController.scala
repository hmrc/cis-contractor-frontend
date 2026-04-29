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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.verification.CurrentVerificationBatchResponsePage
import pages.verify.{ReverifySubcontractorsPage, SelectedSubcontractorsToVerifyPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VerificationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreateVerificationBatchAndVerificationsController @Inject() (
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

  private def hasCurrentBatch(ua: models.UserAnswers): Boolean =
    ua.get(CurrentVerificationBatchResponsePage).exists { current =>
      current.verificationBatch.nonEmpty || current.verifications.nonEmpty
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val vf03Ids: Seq[Long] =
        request.userAnswers.get(SelectedSubcontractorsToVerifyPage).getOrElse(Nil)

      val vf03cIds: Seq[Long] =
        request.userAnswers.get(ReverifySubcontractorsPage).getOrElse(Nil)

      val selectedIds = (vf03Ids ++ vf03cIds).distinct

      request.userAnswers.get(CurrentVerificationBatchResponsePage) match {

        case None =>
          // We rely on CurrentVerificationBatchResponsePage for the id->resourceRef mapping in the service.
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

        case Some(_) if hasCurrentBatch(request.userAnswers) =>
          // Todo: when modifyCurrentBatch endpoint is implemented, call that here instead.
          Future.successful(Ok("Current verification batch exists - modify flow to be implemented"))

        case Some(_) =>
          verificationService
            .createVerificationBatchAndVerifications(
              userAnswers = request.userAnswers,
              selectedSubcontractorIds = selectedIds,
              actionIndicator = None
            )
            .map { _ =>
              // Todo redirect to the real page later
              Redirect(controllers.routes.IndexController.onPageLoad())
            }
            .recover { case t =>
              logger.error(
                "[CreateVerificationBatchAndVerificationsController.onSubmit] Failed to create verification batch/verifications",
                t
              )
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
      }
    }
}

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
import models.NormalMode
import pages.verify.NewestVerificationBatchResponsePage
import pages.verify.UnverifiedSubcontractorsPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VerificationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class NewestVerificationBatchController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  verificationBatchService: VerificationService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      verificationBatchService
        .refreshNewestVerificationBatch(request.userAnswers)
        .map { updatedAnswers =>

          val batch      = updatedAnswers.get(NewestVerificationBatchResponsePage)
          val unverified = updatedAnswers.get(UnverifiedSubcontractorsPage).getOrElse(Seq.empty)

          batch match {
            case Some(response) if response.subcontractors.isEmpty =>
              Redirect(controllers.verify.routes.NoSubcontractorsAddedController.onPageLoad())

            case Some(response) if response.subcontractors.nonEmpty && unverified.isEmpty =>
              Redirect(controllers.verify.routes.VerifyYourSubcontractorsYesNoController.onPageLoad)

            case Some(_) =>
              Redirect(controllers.verify.routes.SelectSubcontractorController.onPageLoad(NormalMode))

            case None =>
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
        }
        .recover { case t =>
          logger.error(
            "[NewestVerificationBatchController.onPageLoad] Failed to refresh newest verification batch",
            t
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }
}

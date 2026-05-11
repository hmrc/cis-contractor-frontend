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
import pages.verify.{CurrentVerificationBatchResponsePage, SelectSubcontractorPage, SelectSubcontractorsToReverifyPage}
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

  private def parseIds(label: String, ids: Iterable[String]): Either[String, Seq[Long]] = {
    val parsed = ids.toSeq.distinct.map(_.trim).map(_.toLongOption)
    if (parsed.forall(_.isDefined)) Right(parsed.flatten)
    else Left(s"Invalid subcontractor id(s) found in $label")
  }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val verifyIdsRaw: Seq[String] =
        request.userAnswers
          .get(SelectSubcontractorPage)
          .map(_.toSeq.map(_.id))
          .getOrElse(Seq.empty)

      val reverifyIdsRaw: Seq[String] =
        request.userAnswers
          .get(SelectSubcontractorsToReverifyPage)
          .map(_.toSeq.map(_.id))
          .getOrElse(Seq.empty)

      val selectedIdsEither =
        for {
          verifyIds   <- parseIds("SelectSubcontractorPage", verifyIdsRaw)
          reverifyIds <- parseIds("SelectSubcontractorsToReverifyPage", reverifyIdsRaw)
        } yield (verifyIds ++ reverifyIds).distinct

      selectedIdsEither match {

        case Left(msg) =>
          logger.error(s"[CreateVerificationBatchAndVerificationsController.onSubmit] $msg")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

        case Right(selectedIds) =>
          request.userAnswers.get(CurrentVerificationBatchResponsePage) match {

            case None =>
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

            case Some(_) if hasCurrentBatch(request.userAnswers) =>
              Future.successful(Ok("Current verification batch exists - modify flow to be implemented"))

            case Some(_) =>
              verificationService
                .createVerificationBatchAndVerifications(
                  userAnswers = request.userAnswers,
                  selectedSubcontractorIds = selectedIds,
                  actionIndicator = None
                )
                .map(_ => Redirect(controllers.routes.IndexController.onPageLoad()))
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
}

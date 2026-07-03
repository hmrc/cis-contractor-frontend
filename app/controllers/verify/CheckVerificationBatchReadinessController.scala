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
import models.{AmendMode, CheckMode, Mode, NormalMode, UserAnswers}
import models.verify.VerificationBatchReadiness
import pages.verify.{NewestVerificationBatchResponsePage, SelectSubcontractorPage, SelectSubcontractorsToReverifyPage, VerificationBatchReadinessPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckVerificationBatchReadinessController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def checkVerificationBatchReadinessInCheckMode(): Action[AnyContent] =
    checkVerificationBatchReadiness(CheckMode)

  def checkVerificationBatchReadiness(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val ua = request.userAnswers

      val selectedUnverifiedIds: Set[String] =
        ua.get(SelectSubcontractorPage)
          .getOrElse(Set.empty)
          .map(_.id)

      val selectedReverifyIds: Set[String] =
        ua.get(SelectSubcontractorsToReverifyPage)
          .getOrElse(Set.empty)
          .map(_.id)

      val selectedIds: Set[String] =
        selectedUnverifiedIds ++ selectedReverifyIds

      val isReverifyOnly =
        selectedUnverifiedIds.isEmpty && selectedReverifyIds.nonEmpty

      val batchReadyOpt =
        ua.get(NewestVerificationBatchResponsePage)
          .filter(_ => selectedIds.nonEmpty)
          .map { batchResponse =>
            if (isReverifyOnly) {
              val batchSubcontractorIds =
                batchResponse.subcontractors.map(_.subcontractorId.toString).toSet

              selectedReverifyIds.subsetOf(batchSubcontractorIds)
            } else {
              VerificationBatchReadiness.isBatchReady(selectedIds, batchResponse.subcontractors)
            }
          }

      batchReadyOpt match {
        case Some(true) =>
          for {
            updatedAnswers <- Future.fromTry(ua.set(VerificationBatchReadinessPage, true))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            val redirect = mode match {
              case NormalMode =>
                nextEmailConfirmationPage(updatedAnswers)

              case CheckMode =>
                controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()

              case AmendMode =>
                controllers.routes.JourneyRecoveryController.onPageLoad()
            }

            Redirect(redirect)
          }

        case Some(false) =>
          // TODO(DTR-4685): Route to VF-05 once that page is built; for now redirecting to Journey Recovery
          for {
            updatedAnswers <- Future.fromTry(ua.set(VerificationBatchReadinessPage, false))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

        case None =>
          logger.error(
            "[CheckVerificationBatchReadinessController.checkVerificationBatchReadiness] Missing selected subcontractors or verification batch response"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  private def nextEmailConfirmationPage(ua: UserAnswers): Call = {
    val hasStoredEmail = ua
      .get(NewestVerificationBatchResponsePage)
      .flatMap(_.scheme)
      .flatMap(_.emailAddress)
      .isDefined
    if (hasStoredEmail) {
      controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoad(NormalMode)
    } else {
      controllers.verify.routes.ContractorEmailConfirmationNotStoredController.onPageLoad(NormalMode)
    }
  }
}

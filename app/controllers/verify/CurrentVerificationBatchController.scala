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
import models.Mode
import models.response.GetCurrentVerificationBatchResponse
import pages.validation.SubcontractorValidationFailuresPage
import pages.verify.CurrentVerificationBatchResponsePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.{SubcontractorDetailsValidator, VerificationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CurrentVerificationBatchController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  verificationBatchService: VerificationService,
  subcontractorDetailsValidator: SubcontractorDetailsValidator,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      verificationBatchService
        .getCurrentVerificationBatch(
          request.userAnswers
        )
        .flatMap { updatedAnswers =>
          updatedAnswers
            .get(CurrentVerificationBatchResponsePage)
            .map { response =>
              val validationFailures =
                subcontractorDetailsValidator.validate(
                  response.subcontractors
                )

              for {
                answersWithFailures <-
                  Future.fromTry(
                    updatedAnswers.set(
                      SubcontractorValidationFailuresPage,
                      validationFailures
                    )
                  )

                _ <- sessionRepository.set(
                       answersWithFailures
                     )
              } yield redirectFor(response, mode)
            }
            .getOrElse {
              Future.successful(
                Redirect(
                  controllers.routes.JourneyRecoveryController
                    .onPageLoad()
                )
              )
            }
        }
        .recover { case throwable =>
          logger.error(
            "[CurrentVerificationBatchController.onPageLoad] Failed to refresh, validate or persist the current verification batch",
            throwable
          )

          Redirect(
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
          )
        }
    }

  private def redirectFor(
    response: GetCurrentVerificationBatchResponse,
    mode: Mode
  ): Result =
    if (
      response.verificationBatch.nonEmpty ||
      response.verifications.nonEmpty
    ) {
      Redirect(
        controllers.verify.routes.ModifyVerificationBatchAndVerificationsController
          .modifyVerificationBatch(mode)
      )
    } else {
      Redirect(
        controllers.verify.routes.CreateVerificationBatchAndVerificationsController
          .onSubmit(mode)
      )
    }
}

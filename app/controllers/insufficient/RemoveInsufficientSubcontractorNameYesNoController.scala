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

package controllers.insufficient

import controllers.actions.*
import forms.insufficient.RemoveInsufficientSubcontractorNameYesNoFormProvider
import models.{Mode, NormalMode, SubcontractorCurrentVerification, TypeOfSubcontractor}
import models.TypeOfSubcontractor.*
import models.requests.DataRequest
import pages.insufficient.RemoveInsufficientSubcontractorNameYesNoPage
import pages.verify.CurrentVerificationBatchResponsePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.VerificationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.SubcontractorNameExtractor
import views.html.insufficient.RemoveInsufficientSubcontractorNameYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveInsufficientSubcontractorNameYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  verificationService: VerificationService,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: RemoveInsufficientSubcontractorNameYesNoFormProvider,
  subcontractorNameExtractor: SubcontractorNameExtractor,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveInsufficientSubcontractorNameYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  private def recoveryRedirect =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  private def preparedForm(verificationResourceRef: Long)(implicit request: DataRequest[?]) =
    request.userAnswers
      .get(RemoveInsufficientSubcontractorNameYesNoPage(verificationResourceRef))
      .fold(form)(form.fill)

  def onPageLoad(verificationResourceRef: Long = -1L, mode: Mode = NormalMode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      subcontractorName(request, verificationResourceRef)
        .fold(recoveryRedirect) { subcontractorName =>
          Ok(
            view(
              preparedForm(verificationResourceRef),
              mode,
              subcontractorName,
              verificationResourceRef
            )
          )
        }
    }

  def onSubmit(verificationResourceRef: Long = -1L, mode: Mode = NormalMode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      subcontractorName(request, verificationResourceRef)
        .fold(Future.successful(recoveryRedirect)) { subcontractorName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      mode,
                      subcontractorName,
                      verificationResourceRef
                    )
                  )
                ),
              value =>
                for {
                  updatedAnswers <-
                    Future.fromTry(
                      request.userAnswers.set(
                        RemoveInsufficientSubcontractorNameYesNoPage(verificationResourceRef),
                        value
                      )
                    )

                  _ <- sessionRepository.set(updatedAnswers)

                  redirect <-
                    if (value) {
                      deleteAndRedirect(updatedAnswers, verificationResourceRef, mode)
                    } else {
                      Future.successful(
                        Redirect(
                          controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController.onPageLoad()
                        )
                      )
                    }
                } yield redirect
            )
        }
    }

  private def deleteAndRedirect(
    userAnswers: models.UserAnswers,
    verificationResourceRef: Long,
    mode: Mode
  )(implicit request: DataRequest[?]): Future[play.api.mvc.Result] =
    if (verificationResourceRef < 0) {
      Future.successful(recoveryRedirect)
    } else {
      verificationService
        .deleteVerification(userAnswers, verificationResourceRef)
        .map {
          case response if response.verificationsCounter.exists(_ > 0) =>
            Redirect(controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController.onPageLoad())

          case response if response.verificationsCounter.contains(0L) =>
            Redirect(
              controllers.verify.routes.CheckVerificationBatchReadinessController.checkVerificationBatchReadiness(mode)
            )

          case _ =>
            recoveryRedirect
        }
        .recover { case _ =>
          recoveryRedirect
        }
    }

  private def subcontractorName(request: DataRequest[?], verificationResourceRef: Long): Option[String] =
    nameFromCurrentBatch(request, verificationResourceRef)
      .orElse(subcontractorNameExtractor.getSubcontractorName(request.userAnswers))

  private def nameFromCurrentBatch(request: DataRequest[?], verificationResourceRef: Long): Option[String] =
    if (verificationResourceRef < 0) {
      None
    } else {
      for {
        batch         <- request.userAnswers.get(CurrentVerificationBatchResponsePage)
        verification  <- batch.verifications.find(_.verificationResourceRef.contains(verificationResourceRef))
        subId         <- verification.subcontractorId
        subcontractor <- batch.subcontractors.find(_.subcontractorId == subId)
        name          <- displayName(subcontractor)
      } yield name
    }

  private def displayName(sub: SubcontractorCurrentVerification): Option[String] = {
    val first              = sub.firstName.map(_.trim).filter(_.nonEmpty)
    val surname            = sub.surname.map(_.trim).filter(_.nonEmpty)
    val trading            = sub.tradingName.map(_.trim).filter(_.nonEmpty)
    val partnershipTrading = sub.partnershipTradingName.map(_.trim).filter(_.nonEmpty)

    val individualName =
      surname.map { s =>
        first.map(f => s"$f $s").getOrElse(s)
      }

    sub.subcontractorType.flatMap(TypeOfSubcontractor.enumerable.withName) match {
      case Some(Individualorsoletrader) => individualName.orElse(trading)
      case Some(Limitedcompany)         => trading
      case Some(Trust)                  => trading.orElse(partnershipTrading)
      case Some(Partnership)            => partnershipTrading.orElse(trading)
      case _                            => partnershipTrading.orElse(trading).orElse(individualName)
    }
  }
}

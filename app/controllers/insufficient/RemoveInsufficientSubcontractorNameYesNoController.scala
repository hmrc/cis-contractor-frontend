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
import controllers.verify.CheckVerificationBatchReadinessController
import forms.insufficient.RemoveInsufficientSubcontractorNameYesNoFormProvider
import models.TypeOfSubcontractor.*
import models.requests.DataRequest
import models.{Mode, NormalMode, SubcontractorCurrentVerification, TypeOfSubcontractor, UserAnswers}
import pages.insufficient.{RemoveInsufficientSubcontractorNameYesNoForFlowPage, RemoveInsufficientSubcontractorNameYesNoPage}
import pages.verify.{CurrentVerificationBatchResponsePage, SelectSubcontractorPage, SelectSubcontractorsToReverifyPage, UnverifiedSubcontractorsPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.VerificationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.insufficient.RemoveInsufficientSubcontractorNameYesNoView

import java.util.UUID
import scala.util.{Success, Try}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveInsufficientSubcontractorNameYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  verificationService: VerificationService,
  checkVerificationBatchReadinessController: CheckVerificationBatchReadinessController,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: RemoveInsufficientSubcontractorNameYesNoFormProvider,
  multiTabAction: MultiTabAction,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveInsufficientSubcontractorNameYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form: Form[Boolean] = formProvider()

  private def recoveryRedirect =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  private def preparedForm(verificationResourceRef: Long)(implicit request: DataRequest[?]) =
    request.userAnswers
      .get(RemoveInsufficientSubcontractorNameYesNoPage(verificationResourceRef))
      .fold(form)(form.fill)

  // DTR-9569 Multitab preparedForm
  private def preparedFormWithId(id: String, verificationResourceRef: Long)(implicit request: DataRequest[?]) =
    request.userAnswers
      .get(RemoveInsufficientSubcontractorNameYesNoForFlowPage(id, verificationResourceRef))
      .fold(form)(form.fill)

  def onPageLoad(
    verificationResourceRef: Long = -1L,
    mode: Mode = NormalMode
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val formAction = routes.RemoveInsufficientSubcontractorNameYesNoController.onSubmit(verificationResourceRef)

      val subcontractorName =
        getSubcontractorName(request, verificationResourceRef)

      Ok(
        view(
          preparedForm(verificationResourceRef),
          mode,
          subcontractorName,
          verificationResourceRef,
          formAction
        )
      )
    }

  def onSubmit(
    verificationResourceRef: Long = -1L,
    mode: Mode = NormalMode
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val formAction = routes.RemoveInsufficientSubcontractorNameYesNoController.onSubmit(verificationResourceRef)

      val subcontractorName =
        getSubcontractorName(request, verificationResourceRef)

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
                  verificationResourceRef,
                  formAction
                )
              )
            ),
          value =>
            val subcontractorIdOpt = for {
              batch        <- request.userAnswers.get(CurrentVerificationBatchResponsePage)
              verification <- batch.verifications.find(_.verificationResourceRef.contains(verificationResourceRef))
              subId        <- verification.subcontractorId
            } yield subId.toString

            for {
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers.set(
                    RemoveInsufficientSubcontractorNameYesNoPage(verificationResourceRef),
                    value
                  )
                )

              cleanedAnswers <-
                Future.fromTry(
                  if (value) {
                    removeSubcontractorFromSelection(updatedAnswers, verificationResourceRef, subcontractorIdOpt)
                  } else {
                    updatedAnswers.remove(RemoveInsufficientSubcontractorNameYesNoPage(verificationResourceRef))
                  }
                )

              _ <- sessionRepository.set(cleanedAnswers)

              redirect <-
                if (value) {
                  deleteAndRedirect(cleanedAnswers, verificationResourceRef)
                } else {
                  Future.successful(
                    Redirect(
                      controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
                        .onPageLoad()
                    )
                  )
                }
            } yield redirect
        )
    }

  // DTR-9569 MultiTab onPageLoad
  def onPageLoadWithId(
                  flowId: UUID,
                  verificationResourceRef: Long = -1L,
                  mode: Mode = NormalMode
                ): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val id         = flowId.toString
      val formAction =
        routes.RemoveInsufficientSubcontractorNameYesNoController.onSubmitWithId(flowId, verificationResourceRef)

      val subcontractorName =
        getSubcontractorName(request, verificationResourceRef)

      Ok(
        view(
          preparedFormWithId(id, verificationResourceRef),
          mode,
          subcontractorName,
          verificationResourceRef,
          formAction
        )
      )
    }

  def onSubmitWithId(
                flowId: UUID,
                verificationResourceRef: Long = -1L,
                mode: Mode = NormalMode
              ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val id = flowId.toString
      val formAction = routes.RemoveInsufficientSubcontractorNameYesNoController.onSubmitWithId(flowId, verificationResourceRef)

      val subcontractorName =
        getSubcontractorNameWithId(id, request, verificationResourceRef)

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
                  verificationResourceRef,
                  formAction
                )
              )
            ),
          value =>
            val subcontractorIdOpt = for {
              batch <- request.userAnswers.get(CurrentVerificationBatchResponsePage(id))
              verification <- batch.verifications.find(_.verificationResourceRef.contains(verificationResourceRef))
              subId <- verification.subcontractorId
            } yield subId.toString

            for {
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers.set(
                    RemoveInsufficientSubcontractorNameYesNoForFlowPage(id, verificationResourceRef),
                    value
                  )
                )

              cleanedAnswers <-
                Future.fromTry(
                  if (value) {
                    removeSubcontractorFromSelectionWithId(id, updatedAnswers, verificationResourceRef, subcontractorIdOpt)
                  } else {
                    updatedAnswers.remove(RemoveInsufficientSubcontractorNameYesNoForFlowPage(id, verificationResourceRef))
                  }
                )

              _ <- sessionRepository.set(cleanedAnswers)

              redirect <-
                if (value) {
                  deleteAndRedirect(cleanedAnswers, verificationResourceRef)
                } else {
                  Future.successful(
                    Redirect(routes.RemoveInsufficientSubcontractorNameYesNoController.onPageLoadWithId(flowId,
                      verificationResourceRef))
//                    TODO: uncomment below and replace above redirect
//                    Redirect(
//                      controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
//                        .onPageLoadWithId(flowId)
//                    )
                  )
                }
            } yield redirect
        )
    }

  private def removeSubcontractorFromSelection(
    userAnswers: UserAnswers,
    verificationResourceRef: Long,
    subcontractorId: Option[String]
  ): Try[UserAnswers] = {
    val withSelectionsRemoved =
      subcontractorId.fold[Try[UserAnswers]](Success(userAnswers)) { id =>
        for {
          afterSelect   <- userAnswers
                             .get(SelectSubcontractorPage)
                             .fold[Try[UserAnswers]](Success(userAnswers))(selected =>
                               userAnswers.set(SelectSubcontractorPage, selected.filterNot(_.id == id))
                             )
          afterReverify <- afterSelect
                             .get(SelectSubcontractorsToReverifyPage)
                             .fold[Try[UserAnswers]](Success(afterSelect))(selected =>
                               afterSelect.set(SelectSubcontractorsToReverifyPage, selected.filterNot(_.id == id))
                             )
        } yield afterReverify
      }

    withSelectionsRemoved.flatMap(
      _.remove(RemoveInsufficientSubcontractorNameYesNoPage(verificationResourceRef))
    )
  }

  //DTR-9569 removeSubcontractorFromSelection UUID scoped
  private def removeSubcontractorFromSelectionWithId(
                                                flowId: String,
                                                userAnswers: UserAnswers,
                                                verificationResourceRef: Long,
                                                subcontractorId: Option[String]
                                              ): Try[UserAnswers] = {
    val withSelectionsRemoved =
      subcontractorId.fold[Try[UserAnswers]](Success(userAnswers)) { id =>
        for {
          afterSelect <- userAnswers
            .get(SelectSubcontractorPage(flowId))
            .fold[Try[UserAnswers]](Success(userAnswers))(selected =>
              userAnswers.set(SelectSubcontractorPage(flowId), selected.filterNot(_.id == id))
            )
          afterReverify <- afterSelect
            .get(SelectSubcontractorsToReverifyPage(flowId))
            .fold[Try[UserAnswers]](Success(afterSelect))(selected =>
              afterSelect.set(SelectSubcontractorsToReverifyPage(flowId), selected.filterNot(_.id == id))
            )
        } yield afterReverify
      }

    withSelectionsRemoved.flatMap(
      _.remove(RemoveInsufficientSubcontractorNameYesNoForFlowPage(flowId, verificationResourceRef))
    )
  }

  //TODO make ID overload of deleteAndRedirect
  private def deleteAndRedirect(
    userAnswers: models.UserAnswers,
    verificationResourceRef: Long
  )(implicit request: DataRequest[?]): Future[play.api.mvc.Result] =
    if (verificationResourceRef < 0) {
      logger.error(
        s"[RemoveInsufficientSubcontractorNameYesNoController][deleteAndRedirect] - invalid verificationResourceRef=$verificationResourceRef"
      )
      Future.successful(recoveryRedirect)
    } else {
      verificationService
        .deleteVerification(userAnswers, verificationResourceRef)
        .flatMap {
          case response if response.verificationsCounter.exists(_ > 0) =>
            checkVerificationBatchReadiness().flatMap {
              case Some(updatedAnswers) =>
                verificationService
                  .refreshNewestVerificationBatch(updatedAnswers)
                  .map(_ =>
                    Redirect(controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController.onPageLoad())
                  )

              case None =>
                logger.error(
                  "[RemoveInsufficientSubcontractorNameYesNoController][deleteAndRedirect] - " +
                    s"userAnswers missing or batch readiness check failed, verificationResourceRef=$verificationResourceRef"
                )
                Future.successful(recoveryRedirect)
            }

          case response if response.verificationsCounter.contains(0L) =>
            verificationService
              .getCurrentVerificationBatch(userAnswers)
              .flatMap(refreshNewestBatchAndRedirectToVerificationSelection)

          case _ =>
            logger.error(
              s"[RemoveInsufficientSubcontractorNameYesNoController][deleteAndRedirect] - " +
                s"unexpected verificationsCounter after delete, verificationResourceRef=$verificationResourceRef"
            )
            Future.successful(recoveryRedirect)
        }
        .recover { case e =>
          logger.error(
            s"[RemoveInsufficientSubcontractorNameYesNoController.deleteAndRedirect] Failed to delete verificationResourceRef=$verificationResourceRef",
            e
          )
          recoveryRedirect
        }
    }

  // TODO ID overload
  private def checkVerificationBatchReadiness()(implicit request: DataRequest[?]): Future[Option[models.UserAnswers]] =
    sessionRepository
      .get(request.userId)
      .flatMap {
        case Some(updatedAnswers) =>
          logger.info("Remove Insufficient verification batch readiness check")
          checkVerificationBatchReadinessController.updateVerificationBatchReadiness(updatedAnswers)

        case None =>
          Future.successful(None)
      }

  private def refreshNewestBatchAndRedirectToVerificationSelection(
    userAnswers: models.UserAnswers
  )(implicit request: DataRequest[?]): Future[play.api.mvc.Result] =
    verificationService
      .refreshNewestVerificationBatch(userAnswers)
      .map { updatedAnswers =>
        val redirect =
          if (updatedAnswers.get(UnverifiedSubcontractorsPage).exists(_.nonEmpty)) {
            controllers.verify.routes.SelectSubcontractorController.onPageLoad(NormalMode)
          } else {
            controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(NormalMode)
          }

        Redirect(redirect)
      }

  // DTR-9569 refreshNewestBatchAndRedirectToVerificationSelection ID overload
  // TODO to be used in deleteAndRedirect ID overload method
  private def refreshNewestBatchAndRedirectToVerificationSelectionWithId(
    flowId: UUID,
    userAnswers: models.UserAnswers
  )(implicit request: DataRequest[?]): Future[play.api.mvc.Result] = {
    val id = flowId.toString
    verificationService
      .refreshNewestVerificationBatch(userAnswers)
      .map { updatedAnswers =>
        val redirect =
          if (updatedAnswers.get(UnverifiedSubcontractorsPage(id)).exists(_.nonEmpty)) {
            routes.RemoveInsufficientSubcontractorNameYesNoController.onPageLoadWithId(flowId)
            // TODO remove above replace with below
//            controllers.verify.routes.SelectSubcontractorController.onPageLoadWithId(flowId, NormalMode)
          } else {
            routes.RemoveInsufficientSubcontractorNameYesNoController.onPageLoadWithId(flowId)
            // TODO remove above replace with below
//            controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoadWithId(id, NormalMode)
          }

        Redirect(redirect)
      }
  }

  private def getSubcontractorName(
    request: DataRequest[?],
    verificationResourceRef: Long
  )(implicit messages: Messages): String =
    nameFromCurrentBatch(request, verificationResourceRef)
      .getOrElse(messages("verify.noName"))

  // DTR-9569 getSubcontractorName ID overload
  private def getSubcontractorNameWithId(
    id: String,
    request: DataRequest[?],
    verificationResourceRef: Long
  )(implicit messages: Messages): String =
    nameFromCurrentBatchWithId(id, request, verificationResourceRef)
      .getOrElse(messages("verify.noName"))

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

  // DTR-9569 nameFromCurrentBatch ID overload
  private def nameFromCurrentBatchWithId(id: String, request: DataRequest[?], verificationResourceRef: Long): Option[String] =
    if (verificationResourceRef < 0) {
      None
    } else {
      for {
        batch <- request.userAnswers.get(CurrentVerificationBatchResponsePage(id))
        verification <- batch.verifications.find(_.verificationResourceRef.contains(verificationResourceRef))
        subId <- verification.subcontractorId
        subcontractor <- batch.subcontractors.find(_.subcontractorId == subId)
        name <- displayName(subcontractor)
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
      case Some(Trust)                  => trading
      case Some(Partnership)            => partnershipTrading.orElse(trading)
      case _                            => partnershipTrading.orElse(trading).orElse(individualName)
    }
  }
}

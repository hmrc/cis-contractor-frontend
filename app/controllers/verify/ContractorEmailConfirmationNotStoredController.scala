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
import forms.verify.ContractorEmailConfirmationNotStoredFormProvider
import models.Mode
import models.UserAnswers
import models.finalvalidation.{FinalValidationDraftRequestBuilder, VerifyFinalValidationContinuation}
import navigation.Navigator
import pages.finalvalidation.{FinalValidationDraftIdPage, VerifyFinalValidationContinuationPage, VerifyFinalValidationModePage}
import pages.verify.{ContractorEmailConfirmationNotStoredPage, ContractorEmailConfirmationStoredPage, NewestVerificationBatchResponsePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import queries.VerifyCisIdQuery
import repositories.SessionRepository
import services.VerifyFinalValidationService
import services.finalvalidation.FinalValidationDraftService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.ContractorEmailConfirmationNotStoredView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContractorEmailConfirmationNotStoredController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  requireCisId: CisIdRequiredAction,
  multiTabAction: MultiTabAction,
  verifyFinalValidationService: VerifyFinalValidationService,
  finalValidationDraftService: FinalValidationDraftService,
  finalValidationDraftRequestBuilder: FinalValidationDraftRequestBuilder,
  formProvider: ContractorEmailConfirmationNotStoredFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ContractorEmailConfirmationNotStoredView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  private def hasStoredEmail(ua: UserAnswers, subJourneyId: Option[String] = None): Boolean = {
    val page =
      subJourneyId.map(NewestVerificationBatchResponsePage.apply).getOrElse(NewestVerificationBatchResponsePage)
    ua.get(page)
      .exists(_.scheme.exists(_.emailAddress.exists(_.nonEmpty)))
  }

  private def notStoredSubmitAction(mode: Mode, flowId: Option[UUID]): Call =
    flowId
      .map(id => routes.ContractorEmailConfirmationNotStoredController.onSubmitWithId(id, mode))
      .getOrElse(routes.ContractorEmailConfirmationNotStoredController.onSubmit(mode))

  private def redirectToStored(mode: Mode, flowId: Option[UUID] = None): Result =
    Redirect(
      flowId
        .map(id => controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoadWithId(id, mode))
        .getOrElse(controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoad(mode))
    )

  private def redirectToStoredAfterFinalValidation(mode: Mode): Result =
    Redirect(
      controllers.verify.routes.ContractorEmailConfirmationStoredController.onPageLoadAfterFinalValidation(mode)
    )

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireCisId).async { implicit request =>
      if (hasStoredEmail(request.userAnswers)) {
        Future.successful(redirectToStored(mode))
      } else {
        for {
          validation <- verifyFinalValidationService.validate(
                          request.cisId,
                          request.userAnswers
                        )

          result <-
            if (validation.hasErrors) {
              for {
                createRequest <- Future.fromTry(
                                   finalValidationDraftRequestBuilder.build(
                                     request.cisId,
                                     validation
                                   )
                                 )

                draftId <- finalValidationDraftService.create(createRequest)

                withContinuation <- Future.fromTry(
                                      request.userAnswers.set(
                                        VerifyFinalValidationContinuationPage,
                                        VerifyFinalValidationContinuation.ContractorEmailConfirmationNotStored
                                      )
                                    )

                withDraftId <- Future.fromTry(
                                 withContinuation.set(
                                   FinalValidationDraftIdPage,
                                   draftId
                                 )
                               )

                withMode <- Future.fromTry(
                              withDraftId.set(
                                VerifyFinalValidationModePage,
                                mode.toString
                              )
                            )

                _ <- sessionRepository.set(withMode)

              } yield Redirect(
                controllers.finalvalidations.routes.ReviewSubcontractorDetailsController.onPageLoad()
              )
            } else {
              val preparedForm = request.userAnswers.get(ContractorEmailConfirmationNotStoredPage) match {
                case None        => form
                case Some(value) => form.fill(value)
              }

              Future.successful(Ok(view(preparedForm, mode, notStoredSubmitAction(mode, None))))
            }

        } yield result
      }
    }

  def onPageLoadAfterFinalValidation(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      if (hasStoredEmail(request.userAnswers)) {
        Future.successful(redirectToStoredAfterFinalValidation(mode))
      } else {
        for {
          updatedAnswers <- Future.fromTry(
                              request.userAnswers.remove(
                                VerifyFinalValidationContinuationPage
                              )
                            )

          _ <- sessionRepository.set(updatedAnswers)

        } yield {
          val preparedForm = updatedAnswers.get(ContractorEmailConfirmationNotStoredPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, mode, notStoredSubmitAction(mode, None)))
        }
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      if (hasStoredEmail(request.userAnswers)) {
        Future.successful(redirectToStored(mode))
      } else {
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, notStoredSubmitAction(mode, None)))),
            value =>
              for {
                cleanedAnswers <-
                  Future.fromTry(request.userAnswers.remove(ContractorEmailConfirmationStoredPage))
                updatedAnswers <-
                  Future.fromTry(cleanedAnswers.set(ContractorEmailConfirmationNotStoredPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(ContractorEmailConfirmationNotStoredPage, mode, updatedAnswers))
          )
      }
    }

  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)).async {
      implicit request =>
        val id = flowId.toString
        if (hasStoredEmail(request.userAnswers, Some(id))) {
          Future.successful(redirectToStored(mode, Some(flowId)))
        } else {
          request.userAnswers.get(VerifyCisIdQuery(id)) match {
            case None        =>
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            case Some(cisId) =>
              for {
                validation <- verifyFinalValidationService.validate(cisId, request.userAnswers)
                result     <-
                  if (validation.hasErrors) {
                    for {
                      createRequest    <- Future.fromTry(finalValidationDraftRequestBuilder.build(cisId, validation))
                      draftId          <- finalValidationDraftService.create(createRequest)
                      withContinuation <- Future.fromTry(
                                            request.userAnswers.set(
                                              VerifyFinalValidationContinuationPage,
                                              VerifyFinalValidationContinuation.ContractorEmailConfirmationNotStored
                                            )
                                          )
                      withDraftId      <- Future.fromTry(withContinuation.set(FinalValidationDraftIdPage, draftId))
                      withMode         <- Future.fromTry(withDraftId.set(VerifyFinalValidationModePage, mode.toString))
                      _                <- sessionRepository.set(withMode)
                    } yield Redirect(
                      controllers.finalvalidations.routes.ReviewSubcontractorDetailsController.onPageLoad()
                    )
                  } else {
                    val preparedForm =
                      request.userAnswers.get(ContractorEmailConfirmationNotStoredPage(id)).fold(form)(form.fill)
                    Future.successful(
                      Ok(view(preparedForm, mode, notStoredSubmitAction(mode, Some(flowId))))
                    )
                  }
              } yield result
          }
        }
    }

  def onSubmitWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)).async {
      implicit request =>
        val id = flowId.toString
        if (hasStoredEmail(request.userAnswers, Some(id))) {
          Future.successful(redirectToStored(mode, Some(flowId)))
        } else {
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(view(formWithErrors, mode, notStoredSubmitAction(mode, Some(flowId))))
                ),
              value =>
                for {
                  cleanedAnswers <-
                    Future.fromTry(request.userAnswers.remove(ContractorEmailConfirmationStoredPage(id)))
                  updatedAnswers <-
                    Future.fromTry(cleanedAnswers.set(ContractorEmailConfirmationNotStoredPage(id), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator.nextPage(ContractorEmailConfirmationNotStoredPage(id), mode, updatedAnswers)
                )
            )
        }
    }
}

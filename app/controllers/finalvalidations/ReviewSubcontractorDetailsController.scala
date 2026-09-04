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

package controllers.finalvalidations

import controllers.actions.*
import models.{CheckMode, Mode, NormalMode}
import models.finalvalidation.VerifyFinalValidationSource.*
import models.finalvalidation.*
import navigation.Navigator
import pages.finalvalidation.*
import pages.verify.{SelectSubcontractorPage, SelectSubcontractorsToReverifyPage}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.finalvalidation.FinalValidationDraftService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.finalvalidations.ReviewSubcontractorDetailsView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReviewSubcontractorDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  requireCisId: CisIdRequiredAction,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  finalValidationDraftService: FinalValidationDraftService,
  val controllerComponents: MessagesControllerComponents,
  view: ReviewSubcontractorDetailsView
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireCisId).async { implicit request =>

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      (
        request.userAnswers.get(FinalValidationDraftIdPage),
        request.userAnswers.get(VerifyFinalValidationSourcePage),
        request.userAnswers.get(VerifyFinalValidationModePage).flatMap(modeFromString)
      ) match {

        case (Some(draftId), Some(source), Some(mode)) =>
          finalValidationDraftService.get(request.cisId, draftId).map { draft =>

            val rows =
              draft.subcontractors.map { subcontractor =>
                ReviewSubcontractorDetailsRow(
                  subcontractorId = subcontractor.subcontractorId,
                  name = subcontractor.displayName,
                  hasErrors = subcontractor.readiness == FinalValidationReadiness.Incomplete
                )
              }

            Ok(
              view(
                ReviewSubcontractorDetailsPageModel(
                  subcontractors = rows,
                  canContinue = draft.allComplete,
                  backUrl = backUrl(source, mode)
                )
              )
            )
          }

        case _ =>
          Future.successful(
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          )
      }
    }

  def onSubmit: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireCisId).async { implicit request =>

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      (
        request.userAnswers.get(FinalValidationDraftIdPage),
        request.userAnswers.get(VerifyFinalValidationSourcePage),
        request.userAnswers.get(VerifyFinalValidationModePage).flatMap(modeFromString)
      ) match {

        case (Some(draftId), Some(source), Some(mode)) =>
          source match {

            case SelectSubcontractor | SelectSubcontractorsToReverify =>
              finalValidationDraftService.get(request.cisId, draftId).flatMap { draft =>
                if (!draft.allComplete) {
                  Future.successful(
                    Redirect(
                      controllers.finalvalidations.routes.ReviewSubcontractorDetailsController.onPageLoad()
                    )
                  )
                } else {
                  finalValidationDraftService.commit(request.cisId, draftId).flatMap { _ =>

                    val cleanedAnswers =
                      for {
                        withoutDraftId      <- request.userAnswers.remove(FinalValidationDraftIdPage)
                        withoutSource       <- withoutDraftId.remove(VerifyFinalValidationSourcePage)
                        withoutMode         <- withoutSource.remove(VerifyFinalValidationModePage)
                        withoutContext      <- withoutMode.remove(FinalValidationContextPage)
                        withoutPayload      <- withoutContext.remove(VerifyFinalValidationPayloadPage)
                        withoutChangeTarget <- withoutPayload.remove(FinalValidationChangeTargetPage)
                      } yield withoutChangeTarget

                    Future.fromTry(cleanedAnswers).flatMap { answers =>
                      sessionRepository.set(answers).map { _ =>
                        source match {
                          case SelectSubcontractor =>
                            Redirect(
                              navigator.nextPage(
                                SelectSubcontractorPage,
                                mode,
                                answers
                              )
                            )

                          case SelectSubcontractorsToReverify =>
                            Redirect(
                              navigator.nextPage(
                                SelectSubcontractorsToReverifyPage,
                                mode,
                                answers
                              )
                            )

                          case _ =>
                            Redirect(
                              controllers.routes.JourneyRecoveryController.onPageLoad()
                            )
                        }
                      }
                    }
                  }
                }
              }

            case ReviewUnmatchedSubcontractors | ReviewInsufficientInfoSubcontractors =>
              Future.successful(
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              )
          }

        case _ =>
          Future.successful(
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          )
      }
    }

  private def backUrl(
    source: VerifyFinalValidationSource,
    mode: Mode
  ): String =
    source match {
      case SelectSubcontractor =>
        controllers.verify.routes.SelectSubcontractorController.onPageLoad(mode).url

      case SelectSubcontractorsToReverify =>
        controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(mode).url

      case ReviewUnmatchedSubcontractors =>
        controllers.routes.JourneyRecoveryController.onPageLoad().url

      case ReviewInsufficientInfoSubcontractors =>
        controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController.onPageLoad().url
    }

  private def modeFromString(value: String): Option[Mode] =
    value match {
      case "NormalMode" => Some(NormalMode)
      case "CheckMode"  => Some(CheckMode)
      case _            => None
    }

}

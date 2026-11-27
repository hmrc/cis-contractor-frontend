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
import models.UserAnswers
import models.contractordetails.ContractorDetailsValidationTarget
import pages.verify.LastSubmittedVerificationBatchResponsePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.VerifyCisIdQuery
import services.{CheckUnmatchedSubcontractorsService, ContractorDetailsFinalValidationService, VerificationService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReviewUnmatchedSubcontractorsRoutingController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  cisIdRequired: CisIdRequiredAction,
  multiTabAction: MultiTabAction,
  val controllerComponents: MessagesControllerComponents,
  verificationService: VerificationService,
  contractorDetailsFinalValidationService: ContractorDetailsFinalValidationService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen cisIdRequired).async { implicit request =>
      routeUnmatched(request.userAnswers, request.cisId, None)
    }

  def onPageLoadWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)).async {
      implicit request =>
        val id = flowId.toString
        request.userAnswers.get(VerifyCisIdQuery(id)) match {
          case Some(cisId) =>
            routeUnmatched(request.userAnswers, cisId, Some(flowId))
          case None        =>
            logger.error(
              "[ReviewUnmatchedSubcontractorsRoutingController][onPageLoadWithId] - VerifyCisIdQuery missing from userAnswers"
            )
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }

  private def routeUnmatched(
    userAnswers: UserAnswers,
    cisId: String,
    flowId: Option[UUID]
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val lastSubmittedPage =
      flowId
        .map(id => LastSubmittedVerificationBatchResponsePage(id.toString))
        .getOrElse(LastSubmittedVerificationBatchResponsePage)

    contractorDetailsFinalValidationService
      .refreshAndValidate(userAnswers, ContractorDetailsValidationTarget.ReviewUnmatchedSubcontractors)
      .flatMap { case (updatedAnswers, validation) =>
        if (!validation.allComplete) {
          Future.successful(
            Redirect(controllers.finalvalidations.routes.ContractorDetailsFinalValidationController.onPageLoad())
          )
        } else {
          updatedAnswers.get(lastSubmittedPage) match {
            case Some(response) =>
              val hasUnmatched = response.verifications.exists(CheckUnmatchedSubcontractorsService.isUnmatched)

              if (!hasUnmatched) {
                Future.successful(
                  Redirect(
                    flowId
                      .map(id => controllers.verify.routes.VerificationResultsController.onPageLoadWithId(id))
                      .getOrElse(controllers.verify.routes.VerificationResultsController.onPageLoad())
                  )
                )
              } else {
                verificationService
                  .anyUnmatchedResourceRefsStillPresent(cisId, response)
                  .recoverWith { case t =>
                    logger.error(
                      "[ReviewUnmatchedSubcontractorsRoutingController.onPageLoad] " +
                        "Failed to check whether unmatched subcontractors are still present",
                      t
                    )

                    Future.failed(t)
                  }
                  .flatMap {
                    case true =>
                      val recreated =
                        flowId.fold(
                          verificationService.recreateCurrentBatchFromUnmatchedVerifications(cisId, updatedAnswers)
                        )(id =>
                          verificationService.recreateCurrentBatchFromUnmatchedVerifications(
                            cisId,
                            updatedAnswers,
                            Some(id.toString)
                          )
                        )
                      recreated
                        .map { _ =>
                          Redirect(
                            flowId
                              .map(id =>
                                controllers.verify.routes.ReviewUnmatchedSubcontractorsController.onPageLoadWithId(id)
                              )
                              .getOrElse(
                                controllers.verify.routes.ReviewUnmatchedSubcontractorsController.onPageLoad()
                              )
                          )
                        }
                        .recover { case t =>
                          logger.error(
                            "[ReviewUnmatchedSubcontractorsRoutingController.onPageLoad] " +
                              "Failed to recreate the current batch from unmatched verifications",
                            t
                          )

                          Redirect(
                            controllers.routes.SystemErrorController.onPageLoad()
                          )
                        }

                    case false =>
                      Future.successful(
                        Redirect(
                          flowId
                            .map(id => controllers.routes.NoUnmatchedSubcontractorsController.onPageLoadWithId(id))
                            .getOrElse(controllers.routes.NoUnmatchedSubcontractorsController.onPageLoad())
                        )
                      )
                  }
                  .recover { case t =>
                    logger.error(
                      "[ReviewUnmatchedSubcontractorsRoutingController.onPageLoad] " +
                        "Unexpected failure routing unmatched subcontractors",
                      t
                    )

                    Redirect(
                      controllers.routes.SystemErrorController.onPageLoad()
                    )
                  }
              }

            case _ =>
              logger.error(
                "[ReviewUnmatchedSubcontractorsRoutingController][onPageLoad] - LastSubmittedVerificationBatchResponsePage missing from userAnswers"
              )
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
        }
      }
      .recover { case t =>
        logger.error(
          "[ReviewUnmatchedSubcontractorsRoutingController.onPageLoad] Failed final contractor validation",
          t
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }
}

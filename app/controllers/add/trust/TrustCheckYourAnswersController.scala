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

package controllers.add.trust

import controllers.actions.*
import models.CheckMode
import models.add.trust.ValidatedTrust
import pages.add.CheckYourAnswersSubmittedPage
import pages.add.trust.TrustUtrPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AuditService, SubcontractorService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.add.TypeOfSubcontractorSummary
import viewmodels.checkAnswers.add.trust.*
import viewmodels.govuk.summarylist.*
import views.html.add.trust.TrustCheckYourAnswersView

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject

class TrustCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  val controllerComponents: MessagesControllerComponents,
  subcontractorService: SubcontractorService,
  sessionRepository: SessionRepository,
  auditService: AuditService,
  view: TrustCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val ua = request.userAnswers

      ValidatedTrust.build(ua) match {
        case Right(_)    =>
          val list = SummaryListViewModel(
            rows = Seq(
              TypeOfSubcontractorSummary.row(ua),
              TrustNameSummary.row(ua),
              TrustAddressYesNoSummary.row(ua),
              TrustAddressSummary.row(ua),
              AddTrustContactMethodsYesNoSummary.row(ua),
              TrustContactMethodOptionsSummary.row(ua),
              TrustEmailAddressSummary.row(ua),
              TrustPhoneNumberSummary.row(ua),
              TrustMobileNumberSummary.row(ua),
              TrustUtrYesNoSummary.row(ua),
              TrustUtrSummary.row(ua),
              TrustWorksReferenceYesNoSummary.row(ua),
              TrustWorksReferenceSummary.row(ua)
            ).flatten
          )
          Ok(view(list, routes.TrustCheckYourAnswersController.onSubmit()))
        case Left(error) =>
          logger.error(s"[TrustCheckYourAnswersController.onPageLoad] Failed to load the page: $error")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      if (request.userAnswers.get(CheckYourAnswersSubmittedPage).contains(true)) {
        logger.error("[TrustCheckYourAnswersController][onSubmit] - CheckYourAnswersSubmittedPage already true")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      } else {
        ValidatedTrust.build(request.userAnswers) match {
          case Right(_)    =>
            subcontractorService
              .createAndUpdateSubcontractor(request.userAnswers)
              .flatMap { _ =>
                auditService.addSubcontractorEvent(request.userAnswers)
                Future
                  .fromTry(request.userAnswers.set(CheckYourAnswersSubmittedPage, true))
                  .flatMap(updated => sessionRepository.set(updated).map(_ => ()))
                  .map { _ =>
                    Redirect(controllers.add.routes.SubcontractorAddedController.trustSubcontractorAdded())
                  }
              }
              .recover { case t =>
                logger.error(
                  "[TrustCheckYourAnswersController.onSubmit] Failed to create/update subcontractor",
                  t
                )
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }
          case Left(error) =>
            logger.error(s"[TrustCheckYourAnswersController.onSubmit] Validation failed: $error")
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      }
    }

  def onPageLoadWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val id = flowId.toString
      val ua = request.userAnswers
      ValidatedTrust.build(ua, id) match {
        case Right(_)    =>
          val list = SummaryListViewModel(
            rows = Seq(
              TypeOfSubcontractorSummary.row(
                ua,
                subJourneyId = Some(id),
                changeUrl = Some(
                  controllers.add.routes.TypeOfSubcontractorController.onPageLoadWithId(flowId, CheckMode).url
                )
              ),
              TrustNameSummary.row(
                ua,
                changeUrl = Some(routes.TrustNameController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              TrustAddressYesNoSummary.row(
                ua,
                changeUrl = Some(routes.TrustAddressYesNoController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              TrustAddressSummary.row(
                ua,
                changeUrl = Some(
                  routes.TrustAddressController.redirectToAddressLookupWithId(flowId, Some("change")).url
                ),
                subJourneyId = Some(id)
              ),
              AddTrustContactMethodsYesNoSummary.row(
                ua,
                changeUrl = Some(
                  routes.AddTrustContactMethodsYesNoController.onPageLoadWithId(flowId, CheckMode).url
                ),
                subJourneyId = Some(id)
              ),
              TrustContactMethodOptionsSummary.row(
                ua,
                changeUrl = Some(routes.TrustContactMethodOptionsController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              TrustEmailAddressSummary.row(
                ua,
                changeUrl = Some(routes.TrustEmailAddressController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              TrustPhoneNumberSummary.row(
                ua,
                changeUrl = Some(routes.TrustPhoneNumberController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              TrustMobileNumberSummary.row(
                ua,
                changeUrl = Some(routes.TrustMobileNumberController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              TrustUtrYesNoSummary.row(
                ua,
                changeUrl = Some(routes.TrustUtrYesNoController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              TrustUtrSummary.row(
                ua,
                changeUrl = Some(routes.TrustUtrController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              TrustWorksReferenceYesNoSummary.row(
                ua,
                changeUrl = Some(routes.TrustWorksReferenceYesNoController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              TrustWorksReferenceSummary.row(
                ua,
                changeUrl = Some(routes.TrustWorksReferenceController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              )
            ).flatten
          )

          Ok(view(list, routes.TrustCheckYourAnswersController.onSubmitWithId(flowId)))
        case Left(error) =>
          logger.error(s"[TrustCheckYourAnswersController.onPageLoadWithId] Failed to load the page: $error")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmitWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val id = flowId.toString
      if (request.userAnswers.get(CheckYourAnswersSubmittedPage(id)).contains(true)) {
        logger.error(
          "[TrustCheckYourAnswersController][onSubmitWithId] - CheckYourAnswersSubmittedPage already true"
        )
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      } else {
        ValidatedTrust.build(request.userAnswers, id) match {
          case Right(_) =>
            val duplicateUtr =
              request.userAnswers.get(TrustUtrPage(id)) match {
                case Some(utr) => subcontractorService.isDuplicateUTR(request.userAnswers, utr, Some(id))
                case None      => Future.successful(false)
              }

            duplicateUtr
              .flatMap {
                case true  =>
                  logger.error("[TrustCheckYourAnswersController][onSubmitWithId] - duplicate UTR")
                  Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                case false =>
                  subcontractorService
                    .createAndUpdateSubcontractor(request.userAnswers, Some(id))
                    .flatMap { _ =>
                      auditService.addSubcontractorEvent(request.userAnswers, Some(id))
                      Future
                        .fromTry(request.userAnswers.set(CheckYourAnswersSubmittedPage(id), true))
                        .flatMap(updated => sessionRepository.set(updated).map(_ => ()))
                        .map { _ =>
                          Redirect(
                            controllers.add.routes.SubcontractorAddedController.trustSubcontractorAddedWithId(flowId)
                          )
                        }
                    }
                    .recover { case t =>
                      logger.error(
                        "[TrustCheckYourAnswersController.onSubmitWithId] Failed to create/update subcontractor",
                        t
                      )
                      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                    }
              }
              .recover { case t =>
                logger.error(
                  "[TrustCheckYourAnswersController.onSubmitWithId] Failed duplicate UTR check",
                  t
                )
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }

          case Left(error) =>
            logger.error(s"[TrustCheckYourAnswersController.onSubmitWithId] Validation failed: $error")
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      }
    }
}

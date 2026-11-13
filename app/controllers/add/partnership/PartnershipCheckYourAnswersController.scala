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

package controllers.add.partnership

import controllers.actions.*
import models.CheckMode
import models.add.partnership.ValidatedPartnership
import pages.add.CheckYourAnswersSubmittedPage
import pages.add.partnership.PartnershipUniqueTaxpayerReferencePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AuditService, SubcontractorService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.add.TypeOfSubcontractorSummary
import viewmodels.checkAnswers.add.partnership.*
import viewmodels.govuk.summarylist.*
import views.html.add.partnership.PartnershipCheckYourAnswersView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  val controllerComponents: MessagesControllerComponents,
  subcontractorService: SubcontractorService,
  sessionRepository: SessionRepository,
  auditService: AuditService,
  view: PartnershipCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val ua = request.userAnswers
    ValidatedPartnership.build(ua) match {
      case Right(_)    =>
        val list = SummaryListViewModel(
          rows = Seq(
            TypeOfSubcontractorSummary.row(ua),
            PartnershipNameSummary.row(ua),
            PartnershipAddressYesNoSummary.row(ua),
            PartnershipAddressSummary.row(ua),
            AddPartnershipContactMethodsYesNoSummary.row(ua),
            PartnershipContactMethodOptionsSummary.row(ua),
            PartnershipEmailAddressSummary.row(ua),
            PartnershipPhoneNumberSummary.row(ua),
            PartnershipMobileNumberSummary.row(ua),
            PartnershipHasUtrYesNoSummary.row(ua),
            PartnershipUniqueTaxpayerReferenceSummary.row(ua),
            PartnershipNominatedPartnerNameSummary.row(ua),
            PartnershipNominatedPartnerUtrYesNoSummary.row(ua),
            PartnershipNominatedPartnerUtrSummary.row(ua),
            PartnershipNominatedPartnerNinoYesNoSummary.row(ua),
            PartnershipNominatedPartnerNinoSummary.row(ua),
            PartnershipNominatedPartnerCrnYesNoSummary.row(ua),
            PartnershipNominatedPartnerCrnSummary.row(ua),
            PartnershipWorksReferenceNumberYesNoSummary.row(ua),
            PartnershipWorksReferenceNumberSummary.row(ua)
          ).flatten
        )

        Ok(view(list, routes.PartnershipCheckYourAnswersController.onSubmit()))
      case Left(error) =>
        logger.error(s"[PartnershipCheckYourAnswersController.onPageLoad] Failed to load the page: $error")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      if (request.userAnswers.get(CheckYourAnswersSubmittedPage).contains(true)) {
        logger.error("[PartnershipCheckYourAnswersController][onSubmit] - CheckYourAnswersSubmittedPage already true")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      } else {

        ValidatedPartnership.build(request.userAnswers) match {

          case Right(_) =>
            subcontractorService
              .createAndUpdateSubcontractor(request.userAnswers)
              .flatMap { _ =>
                auditService.addSubcontractorEvent(request.userAnswers)
                Future
                  .fromTry(request.userAnswers.set(CheckYourAnswersSubmittedPage, true))
                  .flatMap(updated => sessionRepository.set(updated).map(_ => ()))
                  .map { _ =>
                    Redirect(controllers.add.routes.SubcontractorAddedController.partnershipSubcontractorAdded())
                  }
              }
              .recover { case t =>
                logger.error(
                  "[PartnershipCheckYourAnswersController.onSubmit] Failed to create/update subcontractor",
                  t
                )
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }

          case Left(error) =>
            logger.error(s"[PartnershipCheckYourAnswersController.onSubmit] Validation failed: $error")
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      }
    }

  def onPageLoadWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val id = flowId.toString
      val ua = request.userAnswers
      ValidatedPartnership.build(ua, id) match {
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
              PartnershipNameSummary.row(
                ua,
                changeUrl = Some(routes.PartnershipNameController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              PartnershipAddressYesNoSummary.row(
                ua,
                changeUrl = Some(routes.PartnershipAddressYesNoController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              PartnershipAddressSummary.row(
                ua,
                changeUrl = Some(
                  routes.PartnershipAddressController
                    .redirectToAddressLookupWithId(flowId, Some("change"))
                    .url
                ),
                subJourneyId = Some(id)
              ),
              AddPartnershipContactMethodsYesNoSummary.row(
                ua,
                changeUrl = Some(
                  routes.AddPartnershipContactMethodsYesNoController.onPageLoadWithId(flowId, CheckMode).url
                ),
                subJourneyId = Some(id)
              ),
              PartnershipContactMethodOptionsSummary.row(
                ua,
                changeUrl =
                  Some(routes.PartnershipContactMethodOptionsController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              PartnershipEmailAddressSummary.row(
                ua,
                changeUrl = Some(routes.PartnershipEmailAddressController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              PartnershipPhoneNumberSummary.row(
                ua,
                changeUrl = Some(routes.PartnershipPhoneNumberController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              PartnershipMobileNumberSummary.row(
                ua,
                changeUrl = Some(routes.PartnershipMobileNumberController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              PartnershipHasUtrYesNoSummary.row(
                ua,
                changeUrl = Some(routes.PartnershipHasUtrYesNoController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              PartnershipUniqueTaxpayerReferenceSummary.row(
                ua,
                changeUrl = Some(
                  routes.PartnershipUniqueTaxpayerReferenceController.onPageLoadWithId(flowId, CheckMode).url
                ),
                subJourneyId = Some(id)
              ),
              PartnershipNominatedPartnerNameSummary.row(
                ua,
                changeUrl =
                  Some(routes.PartnershipNominatedPartnerNameController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              PartnershipNominatedPartnerUtrYesNoSummary.row(
                ua,
                changeUrl = Some(
                  routes.PartnershipNominatedPartnerUtrYesNoController.onPageLoadWithId(flowId, CheckMode).url
                ),
                subJourneyId = Some(id)
              ),
              PartnershipNominatedPartnerUtrSummary.row(
                ua,
                changeUrl =
                  Some(routes.PartnershipNominatedPartnerUtrController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              PartnershipNominatedPartnerNinoYesNoSummary.row(
                ua,
                changeUrl = Some(
                  routes.PartnershipNominatedPartnerNinoYesNoController.onPageLoadWithId(flowId, CheckMode).url
                ),
                subJourneyId = Some(id)
              ),
              PartnershipNominatedPartnerNinoSummary.row(
                ua,
                changeUrl =
                  Some(routes.PartnershipNominatedPartnerNinoController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              PartnershipNominatedPartnerCrnYesNoSummary.row(
                ua,
                changeUrl = Some(
                  routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoadWithId(flowId, CheckMode).url
                ),
                subJourneyId = Some(id)
              ),
              PartnershipNominatedPartnerCrnSummary.row(
                ua,
                changeUrl =
                  Some(routes.PartnershipNominatedPartnerCrnController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              PartnershipWorksReferenceNumberYesNoSummary.row(
                ua,
                changeUrl = Some(
                  routes.PartnershipWorksReferenceNumberYesNoController.onPageLoadWithId(flowId, CheckMode).url
                ),
                subJourneyId = Some(id)
              ),
              PartnershipWorksReferenceNumberSummary.row(
                ua,
                changeUrl = Some(
                  routes.PartnershipWorksReferenceNumberController.onPageLoadWithId(flowId, CheckMode).url
                ),
                subJourneyId = Some(id)
              )
            ).flatten
          )

          Ok(view(list, routes.PartnershipCheckYourAnswersController.onSubmitWithId(flowId)))
        case Left(error) =>
          logger.error(s"[PartnershipCheckYourAnswersController.onPageLoadWithId] Failed to load the page: $error")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmitWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val id = flowId.toString
      if (request.userAnswers.get(CheckYourAnswersSubmittedPage(id)).contains(true)) {
        logger.error(
          "[PartnershipCheckYourAnswersController][onSubmitWithId] - CheckYourAnswersSubmittedPage already true"
        )
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      } else {
        ValidatedPartnership.build(request.userAnswers, id) match {
          case Right(_) =>
            val duplicateUtr =
              request.userAnswers.get(PartnershipUniqueTaxpayerReferencePage(id)) match {
                case Some(utr) => subcontractorService.isDuplicateUTR(request.userAnswers, utr, Some(id))
                case None      => Future.successful(false)
              }

            duplicateUtr
              .flatMap {
                case true  =>
                  logger.error("[PartnershipCheckYourAnswersController][onSubmitWithId] - duplicate UTR")
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
                            controllers.add.routes.SubcontractorAddedController
                              .partnershipSubcontractorAddedWithId(flowId)
                          )
                        }
                    }
                    .recover { case t =>
                      logger.error(
                        "[PartnershipCheckYourAnswersController.onSubmitWithId] Failed to create/update subcontractor",
                        t
                      )
                      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                    }
              }
              .recover { case t =>
                logger.error(
                  "[PartnershipCheckYourAnswersController.onSubmitWithId] Failed duplicate UTR check",
                  t
                )
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }

          case Left(error) =>
            logger.error(s"[PartnershipCheckYourAnswersController.onSubmitWithId] Validation failed: $error")
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      }
    }

}

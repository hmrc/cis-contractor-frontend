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

package controllers.add.company

import controllers.actions.*
import models.add.company.ValidatedCompany
import models.CheckMode
import pages.add.CheckYourAnswersSubmittedPage
import pages.add.company.CompanyUtrPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AuditService, SubcontractorService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.add.TypeOfSubcontractorSummary
import viewmodels.checkAnswers.add.company.*
import viewmodels.govuk.summarylist.*
import views.html.add.company.CompanyCheckYourAnswersView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  val controllerComponents: MessagesControllerComponents,
  subcontractorService: SubcontractorService,
  sessionRepository: SessionRepository,
  auditService: AuditService,
  view: CompanyCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val ua = request.userAnswers

      ValidatedCompany.build(ua) match {
        case Right(_) =>
          val list = SummaryListViewModel(
            rows = Seq(
              TypeOfSubcontractorSummary.row(ua),
              CompanyNameSummary.row(ua),
              CompanyAddressYesNoSummary.row(ua),
              CompanyAddressSummary.row(ua),
              AddCompanyContactMethodsYesNoSummary.row(ua),
              CompanyContactMethodOptionsSummary.row(ua),
              CompanyEmailAddressSummary.row(ua),
              CompanyPhoneNumberSummary.row(ua),
              CompanyMobileNumberSummary.row(ua),
              CompanyUtrYesNoSummary.row(ua),
              CompanyUtrSummary.row(ua),
              CompanyCrnYesNoSummary.row(ua),
              CompanyCrnSummary.row(ua),
              CompanyWorksReferenceYesNoSummary.row(ua),
              CompanyWorksReferenceSummary.row(ua)
            ).flatten
          )

          Ok(view(list, routes.CompanyCheckYourAnswersController.onSubmit()))

        case Left(error) =>
          logger.error(
            s"[CompanyCheckYourAnswersController.onPageLoad] Failed to load the page: $error"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      if (request.userAnswers.get(CheckYourAnswersSubmittedPage).contains(true)) {
        logger.error(
          "[CompanyCheckYourAnswersController][onSubmit] - CheckYourAnswersSubmittedPage is true, resubmission blocked"
        )
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      } else {
        ValidatedCompany.build(request.userAnswers) match {

          case Right(_) =>
            subcontractorService
              .createAndUpdateSubcontractor(request.userAnswers)
              .flatMap { _ =>
                auditService.addSubcontractorEvent(request.userAnswers)
                Future
                  .fromTry(request.userAnswers.set(CheckYourAnswersSubmittedPage, true))
                  .flatMap(updated => sessionRepository.set(updated).map(_ => ()))
                  .map { _ =>
                    Redirect(
                      controllers.add.routes.SubcontractorAddedController.companySubcontractorAdded()
                    )
                  }
              }
              .recover { case t =>
                logger.error(
                  "[CompanyCheckYourAnswersController.onSubmit] Failed to create/update company subcontractor",
                  t
                )
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }

          case Left(error) =>
            logger.error(
              s"[CompanyCheckYourAnswersController.onSubmit] Validation failed: $error"
            )
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      }
    }

  def onPageLoadWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val id = flowId.toString
      val ua = request.userAnswers

      ValidatedCompany.build(ua, id) match {
        case Right(_) =>
          val list = SummaryListViewModel(
            rows = Seq(
              TypeOfSubcontractorSummary.row(
                ua,
                subJourneyId = Some(id),
                changeUrl = Some(
                  controllers.add.routes.TypeOfSubcontractorController.onPageLoadWithId(flowId, CheckMode).url
                )
              ),
              CompanyNameSummary.row(
                ua,
                changeUrl = Some(routes.CompanyNameController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              CompanyAddressYesNoSummary.row(
                ua,
                changeUrl = Some(routes.CompanyAddressYesNoController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              CompanyAddressSummary.row(
                ua,
                changeUrl = Some(
                  routes.CompanyAddressController
                    .redirectToAddressLookupWithId(flowId, Some("change"))
                    .url
                ),
                subJourneyId = Some(id)
              ),
              AddCompanyContactMethodsYesNoSummary.row(
                ua,
                changeUrl = Some(
                  routes.AddCompanyContactMethodsYesNoController.onPageLoadWithId(flowId, CheckMode).url
                ),
                subJourneyId = Some(id)
              ),
              CompanyContactMethodOptionsSummary.row(
                ua,
                changeUrl = Some(
                  routes.CompanyContactMethodOptionsController.onPageLoadWithId(flowId, CheckMode).url
                ),
                subJourneyId = Some(id)
              ),
              CompanyEmailAddressSummary.row(
                ua,
                changeUrl = Some(routes.CompanyEmailAddressController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              CompanyPhoneNumberSummary.row(
                ua,
                changeUrl = Some(routes.CompanyPhoneNumberController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              CompanyMobileNumberSummary.row(
                ua,
                changeUrl = Some(routes.CompanyMobileNumberController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              CompanyUtrYesNoSummary.row(
                ua,
                changeUrl = Some(routes.CompanyUtrYesNoController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              CompanyUtrSummary.row(
                ua,
                changeUrl = Some(routes.CompanyUtrController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              CompanyCrnYesNoSummary.row(
                ua,
                changeUrl = Some(routes.CompanyCrnYesNoController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              CompanyCrnSummary.row(
                ua,
                changeUrl = Some(routes.CompanyCrnController.onPageLoadWithId(flowId, CheckMode).url),
                subJourneyId = Some(id)
              ),
              CompanyWorksReferenceYesNoSummary.row(
                ua,
                changeUrl = Some(
                  routes.CompanyWorksReferenceYesNoController.onPageLoadWithId(flowId, CheckMode).url
                ),
                subJourneyId = Some(id)
              ),
              CompanyWorksReferenceSummary.row(
                ua,
                changeUrl = Some(
                  routes.CompanyWorksReferenceController.onPageLoadWithId(flowId, CheckMode).url
                ),
                subJourneyId = Some(id)
              )
            ).flatten
          )

          Ok(view(list, routes.CompanyCheckYourAnswersController.onSubmitWithId(flowId)))

        case Left(error) =>
          logger.error(
            s"[CompanyCheckYourAnswersController.onPageLoadWithId] Failed to load the page: $error"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmitWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val id = flowId.toString
      if (request.userAnswers.get(CheckYourAnswersSubmittedPage(id)).contains(true)) {
        logger.error(
          "[CompanyCheckYourAnswersController][onSubmitWithId] - CheckYourAnswersSubmittedPage already true"
        )
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      } else {
        ValidatedCompany.build(request.userAnswers, id) match {
          case Right(_) =>
            val duplicateUtr =
              request.userAnswers.get(CompanyUtrPage(id)) match {
                case Some(utr) => subcontractorService.isDuplicateUTR(request.userAnswers, utr, Some(id))
                case None      => Future.successful(false)
              }

            duplicateUtr
              .flatMap {
                case true =>
                  logger.error("[CompanyCheckYourAnswersController][onSubmitWithId] - duplicate UTR")
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
                              .companySubcontractorAddedWithId(flowId)
                          )
                        }
                    }
                    .recover { case t =>
                      logger.error(
                        "[CompanyCheckYourAnswersController.onSubmitWithId] Failed to create/update subcontractor",
                        t
                      )
                      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                    }
              }
              .recover { case t =>
                logger.error(
                  "[CompanyCheckYourAnswersController.onSubmitWithId] Failed duplicate UTR check",
                  t
                )
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }

          case Left(error) =>
            logger.error(s"[CompanyCheckYourAnswersController.onSubmitWithId] Validation failed: $error")
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      }
    }
}

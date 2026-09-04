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

package controllers.amend.company

import config.FrontendAppConfig
import controllers.actions.*
import controllers.amend.AmendControllerUtils
import controllers.routes
import models.add.company.ValidatedCompany
import models.amend.AmendJourneyType
import models.requests.CisIdDataRequest
import models.{AmendMode, UserAnswers}
import pages.add.*
import pages.add.company.CompanyNamePage
import pages.amend.{AmendCheckYourAnswersSubmittedPage, AmendJourneyTypePage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.*
import queries.OriginalCompanyAnswersQuery
import repositories.SessionRepository
import services.{AuditService, SubcontractorService}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AmendmentHelper
import viewmodels.checkAnswers.add.*
import viewmodels.checkAnswers.add.company.*
import viewmodels.govuk.summarylist.*
import views.html.amend.AmendCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendCompanyCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  cisIdRequiredAction: CisIdRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  subcontractorService: SubcontractorService,
  auditService: AuditService,
  sessionRepository: SessionRepository,
  view: AmendCheckYourAnswersView,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(subbieResourceRef: Long = -1L): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val ua = request.userAnswers

      ValidatedCompany.build(ua) match {
        case Right(_) =>
          val isVerified  = AmendControllerUtils.isVerifiedForAmendJourney(ua)
          val companyName = ua.get(CompanyNamePage).getOrElse("")

          val subcontractorInformationList =
            SummaryListViewModel(rows = subcontractorInformationRows(ua, isVerified).flatten)

          val detailsList =
            SummaryListViewModel(rows = detailsRows(ua, isVerified).flatten)

          val submitUrl =
            controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onSubmit(subbieResourceRef)
          val cancelUrl = controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onCancel()

          Ok(view(subcontractorInformationList, detailsList, companyName, submitUrl, cancelUrl))

        case Left(error) =>
          logger.error(s"[AmendCompanyCheckYourAnswersController.onPageLoad] Failed to load the page: $error")
          Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
  }

  private def subcontractorInformationRows(
    ua: UserAnswers,
    isVerified: Boolean
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val verificationRows =
      if (isVerified) {

        Seq(
          CompanyUtrSummary.row(
            ua,
            AmendMode,
            showActions = false
          ),
          ua.get(OriginalCompanyAnswersQuery)
            .flatMap(_.verificationNumber)
            .filter(_.trim.nonEmpty)
            .map { verificationNumber =>
              SummaryListRowViewModel(
                key = Key(Text(messages("amendCheckYourAnswers.verificationNumber.label"))),
                value = Value(Text(verificationNumber))
              )
            }
        )
      } else {
        Nil
      }

    Seq(
      TypeOfSubcontractorSummary.row(
        ua,
        showActions = false
      )
    ) ++ verificationRows
  }

  private def detailsRows(
    ua: UserAnswers,
    isVerified: Boolean
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val nameRows =
      if (isVerified) {
        Nil
      } else {
        Seq(CompanyNameSummary.row(ua, AmendMode))
      }

    val utrRows =
      if (isVerified) {
        Nil
      } else {
        Seq(
          CompanyUtrYesNoSummary.row(ua, AmendMode),
          CompanyUtrSummary.row(ua, AmendMode)
        )
      }

    nameRows ++
      Seq(
        CompanyAddressYesNoSummary.row(ua, AmendMode),
        CompanyAddressSummary.row(ua, AmendMode),
        AddCompanyContactMethodsYesNoSummary.row(ua, AmendMode),
        CompanyContactMethodOptionsSummary.row(ua, AmendMode),
        CompanyEmailAddressSummary.row(ua, AmendMode),
        CompanyPhoneNumberSummary.row(ua, AmendMode),
        CompanyMobileNumberSummary.row(ua, AmendMode)
      ) ++
      utrRows ++
      Seq(
        CompanyCrnYesNoSummary.row(ua, AmendMode),
        CompanyCrnSummary.row(ua, AmendMode),
        CompanyWorksReferenceYesNoSummary.row(ua, AmendMode),
        CompanyWorksReferenceSummary.row(ua, AmendMode)
      )
  }

  def onSubmit(subbieResourceRef: Long = -1L): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen cisIdRequiredAction).async { implicit request =>
      ValidatedCompany.build(request.userAnswers) match {

        case Left(error) =>
          logger.error(
            s"[AmendCompanyCheckYourAnswersController.onSubmit] Validation failed: $error"
          )

          Future.successful(
            Redirect(routes.JourneyRecoveryController.onPageLoad())
          )

        case Right(_)
            if request.userAnswers
              .get(AmendCheckYourAnswersSubmittedPage)
              .contains(true) =>
          Future.successful(
            Redirect(routes.JourneyRecoveryController.onPageLoad())
          )

        case Right(_)
            if !AmendmentHelper.companyHasChanges(
              request.userAnswers
            ) =>
          handleNoChanges()

        case Right(_) =>
          submitAmendJourney(
            request.userAnswers,
            subbieResourceRef
          )
      }
    }

  private def handleNoChanges()(implicit
    request: CisIdDataRequest[AnyContent]
  ): Future[Result] = {

    val redirectCall =
      noChangesRedirect(
        request.userAnswers,
        request.cisId
      )

    sessionRepository
      .set(UserAnswers(request.userAnswers.id))
      .map(_ => Redirect(redirectCall))
      .recover { case t =>
        logger.error(
          s"[AmendCompanyCheckYourAnswersController.onSubmit] Failed to clear user answers for session ${request.userAnswers.id}",
          t
        )

        Redirect(
          routes.JourneyRecoveryController.onPageLoad()
        )
      }
  }

  private def noChangesRedirect(
    userAnswers: UserAnswers,
    cisId: String
  ): Call =
    userAnswers.get(AmendJourneyTypePage) match {

      case Some(AmendJourneyType.Standard) =>
        Call(
          "GET",
          appConfig.manageYourSubcontractorsUrl(cisId)
        )

      case Some(AmendJourneyType.InsufficientInfo) =>
        controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
          .onPageLoad()

      case Some(AmendJourneyType.UnmatchedInfo) =>
        controllers.verify.routes.ReviewUnmatchedSubcontractorsRoutingController
          .onPageLoad()

      case None =>
        logger.error(
          "[AmendCompanyCheckYourAnswersController.onSubmit] Missing AmendJourneyTypePage when handling no changes"
        )

        routes.JourneyRecoveryController.onPageLoad()
    }

  private def submitAmendJourney(
    userAnswers: UserAnswers,
    subbieResourceRef: Long
  )(implicit request: CisIdDataRequest[AnyContent]): Future[Result] =
    userAnswers
      .get(AmendJourneyTypePage)
      .fold {
        logger.error(
          "[AmendCompanyCheckYourAnswersController.onSubmit] Missing AmendJourneyTypePage"
        )

        Future.successful(
          Redirect(routes.JourneyRecoveryController.onPageLoad())
        )
      } { journeyType =>
        Future
          .fromTry(
            userAnswers.set(
              AmendCheckYourAnswersSubmittedPage,
              true
            )
          )
          .flatMap { updated =>
            sessionRepository
              .set(updated)
              .flatMap { _ =>
                subcontractorService.submitAmendSubcontractor(
                  journeyType,
                  updated,
                  submittedSubbieResourceRef(subbieResourceRef)
                )
              }
              .map { _ =>
                auditService.amendSubcontractorEvent(updated)

                confirmationRedirect(journeyType)
              }
          }
          .recover { case t =>
            logger.error(
              "[AmendCompanyCheckYourAnswersController.onSubmit] Failed to submit amend subcontractor",
              t
            )

            Redirect(
              routes.JourneyRecoveryController.onPageLoad()
            )
          }
      }

  private def confirmationRedirect(
    journeyType: AmendJourneyType
  ): Result =
    journeyType match {

      case AmendJourneyType.Standard =>
        Redirect(
          controllers.amend.company.routes.AmendCompanyConfirmationController
            .onPageLoad()
        )

      case AmendJourneyType.InsufficientInfo =>
        Redirect(
          controllers.insufficient.routes.InsufficientSubcontractorDetailsUpdatedController
            .onPageLoad()
        )

      case AmendJourneyType.UnmatchedInfo =>
        Redirect(
          controllers.unmatched.routes.UnmatchedSubcontractorDetailsUpdatedController
            .onPageLoad()
        )
    }

  private def submittedSubbieResourceRef(subbieResourceRef: Long): Option[Long] =
    Option.when(subbieResourceRef >= 0L)(subbieResourceRef)

  def onCancel(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen cisIdRequiredAction).async { implicit request =>
      sessionRepository
        .set(UserAnswers(request.userAnswers.id))
        .map { _ =>
          Redirect(
            appConfig.manageYourSubcontractorsUrl(
              request.cisId
            )
          )
        }
        .recover { case t =>
          logger.error(
            s"[AmendCompanyCheckYourAnswersController.onCancel] Failed to clear user answers for session ${request.userAnswers.id}",
            t
          )

          Redirect(
            routes.JourneyRecoveryController.onPageLoad()
          )
        }
    }
}

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
import queries.CisIdQuery
import models.add.company.ValidatedCompany
import models.amend.AmendJourneyType
import models.amend.company.AmendCompanyRemoveDetail
import models.requests.{CisIdDataRequest, DataRequest}
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
import utils.{AmendmentHelper, DefaultSubcontractorCleanupService}
import viewmodels.checkAnswers.add.*
import viewmodels.checkAnswers.add.company.*
import viewmodels.govuk.summarylist.*
import views.html.amend.AmendCheckYourAnswersView

import javax.inject.Inject
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AmendCompanyCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  multiTabAction: MultiTabAction,
  requireData: DataRequiredAction,
  cisIdRequiredAction: CisIdRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  cleanupService: DefaultSubcontractorCleanupService,
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

      ValidatedCompany.buildForAmend(ua) match {
        case Right(_) =>
          val isVerified  = AmendControllerUtils.isVerifiedForAmendJourney(ua)
          val companyName = ua.get(CompanyNamePage).getOrElse(Messages("verify.noName"))

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

  def onPageLoadWithId(flowId: UUID, subbieResourceRef: Long = -1L): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>

      val subJourneyId = flowId.toString
      val ua           = request.userAnswers

      ValidatedCompany.buildForAmend(ua) match {
        case Right(_) =>
          val isVerified  = AmendControllerUtils.isVerifiedForAmendJourney(ua, subJourneyId)
          val companyName = ua
            .get(CompanyNamePage(subJourneyId))
            .getOrElse(Messages("verify.noName")) // TODO DTR-9463: add/amend conflict

          val subcontractorInformationList =
            SummaryListViewModel(rows = subcontractorInformationRowsWithId(flowId, ua, isVerified).flatten)

          val detailsList =
            SummaryListViewModel(rows = detailsRowsWithId(flowId, ua, isVerified).flatten)

          val submitUrl =
            controllers.amend.company.routes.AmendCompanyCheckYourAnswersController
              .onSubmitWithId(flowId, subbieResourceRef)
          val cancelUrl = controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onCancelWithId(flowId)

          Ok(view(subcontractorInformationList, detailsList, companyName, submitUrl, cancelUrl))

        case Left(error) =>
          logger.error(s"[AmendCompanyCheckYourAnswersController.onPageLoadWithId] Failed to load the page: $error")
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

  private def subcontractorInformationRowsWithId(
    flowId: UUID,
    ua: UserAnswers,
    isVerified: Boolean
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {
    val subJourneyId     = Some(flowId.toString)
    val verificationRows =
      if (isVerified) {

        Seq(
          CompanyUtrSummary.row(
            ua,
            AmendMode,
            showActions = false,
            Some(controllers.add.company.routes.CompanyUtrController.onPageLoadWithId(flowId, AmendMode).url),
            subJourneyId
          ),
          ua.get(OriginalCompanyAnswersQuery) // TODO DTR-9463: Do I need to update with id?
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
        showActions = false,
        subJourneyId,
        Some(controllers.add.routes.TypeOfSubcontractorController.onPageLoadWithId(flowId, AmendMode).url)
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

  private def detailsRowsWithId(
    flowId: UUID,
    ua: UserAnswers,
    isVerified: Boolean
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val subJourneyId = flowId.toString
    val nameRows     =
      if (isVerified) {
        Nil
      } else {
        Seq(
          CompanyNameSummary.row(
            ua,
            AmendMode,
            Some(controllers.add.company.routes.CompanyNameController.onPageLoadWithId(flowId, AmendMode).url),
            Some(subJourneyId)
          )
        )
      }

    val utrRows =
      if (isVerified) {
        Nil
      } else {
        Seq(
          CompanyUtrYesNoSummary.row(
            ua,
            AmendMode,
            Some(
              controllers.amend.company.routes.AmendCompanyRemoveDetailYesNoController
                .onPageLoadWithId(flowId, AmendCompanyRemoveDetail.Utr.key)
                .url
            ), // TODO DTR-9463: Update with id
            Some(subJourneyId)
          ),
          CompanyUtrSummary.row(
            ua,
            AmendMode,
            true,
            Some(controllers.add.company.routes.CompanyUtrController.onPageLoadWithId(flowId, AmendMode).url),
            Some(subJourneyId)
          )
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
      ValidatedCompany.buildForAmend(request.userAnswers) match {

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
          logger.error(
            "[AmendCompanyCheckYourAnswersController.onSubmit] AmendCheckYourAnswersSubmittedPage already submitted"
          )

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

  def onSubmitWithId(flowId: UUID, subbieResourceRef: Long = -1L): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val subJourneyId = flowId.toString

      ValidatedCompany.buildForAmend(request.userAnswers, Some(subJourneyId)) match {

        case Left(error) =>
          logger.error(
            s"[AmendCompanyCheckYourAnswersController.onSubmitWithId] Validation failed: $error"
          )

          Future.successful(
            Redirect(routes.JourneyRecoveryController.onPageLoad())
          )

        case Right(_)
            if request.userAnswers
              .get(AmendCheckYourAnswersSubmittedPage(subJourneyId))
              .contains(true) =>
          logger.error(
            "[AmendCompanyCheckYourAnswersController.onSubmitWithId] AmendCheckYourAnswersSubmittedPage already submitted"
          )

          Future.successful(
            Redirect(routes.JourneyRecoveryController.onPageLoad())
          )

        case Right(_)
            if !AmendmentHelper.companyHasChanges(
              request.userAnswers
            ) =>
          handleNoChangesWithId(flowId)

        case Right(_) =>
          submitAmendJourneyWithId(
            flowId,
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

    Future
      .fromTry(
        cleanupService.cleanAmend(request.userAnswers)
      )
      .flatMap(sessionRepository.set)
      .map { _ =>
        Redirect(redirectCall)
      }
      .recover { case t =>
        logger.error(
          s"[AmendCompanyCheckYourAnswersController.onSubmit] " +
            s"Failed to clean amend user answers for session ${request.userAnswers.id}",
          t
        )

        Redirect(
          routes.JourneyRecoveryController.onPageLoad()
        )
      }
  }

  private def handleNoChangesWithId(flowId: UUID)(implicit
    request: DataRequest[AnyContent]
  ): Future[Result] =
    request.userAnswers.get(CisIdQuery(flowId.toString)) match {
      case Some(id: String) =>
        val redirectCall = noChangesRedirect(request.userAnswers, id)

        Future
          .fromTry(
            cleanupService.cleanAmend(request.userAnswers)
          )
          .flatMap(sessionRepository.set)
          .map { _ =>
            Redirect(redirectCall)
          }
          .recover { case t =>
            logger.error(
              s"[AmendCompanyCheckYourAnswersController.onSubmit] " +
                s"Failed to clean amend user answers for session ${request.userAnswers.id}",
              t
            )

            Redirect(
              routes.JourneyRecoveryController.onPageLoad()
            )
          }

      case _ =>
        logger.error(s"[AmendCompanyCheckYourAnswersController.handleNoChangesWithId] - No cisId found")
        Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
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

  private def noChangesRedirectWithId(
    flowId: UUID,
    userAnswers: UserAnswers,
    cisId: String
  ): Call = {
    val subJourneyId = flowId.toString

    userAnswers.get(AmendJourneyTypePage(subJourneyId)) match {

      case Some(AmendJourneyType.Standard) =>
        Call(
          "GET",
          appConfig.manageYourSubcontractorsUrl(cisId)
        )

      case Some(AmendJourneyType.InsufficientInfo) =>
        controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
          .onPageLoadWithId(flowId)

      case Some(AmendJourneyType.UnmatchedInfo) =>
        controllers.verify.routes.ReviewUnmatchedSubcontractorsRoutingController
          .onPageLoadWithId(flowId)

      case None =>
        logger.error(
          "[AmendCompanyCheckYourAnswersController.onSubmitWithId] Missing AmendJourneyTypePage when handling no changes"
        )

        routes.JourneyRecoveryController.onPageLoad()
    }
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

                Redirect(
                  controllers.amend.company.routes.AmendCompanyConfirmationController
                    .onPageLoad()
                )
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

  private def submitAmendJourneyWithId(
    flowId: UUID,
    userAnswers: UserAnswers,
    subbieResourceRef: Long
  )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val subJourneyId = flowId.toString

    userAnswers
      .get(AmendJourneyTypePage(subJourneyId))
      .fold {
        logger.error(
          "[AmendCompanyCheckYourAnswersController.onSubmitWithId] Missing AmendJourneyTypePage"
        )

        Future.successful(
          Redirect(routes.JourneyRecoveryController.onPageLoad())
        )
      } { journeyType =>
        Future
          .fromTry(
            userAnswers.set(
              AmendCheckYourAnswersSubmittedPage(subJourneyId),
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

                Redirect(
                  controllers.amend.company.routes.AmendCompanyConfirmationController
                    .onPageLoadWithId(flowId)
                )
              }
          }
          .recover { case t =>
            logger.error(
              "[AmendCompanyCheckYourAnswersController.onSubmitWithId] Failed to submit amend subcontractor",
              t
            )

            Redirect(
              routes.JourneyRecoveryController.onPageLoad()
            )
          }
      }
  }

  private def submittedSubbieResourceRef(subbieResourceRef: Long): Option[Long] =
    Option.when(subbieResourceRef >= 0L)(subbieResourceRef)

  def onCancel(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen cisIdRequiredAction).async { implicit request =>

      val redirectCall =
        noChangesRedirect(
          request.userAnswers,
          request.cisId
        )

      Future
        .fromTry(
          cleanupService.cleanAmend(request.userAnswers)
        )
        .flatMap(sessionRepository.set)
        .map { _ =>
          Redirect(redirectCall)
        }
        .recover { case t =>
          logger.error(
            s"[AmendCompanyCheckYourAnswersController.onCancel] " +
              s"Failed to clean amend user answers for session ${request.userAnswers.id}",
            t
          )

          Redirect(
            routes.JourneyRecoveryController.onPageLoad()
          )
        }
    }

  def onCancelWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      request.userAnswers.get(CisIdQuery(flowId.toString)) match {
        case Some(id: String) =>
          val redirectCall = noChangesRedirect(request.userAnswers, id)

          Future
            .fromTry(
              cleanupService.cleanAmend(request.userAnswers)
            )
            .flatMap(sessionRepository.set)
            .map { _ =>
              Redirect(redirectCall)
            }
            .recover { case t =>
              logger.error(
                s"[AmendCompanyCheckYourAnswersController.onCancel] " +
                  s"Failed to clean amend user answers for session ${request.userAnswers.id}",
                t
              )

              Redirect(
                routes.JourneyRecoveryController.onPageLoad()
              )
            }
        case _                =>
          logger.error(s"[AmendCompanyCheckYourAnswersController.onCancel] - No cisId found")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}

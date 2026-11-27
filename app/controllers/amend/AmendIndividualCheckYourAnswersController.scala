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

package controllers.amend

import config.FrontendAppConfig
import controllers.actions.*
import controllers.routes
import models.add.ValidatedSubcontractor
import models.amend.{AmendIndividualRemoveDetail, AmendJourneyType, OriginalIndividualAnswers}
import models.requests.{CisIdDataRequest, DataRequest}
import models.{AmendMode, UserAnswers}
import pages.amend.{AmendCheckYourAnswersSubmittedPage, AmendJourneyTypePage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.*
import queries.{CisIdQuery, OriginalIndividualAnswersQuery}
import repositories.SessionRepository
import services.{AuditService, SubcontractorService}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{AmendmentHelper, DefaultSubcontractorCleanupService, SubcontractorNameExtractor}
import viewmodels.checkAnswers.add.*
import viewmodels.govuk.summarylist.*
import views.html.amend.AmendCheckYourAnswersView

import javax.inject.Inject
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AmendIndividualCheckYourAnswersController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  multiTabAction: MultiTabAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  subcontractorService: SubcontractorService,
  cleanupService: DefaultSubcontractorCleanupService,
  auditService: AuditService,
  sessionRepository: SessionRepository,
  view: AmendCheckYourAnswersView,
  appConfig: FrontendAppConfig,
  subcontractorNameExtractor: SubcontractorNameExtractor,
  cisIdRequiredAction: CisIdRequiredAction
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(subbieResourceRef: Long = -1L): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val ua = request.userAnswers

      ValidatedSubcontractor.build(ua) match {
        case Right(_) =>
          val originalAnswers = ua.get(OriginalIndividualAnswersQuery)

          val isVerified = AmendControllerUtils.isVerifiedForAmendJourney(ua)

          val subcontractorInformationList =
            SummaryListViewModel(rows = subcontractorInformationRows(ua, isVerified, originalAnswers).flatten)

          val detailsList =
            SummaryListViewModel(rows = detailsRows(ua, isVerified).flatten)

          Ok(
            view(
              subcontractorInformationList,
              detailsList,
              subcontractorNameExtractor.displaySubcontractorName(ua),
              controllers.amend.routes.AmendIndividualCheckYourAnswersController.onSubmit(subbieResourceRef),
              controllers.amend.routes.AmendIndividualCheckYourAnswersController.onCancel()
            )
          )

        case Left(error) =>
          logger.error(s"[AmendIndividualCheckYourAnswersController.onPageLoad] Failed to load the page: $error")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onPageLoadWithId(flowId: UUID, subbieResourceRef: Long = -1L): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val subJourneyId = flowId.toString
      val ua           = request.userAnswers

      ValidatedSubcontractor.build(ua) match { // TODO DTR-9463: Does ValidatedSubcontractor need updated to use id?
        case Right(_) =>
          val originalAnswers = ua.get(
            OriginalIndividualAnswersQuery
          ) // TODO DTR-9463: Does OriginalIndividualAnswersQuery need updated to use id?

          val isVerified = AmendControllerUtils.isVerifiedForAmendJourney(ua, subJourneyId)

          val subcontractorInformationList =
            SummaryListViewModel(rows =
              subcontractorInformationRowsWithId(flowId, ua, isVerified, originalAnswers).flatten
            )

          val detailsList =
            SummaryListViewModel(rows = detailsRowsWithId(flowId, ua, isVerified).flatten)

          Ok(
            view(
              subcontractorInformationList,
              detailsList,
              subcontractorNameExtractor.displaySubcontractorName(
                ua
              ), // TODO DTR-9463: Does this need updated to use id?
              controllers.amend.routes.AmendIndividualCheckYourAnswersController
                .onSubmitWithId(flowId, subbieResourceRef),
              controllers.amend.routes.AmendIndividualCheckYourAnswersController.onCancelWithId(flowId)
            )
          )

        case Left(error) =>
          logger.error(s"[AmendIndividualCheckYourAnswersController.onPageLoadWithId] Failed to load the page: $error")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  private def subcontractorInformationRows(
    ua: UserAnswers,
    isVerified: Boolean,
    originalAnswers: Option[OriginalIndividualAnswers]
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val verificationRows =
      if (isVerified) {
        Seq(
          SubcontractorsUniqueTaxpayerReferenceSummary.row(
            ua,
            AmendMode,
            showActions = false
          ),
          originalAnswers
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
    isVerified: Boolean,
    originalAnswers: Option[OriginalIndividualAnswers]
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val verificationRows =
      if (isVerified) {
        Seq(
          SubcontractorsUniqueTaxpayerReferenceSummary.row(
            ua,
            AmendMode,
            showActions = false
          ),
          originalAnswers
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
        Some(flowId.toString),
        Some(controllers.add.routes.TypeOfSubcontractorController.onPageLoadWithId(flowId, AmendMode).url)
      )
    ) ++ verificationRows
  }

  private def detailsRows(
    ua: UserAnswers,
    isVerified: Boolean
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val nameRows =
      if (!isVerified) {
        Seq(
          IndividualNamesOptionsSummary.row(ua, AmendMode),
          SubcontractorNameSummary.row(ua, AmendMode),
          TradingNameOfSubcontractorSummary.row(ua, AmendMode)
        )
      } else {
        Nil
      }

    val addressRows =
      Seq(
        SubAddressYesNoSummary.row(ua, AmendMode),
        AddressOfSubcontractorSummary.row(ua, AmendMode)
      )

    val utrRows =
      if (!isVerified) {
        Seq(
          UniqueTaxpayerReferenceYesNoSummary.row(ua, AmendMode),
          SubcontractorsUniqueTaxpayerReferenceSummary.row(ua, AmendMode)
        )
      } else {
        Nil
      }

    val contactRows =
      Seq(
        AddIndividualContactMethodsYesNoSummary.row(ua, AmendMode),
        IndividualContactMethodOptionsSummary.row(ua, AmendMode),
        IndividualEmailAddressSummary.row(ua, AmendMode),
        IndividualPhoneNumberSummary.row(ua, AmendMode),
        IndividualMobileNumberSummary.row(ua, AmendMode)
      )

    val additionalRows =
      Seq(
        NationalInsuranceNumberYesNoSummary.row(ua, AmendMode),
        SubNationalInsuranceNumberSummary.row(ua, AmendMode),
        WorksReferenceNumberYesNoSummary.row(ua, AmendMode),
        WorksReferenceNumberSummary.row(ua, AmendMode)
      )

    nameRows ++ addressRows ++ contactRows ++ utrRows ++ additionalRows
  }

  private def detailsRowsWithId(
    flowId: UUID,
    ua: UserAnswers,
    isVerified: Boolean
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {
    val subJourneyId = flowId.toString
    val nameRows     =
      if (!isVerified) {
        Seq(
          IndividualNamesOptionsSummary.row(ua, AmendMode),
          SubcontractorNameSummary.row(ua, AmendMode),
          TradingNameOfSubcontractorSummary.row(ua, AmendMode)
        )
      } else {
        Nil
      }

    val addressRows =
      Seq(
        SubAddressYesNoSummary.row(ua, AmendMode),
        AddressOfSubcontractorSummary.row(ua, AmendMode)
      )

    val utrRows =
      if (!isVerified) {
        Seq(
          UniqueTaxpayerReferenceYesNoSummary.row(ua, AmendMode),
          SubcontractorsUniqueTaxpayerReferenceSummary.row(ua, AmendMode)
        )
      } else {
        Nil
      }

    val contactRows =
      Seq(
        AddIndividualContactMethodsYesNoSummary.row(ua, AmendMode),
        IndividualContactMethodOptionsSummary.row(ua, AmendMode),
        IndividualEmailAddressSummary.row(ua, AmendMode),
        IndividualPhoneNumberSummary.row(ua, AmendMode),
        IndividualMobileNumberSummary.row(ua, AmendMode)
      )

    val additionalRows =
      Seq(
        NationalInsuranceNumberYesNoSummary.row(ua, AmendMode),
        SubNationalInsuranceNumberSummary.row(ua, AmendMode),
        WorksReferenceNumberYesNoSummary.row(ua, AmendMode),
        WorksReferenceNumberSummary.row(ua, AmendMode)
      )

    nameRows ++ addressRows ++ contactRows ++ utrRows ++ additionalRows
  }

  def onSubmit(subbieResourceRef: Long = -1L): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen cisIdRequiredAction).async { implicit request =>
      ValidatedSubcontractor.build(request.userAnswers) match {

        case Left(error) =>
          logger.error(
            s"[AmendIndividualCheckYourAnswersController.onSubmit] Validation failed: $error"
          )

          Future.successful(
            Redirect(routes.JourneyRecoveryController.onPageLoad())
          )

        case Right(_)
            if request.userAnswers
              .get(AmendCheckYourAnswersSubmittedPage)
              .contains(true) =>
          logger.error(
            "[AmendIndividualCheckYourAnswersController.onSubmit] AmendCheckYourAnswersSubmittedPage already submitted"
          )

          Future.successful(
            Redirect(routes.JourneyRecoveryController.onPageLoad())
          )

        case Right(_)
            if !AmendmentHelper.individualHasChanges(
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

      ValidatedSubcontractor.build(request.userAnswers) match { // TODO DTR-9463: Does this need updated with id?

        case Left(error) =>
          logger.error(
            s"[AmendIndividualCheckYourAnswersController.onSubmitWithId] Validation failed: $error"
          )

          Future.successful(
            Redirect(routes.JourneyRecoveryController.onPageLoad())
          )

        case Right(_)
            if request.userAnswers
              .get(AmendCheckYourAnswersSubmittedPage(subJourneyId))
              .contains(true) =>
          logger.error(
            "[AmendIndividualCheckYourAnswersController.onSubmitWithId] AmendCheckYourAnswersSubmittedPage already submitted"
          )

          Future.successful(
            Redirect(routes.JourneyRecoveryController.onPageLoad())
          )

        case Right(_)
            if !AmendmentHelper.individualHasChanges(
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

  private def submitAmendJourney(
    userAnswers: UserAnswers,
    subbieResourceRef: Long
  )(implicit request: CisIdDataRequest[AnyContent]): Future[Result] =
    userAnswers
      .get(AmendJourneyTypePage)
      .fold {
        logger.error(
          "[AmendIndividualCheckYourAnswersController.onSubmit] Missing AmendJourneyTypePage"
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
                  controllers.amend.routes.AmendIndividualConfirmationController
                    .onPageLoad()
                )
              }
          }
          .recover { case t =>
            logger.error(
              "[AmendIndividualCheckYourAnswersController.onSubmit] Failed to submit amend subcontractor",
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
          "[AmendIndividualCheckYourAnswersController.onSubmit] Missing AmendJourneyTypePage"
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

                Redirect(controllers.amend.routes.AmendIndividualConfirmationController.onPageLoadWithId(flowId))
              }
          }
          .recover { case t =>
            logger.error(
              "[AmendIndividualCheckYourAnswersController.onSubmitWithId] Failed to submit amend subcontractor",
              t
            )

            Redirect(
              routes.JourneyRecoveryController.onPageLoad()
            )
          }
      }
  }

  private def handleNoChanges()(implicit
    request: CisIdDataRequest[AnyContent]
  ): Future[Result] = {

    val redirectCall =
      amendJourneyExitRedirect(
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
          s"[AmendIndividualCheckYourAnswersController.handleNoChanges] " +
            s"Failed to clean amend data for session ${request.userAnswers.id}",
          t
        )

        Redirect(
          routes.JourneyRecoveryController.onPageLoad()
        )
      }
  }

  private def handleNoChangesWithId(flowId: UUID)(implicit
    request: DataRequest[AnyContent]
  ): Future[Result] = {
    val subJourneyId     = flowId.toString
    val cisId            = request.userAnswers.get(CisIdQuery(flowId.toString))
    def recoveryRedirect = Redirect(routes.JourneyRecoveryController.onPageLoad())

    cisId match {
      case Some(cisId) =>
        val redirectCall =
          amendJourneyExitRedirectWithId(
            flowId,
            request.userAnswers,
            cisId
          )

        Future
          .fromTry(
            cleanupService.cleanAmend(request.userAnswers, subJourneyId)
          )
          .flatMap(sessionRepository.set)
          .map { _ =>
            Redirect(redirectCall)
          }
          .recover { case t =>
            logger.error(
              s"[AmendIndividualCheckYourAnswersController.handleNoChangesWithId] " +
                s"Failed to clean amend data for session ${request.userAnswers.id}",
              t
            )
            recoveryRedirect
          }

      case None =>
        logger.error(
          "[AmendIndividualCheckYourAnswersController][onCancelWithId] - cisId missing from userAnswers"
        )
        Future.successful(recoveryRedirect)
    }

  }

  private def amendJourneyExitRedirect(
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
          "[AmendIndividualCheckYourAnswersController] " +
            "Missing AmendJourneyTypePage when exiting amend journey"
        )

        routes.JourneyRecoveryController.onPageLoad()
    }

  private def amendJourneyExitRedirectWithId(
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
          "[AmendIndividualCheckYourAnswersController] " +
            "Missing AmendJourneyTypePage when exiting amend journey"
        )

        routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def submittedSubbieResourceRef(
    subbieResourceRef: Long
  ): Option[Long] =
    Option.when(subbieResourceRef >= 0L)(
      subbieResourceRef
    )

  def onCancel(): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen cisIdRequiredAction).async { implicit request =>

      val redirectCall =
        amendJourneyExitRedirect(
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
            s"[AmendIndividualCheckYourAnswersController.onCancel] " +
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
      val subJourneyId = flowId.toString
      val cisId        = request.userAnswers.get(CisIdQuery(flowId.toString))

      def recoveryRedirect = Redirect(routes.JourneyRecoveryController.onPageLoad())

      cisId match {
        case Some(cisId) =>
          val redirectCall =
            amendJourneyExitRedirectWithId(
              flowId,
              request.userAnswers,
              cisId
            )

          Future
            .fromTry(
              cleanupService.cleanAmend(request.userAnswers, subJourneyId)
            )
            .flatMap(sessionRepository.set)
            .map { _ =>
              Redirect(redirectCall)
            }
            .recover { case t =>
              logger.error(
                s"[AmendIndividualCheckYourAnswersController.onCancelWithId] " +
                  s"Failed to clean amend user answers for session ${request.userAnswers.id}",
                t
              )
              recoveryRedirect
            }

        case None =>
          logger.error(
            "[AmendIndividualCheckYourAnswersController][onCancelWithId] - cisId missing from userAnswers"
          )
          Future.successful(recoveryRedirect)
      }
    }
}

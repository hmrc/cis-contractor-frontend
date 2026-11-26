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

package controllers.amend.trust

import config.FrontendAppConfig
import controllers.actions.*
import controllers.amend.AmendControllerUtils
import controllers.helpers.SubcontractorNameDisplayHelper
import controllers.routes
import models.add.trust.ValidatedTrust
import models.amend.AmendJourneyType
import models.requests.{CisIdDataRequest, DataRequest}
import models.{AmendMode, UserAnswers}
import pages.amend.{AmendCheckYourAnswersSubmittedPage, AmendJourneyTypePage}
import queries.CisIdQuery
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.*
import queries.OriginalTrustAnswersQuery
import repositories.SessionRepository
import services.{AuditService, SubcontractorService}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{AmendmentHelper, DefaultSubcontractorCleanupService}
import viewmodels.checkAnswers.add.*
import viewmodels.checkAnswers.add.trust.*
import viewmodels.govuk.summarylist.*
import views.html.amend.AmendCheckYourAnswersView

import javax.inject.Inject
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AmendTrustCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  multiTabAction: MultiTabAction,
  requireData: DataRequiredAction,
  cisIdRequiredAction: CisIdRequiredAction,
  cleanupService: DefaultSubcontractorCleanupService,
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

      ValidatedTrust.buildForAmend(ua) match {
        case Right(_) =>
          val isVerified = AmendControllerUtils.isVerifiedForAmendJourney(ua)
          val trustName  = SubcontractorNameDisplayHelper.trustDisplayName(ua, AmendMode)

          val subcontractorInformationList =
            SummaryListViewModel(rows = subcontractorInformationRows(ua, isVerified).flatten)

          val detailsList =
            SummaryListViewModel(rows = detailsRows(ua, isVerified).flatten)

          val submitUrl =
            controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onSubmit(subbieResourceRef)
          val cancelUrl = controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onCancel()

          Ok(view(subcontractorInformationList, detailsList, trustName, submitUrl, cancelUrl))

        case Left(error) =>
          logger.error(s"[AmendTrustCheckYourAnswersController.onPageLoad] Failed to load the page: $error")
          Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onPageLoadWithId(flowId: UUID, subbieResourceRef: Long = -1L): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val subJourneyId = flowId.toString
      val ua           = request.userAnswers

      ValidatedTrust.buildForAmend(ua) match {
        case Right(_) =>
          val isVerified = AmendControllerUtils.isVerifiedForAmendJourney(ua, subJourneyId)
          val trustName  = SubcontractorNameDisplayHelper.trustDisplayName(ua, AmendMode, subJourneyId)

          val subcontractorInformationList =
            SummaryListViewModel(rows = subcontractorInformationRowsWithId(flowId, ua, isVerified).flatten)

          val detailsList =
            SummaryListViewModel(rows = detailsRowsWithId(flowId, ua, isVerified).flatten)

          val submitUrl =
            controllers.amend.trust.routes.AmendTrustCheckYourAnswersController
              .onSubmitWithId(flowId, subbieResourceRef)
          val cancelUrl = controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onCancelWithId(flowId)

          Ok(view(subcontractorInformationList, detailsList, trustName, submitUrl, cancelUrl))

        case Left(error) =>
          logger.error(s"[AmendTrustCheckYourAnswersController.onPageLoad] Failed to load the page: $error")
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
          TrustUtrSummary.row(
            ua,
            AmendMode,
            showActions = false
          ),
          ua.get(OriginalTrustAnswersQuery)
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
    val subJourneyId     = flowId.toString
    val verificationRows =
      if (isVerified) {
        Seq(
          TrustUtrSummary.row(
            ua,
            AmendMode,
            showActions = false,
            Some(controllers.add.trust.routes.TrustUtrController.onPageLoadWithId(flowId, AmendMode).url),
            Some(subJourneyId)
          ),
          ua.get(OriginalTrustAnswersQuery) // TODO DTR-9463: Do I need to update with id
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
        Seq(TrustNameSummary.row(ua, AmendMode))
      }

    val utrRows =
      if (isVerified) {
        Nil
      } else {
        Seq(
          TrustUtrYesNoSummary.row(ua, AmendMode),
          TrustUtrSummary.row(ua, AmendMode)
        )
      }

    nameRows ++
      Seq(
        TrustAddressYesNoSummary.row(ua, AmendMode),
        TrustAddressSummary.row(ua, AmendMode),
        AddTrustContactMethodsYesNoSummary.row(ua, AmendMode),
        TrustContactMethodOptionsSummary.row(ua, AmendMode),
        TrustEmailAddressSummary.row(ua, AmendMode),
        TrustPhoneNumberSummary.row(ua, AmendMode),
        TrustMobileNumberSummary.row(ua, AmendMode)
      ) ++
      utrRows ++
      Seq(
        TrustWorksReferenceYesNoSummary.row(ua, AmendMode),
        TrustWorksReferenceSummary.row(ua, AmendMode)
      )
  }

  private def detailsRowsWithId(
    flowId: UUID,
    ua: UserAnswers,
    isVerified: Boolean
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {
    val subJourneyId = flowId.toString

    val nameRows =
      if (isVerified) {
        Nil
      } else {
        Seq(
          TrustNameSummary.row(
            ua,
            AmendMode,
            Some(controllers.add.trust.routes.TrustNameController.onPageLoadWithId(flowId, AmendMode).url),
            Some(subJourneyId)
          )
        )
      }

    val utrRows =
      if (isVerified) {
        Nil
      } else {
        Seq(
          TrustUtrYesNoSummary.row(
            ua,
            AmendMode,
            Some(
              controllers.amend.trust.routes.AmendTrustRemoveDetailYesNoController.onPageLoadWithId("utr").url
            ), // TODO DTR-9463: Update with id
            Some(subJourneyId)
          ),
          TrustUtrSummary.row(
            ua,
            AmendMode,
            true,
            Some(controllers.add.trust.routes.TrustUtrController.onPageLoadWithId(flowId, AmendMode).url),
            Some(subJourneyId)
          )
        )
      }

    nameRows ++
      Seq(
        TrustAddressYesNoSummary.row(
          ua,
          AmendMode,
          Some(
            controllers.amend.trust.routes.AmendTrustRemoveDetailYesNoController
              .onPageLoadWithId(flowId, "address")
              .url // TODO DTR-9463: Update with id
          ),
          Some(subJourneyId)
        ),
        TrustAddressSummary.row(
          ua,
          AmendMode,
          Some(
            controllers.add.trust.routes.TrustAddressController.redirectToAmendAddressLookupWithId(flowId)
          ), // TODO DTR-9463: Update with id
          Some(subJourneyId)
        ),
        AddTrustContactMethodsYesNoSummary.row(
          ua,
          AmendMode,
          Some(
            controllers.amend.trust.routes.AmendTrustRemoveDetailYesNoController
              .onPageLoadWithId(flowId, "contact-details")
              .url
          ),
          Some(subJourneyId)
        ),
        TrustContactMethodOptionsSummary.row(
          ua,
          AmendMode,
          Some(
            controllers.add.trust.routes.TrustContactMethodOptionsController.onPageLoadWithId(flowId, AmendMode).url
          ),
          Some(subJourneyId)
        ),
        TrustEmailAddressSummary.row(
          ua,
          AmendMode,
          Some(controllers.add.trust.routes.TrustEmailAddressController.onPageLoadWithId(flowId, AmendMode).url),
          Some(subJourneyId)
        ),
        TrustPhoneNumberSummary.row(
          ua,
          AmendMode,
          Some(controllers.add.trust.routes.TrustPhoneNumberController.onPageLoadWithId(flowId, AmendMode).url),
          Some(subJourneyId)
        ),
        TrustMobileNumberSummary.row(
          ua,
          AmendMode,
          Some(controllers.add.trust.routes.TrustMobileNumberController.onPageLoadWithId(flowId, AmendMode).url),
          Some(subJourneyId)
        )
      ) ++
      utrRows ++
      Seq(
        TrustWorksReferenceYesNoSummary.row(
          ua,
          AmendMode,
          Some(
            controllers.amend.trust.routes.AmendTrustRemoveDetailYesNoController
              .onPageLoadWithId(flowId, "works-reference-number")
              .url
          ),
          Some(subJourneyId)
        ),
        TrustWorksReferenceSummary.row(
          ua,
          AmendMode,
          Some(controllers.add.trust.routes.TrustWorksReferenceController.onPageLoadWithId(flowId, AmendMode).url),
          Some(subJourneyId)
        )
      )
  }

  def onSubmit(subbieResourceRef: Long = -1L): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen cisIdRequiredAction).async { implicit request =>
      ValidatedTrust.buildForAmend(request.userAnswers) match {

        case Left(error) =>
          logger.error(
            s"[AmendTrustCheckYourAnswersController.onSubmit] Validation failed: $error"
          )

          Future.successful(
            Redirect(routes.JourneyRecoveryController.onPageLoad())
          )

        case Right(_)
            if request.userAnswers
              .get(AmendCheckYourAnswersSubmittedPage)
              .contains(true) =>
          logger.error(
            "[AmendTrustCheckYourAnswersController.onSubmit] AmendCheckYourAnswersSubmittedPage already submitted"
          )

          Future.successful(
            Redirect(routes.JourneyRecoveryController.onPageLoad())
          )

        case Right(_) if !AmendmentHelper.trustHasChanges(request.userAnswers) =>
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

      ValidatedTrust.buildForAmend(request.userAnswers, subJourneyId) match {

        case Left(error) =>
          logger.error(
            s"[AmendTrustCheckYourAnswersController.onSubmitWithId] Validation failed: $error"
          )

          Future.successful(
            Redirect(routes.JourneyRecoveryController.onPageLoad())
          )

        case Right(_)
            if request.userAnswers
              .get(AmendCheckYourAnswersSubmittedPage(subJourneyId))
              .contains(true) =>
          logger.error(
            "[AmendTrustCheckYourAnswersController.onSubmitWithId] AmendCheckYourAnswersSubmittedPage already submitted"
          )

          Future.successful(
            Redirect(routes.JourneyRecoveryController.onPageLoad())
          )

        case Right(_) if !AmendmentHelper.trustHasChanges(request.userAnswers) =>
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
          "[AmendTrustCheckYourAnswersController.onSubmit] Missing AmendJourneyTypePage"
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
                  controllers.amend.trust.routes.AmendTrustConfirmationController
                    .onPageLoad()
                )
              }
          }
          .recover { case t =>
            logger.error(
              "[AmendTrustCheckYourAnswersController.onSubmit] Failed to submit amend subcontractor",
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
          "[AmendTrustCheckYourAnswersController.onSubmit] Missing AmendJourneyTypePageWithId"
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
                  controllers.amend.trust.routes.AmendTrustConfirmationController // TODO DTR-9463: Update with id
                    .onPageLoadWithId(flowId)
                )
              }
          }
          .recover { case t =>
            logger.error(
              "[AmendTrustCheckYourAnswersController.onSubmit] Failed to submit amend subcontractor",
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
          s"[AmendTrustCheckYourAnswersController.onSubmit] " +
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
      case Some(id) =>
        val redirectCall =
          noChangesRedirect(
            request.userAnswers,
            id
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
              s"[AmendTrustCheckYourAnswersController.onSubmit] " +
                s"Failed to clean amend user answers for session ${request.userAnswers.id}",
              t
            )

            Redirect(
              routes.JourneyRecoveryController.onPageLoad()
            )
          }
      case _        =>
        logger.error(
          s"[AmendTrustCheckYourAnswersController.onSubmit] - No cisId found"
        )

        Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
    }

  private def noChangesRedirect(
    userAnswers: UserAnswers,
    cisId: String
  ): Call =
    userAnswers.get(AmendJourneyTypePage) match {

      case Some(AmendJourneyType.InsufficientInfo) =>
        controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
          .onPageLoad()

      case Some(AmendJourneyType.UnmatchedInfo) =>
        controllers.verify.routes.ReviewUnmatchedSubcontractorsRoutingController
          .onPageLoad()

      case Some(AmendJourneyType.Standard) =>
        Call(
          "GET",
          appConfig.manageYourSubcontractorsUrl(cisId)
        )

      case None =>
        logger.error(
          "[AmendTrustCheckYourAnswersController.onSubmit] " +
            "Missing AmendJourneyTypePage when handling no changes"
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

      case Some(AmendJourneyType.InsufficientInfo) =>
        controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
          .onPageLoadWithId(flowId)

      case Some(AmendJourneyType.UnmatchedInfo) =>
        controllers.verify.routes.ReviewUnmatchedSubcontractorsRoutingController
          .onPageLoadWithId(flowId)

      case Some(AmendJourneyType.Standard) =>
        Call(
          "GET",
          appConfig.manageYourSubcontractorsUrl(cisId)
        )

      case None =>
        logger.error(
          "[AmendTrustCheckYourAnswersController.onSubmit] " +
            "Missing AmendJourneyTypePage when handling no changes"
        )

        routes.JourneyRecoveryController.onPageLoad()
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
            s"[AmendTrustCheckYourAnswersController.onCancel] " +
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

      request.userAnswers.get(CisIdQuery(subJourneyId)) match {
        case Some(id) =>
          val redirectCall =
            noChangesRedirect(
              request.userAnswers,
              id
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
                s"[AmendTrustCheckYourAnswersController.onCancel] " +
                  s"Failed to clean amend user answers for session ${request.userAnswers.id}",
                t
              )

              Redirect(
                routes.JourneyRecoveryController.onPageLoad()
              )
            }
        case _        =>
          logger.error(
            s"[AmendTrustCheckYourAnswersController.onCancel] - no cisId"
          )
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}

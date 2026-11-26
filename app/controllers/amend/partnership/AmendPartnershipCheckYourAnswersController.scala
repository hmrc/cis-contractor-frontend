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

package controllers.amend.partnership

import config.FrontendAppConfig
import controllers.actions.*
import controllers.amend.AmendControllerUtils
import controllers.helpers.SubcontractorNameDisplayHelper
import controllers.routes
import models.amend.partnership.AmendPartnershipRemoveDetail
import models.add.partnership.ValidatedPartnership
import models.amend.AmendJourneyType
import models.requests.{CisIdDataRequest, DataRequest}
import models.{AmendMode, UserAnswers}
import pages.amend.{AmendCheckYourAnswersSubmittedPage, AmendJourneyTypePage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.*
import queries.OriginalPartnershipAnswersQuery
import queries.CisIdQuery
import repositories.SessionRepository
import services.{AuditService, SubcontractorService}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{AmendmentHelper, DefaultSubcontractorCleanupService}
import viewmodels.checkAnswers.add.*
import viewmodels.checkAnswers.add.partnership.*
import viewmodels.govuk.summarylist.*
import views.html.amend.AmendCheckYourAnswersView

import javax.inject.Inject
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AmendPartnershipCheckYourAnswersController @Inject() (
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

      ValidatedPartnership.buildForAmend(ua) match {
        case Right(_) =>
          val isVerified = AmendControllerUtils.isVerifiedForAmendJourney(ua)

          val partnershipName              = SubcontractorNameDisplayHelper.partnershipDisplayName(ua, AmendMode)
          val subcontractorInformationList =
            SummaryListViewModel(rows = subcontractorInformationRows(ua, isVerified).flatten)

          val detailsList =
            SummaryListViewModel(rows = detailsRows(ua, isVerified).flatten)

          val submitUrl =
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onSubmit(subbieResourceRef)
          val cancelUrl = controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onCancel()

          Ok(view(subcontractorInformationList, detailsList, partnershipName, submitUrl, cancelUrl))

        case Left(error) =>
          logger.error(s"[AmendPartnershipCheckYourAnswersController.onPageLoad] Failed to load the page: $error")
          Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onPageLoadWithId(flowId: UUID, subbieResourceRef: Long = -1L): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val subJourneyId = flowId.toString
      val ua           = request.userAnswers

      ValidatedPartnership.buildForAmend(ua, Some(subJourneyId)) match {
        case Right(_) =>
          val isVerified = AmendControllerUtils.isVerifiedForAmendJourney(ua, subJourneyId)

          val partnershipName              = SubcontractorNameDisplayHelper.partnershipDisplayName(ua, AmendMode, Some(subJourneyId))
          val subcontractorInformationList =
            SummaryListViewModel(rows = subcontractorInformationRows(ua, isVerified).flatten)

          val detailsList =
            SummaryListViewModel(rows = detailsRows(ua, isVerified).flatten)

          val submitUrl =
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onSubmitWithId(flowId, subbieResourceRef)
          val cancelUrl =
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onCancelWithId(flowId)

          Ok(view(subcontractorInformationList, detailsList, partnershipName, submitUrl, cancelUrl))

        case Left(error) =>
          logger.error(s"[AmendPartnershipCheckYourAnswersController.onPageLoad] Failed to load the page: $error")
          Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
    }

  private def subcontractorInformationRows(
    ua: UserAnswers,
    isVerified: Boolean
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val verificationRows =
      Option
        .when(isVerified) {

          val verificationNumberOpt =
            ua.get(OriginalPartnershipAnswersQuery)
              .flatMap(_.verificationNumber)
              .filter(_.trim.nonEmpty)

          Seq(
            PartnershipUniqueTaxpayerReferenceSummary.row(ua, AmendMode, showActions = false),
            PartnershipNominatedPartnerUtrSummary.row(ua, AmendMode, showActions = false)
          ) ++ verificationNumberOpt.map { verificationNumber =>
            Some(
              SummaryListRowViewModel(
                key = Key(Text(messages("amendCheckYourAnswers.verificationNumber.label"))),
                value = Value(Text(verificationNumber))
              )
            )
          }
        }
        .getOrElse(Nil)

    Seq(
      TypeOfSubcontractorSummary.row(ua, showActions = false)
    ) ++ verificationRows
  }

  private def subcontractorInformationRowsWithId(
    flowId: UUID,
    ua: UserAnswers,
    isVerified: Boolean
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val subJourneyId     = flowId.toString
    val verificationRows =
      Option
        .when(isVerified) {

          val verificationNumberOpt =
            ua.get(OriginalPartnershipAnswersQuery) // TODO DTR-9463: Do I need to update with id?
              .flatMap(_.verificationNumber)
              .filter(_.trim.nonEmpty)

          Seq(
            PartnershipUniqueTaxpayerReferenceSummary.row(
              ua,
              AmendMode,
              showActions = false,
              Some(
                controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController.onPageLoadWithId(flowId, AmendMode).url
              ),
              Some(subJourneyId)
            ),
            PartnershipNominatedPartnerUtrSummary.row(
              ua,
              AmendMode,
              showActions = false,
              Some(
                controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController.onPageLoadWithId(flowId, AmendMode).url
              ),
              Some(subJourneyId)
            )
          ) ++ verificationNumberOpt.map { verificationNumber =>
            Some(
              SummaryListRowViewModel(
                key = Key(Text(messages("amendCheckYourAnswers.verificationNumber.label"))),
                value = Value(Text(verificationNumber))
              )
            )
          }
        }
        .getOrElse(Nil)

    Seq(
      TypeOfSubcontractorSummary.row(ua, showActions = false)
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
        Seq(PartnershipNameSummary.row(ua, AmendMode))
      }

    val utrRows =
      if (isVerified) {
        Seq(PartnershipNominatedPartnerNameSummary.row(ua, AmendMode))
      } else {
        Seq(
          PartnershipHasUtrYesNoSummary.row(ua, AmendMode),
          PartnershipUniqueTaxpayerReferenceSummary.row(ua, AmendMode),
          PartnershipNominatedPartnerNameSummary.row(ua, AmendMode),
          PartnershipNominatedPartnerUtrYesNoSummary.row(ua, AmendMode),
          PartnershipNominatedPartnerUtrSummary.row(ua, AmendMode)
        )
      }

    nameRows ++
      Seq(
        PartnershipAddressYesNoSummary.row(ua, AmendMode),
        PartnershipAddressSummary.row(ua, AmendMode),
        AddPartnershipContactMethodsYesNoSummary.row(ua, AmendMode),
        PartnershipContactMethodOptionsSummary.row(ua, AmendMode),
        PartnershipEmailAddressSummary.row(ua, AmendMode),
        PartnershipPhoneNumberSummary.row(ua, AmendMode),
        PartnershipMobileNumberSummary.row(ua, AmendMode)
      ) ++
      utrRows ++
      Seq(
        PartnershipNominatedPartnerNinoYesNoSummary.row(ua, AmendMode),
        PartnershipNominatedPartnerNinoSummary.row(ua, AmendMode),
        PartnershipNominatedPartnerCrnYesNoSummary.row(ua, AmendMode),
        PartnershipNominatedPartnerCrnSummary.row(ua, AmendMode),
        PartnershipWorksReferenceNumberYesNoSummary.row(ua, AmendMode),
        PartnershipWorksReferenceNumberSummary.row(ua, AmendMode)
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
          PartnershipNameSummary.row(
            ua,
            AmendMode,
            Some(
              controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController
                .onPageLoadWithId(flowId, AmendMode)
                .url
            ), // TODO DTR-9463: Update with Id
            Some(subJourneyId)
          )
        )
      }

    val utrRows =
      if (isVerified) {
        Seq(
          PartnershipNominatedPartnerNameSummary.row(
            ua,
            AmendMode,
            Some(
              controllers.add.partnership.routes.PartnershipNominatedPartnerNameController.onPageLoad(AmendMode).url
            ),
            Some(subJourneyId)
          )
        )
      } else {
        Seq(
          PartnershipHasUtrYesNoSummary.row(
            ua,
            AmendMode,
            controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController // TODO DTR-9463: Update with id
              .onPageLoadWithId(flowId, AmendPartnershipRemoveDetail.Utr.key)
              .url,
            Some(subJourneyId)
          ),
          PartnershipUniqueTaxpayerReferenceSummary.row(
            ua,
            AmendMode,
            true,
            Some(
              controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController
                .onPageLoadWithId(flowId, AmendMode)
                .url
            ),
            Some(subJourneyId)
          ),
          PartnershipNominatedPartnerNameSummary.row(
            ua,
            AmendMode,
            Some(
              controllers.add.partnership.routes.PartnershipNominatedPartnerNameController
                .onPageLoadWithId(flowId, AmendMode)
                .url
            ),
            Some(subJourneyId)
          ),
          PartnershipNominatedPartnerUtrYesNoSummary.row(
            ua,
            AmendMode,
            true,
            controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController // TODO DTR-9463: Update with id
              .onPageLoadWithId(flowId, AmendPartnershipRemoveDetail.NominatedPartnerUtr.key)
              .url,
            subJourneyId
          ),
          PartnershipNominatedPartnerUtrSummary.row(
            ua,
            AmendMode,
            true,
            Some(
              controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController
                .onPageLoadWithId(flowId, AmendMode)
                .url
            ),
            Some(subJourneyId)
          )
        )
      }

    nameRows ++
      Seq(
        PartnershipAddressYesNoSummary.row(
          ua,
          AmendMode,
          controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController // TODO DTR-9463: Update with id
            .onPageLoadWithId(flowId, AmendPartnershipRemoveDetail.Address.key)
            .url,
          Some(subJourneyId)
        ),
        PartnershipAddressSummary.row(
          ua,
          AmendMode,
          controllers.add.partnership.routes.PartnershipAddressController.redirectToAmendAddressLookupWithId(flowId), // TODO DTR-9463: Update with id
          Some(subJourneyId)
        ),
        AddPartnershipContactMethodsYesNoSummary.row(
          ua,
          AmendMode,
          controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
            .onPageLoadWithId(flowId, AmendPartnershipRemoveDetail.ContactDetails.key) // TODO DTR-9463: Update with id
            .url,
          Some(subJourneyId)
        ),
        PartnershipContactMethodOptionsSummary.row(
          ua,
          AmendMode,
          Some(
            controllers.add.partnership.routes.PartnershipContactMethodOptionsController
              .onPageLoadWithId(flowId, AmendMode)
              .url
          ),
          Some(subJourneyId)
        ),
        PartnershipEmailAddressSummary.row(
          ua,
          AmendMode,
          Some(
            controllers.add.partnership.routes.PartnershipEmailAddressController.onPageLoadWithId(flowId, AmendMode).url
          ),
          Some(subJourneyId)
        ),
        PartnershipPhoneNumberSummary.row(
          ua,
          AmendMode,
          Some(
            controllers.add.partnership.routes.PartnershipPhoneNumberController.onPageLoadWithId(flowId, AmendMode).url
          ),
          Some(subJourneyId)
        ),
        PartnershipMobileNumberSummary.row(
          ua,
          AmendMode,
          Some(
            controllers.add.partnership.routes.PartnershipMobileNumberController.onPageLoadWithId(flowId, AmendMode).url
          ),
          Some(subJourneyId)
        )
      ) ++
      utrRows ++
      Seq(
        PartnershipNominatedPartnerNinoYesNoSummary.row(
          ua,
          AmendMode,
          controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
            .onPageLoadWithId(flowId, AmendPartnershipRemoveDetail.NominatedPartnerNino.key)
            .url,
          subJourneyId
        ),
        PartnershipNominatedPartnerNinoSummary.row(
          ua,
          AmendMode,
          controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController
            .onPageLoadWithId(flowId, AmendMode)
            .url,
          subJourneyId
        ),
        PartnershipNominatedPartnerCrnYesNoSummary.row(
          ua,
          AmendMode,
          controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
            .onPageLoadWithId(AmendPartnershipRemoveDetail.NominatedPartnerCompanyRegistrationNumber.key)
            .url,
          subJourneyId
        ),
        PartnershipNominatedPartnerCrnSummary.row(
          ua,
          AmendMode,
          controllers.amend.partnership.routes.PartnershipNominatedPartnerCrnController
            .onPageLoadWithId(flowId, AmendMode)
            .url,
          subJourneyId
        ),
        PartnershipWorksReferenceNumberYesNoSummary.row(
          ua,
          AmendMode,
          controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
            .onPageLoadWithId(AmendPartnershipRemoveDetail.WorksReferenceNumber.key)
            .url,
          subJourneyId
        ),
        PartnershipWorksReferenceNumberSummary.row(
          ua,
          AmendMode,
          controllers.add.partnership.routes.PartnershipWorksReferenceNumberController
            .onPageLoadWithId(flowId, AmendMode)
            .url,
          subJourneyId
        )
      )
  }

  def onSubmit(
    subbieResourceRef: Long = -1L
  ): Action[AnyContent] =
    (
      identify
        andThen getData
        andThen requireData
        andThen cisIdRequiredAction
    ).async { implicit request =>
      ValidatedPartnership.buildForAmend(request.userAnswers) match {

        case Left(error) =>
          logger.error(
            s"[AmendPartnershipCheckYourAnswersController.onSubmit] " +
              s"Validation failed: $error"
          )

          Future.successful(
            Redirect(
              routes.JourneyRecoveryController.onPageLoad()
            )
          )

        case Right(_)
            if request.userAnswers
              .get(AmendCheckYourAnswersSubmittedPage)
              .contains(true) =>
          logger.error(
            "[AmendPartnershipCheckYourAnswersController.onSubmit] AmendCheckYourAnswersSubmittedPage already submitted"
          )

          Future.successful(
            Redirect(
              routes.JourneyRecoveryController.onPageLoad()
            )
          )

        case Right(_)
            if !AmendmentHelper.partnershipHasChanges(
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

  def onSubmitWithId(
    flowId: UUID,
    subbieResourceRef: Long = -1L
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val subJourneyId = flowId.toString
      ValidatedPartnership.buildForAmend(request.userAnswers, Some(subJourneyId)) match {

        case Left(error) =>
          logger.error(
            s"[AmendPartnershipCheckYourAnswersController.onSubmitWithId] " +
              s"Validation failed: $error"
          )

          Future.successful(
            Redirect(
              routes.JourneyRecoveryController.onPageLoad()
            )
          )

        case Right(_)
            if request.userAnswers
              .get(AmendCheckYourAnswersSubmittedPage(subJourneyId))
              .contains(true) =>
          logger.error(
            "[AmendPartnershipCheckYourAnswersController.onSubmitWithId] AmendCheckYourAnswersSubmittedPage already submitted"
          )

          Future.successful(
            Redirect(
              routes.JourneyRecoveryController.onPageLoad()
            )
          )

        case Right(_)
            if !AmendmentHelper.partnershipHasChanges(
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
  )(implicit
    request: CisIdDataRequest[AnyContent]
  ): Future[Result] =
    userAnswers
      .get(AmendJourneyTypePage)
      .fold {
        logger.error(
          "[AmendPartnershipCheckYourAnswersController.onSubmit] " +
            "Missing AmendJourneyTypePage"
        )

        Future.successful(
          Redirect(
            routes.JourneyRecoveryController.onPageLoad()
          )
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
                  submittedSubbieResourceRef(
                    subbieResourceRef
                  )
                )
              }
              .map { _ =>
                auditService.amendSubcontractorEvent(updated)

                Redirect(
                  controllers.amend.partnership.routes.AmendPartnershipConfirmationController
                    .onPageLoad()
                )
              }
          }
          .recover { case throwable =>
            logger.error(
              "[AmendPartnershipCheckYourAnswersController.onSubmit] " +
                "Failed to submit amend subcontractor",
              throwable
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
  )(implicit
    request: DataRequest[AnyContent]
  ): Future[Result] = {
    val subJourneyId = flowId.toString

    userAnswers
      .get(AmendJourneyTypePage(subJourneyId))
      .fold {
        logger.error(
          "[AmendPartnershipCheckYourAnswersController.onSubmitWithId] " +
            "Missing AmendJourneyTypePage"
        )

        Future.successful(
          Redirect(
            routes.JourneyRecoveryController.onPageLoad()
          )
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
                  submittedSubbieResourceRef(
                    subbieResourceRef
                  )
                )
              }
              .map { _ =>
                auditService.amendSubcontractorEvent(updated)

                Redirect(
                  controllers.amend.partnership.routes.AmendPartnershipConfirmationController
                    .onPageLoadWithId(flowId) // TODO DTR-9463: Update with id
                )
              }
          }
          .recover { case throwable =>
            logger.error(
              "[AmendPartnershipCheckYourAnswersController.onSubmitWithId] " +
                "Failed to submit amend subcontractor",
              throwable
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
          s"[AmendPartnershipCheckYourAnswersController.onSubmit] " +
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
  ): Future[Result] = {
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
            cleanupService.cleanAmend(request.userAnswers, subJourneyId)
          )
          .flatMap(sessionRepository.set)
          .map { _ =>
            Redirect(redirectCall)
          }
          .recover { case t =>
            logger.error(
              s"[AmendPartnershipCheckYourAnswersController.onSubmitWithId] " +
                s"Failed to clean amend user answers for session ${request.userAnswers.id}",
              t
            )

            Redirect(
              routes.JourneyRecoveryController.onPageLoad()
            )
          }
      case None     =>
        logger.error(s"[AmendPartnershipCheckYourAnswersController.onSubmitWithId] - No cisId")
        Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  private def noChangesRedirect(
    userAnswers: UserAnswers,
    cisId: String
  ): Call =
    userAnswers.get(AmendJourneyTypePage) match {

      case Some(AmendJourneyType.Standard) =>
        Call(
          method = "GET",
          url = appConfig.manageYourSubcontractorsUrl(cisId)
        )

      case Some(AmendJourneyType.InsufficientInfo) =>
        controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
          .onPageLoad()

      case Some(AmendJourneyType.UnmatchedInfo) =>
        controllers.verify.routes.ReviewUnmatchedSubcontractorsRoutingController
          .onPageLoad()

      case None =>
        logger.error(
          "[AmendPartnershipCheckYourAnswersController.onSubmit] " +
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

      case Some(AmendJourneyType.Standard) =>
        Call(
          method = "GET",
          url = appConfig.manageYourSubcontractorsUrl(cisId)
        )

      case Some(AmendJourneyType.InsufficientInfo) =>
        controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
          .onPageLoadWithId(flowId)

      case Some(AmendJourneyType.UnmatchedInfo) =>
        controllers.verify.routes.ReviewUnmatchedSubcontractorsRoutingController
          .onPageLoadWithId(flowId)

      case None =>
        logger.error(
          "[AmendPartnershipCheckYourAnswersController.onSubmitWithId] " +
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
            s"[AmendPartnershipCheckYourAnswersController.onCancel] " +
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
        case Some(id) => {
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
                s"[AmendPartnershipCheckYourAnswersController.onCancel] " +
                  s"Failed to clean amend user answers for session ${request.userAnswers.id}",
                t
              )

              Redirect(
                routes.JourneyRecoveryController.onPageLoad()
              )
            }
        }
        case None => {
          logger.error(s"[AmendPartnershipCheckYourAnswersController.onCancelWithId] - No cisId")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        }
      }
    }
}

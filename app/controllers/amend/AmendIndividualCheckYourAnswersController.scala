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
import models.amend.{AmendJourneyType, OriginalIndividualAnswers}
import models.requests.CisIdDataRequest
import models.{AmendMode, UserAnswers}
import pages.add.*
import pages.amend.{AmendCheckYourAnswersSubmittedPage, AmendJourneyTypePage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.*
import queries.OriginalIndividualAnswersQuery
import repositories.SessionRepository
import services.{AuditService, SubcontractorService}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AmendmentHelper
import viewmodels.checkAnswers.add.*
import viewmodels.govuk.summarylist.*
import views.html.amend.AmendCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendIndividualCheckYourAnswersController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  subcontractorService: SubcontractorService,
  auditService: AuditService,
  sessionRepository: SessionRepository,
  view: AmendCheckYourAnswersView,
  appConfig: FrontendAppConfig,
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
              displayName(ua),
              controllers.amend.routes.AmendIndividualCheckYourAnswersController.onSubmit(subbieResourceRef),
              controllers.amend.routes.AmendIndividualCheckYourAnswersController.onCancel()
            )
          )

        case Left(error) =>
          logger.error(s"[AmendIndividualCheckYourAnswersController.onPageLoad] Failed to load the page: $error")
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

  private def detailsRows(
    ua: UserAnswers,
    isVerified: Boolean
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val nameRows =
      if (!isVerified) {
        Seq(
          SubTradingNameYesNoSummary.row(ua, AmendMode),
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

  private def displayName(ua: UserAnswers): String =
    ua.get(SubcontractorNamePage)
      .map(n => s"${n.firstName} ${n.lastName}")
      .orElse(ua.get(TradingNameOfSubcontractorPage))
      .getOrElse("")

  private def confirmationRedirect(
    amendJourneyType: AmendJourneyType
  ): Result =
    amendJourneyType match {

      case AmendJourneyType.Standard =>
        Redirect(
          controllers.amend.routes.AmendIndividualConfirmationController
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

                confirmationRedirect(journeyType)
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

  private def handleNoChanges()(implicit
    request: CisIdDataRequest[AnyContent]
  ): Future[Result] = {

    val redirectCall =
      request.userAnswers.get(AmendJourneyTypePage) match {

        case Some(AmendJourneyType.InsufficientInfo) =>
          controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
            .onPageLoad()

        case Some(AmendJourneyType.UnmatchedInfo) =>
          controllers.verify.routes.ReviewUnmatchedSubcontractorsRoutingController
            .onPageLoad()

        case Some(AmendJourneyType.Standard) =>
          Call(
            "GET",
            appConfig.manageYourSubcontractorsUrl(request.cisId)
          )

        case None =>
          logger.error(
            "[AmendIndividualCheckYourAnswersController.handleNoChanges] Missing AmendJourneyTypePage"
          )

          controllers.routes.JourneyRecoveryController.onPageLoad()
      }

    sessionRepository
      .set(UserAnswers(request.userAnswers.id))
      .map(_ => Redirect(redirectCall))
      .recover { case t =>
        logger.error(
          s"[AmendIndividualCheckYourAnswersController.onSubmit] Failed to clear user answers for session ${request.userAnswers.id}",
          t
        )

        Redirect(
          routes.JourneyRecoveryController.onPageLoad()
        )
      }
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
            s"[AmendIndividualCheckYourAnswersController.onCancel] Failed to clear user answers for session ${request.userAnswers.id}",
            t
          )

          Redirect(
            routes.JourneyRecoveryController.onPageLoad()
          )
        }
    }
}

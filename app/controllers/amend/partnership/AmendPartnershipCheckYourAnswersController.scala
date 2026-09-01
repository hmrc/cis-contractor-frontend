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
import controllers.routes
import models.add.partnership.ValidatedPartnership
import models.amend.AmendJourneyType
import models.requests.CisIdDataRequest
import models.{AmendMode, UserAnswers}
import pages.add.*
import pages.add.partnership.PartnershipNamePage
import pages.amend.{AmendCheckYourAnswersSubmittedPage, AmendJourneyTypePage, ShowVerificationDetailsPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.*
import queries.OriginalPartnershipAnswersQuery
import repositories.SessionRepository
import services.{AuditService, SubcontractorService}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AmendmentHelper
import viewmodels.checkAnswers.add.*
import viewmodels.checkAnswers.add.partnership.*
import viewmodels.govuk.summarylist.*
import views.html.amend.AmendCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendPartnershipCheckYourAnswersController @Inject() (
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

      ValidatedPartnership.build(ua) match {
        case Right(_) =>
          val isVerified      = ua.get(ShowVerificationDetailsPage)
          val partnershipName = ua.get(PartnershipNamePage).getOrElse("")

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

  private def subcontractorInformationRows(
    ua: UserAnswers,
    isVerified: Option[Boolean]
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val verificationRows =
      Option
        .when(isVerified.contains(true)) {

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

  private def detailsRows(
    ua: UserAnswers,
    isVerified: Option[Boolean]
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val nameRows =
      if (isVerified.contains(true)) {
        Nil
      } else {
        Seq(PartnershipNameSummary.row(ua, AmendMode))
      }

    val utrRows =
      if (isVerified.contains(true)) {
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

  def onSubmit(
                subbieResourceRef: Long = -1L
              ): Action[AnyContent] =
    (
      identify
        andThen getData
        andThen requireData
        andThen cisIdRequiredAction
      ).async { implicit request =>

      ValidatedPartnership.build(request.userAnswers) match {

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

                confirmationRedirect(journeyType)
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

  private def handleNoChanges()(implicit
                                request: CisIdDataRequest[AnyContent]
  ): Future[Result] = {

    val redirectCall =
      noChangesRedirect(
        userAnswers = request.userAnswers,
        cisId = request.cisId
      )

    sessionRepository
      .set(
        UserAnswers(request.userAnswers.id)
      )
      .map { _ =>
        Redirect(redirectCall)
      }
      .recover { case throwable =>
        logger.error(
          s"[AmendPartnershipCheckYourAnswersController.onSubmit] " +
            s"Failed to clear user answers for session " +
            s"${request.userAnswers.id}",
          throwable
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
          method = "GET",
          url = appConfig.manageYourSubcontractorsUrl(cisId)
        )

      case Some(AmendJourneyType.InsufficientInfo) =>
        controllers.verify.routes
          .ReviewInsufficientInfoSubcontractorsController
          .onPageLoad()

      case Some(AmendJourneyType.UnmatchedInfo) =>
        controllers.verify.routes
          .ReviewUnmatchedSubcontractorsRoutingController
          .onPageLoad()

      case None =>
        logger.error(
          "[AmendPartnershipCheckYourAnswersController.onSubmit] " +
            "Missing AmendJourneyTypePage when handling no changes"
        )

        routes.JourneyRecoveryController.onPageLoad()
    }
  
  private def confirmationRedirect(
    journeyType: AmendJourneyType
  ): Result =
    journeyType match {

      case AmendJourneyType.Standard =>
        Redirect(
          controllers.amend.partnership.routes.AmendPartnershipConfirmationController
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
    (
      identify
        andThen getData
        andThen requireData
        andThen cisIdRequiredAction
      ).async { implicit request =>

      sessionRepository
        .set(
          UserAnswers(request.userAnswers.id)
        )
        .map { _ =>
          Redirect(
            appConfig.manageYourSubcontractorsUrl(
              request.cisId
            )
          )
        }
        .recover { case throwable =>
          logger.error(
            s"[AmendPartnershipCheckYourAnswersController.onCancel] " +
              s"Failed to clear user answers for session " +
              s"${request.userAnswers.id}",
            throwable
          )

          Redirect(
            routes.JourneyRecoveryController.onPageLoad()
          )
        }
    }
}

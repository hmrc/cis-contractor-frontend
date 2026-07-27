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

import controllers.actions.*
import models.add.partnership.ValidatedPartnership
import models.{AmendMode, UserAnswers}
import pages.add.*
import pages.add.partnership.PartnershipNamePage
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.OriginalPartnershipAnswersQuery
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.add.*
import viewmodels.checkAnswers.add.partnership.*
import viewmodels.govuk.summarylist.*
import views.html.amend.AmendCheckYourAnswersView
import controllers.routes

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendPartnershipCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  subcontractorService: SubcontractorService,
  sessionRepository: SessionRepository,
  view: AmendCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val ua = request.userAnswers

    ValidatedPartnership.build(ua) match {
      case Right(_) =>
        val isVerified      = ua.get(OriginalPartnershipAnswersQuery).flatMap(_.isVerified)
        val partnershipName = ua.get(PartnershipNamePage).getOrElse("")

        val subcontractorInformationList =
          SummaryListViewModel(rows = subcontractorInformationRows(ua, isVerified).flatten)

        val detailsList =
          SummaryListViewModel(rows = detailsRows(ua, isVerified).flatten)
        val submitUrl   = controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onSubmit()
        val cancelUrl   = controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onCancel()

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
          val verificationNumber =
            ua.get(OriginalPartnershipAnswersQuery).flatMap(_.verificationNumber).getOrElse("")

          Seq(
            PartnershipUniqueTaxpayerReferenceSummary.row(ua, AmendMode, showActions = false),
            Some(
              SummaryListRowViewModel(
                key = Key(Text(messages("amendCheckYourAnswers.verificationNumber.label"))),
                value = Value(Text(verificationNumber))
              )
            )
          )
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
        Nil
      } else {
        Seq(
          PartnershipHasUtrYesNoSummary.row(ua, AmendMode),
          PartnershipUniqueTaxpayerReferenceSummary.row(ua, AmendMode)
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
        PartnershipNominatedPartnerNameSummary.row(ua, AmendMode),
        PartnershipNominatedPartnerCrnYesNoSummary.row(ua, AmendMode),
        PartnershipNominatedPartnerCrnSummary.row(ua, AmendMode),
        PartnershipNominatedPartnerNinoYesNoSummary.row(ua, AmendMode),
        PartnershipNominatedPartnerNinoSummary.row(ua, AmendMode),
        PartnershipNominatedPartnerUtrYesNoSummary.row(ua, AmendMode),
        PartnershipNominatedPartnerUtrSummary.row(ua, AmendMode),
        PartnershipWorksReferenceNumberYesNoSummary.row(ua, AmendMode),
        PartnershipWorksReferenceNumberSummary.row(ua, AmendMode)
      )
  }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      ValidatedPartnership.build(request.userAnswers) match {
        case Right(_) =>
          subcontractorService
            .createAndUpdateSubcontractor(request.userAnswers)
            .map(_ =>
              Redirect(controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onPageLoad())
            )
            .recover { case t =>
              logger.error(
                "[AmendPartnershipCheckYourAnswersController.onSubmit] Failed to update subcontractor",
                t
              )
              Redirect(routes.JourneyRecoveryController.onPageLoad())
            }

        case Left(error) =>
          logger.error(s"[AmendPartnershipCheckYourAnswersController.onSubmit] Validation failed: $error")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def onCancel(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionRepository
      .set(UserAnswers(request.userAnswers.id))
      .map(_ => Redirect(routes.IndexController.onPageLoad()))
  }
}

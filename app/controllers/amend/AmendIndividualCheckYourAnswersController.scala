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

import controllers.actions.*
import models.add.ValidatedSubcontractor
import models.contact.ContactOptions.*
import models.{AmendMode, UserAnswers}
import pages.add.*
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.SubContractorVerifiedQuery
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.add.*
import viewmodels.govuk.summarylist.*
import views.html.amend.AmendIndividualCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendIndividualCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  subcontractorService: SubcontractorService,
  sessionRepository: SessionRepository,
  view: AmendIndividualCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val ua = request.userAnswers

    ValidatedSubcontractor.build(ua) match {
      case Right(_) =>
        val isVerified = ua.get(SubContractorVerifiedQuery).contains(true)

        val subcontractorInformationList =
          SummaryListViewModel(rows = subcontractorInformationRows(ua, isVerified).flatten)

        val detailsList =
          SummaryListViewModel(rows = detailsRows(ua, isVerified).flatten)

        Ok(view(subcontractorInformationList, detailsList, displayName(ua)))

      case Left(error) =>
        logger.error(s"[AmendIndividualCheckYourAnswersController.onPageLoad] Failed to load the page: $error")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  private def subcontractorInformationRows(
    ua: UserAnswers,
    isVerified: Boolean
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] =
    Seq(TypeOfSubcontractorSummary.row(ua, showActions = false)) ++
      (if (isVerified) {
         Seq(
           SubcontractorsUniqueTaxpayerReferenceSummary.row(
             ua,
             AmendMode,
             showActions = false
           )
         )
       } else {
         Seq.empty
       })

  private def detailsRows(
    ua: UserAnswers,
    isVerified: Boolean
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val nameRows =
      if (isVerified) { Seq.empty }
      else {
        Seq(
          SubTradingNameYesNoSummary.row(ua, AmendMode),
          SubcontractorNameSummary.row(ua, AmendMode),
          TradingNameOfSubcontractorSummary.row(ua, AmendMode)
        )
      }

    val utrRows =
      if (isVerified) { Seq.empty }
      else {
        Seq(
          UniqueTaxpayerReferenceYesNoSummary.row(ua, AmendMode),
          SubcontractorsUniqueTaxpayerReferenceSummary.row(
            ua,
            AmendMode
          )
        )
      }

    nameRows ++
      Seq(
        SubAddressYesNoSummary.row(ua, AmendMode),
        AddressOfSubcontractorSummary.row(ua, AmendMode),
        AddIndividualContactMethodsYesNoSummary.row(ua, AmendMode),
        IndividualChooseContactDetailsSummary.row(ua, AmendMode),
        contactDetailsRow(ua)
      ) ++
      utrRows ++
      Seq(
        NationalInsuranceNumberYesNoSummary.row(ua, AmendMode),
        SubNationalInsuranceNumberSummary.row(ua, AmendMode),
        WorksReferenceNumberYesNoSummary.row(ua, AmendMode),
        WorksReferenceNumberSummary.row(ua, AmendMode)
      )
  }

  private def contactDetailsRow(ua: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    ua.get(IndividualChooseContactDetailsPage).flatMap {
      case Email  => IndividualEmailAddressSummary.row(ua, AmendMode)
      case Phone  => IndividualPhoneNumberSummary.row(ua, AmendMode)
      case Mobile => IndividualMobileNumberSummary.row(ua, AmendMode)
      case _      => None
    }

  private def displayName(ua: UserAnswers): String =
    ua.get(SubcontractorNamePage)
      .map(n => s"${n.firstName} ${n.lastName}")
      .orElse(ua.get(TradingNameOfSubcontractorPage))
      .getOrElse("")

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      ValidatedSubcontractor.build(request.userAnswers) match {
        case Right(_) =>
          subcontractorService
            .createAndUpdateSubcontractor(request.userAnswers)
            .map(_ => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            .recover { case t =>
              logger.error(
                "[AmendIndividualCheckYourAnswersController.onSubmit] Failed to update subcontractor",
                t
              )
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }

        case Left(error) =>
          logger.error(s"[AmendIndividualCheckYourAnswersController.onSubmit] Validation failed: $error")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def onCancel(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionRepository
      .set(UserAnswers(request.userAnswers.id))
      .map(_ => Redirect(controllers.routes.IndexController.onPageLoad()))
  }
}

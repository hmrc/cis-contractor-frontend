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
import models.UserAnswers
import models.add.ValidatedSubcontractor
import models.contact.ContactOptions.*
import pages.add.*
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.amend.*
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

  private def contactDetailsRow(ua: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    ua.get(IndividualChooseContactDetailsPage).flatMap {
      case Email  => AmendIndividualEmailAddressSummary.row(ua)
      case Phone  => AmendIndividualPhoneNumberSummary.row(ua)
      case Mobile => AmendIndividualMobileNumberSummary.row(ua)
      case _      => None
    }

  private def displayName(ua: UserAnswers): String =
    ua.get(SubcontractorNamePage)
      .map(n => s"${n.lastName}, ${n.firstName}")
      .orElse(ua.get(TradingNameOfSubcontractorPage))
      .getOrElse("")

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val ua = request.userAnswers

    ValidatedSubcontractor.build(ua) match {
      case Right(_) =>
        val list = SummaryListViewModel(
          rows = Seq(
            AmendTypeOfSubcontractorSummary.row(ua),
            AmendSubTradingNameYesNoSummary.row(ua),
            AmendSubcontractorNameSummary.row(ua),
            AmendTradingNameOfSubcontractorSummary.row(ua),
            AmendSubAddressYesNoSummary.row(ua),
            AmendAddressOfSubcontractorSummary.row(ua),
            AmendIndividualChooseContactDetailsSummary.row(ua),
            contactDetailsRow(ua),
            AmendUniqueTaxpayerReferenceYesNoSummary.row(ua),
            AmendSubcontractorsUniqueTaxpayerReferenceSummary.row(ua),
            AmendNationalInsuranceNumberYesNoSummary.row(ua),
            AmendSubNationalInsuranceNumberSummary.row(ua),
            AmendWorksReferenceNumberYesNoSummary.row(ua),
            AmendWorksReferenceNumberSummary.row(ua)
          ).flatten
        )

        Ok(view(list, displayName(ua)))

      case Left(error) =>
        logger.error(s"[AmendIndividualCheckYourAnswersController.onPageLoad] Failed to load the page: $error")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      ValidatedSubcontractor.build(request.userAnswers) match {
        case Right(_) =>
          subcontractorService
            .createAndUpdateSubcontractor(request.userAnswers)
            .map(_ => Redirect(controllers.amend.routes.IndividualAmendedController.onPageLoad()))
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

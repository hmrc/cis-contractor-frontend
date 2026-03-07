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

package controllers.add.partnership

import controllers.actions.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.add.TypeOfSubcontractorSummary
import viewmodels.checkAnswers.add.partnership.{PartnershipWorksReferenceNumberYesNoSummary, *}
import viewmodels.govuk.summarylist.*
import views.html.add.partnership.PartnershipCheckYourAnswersView

import javax.inject.Inject

class PartnershipCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: PartnershipCheckYourAnswersView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val ua   = request.userAnswers
    val list = SummaryListViewModel(
      rows = Seq(
        TypeOfSubcontractorSummary.row(ua),
        PartnershipNameSummary.row(ua),
        PartnershipAddressYesNoSummary.row(ua),
        PartnershipAddressSummary.row(ua),
        PartnershipContactDetailsYesNoSummary.row(ua),
        PartnershipChooseContactDetailsSummary.row(ua),
        PartnershipHasUtrYesNoSummary.row(ua),
        PartnershipUniqueTaxpayerReferenceSummary.row(ua),
        PartnershipNominatedPartnerNameSummary.row(ua),
        PartnershipNominatedPartnerUtrYesNoSummary.row(ua),
        PartnershipNominatedPartnerUtrSummary.row(ua),
        PartnershipNominatedPartnerNinoYesNoSummary.row(ua),
        PartnershipNominatedPartnerNinoSummary.row(ua),
        PartnershipNominatedPartnerCrnYesNoSummary.row(ua),
        PartnershipNominatedPartnerCrnSummary.row(ua),
        PartnershipWorksReferenceNumberYesNoSummary.row(ua),
        PartnershipWorksReferenceNumberSummary.row(ua),
        PartnershipEmailAddressSummary.row(ua)
      ).flatten
    )

    Ok(view(list))
  }
}

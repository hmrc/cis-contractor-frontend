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

/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.add.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.add.*
import viewmodels.govuk.summarylist.*
import views.html.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val ua                                             = request.userAnswers
    val subTradingNameYesNoRow: Option[SummaryListRow] =
      showOnlyIfNo(ua.get(SubTradingNameYesNoPage), SubTradingNameYesNoSummary.row(ua))

    val subAddressYesNoRow: Option[SummaryListRow] =
      showOnlyIfNo(ua.get(SubAddressYesNoPage), SubAddressYesNoSummary.row(ua))

    val ninoYesNoRow: Option[SummaryListRow] =
      showOnlyIfNo(ua.get(NationalInsuranceNumberYesNoPage), NationalInsuranceNumberYesNoSummary.row(ua))

    val utrYesNoRow: Option[SummaryListRow] =
      showOnlyIfNo(ua.get(UniqueTaxpayerReferenceYesNoPage), UniqueTaxpayerReferenceYesNoSummary.row(ua))

    val wrnYesNoRow: Option[SummaryListRow] =
      showOnlyIfNo(ua.get(WorksReferenceNumberYesNoPage), WorksReferenceNumberYesNoSummary.row(ua))

    val subContactDetailsYesNoRow: Option[SummaryListRow] =
      showOnlyIfNo(ua.get(SubcontractorContactDetailsYesNoPage), SubcontractorContactDetailsYesNoSummary.row(ua))

    val list = SummaryListViewModel(
      rows = Seq(
        TypeOfSubcontractorSummary.row(ua),
        SubcontractorNameSummary.row(ua),
        subTradingNameYesNoRow,
        TradingNameOfSubcontractorSummary.row(ua),
        subAddressYesNoRow,
        AddressOfSubcontractorSummary.row(ua),
        ninoYesNoRow,
        SubNationalInsuranceNumberSummary.row(ua),
        utrYesNoRow,
        SubcontractorsUniqueTaxpayerReferenceSummary.row(ua),
        wrnYesNoRow,
        WorksReferenceNumberSummary.row(ua),
        subContactDetailsYesNoRow,
        SubContactDetailsSummary.row(ua)
      ).flatten
    )

    Ok(view(list))
  }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      Future.successful(Redirect(routes.CheckYourAnswersController.onPageLoad()))
    }

  private def showOnlyIfNo(
    boolOpt: Option[Boolean],
    buildRow: => Option[SummaryListRow]
  ): Option[SummaryListRow] =
    boolOpt.filter(_ == false).flatMap(_ => buildRow)
}

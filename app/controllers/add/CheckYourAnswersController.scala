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

package controllers.add

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.add.*
import viewmodels.govuk.summarylist.*
import views.html.add.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  subcontractorService: SubcontractorService,
  view: CheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val ua = request.userAnswers

    val list = SummaryListViewModel(
      rows = Seq(
        TypeOfSubcontractorSummary.row(ua),
        SubTradingNameYesNoSummary.row(ua),
        SubcontractorNameSummary.row(ua),
        TradingNameOfSubcontractorSummary.row(ua),
        SubAddressYesNoSummary.row(ua),
        AddressOfSubcontractorSummary.row(ua),
        NationalInsuranceNumberYesNoSummary.row(ua),
        SubNationalInsuranceNumberSummary.row(ua),
        UniqueTaxpayerReferenceYesNoSummary.row(ua),
        SubcontractorsUniqueTaxpayerReferenceSummary.row(ua),
        WorksReferenceNumberYesNoSummary.row(ua),
        WorksReferenceNumberSummary.row(ua),
        SubcontractorContactDetailsYesNoSummary.row(ua),
        SubContactDetailsSummary.row(ua)
      ).flatten
    )

    Ok(view(list))
  }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      (for {
        userAnswersWithSubbieRef <- subcontractorService.ensureSubcontractorInUserAnswers(request.userAnswers)
        _                        <- sessionRepository.set(userAnswersWithSubbieRef)
        _                        <- subcontractorService.updateSubcontractor(userAnswersWithSubbieRef)
      } yield Redirect(controllers.add.routes.CheckYourAnswersController.onPageLoad())) // change to confirmation page
        .recover { case t: Throwable =>
          logger.error("[CheckYourAnswersController.onSubmit] failed", t)
          Redirect(
            controllers.routes.JourneyRecoveryController.onPageLoad()
          ) // change if you need to specific error page
        }
    }

}

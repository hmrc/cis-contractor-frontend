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

package services
import models.Mode
import pages.QuestionPage
import pages.add.*
import pages.add.company.*
import pages.add.partnership.*
import pages.add.trust.*
import play.api.mvc.Results.Redirect
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Call, Result}

import java.util.UUID

@Singleton
class YesOrNoPageGuardService @Inject() extends LoggingUtil {

  def yesOrNoPageRoute(
    continueRoute: Result,
    guardCheck: Option[Boolean],
    yesOrNoPage: QuestionPage[Boolean],
    mode: Mode
  ): Result =
    guardCheck match {
      case Some(true)  => continueRoute
      case Some(false) => Redirect(fetchRoute(yesOrNoPage, mode))
      case _           =>
        logger.error(s"[YesOrNoPageGuardService][yesOrNoPageRoute] - guard answer missing, page=$yesOrNoPage")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

    }

  private def fetchRoute(page: QuestionPage[Boolean], mode: Mode): Call = page match {
    case AddIndividualContactMethodsYesNoPage =>
      controllers.add.routes.AddIndividualContactMethodsYesNoController.onPageLoad(mode)
    case UniqueTaxpayerReferenceYesNoPage     =>
      controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(mode)
    case NationalInsuranceNumberYesNoPage     =>
      controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(mode)
    case WorksReferenceNumberYesNoPage        => controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(mode)

    case AddCompanyContactMethodsYesNoPage =>
      controllers.add.company.routes.AddCompanyContactMethodsYesNoController.onPageLoad(mode)
    case CompanyUtrYesNoPage               =>
      controllers.add.company.routes.CompanyUtrYesNoController.onPageLoad(mode)
    case CompanyCrnYesNoPage               =>
      controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(mode)
    case CompanyWorksReferenceYesNoPage    =>
      controllers.add.company.routes.CompanyWorksReferenceYesNoController.onPageLoad(mode)

    case AddPartnershipContactMethodsYesNoPage    =>
      controllers.add.partnership.routes.AddPartnershipContactMethodsYesNoController.onPageLoad(mode)
    case PartnershipWorksReferenceNumberYesNoPage =>
      controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController.onPageLoad(mode)
    case PartnershipNominatedPartnerUtrYesNoPage  =>
      controllers.add.partnership.routes.PartnershipNominatedPartnerUtrYesNoController.onPageLoad(mode)
    case PartnershipNominatedPartnerNinoYesNoPage =>
      controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController.onPageLoad(mode)
    case PartnershipNominatedPartnerCrnYesNoPage  =>
      controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(mode)
    case PartnershipHasUtrYesNoPage               =>
      controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(mode)

    case AddTrustContactMethodsYesNoPage =>
      controllers.add.trust.routes.AddTrustContactMethodsYesNoController.onPageLoad(mode)
    case TrustUtrYesNoPage               => controllers.add.trust.routes.TrustUtrYesNoController.onPageLoad(mode)
    case TrustWorksReferenceYesNoPage    =>
      controllers.add.trust.routes.TrustWorksReferenceYesNoController.onPageLoad(mode)

    // Multi-tab — Individual
    case AddIndividualContactMethodsYesNoPage(id, _) => fetchRoute(page, mode, id)
    case UniqueTaxpayerReferenceYesNoPage(id, _) => fetchRoute(page, mode, id)
    case NationalInsuranceNumberYesNoPage(id, _) => fetchRoute(page, mode, id)
    case WorksReferenceNumberYesNoPage(id, _) => fetchRoute(page, mode, id)

    // Multi-tab — Company
    case AddCompanyContactMethodsYesNoPage(id, _) => fetchRoute(page, mode, id)
    case CompanyUtrYesNoPage(id, _) => fetchRoute(page, mode, id)
    case CompanyCrnYesNoPage(id, _) => fetchRoute(page, mode, id)
    case CompanyWorksReferenceYesNoPage(id, _) => fetchRoute(page, mode, id)

    // Multi-tab — Partnership
    case AddPartnershipContactMethodsYesNoPage(id, _) => fetchRoute(page, mode, id)
    case PartnershipWorksReferenceNumberYesNoPage(id, _) => fetchRoute(page, mode, id)
    case PartnershipNominatedPartnerUtrYesNoPage(id, _) => fetchRoute(page, mode, id)
    case PartnershipNominatedPartnerNinoYesNoPage(id, _) => fetchRoute(page, mode, id)
    case PartnershipNominatedPartnerCrnYesNoPage(id, _) => fetchRoute(page, mode, id)
    case PartnershipHasUtrYesNoPage(id, _) => fetchRoute(page, mode, id)

    // Multi-tab — Trust
    case AddTrustContactMethodsYesNoPage(id, _) => fetchRoute(page, mode, id)
    case TrustUtrYesNoPage(id, _) => fetchRoute(page, mode, id)
    case TrustWorksReferenceYesNoPage(id, _) => fetchRoute(page, mode, id)
      
    case _                               =>
      logger.error(s"[YesOrNoPageGuardService][fetchRoute] - navigator fallback reached for page=$page")
      controllers.routes.JourneyRecoveryController.onPageLoad()
  }

  // Multi-tab
  private def fetchRoute(
    page: QuestionPage[Boolean],
    mode: Mode,
    id: String
  ): Call = {

    val flowId = UUID.fromString(id)

    page match {

      // Individual
      case _: AddIndividualContactMethodsYesNoPage =>
        controllers.add.routes.AddIndividualContactMethodsYesNoController.onPageLoadWithId(flowId, mode)
      case _: UniqueTaxpayerReferenceYesNoPage =>
        controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoadWithId(flowId, mode)
      case _: NationalInsuranceNumberYesNoPage =>
        controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoadWithId(flowId, mode)
      case _: WorksReferenceNumberYesNoPage =>
        controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoadWithId(flowId, mode)

      // Company
      case _: AddCompanyContactMethodsYesNoPage =>
        controllers.add.company.routes.AddCompanyContactMethodsYesNoController.onPageLoadWithId(flowId, mode)
      case _: CompanyUtrYesNoPage =>
        controllers.add.company.routes.CompanyUtrYesNoController.onPageLoadWithId(flowId, mode)
      case _: CompanyCrnYesNoPage =>
        controllers.add.company.routes.CompanyCrnYesNoController.onPageLoadWithId(flowId, mode)
      case _: CompanyWorksReferenceYesNoPage =>
        controllers.add.company.routes.CompanyWorksReferenceYesNoController.onPageLoadWithId(flowId, mode)

      // Partnership
      case _: AddPartnershipContactMethodsYesNoPage =>
        controllers.add.partnership.routes.AddPartnershipContactMethodsYesNoController.onPageLoadWithId(flowId, mode)
      case _: PartnershipWorksReferenceNumberYesNoPage =>
        controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController.onPageLoadWithId(flowId, mode)
      case _: PartnershipNominatedPartnerUtrYesNoPage =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerUtrYesNoController.onPageLoadWithId(flowId, mode)
      case _: PartnershipNominatedPartnerNinoYesNoPage =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController.onPageLoadWithId(flowId, mode)
      case _: PartnershipNominatedPartnerCrnYesNoPage =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoadWithId(flowId, mode)
      case _: PartnershipHasUtrYesNoPage =>
        controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoadWithId(flowId, mode)

      // Trust
      case _: AddTrustContactMethodsYesNoPage =>
        controllers.add.trust.routes.AddTrustContactMethodsYesNoController.onPageLoadWithId(flowId, mode)
      case _: TrustUtrYesNoPage =>
        controllers.add.trust.routes.TrustUtrYesNoController.onPageLoadWithId(flowId, mode)
      case _: TrustWorksReferenceYesNoPage =>
        controllers.add.trust.routes.TrustWorksReferenceYesNoController.onPageLoadWithId(flowId, mode)

      case _ =>
        logger.error(
          s"[YesOrNoPageGuardService][fetchRoute] - scoped navigator fallback reached for page=$page"
        )
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }
  
}

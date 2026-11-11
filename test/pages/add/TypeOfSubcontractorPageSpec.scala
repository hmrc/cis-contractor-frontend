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

package pages.add

import models.TypeOfSubcontractor
import pages.add.company.CompanyAddressYesNoPage
import pages.add.partnership.{PartnershipAddressYesNoPage, PartnershipNamePage}
import pages.add.trust.{TrustAddressYesNoPage, TrustNamePage}
import pages.behaviours.PageBehaviours
import play.api.libs.json.JsPath

class TypeOfSubcontractorPageSpec extends PageBehaviours {

  "TypeOfSubcontractorPage" - {

    beRetrievable[TypeOfSubcontractor](TypeOfSubcontractorPage)

    beSettable[TypeOfSubcontractor](TypeOfSubcontractorPage)

    beRemovable[TypeOfSubcontractor](TypeOfSubcontractorPage)

    "must nest the WithId path under addSubcontractor" in {
      TypeOfSubcontractorPage("uuid-1").path mustBe (JsPath \ "addSubcontractor" \ "uuid-1" \ "typeOfSubcontractor")
      TypeOfSubcontractorPage.path mustBe (JsPath \ "typeOfSubcontractor")
    }

    "must store the answer under the subjourney id" in {
      val answers = emptyUserAnswers
        .set(TypeOfSubcontractorPage("uuid-1"), TypeOfSubcontractor.Partnership)
        .success
        .value

      answers.get(TypeOfSubcontractorPage("uuid-1")) mustBe Some(TypeOfSubcontractor.Partnership)
      answers.get(TypeOfSubcontractorPage) mustBe None
    }

    "cleanup: must not remove root answers when Partnership is selected under the subjourney id" in {
      val userAnswers =
        emptyUserAnswers
          .set(NationalInsuranceNumberYesNoPage, true)
          .success
          .value
          .set(CompanyAddressYesNoPage, true)
          .success
          .value
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value
          .set(PartnershipAddressYesNoPage("uuid-1"), true)
          .success
          .value
          .set(TrustAddressYesNoPage, true)
          .success
          .value
          .set(TrustNamePage("uuid-1"), "Nested Trust")
          .success
          .value

      val updatedUserAnswers =
        userAnswers.set(TypeOfSubcontractorPage("uuid-1"), TypeOfSubcontractor.Partnership).success.value

      updatedUserAnswers.get(NationalInsuranceNumberYesNoPage) mustBe Some(true)
      updatedUserAnswers.get(CompanyAddressYesNoPage) mustBe Some(true)
      updatedUserAnswers.get(TrustAddressYesNoPage) mustBe Some(true)
      updatedUserAnswers.get(PartnershipAddressYesNoPage) mustBe Some(true)
      updatedUserAnswers.get(PartnershipAddressYesNoPage("uuid-1")) mustBe Some(true)
      updatedUserAnswers.get(TrustNamePage("uuid-1")) mustBe None
    }

    "cleanup: must remove nested partnership answers when another type is selected under the subjourney id" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value
          .set(PartnershipNamePage("uuid-1"), "Nested Partnership")
          .success
          .value
          .set(PartnershipAddressYesNoPage("uuid-1"), true)
          .success
          .value
          .set(TrustNamePage("uuid-1"), "Nested Trust")
          .success
          .value

      val updatedUserAnswers =
        userAnswers.set(TypeOfSubcontractorPage("uuid-1"), TypeOfSubcontractor.Limitedcompany).success.value

      updatedUserAnswers.get(PartnershipAddressYesNoPage) mustBe Some(true)
      updatedUserAnswers.get(PartnershipNamePage("uuid-1")) mustBe None
      updatedUserAnswers.get(PartnershipAddressYesNoPage("uuid-1")) mustBe None
      updatedUserAnswers.get(TrustNamePage("uuid-1")) mustBe None
    }

    "cleanup: must remove nested partnership answers and keep nested trust when Trust is selected under the subjourney id" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage("uuid-1"), "Nested Partnership")
          .success
          .value
          .set(TrustNamePage("uuid-1"), "Nested Trust")
          .success
          .value

      val updatedUserAnswers =
        userAnswers.set(TypeOfSubcontractorPage("uuid-1"), TypeOfSubcontractor.Trust).success.value

      updatedUserAnswers.get(PartnershipNamePage("uuid-1")) mustBe None
      updatedUserAnswers.get(TrustNamePage("uuid-1")) mustBe Some("Nested Trust")
    }

    "cleanup: must remove all LimitedCompany, Partnership and Trust subcontractor answers when IndividualSoleTraderSubcontractor is selected" in {
      val userAnswers =
        emptyUserAnswers
          .set(NationalInsuranceNumberYesNoPage, true)
          .success
          .value
          .set(CompanyAddressYesNoPage, true)
          .success
          .value
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value
          .set(TrustAddressYesNoPage, true)
          .success
          .value

      val updatedUserAnswers =
        userAnswers.set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader).success.value

      updatedUserAnswers.get(NationalInsuranceNumberYesNoPage) mustBe Some(true)
      updatedUserAnswers.get(CompanyAddressYesNoPage) mustBe None
      updatedUserAnswers.get(PartnershipAddressYesNoPage) mustBe None
      updatedUserAnswers.get(TrustAddressYesNoPage) mustBe None
    }

    "cleanup: must remove all IndividualSoleTrader, LimitedCompany and Trust subcontractor answers when PartnershipSubcontractor is selected" in {
      val userAnswers =
        emptyUserAnswers
          .set(NationalInsuranceNumberYesNoPage, true)
          .success
          .value
          .set(CompanyAddressYesNoPage, true)
          .success
          .value
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value
          .set(TrustAddressYesNoPage, true)
          .success
          .value

      val updatedUserAnswers =
        userAnswers.set(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership).success.value

      updatedUserAnswers.get(NationalInsuranceNumberYesNoPage) mustBe None
      updatedUserAnswers.get(CompanyAddressYesNoPage) mustBe None
      updatedUserAnswers.get(TrustAddressYesNoPage) mustBe None
      updatedUserAnswers.get(PartnershipAddressYesNoPage) mustBe Some(true)
    }

    "cleanup: must remove all IndividualSoleTrader, Partnership and Trust subcontractor answers when LimitedCompanySubcontractor is selected" in {
      val userAnswers =
        emptyUserAnswers
          .set(NationalInsuranceNumberYesNoPage, true)
          .success
          .value
          .set(CompanyAddressYesNoPage, true)
          .success
          .value
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value
          .set(TrustAddressYesNoPage, true)
          .success
          .value

      val updatedUserAnswers =
        userAnswers.set(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany).success.value

      updatedUserAnswers.get(NationalInsuranceNumberYesNoPage) mustBe None
      updatedUserAnswers.get(CompanyAddressYesNoPage) mustBe Some(true)
      updatedUserAnswers.get(TrustAddressYesNoPage) mustBe None
      updatedUserAnswers.get(PartnershipAddressYesNoPage) mustBe None
    }

    "cleanup: must remove all IndividualSoleTrader, LimitedCompany and Partnership subcontractor answers when TrustSubcontractor is selected" in {
      val userAnswers =
        emptyUserAnswers
          .set(NationalInsuranceNumberYesNoPage, true)
          .success
          .value
          .set(CompanyAddressYesNoPage, true)
          .success
          .value
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value
          .set(TrustAddressYesNoPage, true)
          .success
          .value

      val updatedUserAnswers =
        userAnswers.set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust).success.value

      updatedUserAnswers.get(NationalInsuranceNumberYesNoPage) mustBe None
      updatedUserAnswers.get(CompanyAddressYesNoPage) mustBe None
      updatedUserAnswers.get(TrustAddressYesNoPage) mustBe Some(true)
      updatedUserAnswers.get(PartnershipAddressYesNoPage) mustBe None
    }

  }
}

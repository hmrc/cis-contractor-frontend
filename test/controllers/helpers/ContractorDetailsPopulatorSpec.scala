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

package controllers.helpers

import base.SpecBase
import models.Scheme
import pages.contractordetails.*

class ContractorDetailsPopulatorSpec extends SpecBase {

  "ContractorDetailsPopulator.populate" - {

    "populate all contractor details when scheme contains name and email address" in {

      val scheme = Scheme(
        schemeId = 1,
        instanceId = "cisId",
        accountsOfficeReference = "123 PA 87654321",
        taxOfficeNumber = "123",
        taxOfficeReference = "45678",
        utr = Some("1234567890"),
        name = Some("ABC Contractors"),
        emailAddress = Some("abc@test.com")
      )

      val result =
        ContractorDetailsPopulator.populate(
          emptyUserAnswers,
          scheme
        )

      result.get(ContractorUtrPage) mustBe Some("1234567890")
      result.get(AddSchemeNameYesNoPage) mustBe Some(true)
      result.get(SchemeNamePage) mustBe Some("ABC Contractors")
      result.get(AddEmailAddressYesNoPage) mustBe Some(true)
      result.get(EnterContractorEmailAddressPage) mustBe Some("abc@test.com")
      result.get(AccountsOfficeReferencePage) mustBe Some("123 PA 87654321")
    }

    "set scheme name and email flags to false when values are missing" in {

      val scheme = Scheme(
        schemeId = 1,
        instanceId = "cisId",
        accountsOfficeReference = "123 PA 87654321",
        taxOfficeNumber = "123",
        taxOfficeReference = "45678",
        utr = Some("1234567890"),
        name = None,
        emailAddress = None
      )

      val result =
        ContractorDetailsPopulator.populate(
          emptyUserAnswers,
          scheme
        )

      result.get(ContractorUtrPage) mustBe Some("1234567890")
      result.get(AddSchemeNameYesNoPage) mustBe Some(false)
      result.get(SchemeNamePage) mustBe Some("")
      result.get(AddEmailAddressYesNoPage) mustBe Some(false)
      result.get(EnterContractorEmailAddressPage) mustBe Some("")
      result.get(AccountsOfficeReferencePage) mustBe Some("123 PA 87654321")
    }
  }
}

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

      result.isSuccess mustBe true

      val answers = result.get

      answers.get(ContractorUtrPage) mustBe Some("1234567890")
      answers.get(AddSchemeNameYesNoPage) mustBe Some(true)
      answers.get(SchemeNamePage) mustBe Some("ABC Contractors")
      answers.get(AddEmailAddressYesNoPage) mustBe Some(true)
      answers.get(EnterContractorEmailAddressPage) mustBe Some("abc@test.com")
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

      result.isSuccess mustBe true

      val answers = result.get

      answers.get(ContractorUtrPage) mustBe Some("1234567890")
      answers.get(AddSchemeNameYesNoPage) mustBe Some(false)
      answers.get(SchemeNamePage) mustBe None
      answers.get(AddEmailAddressYesNoPage) mustBe Some(false)
      answers.get(EnterContractorEmailAddressPage) mustBe None
    }

    "treat blank name and email values as missing" in {

      val scheme = Scheme(
        schemeId = 1,
        instanceId = "cisId",
        accountsOfficeReference = "123 PA 87654321",
        taxOfficeNumber = "123",
        taxOfficeReference = "45678",
        utr = Some("1234567890"),
        name = Some("   "),
        emailAddress = Some("")
      )

      val result =
        ContractorDetailsPopulator.populate(
          emptyUserAnswers,
          scheme
        )

      result.isSuccess mustBe true

      val answers = result.get

      answers.get(ContractorUtrPage) mustBe Some("1234567890")
      answers.get(AddSchemeNameYesNoPage) mustBe Some(false)
      answers.get(SchemeNamePage) mustBe None
      answers.get(AddEmailAddressYesNoPage) mustBe Some(false)
      answers.get(EnterContractorEmailAddressPage) mustBe None
    }

    "return Failure when UTR is missing" in {

      val scheme = Scheme(
        schemeId = 1,
        instanceId = "cisId",
        accountsOfficeReference = "123 PA 87654321",
        taxOfficeNumber = "123",
        taxOfficeReference = "45678",
        utr = None
      )

      val result =
        ContractorDetailsPopulator.populate(
          emptyUserAnswers,
          scheme
        )

      result.isFailure mustBe true
    }
  }
}

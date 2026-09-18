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

package pages.contractordetails

import models.Scheme
import pages.behaviours.PageBehaviours
import play.api.libs.json.JsPath

class ContractorSchemePageSpec extends PageBehaviours {

  private val scheme = Scheme(
    schemeId = 1,
    instanceId = "cisId",
    accountsOfficeReference = "123 PA 87654321",
    taxOfficeNumber = "123",
    taxOfficeReference = "45678"
  )

  "ContractorSchemePage" - {

    "have the correct path" in {
      ContractorSchemePage.path mustBe (
        JsPath \ "contractordetails" \ "ContractorSchemePage"
      )
    }

    "have the correct toString" in {
      ContractorSchemePage.toString mustBe "ContractorSchemePage"
    }

    "hasExistingUtr" - {

      "must be true when the scheme has a UTR" in {
        val answers = emptyUserAnswers
          .set(ContractorSchemePage, scheme.copy(utr = Some("1234567890")))
          .success
          .value

        ContractorSchemePage.hasExistingUtr(answers) mustBe true
      }

      "must be false when the scheme UTR is blank" in {
        val answers = emptyUserAnswers
          .set(ContractorSchemePage, scheme.copy(utr = Some("  ")))
          .success
          .value

        ContractorSchemePage.hasExistingUtr(answers) mustBe false
      }

      "must be false when the scheme has no UTR" in {
        val answers = emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value

        ContractorSchemePage.hasExistingUtr(answers) mustBe false
      }

      "must be false when the scheme is missing" in {
        ContractorSchemePage.hasExistingUtr(emptyUserAnswers) mustBe false
      }
    }
  }
}

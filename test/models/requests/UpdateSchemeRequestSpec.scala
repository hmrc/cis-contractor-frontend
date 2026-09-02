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

package models.requests

import base.SpecBase
import play.api.libs.json.Json

class UpdateSchemeRequestSpec extends SpecBase {

  "UpdateSchemeRequest" - {

    "must write to JSON" in {
      val request =
        UpdateSchemeRequest(
          schemeId = 1,
          instanceId = "INST-123",
          taxOfficeNumber = "123",
          taxOfficeReference = "AB456",
          accountsOfficeReference = "AO123",
          prePopCount = 2,
          prePopSuccessful = "Y",
          uniqueTaxReference = "1234567890",
          name = "Scheme",
          emailAddress = "test@example.com",
          version = 4
        )

      val json = Json.toJson(request)

      (json \ "schemeId").as[Int] mustBe 1
      (json \ "instanceId").as[String] mustBe "INST-123"
      (json \ "uniqueTaxReference").as[String] mustBe "1234567890"
      (json \ "version").as[Int] mustBe 4
    }
  }
}

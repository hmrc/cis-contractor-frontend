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

package models.finalvalidation

import base.SpecBase

class VerifySelectedSubcontractorSpec extends SpecBase {

  "VerifySelectedSubcontractor" - {

    "must contain the supplied subcontractor details" in {

      val subcontractor =
        VerifySelectedSubcontractor(
          subcontractorId = 1L,
          name = "John Smith"
        )

      subcontractor.subcontractorId mustBe 1L
      subcontractor.name mustBe "John Smith"
    }
  }
}

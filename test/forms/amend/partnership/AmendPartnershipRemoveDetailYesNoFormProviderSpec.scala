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

package forms.amend.partnership

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AmendPartnershipRemoveDetailYesNoFormProviderSpec
  extends AnyWordSpec
    with Matchers {

  private val form =
    new AmendPartnershipRemoveDetailYesNoFormProvider()()

  "AmendPartnershipRemoveDetailYesNoFormProvider" should {

    "bind true successfully" in {
      val result = form.bind(Map("value" -> "true"))

      result.errors shouldBe empty
      result.value shouldBe Some(true)
    }

    "bind false successfully" in {
      val result = form.bind(Map("value" -> "false"))

      result.errors shouldBe empty
      result.value shouldBe Some(false)
    }

    "return a required error when value is missing" in {
      val result = form.bind(Map.empty)

      result.hasErrors shouldBe true
      result.errors.map(_.message) should contain(
        "amendPartnershipRemoveDetailYesNo.error.required"
      )
    }

    "return a required error when value is empty" in {
      val result = form.bind(Map("value" -> ""))

      result.hasErrors shouldBe true
      result.errors.map(_.message) should contain(
        "amendPartnershipRemoveDetailYesNo.error.required"
      )
    }
  }
}

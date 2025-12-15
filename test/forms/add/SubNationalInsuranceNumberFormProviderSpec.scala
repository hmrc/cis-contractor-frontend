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

package forms.add

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class SubNationalInsuranceNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "subNationalInsuranceNumber.error.required"
  val lengthKey = "subNationalInsuranceNumber.error.length"
  val invalidKey  = "subNationalInsuranceNumber.error.invalidCharacters"
  val maxLength = 9

  val form = new SubNationalInsuranceNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    val validNino: Gen[String] = Gen.oneOf(
      "AA123456A",
      "BC000000D",
      "se005000c",
      "AA123456",
      "YZ555555",
      "rj888888",

    )

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validNino
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "remove all whitespaces" in {
      val result = form.bind(Map(fieldName -> "  A A 1 2 3 4 5 6 A  "))
      result.value.value mustBe "AA123456A"
    }

    "reject invalid National Insurance number" in {
      val invalidNino = Seq("AB000000Z", "!A000000A", "AA`00000A")
      invalidNino.foreach { nino =>
        val result = form.bind(Map(fieldName -> nino))
        result.errors.map(_.message) must contain(invalidKey)
      }
    }

  }
}

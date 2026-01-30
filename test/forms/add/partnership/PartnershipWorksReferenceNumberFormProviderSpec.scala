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

package forms.add.partnership

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import org.scalatest.matchers.should.Matchers.should
import play.api.data.{Form, FormError}

class PartnershipWorksReferenceNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "partnershipWorksReferenceNumber.error.required"
  val invalidKey  = "partnershipWorksReferenceNumber.error.invalid"
  val lengthKey   = "partnershipWorksReferenceNumber.error.length"
  val maxLength   = 20

  val form: Form[String] = new PartnershipWorksReferenceNumberFormProvider()()
  val fieldName          = "value"
  ".value" - {

    def worksReferenceNumbers: Gen[String] =
      Gen
        .listOfN(
          maxLength,
          Gen.oneOf(
            ('A' to 'Z') ++
              ('a' to 'z') ++
              ('0' to '9') ++
              Seq(' ', '~', '!', '@', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '=',
                '?', '_', '{', '}', '£', '€')
          )
        )
        .map(_.mkString)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      worksReferenceNumbers
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
  }
  "trim leading and trailing spaces" in {
    val result = form.bind(Map(fieldName -> "   ABC Ltd   "))
    result.value.value mustBe "ABC Ltd"
  }

  "reject invalid characters (backtick, pipe)" in {
    val invalidSamples = Seq("Backtick ` here", "Pipe | symbol")
    invalidSamples.foreach { value =>
      val result = form.bind(Map(fieldName -> value))
      result.errors.map(_.message) must contain(invalidKey)
    }
  }
}

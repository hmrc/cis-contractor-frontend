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
import play.api.data.{Form, FormError}

class SubcontractorsUniqueTaxpayerReferenceFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "subcontractorsUniqueTaxpayerReference.error.required"
  val invalidKey  = "subcontractorsUniqueTaxpayerReference.error.invalid"
  val lengthKey   = "subcontractorsUniqueTaxpayerReference.error.length"

  val maxLength          = 10
  val form: Form[String] = new SubcontractorsUniqueTaxpayerReferenceFormProvider()()

  val fieldName = "value"

  override def stringsLongerThan(n: Int): Gen[String] =
    Gen.listOfN(n + 1, Gen.numChar).map(_.mkString)

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

  "value field" in {
    val result = form.bind(Map(fieldName -> "ABC123"))
    assert(result.hasErrors)
    assert(result.error(fieldName).exists(_.message == invalidKey))
  }

  "value field must bind valid numeric values" in {
    Seq("1", "123", "1234567890").foreach { value =>
      val result = form.bind(Map(fieldName -> value))
      assert(result.errors.isEmpty)
    }
  }
}

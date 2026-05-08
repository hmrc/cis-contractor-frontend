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

package forms.contractordetails

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.{Form, FormError}
import utils.UTRGenerator

class ContractorUtrFormProviderSpec extends AnyFreeSpec with Matchers with StringFieldBehaviours {

  def validUtrGenerator: Gen[String] = Gen.const(UTRGenerator.randomUTR())

  val requiredKey = "contractorUtr.error.required"
  val invalidKey  = "contractorUtr.error.invalid"
  val lengthKey   = invalidKey

  val utrLength          = 10
  val form: Form[String] = new ContractorUtrFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form = form,
      fieldName = fieldName,
      validDataGenerator = validUtrGenerator
    )

    "must not bind a numeric string shorter than 10 digits" in {
      val result = form.bind(Map(fieldName -> "123456789")).apply(fieldName)
      result.errors.map(_.message) must contain(lengthKey)
    }

    "must not bind a numeric string longer than 10 digits" in {
      val result = form.bind(Map(fieldName -> "12345678901")).apply(fieldName)
      result.errors.map(_.message) must contain(lengthKey)
    }

    behave like mandatoryField(
      form = form,
      fieldName = fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind a non-numeric string of correct length" in {
      val result = form.bind(Map(fieldName -> "123456789A")).apply(fieldName)
      result.errors.map(_.message) must contain(invalidKey)
    }

    "must not bind a UTR that fails the check digit algorithm" in {
      val invalidUtr = "1234567890"
      val result     = form.bind(Map(fieldName -> invalidUtr)).apply(fieldName)
      result.errors.map(_.message) must contain(invalidKey)
    }

    "must bind a valid UTR that passes the check digit algorithm" in {
      val validUtr = "5860920998"
      val result   = form.bind(Map(fieldName -> validUtr)).apply(fieldName)
      result.errors mustBe empty
      result.value mustBe Some(validUtr)
    }

    "must trim whitespace before validation" in {
      val validUtr = "5860920998"
      val result   = form.bind(Map(fieldName -> s"  $validUtr  ")).apply(fieldName)
      result.errors mustBe empty
    }
  }
}

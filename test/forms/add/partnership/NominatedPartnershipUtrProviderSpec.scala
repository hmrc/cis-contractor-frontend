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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.{Form, FormError}
import utils.UTRGenerator

class NominatedPartnershipUtrProviderSpec extends AnyFreeSpec with Matchers with StringFieldBehaviours {

  def validUtrGenerator: Gen[String] = Gen.const(UTRGenerator.randomUTR())

  val requiredKey = "partnershipNominatedPartnerUtr.error.required"
  val invalidKey  = "partnershipNominatedPartnerUtr.error.invalid"
  val lengthKey   = "partnershipNominatedPartnerUtr.error.length"

  val utrLength          = 10
  val form: Form[String] = new PartnershipNominatedPartnerUtrFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form = form,
      fieldName = fieldName,
      validDataGenerator = validUtrGenerator
    )

    behave like fieldWithMinLength(
      form = form,
      fieldName = fieldName,
      minLength = utrLength,
      lengthError = FormError(fieldName, lengthKey, Seq(utrLength))
    )

    behave like fieldWithMaxLength(
      form = form,
      fieldName = fieldName,
      maxLength = utrLength,
      lengthError = FormError(fieldName, lengthKey, Seq(utrLength))
    )

    behave like mandatoryField(
      form = form,
      fieldName = fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}

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
import play.api.data.FormError

class PartnershipPhoneNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "partnershipPhoneNumber.error.required"
  val lengthKey = "partnershipPhoneNumber.error.length"
  val invalidKey = "partnershipPhoneNumber.error.invalid"
  val maxLength = 35

  val form = new PartnershipPhoneNumberFormProvider()()

  val validPhoneNumber = Seq(
    "07777777777",
    "447777777777",
    "  07777 77777 ",
    "(44)77777777777",
    "44-777-777"
  )

  val invalidPhoneNumber = Seq(
    "+1 (800) 12-1 ext 9",
    "+91-9876543210 ext.42",
    "11021113751 ext 111",
    "+61.2.9876.5432 x99",
    "+44 20 7946.0958",
    "abc123",
    "!!"
  )

  ".value" - {

    val fieldName = "value"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must bind valid phone number data" in {
      validPhoneNumber.foreach { validPhoneNumber =>
        val result = form.bind(Map(fieldName -> validPhoneNumber))
        result.errors must be(empty)
      }
    }

    "must reject invalid phone number formats" in {
      invalidPhoneNumber.foreach { invalidTelephone =>
        val result = form.bind(Map(fieldName -> invalidTelephone))
        result.errors must contain(
          FormError(fieldName, invalidKey, Seq("^[0-9 )(\\-]+$"))
        )
      }
    }

    "must accept valid telephone formats" in {
      validPhoneNumber.foreach { validPhoneNumber =>
        val result = form.bind(Map(fieldName -> validPhoneNumber))
        result.errors must not contain FormError(fieldName, invalidKey)
      }
    }

    "must display error when too long (more than 35 numbers)" in {
      val tooLongNumbers = Seq(
        "012345678901234567890123456789012345",
        "    012345678901   234567   890123456789012345    "
      )

      tooLongNumbers.foreach { tooLongNumbers =>
        val result = form.bind(Map(fieldName-> tooLongNumbers))
        result.errors must contain(
          FormError(fieldName, lengthKey, Seq(maxLength))
        )
      }
    }

    "trim leading and trailing spaces" in {
      val result = form.bind(Map(fieldName -> "    123 456     "))
      result.hasErrors mustBe false
      result.value.value mustBe "123 456"
    }
  }
}

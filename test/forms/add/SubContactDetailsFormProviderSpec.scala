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
import play.api.data.FormError

class SubContactDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new SubContactDetailsFormProvider()()

  val validEmails = Seq(
    "user@domain.com",
    "user.name@domain.com",
    "user+tag@domain.com",
    "user@domain.co.uk",
    "user123@domain123.com",
    "user!#$%&*+-/=?^_`{|}~@domain.com",
    "user@domain!#$%&*+-/=?^_`{|}~.com"
  )

  val invalidEmails = Seq(
    "invalid-email",
    "@domain.com",
    "user@",
    "user name@domain.com",
    "user@domain com"
  )

  val validTelephones = Seq(
    "07777777777",
    "447777777777",
    "07777777777  ",
    "(44)77777777777",
    "44-777-777"
  )

  val invalidTelephones = Seq(
    "+1 (800) 12-1 ext 9",
    "+91-9876543210 ext.42",
    "11021113751 ext 111",
    "+61.2.9876.5432 x99",
    "+44 20 7946.0958",
    "abc123",
    "!!"
  )

  val emailFieldName   = "email"
  val emailRequiredKey = "subContactDetails.error.email.required"
  val emailLengthKey   = "subContactDetails.error.email.length"
  val emailInvalidKey  = "subContactDetails.error.email.invalid"
  val emailMaxLength   = 254

  val telephoneFieldName   = "telephone"
  val telephoneRequiredKey = "subContactDetails.error.telephone.required"
  val telephoneLengthKey   = "subContactDetails.error.telephone.length"
  val telephoneInvalidKey  = "subContactDetails.error.telephone.invalid"
  val telephoneMaxLength   = 35

  ".email" - {

    behave like fieldWithMaxLength(
      form,
      emailFieldName,
      maxLength = emailMaxLength,
      lengthError = FormError(emailFieldName, emailLengthKey, Seq(emailMaxLength))
    )

    behave like mandatoryField(
      form,
      emailFieldName,
      requiredError = FormError(emailFieldName, emailRequiredKey)
    )

    "must bind valid email data" in {
      validEmails.foreach { validEmail =>
        val result = form.bind(Map(emailFieldName -> validEmail, telephoneFieldName -> validTelephones.head))
        result.errors must be(empty)
      }
    }

    "must reject invalid email formats" in {
      invalidEmails.foreach { invalidEmail =>
        val result = form.bind(Map(emailFieldName -> invalidEmail, telephoneFieldName -> validTelephones.head))
        result.errors must contain(
          FormError(
            emailFieldName,
            emailInvalidKey,
            Seq("^[A-Za-z0-9!#$%&*+-/=?^_`{|}~.]+@[A-Za-z0-9!#$%&*+-/=?^_`{|}~.]+$")
          )
        )
      }
    }

    "must accept valid email formats" in {
      validEmails.foreach { validEmail =>
        val result = form.bind(Map(emailFieldName -> validEmail, telephoneFieldName -> validTelephones.head))
        result.errors must not contain FormError(emailFieldName, emailInvalidKey)
      }
    }
  }

  ".telephone" - {

    behave like fieldWithMaxLength(
      form,
      telephoneFieldName,
      maxLength = telephoneMaxLength,
      lengthError = FormError(telephoneFieldName, telephoneLengthKey, Seq(telephoneMaxLength))
    )

    behave like mandatoryField(
      form,
      telephoneFieldName,
      requiredError = FormError(telephoneFieldName, telephoneRequiredKey)
    )

    "must bind valid telephone data" in {
      validTelephones.foreach { validTelephone =>
        val result = form.bind(Map(emailFieldName -> validEmails.head, telephoneFieldName -> validTelephone))
        result.errors must be(empty)
      }
    }

    "must reject invalid telephone formats" in {
      invalidTelephones.foreach { invalidTelephone =>
        val result = form.bind(Map(emailFieldName -> validEmails.head, telephoneFieldName -> invalidTelephone))
        result.errors must contain(
          FormError(telephoneFieldName, telephoneInvalidKey, Seq("^[0-9 )/(\\-]+$"))
        )
      }
    }

    "must accept valid telephone formats" in {
      validTelephones.foreach { validTelephone =>
        val result = form.bind(Map(emailFieldName -> validEmails.head, telephoneFieldName -> validTelephone))
        result.errors must not contain FormError(telephoneFieldName, telephoneInvalidKey)
      }
    }
  }
}

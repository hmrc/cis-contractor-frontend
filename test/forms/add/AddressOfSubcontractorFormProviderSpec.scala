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

class AddressOfSubcontractorFormProviderSpec extends StringFieldBehaviours {

  private val form = new AddressOfSubcontractorFormProvider()()

  private val maxLength         = 35
  private val postcodeMaxLength = 8

  private val allowedChars: Seq[Char] =
    (('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9')) ++
      Seq(' ', ',', '.', '\'', '(', ')', '-', '/', '&', '£', '€', '_', '[', ']', '{', '}', ':', ';', '?', '!', '~', '@',
        '#', '$', '%', '*', '+', '^', '\\', '"')

  private val letterChars: Seq[Char] = ('A' to 'Z') ++ ('a' to 'z')

  private def validAddressLineGen(max: Int): Gen[String] = for {
    first   <- Gen.oneOf(letterChars)
    restLen <- Gen.choose(0, math.max(0, max - 1))
    rest    <- Gen.listOfN(restLen, Gen.oneOf(allowedChars)).map(_.mkString)
  } yield s"$first$rest"

  private val validPostcodes = Gen.oneOf(
    "SW1A 1AA",
    "EC1A 1BB",
    "W1A 0AX",
    "M1 1AE",
    "B33 8TH",
    "CR2 6XH",
    "DN55 1PT",
    "GIR 0AA"
  )

  private val invalidPostcodes = Gen.oneOf(
    "SW1A|1AA",
    "$$",
    "ABCD£",
    "123456",
    "AAAA AA"
  )

  ".addressLine1" - {

    val fieldName   = "addressLine1"
    val requiredKey = "addressOfSubcontractor.error.addressLine1.required"
    val lengthKey   = "addressOfSubcontractor.error.addressLine1.length"
    val invalidKey  = "addressOfSubcontractor.error.addressLine1.invalidCharacters"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validAddressLineGen(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength,
      FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      FormError(fieldName, requiredKey)
    )

    "must fail when invalid characters are used (while first char is a letter)" in {
      val input  = "A|Street"
      val result = form.bind(
        Map(fieldName -> input, "addressLine2" -> "B Street", "addressLine3" -> "C Town", "postCode" -> "SW1A 1AA")
      )
      result.errors.exists(_.message == invalidKey) mustBe true
    }
  }

  ".addressLine2 (optional)" - {

    val fieldName    = "addressLine2"
    val lengthKey    = "addressOfSubcontractor.error.addressLine2.length"
    val invalidKey   = "addressOfSubcontractor.error.addressLine2.invalidCharacters"
    val firstCharKey = "addressOfSubcontractor.error.addressLine2.firstCharMustBeLetter"

    "must bind valid data when provided" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "A Street",
          fieldName      -> "B Street",
          "addressLine3" -> "C Town",
          "postCode"     -> "EC1A 1BB"
        )
      )

      result.errors mustBe empty
    }

    "must allow the field to be empty" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "A Street",
          fieldName      -> "",
          "addressLine3" -> "C Town",
          "postCode"     -> "EC1A 1BB"
        )
      )

      result.errors mustBe empty
    }

    "must allow the field to be omitted" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "A Street",
          "addressLine3" -> "C Town",
          "postCode"     -> "EC1A 1BB"
        )
      )

      result.errors mustBe empty
    }

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength,
      FormError(fieldName, lengthKey, Seq(maxLength))
    )

    "must fail when invalid characters are used (when provided)" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "A Street",
          fieldName      -> "B|Street",
          "addressLine3" -> "C Town",
          "postCode"     -> "EC1A 1BB"
        )
      )

      result.errors.exists(_.message == invalidKey) mustBe true
    }

    "must fail when first character is not a letter (when provided)" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "A Street",
          fieldName      -> "1B Street",
          "addressLine3" -> "C Town",
          "postCode"     -> "EC1A 1BB"
        )
      )

      result.errors.exists(_.message == firstCharKey) mustBe true
    }
  }

  ".addressLine3" - {

    val fieldName    = "addressLine3"
    val requiredKey  = "addressOfSubcontractor.error.addressLine3.required"
    val lengthKey    = "addressOfSubcontractor.error.addressLine3.length"
    val invalidKey   = "addressOfSubcontractor.error.addressLine3.invalidCharacters"
    val firstCharKey = "addressOfSubcontractor.error.addressLine3.firstCharMustBeLetter"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validAddressLineGen(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength,
      FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      FormError(fieldName, requiredKey)
    )

    "must fail when invalid characters are used (while first char is a letter)" in {
      val result = form.bind(
        Map("addressLine1" -> "A Street", "addressLine2" -> "B Street", fieldName -> "C|Town", "postCode" -> "W1A 0AX")
      )
      result.errors.exists(_.message == invalidKey) mustBe true
    }

    "must fail when first character is not a letter" in {
      val result = form.bind(
        Map("addressLine1" -> "A Street", "addressLine2" -> "B Street", fieldName -> "1C Town", "postCode" -> "W1A 0AX")
      )
      result.errors.exists(_.message == firstCharKey) mustBe true
    }
  }

  ".addressLine4 (optional)" - {

    val fieldName = "addressLine4"
    val lengthKey = "addressOfSubcontractor.error.addressLine4.length"

    "must bind valid optional data when required fields are present" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "A Street",
          "addressLine2" -> "B Street",
          "addressLine3" -> "C Town",
          fieldName      -> "County",
          "postCode"     -> "M1 1AE"
        )
      )
      result.errors mustBe empty
    }

    "may be omitted when required fields are present" in {
      val result = form.bind(
        Map(
          "addressLine1" -> "A Street",
          "addressLine2" -> "B Street",
          "addressLine3" -> "C Town",
          "postCode"     -> "CR2 6XH"
        )
      )
      result.errors mustBe empty
    }

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength,
      FormError(fieldName, lengthKey, Seq(maxLength))
    )
  }

  ".postCode" - {

    val fieldName   = "postCode"
    val requiredKey = "addressOfSubcontractor.error.postCode.required"
    val lengthKey   = "addressOfSubcontractor.error.postCode.length"
    val invalidKey  = "addressOfSubcontractor.error.postCode.invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validPostcodes
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      postcodeMaxLength,
      FormError(fieldName, lengthKey, Seq(postcodeMaxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      FormError(fieldName, requiredKey)
    )

    "must fail when invalid postcode characters are used" in {
      forAll(invalidPostcodes) { bad =>
        val result = form.bind(
          Map(
            "addressLine1" -> "A Street",
            "addressLine3" -> "C Town",
            "postCode"     -> bad
          )
        )

        result.errors.exists(_.message == invalidKey) mustBe true
      }
    }
  }
}

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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import org.scalacheck.Gen
import models.add.SubcontractorName

class SubcontractorNameFormProviderSpec extends StringFieldBehaviours {

  val formProvider = new SubcontractorNameFormProvider()
  val form         = formProvider()

  val validLastName  = "Smith"
  val validFirstName = "John"

  "firstName" - {

    val fieldName = "firstName"

    "must bind valid data" in {
      val validValues = Seq("John", "Ben", "Joe")
      validValues.foreach { value =>
        val result = form.bind(
          Map(
            "firstName"  -> value,
            "middleName" -> "",
            "lastName"   -> validLastName
          )
        )
        result.errors mustBe empty
        result.value.value.firstName mustBe value
      }
    }

    "must not bind empty data" in {
      val result = form.bind(
        Map(
          "firstName"  -> "",
          "middleName" -> "",
          "lastName"   -> validLastName
        )
      )
      result.errors.map(_.message) must contain("subcontractorName.firstName.error.required")
    }

    "must not bind strings longer than 35 characters" in {
      val longString = "A" * 36
      val result     = form.bind(
        Map(
          "firstName"  -> longString,
          "middleName" -> "",
          "lastName"   -> validLastName
        )
      )
      result.errors.map(_.message) must contain("subcontractorName.firstName.error.length")
    }

    "must not bind strings with invalid characters" in {
      val invalid = "John123!"
      val result  = form.bind(
        Map(
          "firstName"  -> invalid,
          "middleName" -> "",
          "lastName"   -> validLastName
        )
      )
      result.errors.map(_.message) must contain("subcontractorName.firstName.error.invalidCharacters")
    }

    "must not bind strings where first character is not a letter" in {
      val invalid = "-John"
      val result = form.bind(Map(
        "firstName" -> invalid,
        "middleName" -> "",
        "lastName" -> validLastName
      ))
      result.errors.map(_.message) must contain("subcontractorName.firstName.error.firstChar")
    }
  }

  "middleName" - {

    val fieldName   = "middleName"
    val maxLength   = 35
    val lengthError = "subcontractorName.middleName.error.length"

    val validFirstName = "John"
    val validLastName  = "Smith"

    "must bind valid data" in {
      val validValues = Seq("John", "Joe", "Ben")
      validValues.foreach { value =>
        val result = form.bind(
          Map(
            "firstName"  -> validFirstName,
            "middleName" -> value,
            "lastName"   -> validLastName
          )
        )
        result.errors mustBe empty
        result.value.value.middleName mustBe Some(value)
      }
    }

    "must bind empty data as None" in {
      val result = form.bind(
        Map(
          "firstName"  -> validFirstName,
          "middleName" -> "",
          "lastName"   -> validLastName
        )
      )
      result.errors mustBe empty
      result.value.value.middleName mustBe None
    }

    s"must not bind strings longer than $maxLength characters" in {
      val longString = "a" * (maxLength + 1)
      val result     = form.bind(
        Map(
          "firstName"  -> validFirstName,
          "middleName" -> longString,
          "lastName"   -> validLastName
        )
      )
      result.errors.map(_.message) must contain(lengthError)
    }

    "must fail when invalid characters present" in {
      val result = form.bind(
        Map(
          "firstName"  -> validFirstName,
          "middleName" -> "Middle123!",
          "lastName"   -> validLastName
        )
      )
      result.errors.map(_.message) must contain("subcontractorName.middleName.error.invalidCharacters")
    }

    "must fail when first character is not a letter" in {
      val result = form.bind(
        Map(
          "firstName"  -> validFirstName,
          "middleName" -> "-Middle",
          "lastName"   -> validLastName
        )
      )
      result.errors.map(_.message) must contain("subcontractorName.middleName.error.firstChar")
    }
  }

  "lastName" - {

    val fieldName   = "lastName"
    val maxLength   = 35
    val lengthError = "subcontractorName.lastName.error.length"

    val validFirstName  = "John"
    val validMiddleName = Some("Joe") // optional field

    "must bind valid data" in {
      val validValues = Seq("Smith", "O'Neil", "McDonald")
      validValues.foreach { value =>
        val result = form.bind(
          Map(
            "firstName"  -> validFirstName,
            "middleName" -> validMiddleName.getOrElse(""),
            "lastName"   -> value
          )
        )
        result.errors mustBe empty
        result.value.value.lastName mustBe value
      }
    }

    "must not bind empty data" in {
      val result = form.bind(
        Map(
          "firstName"  -> validFirstName,
          "middleName" -> validMiddleName.getOrElse(""),
          "lastName"   -> ""
        )
      )
      result.errors.map(_.message) must contain("subcontractorName.lastName.error.required")
    }

    s"must not bind strings longer than $maxLength characters" in {
      val longString = "a" * (maxLength + 1)
      val result     = form.bind(
        Map(
          "firstName"  -> validFirstName,
          "middleName" -> validMiddleName.getOrElse(""),
          "lastName"   -> longString
        )
      )
      result.errors.map(_.message) must contain(lengthError)
    }

    "must fail when invalid characters present" in {
      val result = form.bind(
        Map(
          "firstName"  -> validFirstName,
          "middleName" -> validMiddleName.getOrElse(""),
          "lastName"   -> "Smith123!"
        )
      )
      result.errors.map(_.message) must contain("subcontractorName.lastName.error.invalidCharacters")
    }

    "must fail when first character is not a letter" in {
      val result = form.bind(
        Map(
          "firstName"  -> validFirstName,
          "middleName" -> validMiddleName.getOrElse(""),
          "lastName"   -> "-Smith"
        )
      )
      result.errors.map(_.message) must contain("subcontractorName.lastName.error.firstChar")
    }
  }

}

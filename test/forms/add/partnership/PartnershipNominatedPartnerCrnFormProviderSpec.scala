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

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class PartnershipNominatedPartnerCrnFormProviderSpec extends AnyFreeSpec with Matchers with OptionValues {

  private val form = new PartnershipNominatedPartnerCrnFormProvider()()

  "PartnershipNominatedPartnerCrnFormProvider" - {

    "bind valid CRN formats" in {
      val valid = Seq(
        "AC012345",
        "ac012345",
        "AC 012 345",
        "AB1",
        "ZZ999999",
        "123",
        "00000001",
        "12345678",
        "12 34 56 78"
      )

      valid.foreach { v =>
        val bound = form.bind(Map("value" -> v))
        bound.hasErrors mustBe false
        bound.value.value mustBe v.replaceAll("\\s", "").toUpperCase
      }
    }

    "error when empty" in {
      val bound = form.bind(Map("value" -> ""))
      bound.hasErrors mustBe true
      bound.errors.head.message mustBe "partnershipNominatedPartnerCrn.error.required"
    }

    "error when too long (more than 8 chars ignoring spaces)" in {
      val tooLong = Seq(
        "123456789",
        "AB1234567",
        "12 34 56 78 9"
      )

      tooLong.foreach { v =>
        val bound = form.bind(Map("value" -> v))
        bound.hasErrors mustBe true
        bound.errors.map(_.message) must contain("partnershipNominatedPartnerCrn.error.length")
      }
    }

    "error when invalid format" in {
      val invalid = Seq(
        "A1",
        "ABC123",
        "AB1234567",
        "AC01-234",
        "AC01£345",
        "12AB3456"
      )

      invalid.foreach { v =>
        val bound = form.bind(Map("value" -> v))
        bound.hasErrors mustBe true
        bound.errors.map(_.message) must contain("partnershipNominatedPartnerCrn.error.invalidCharacters")
      }
    }

    "normalise the value by removing spaces and uppercasing" in {
      val bound = form.bind(Map("value" -> "  ac 012 345  "))
      bound.hasErrors mustBe false
      bound.value.value mustBe "AC012345"
    }
  }
}

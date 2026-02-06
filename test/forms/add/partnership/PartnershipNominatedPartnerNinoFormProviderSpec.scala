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


import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class PartnershipNominatedPartnerNinoFormProviderSpec extends AnyFreeSpec with Matchers {

  private val form = new PartnershipNominatedPartnerNinoFormProvider()()

  "PartnershipNominatedPartnerNinoFormProvider" - {

    "bind valid NINO formats" in {
      val valid = Seq(
        "QQ123456C",
        "QQ 12 34 56 C",
        "qq123456c",
        "  QQ 12 34 56 C  "
      )

      valid.foreach { v =>
        val bound = form.bind(Map("value" -> v))
        bound.hasErrors mustBe false
      }
    }

    "error when empty" in {
      val bound = form.bind(Map("value" -> ""))
      bound.hasErrors mustBe true
      bound.errors.head.message mustBe "partnershipNominatedPartnerNino.error.required"
    }

    "error when too long (more than 9 chars ignoring spaces)" in {
      val bound = form.bind(Map("value" -> "QQ123456CA"))
      bound.hasErrors mustBe true
      bound.errors.map(_.message) must contain("partnershipNominatedPartnerNino.error.length")
    }

    "error when invalid format" in {
      val bound = form.bind(Map("value" -> "123"))
      bound.hasErrors mustBe true
      bound.errors.map(_.message) must contain("partnershipNominatedPartnerNino.error.invalidCharacters")
    }

    "allow no suffix letter" in {
      val bound = form.bind(Map("value" -> "QQ123456"))
      bound.hasErrors mustBe false
    }
  }
}


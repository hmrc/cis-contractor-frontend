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

package forms

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.validation.{Invalid, Valid}

class ValidationSpec extends AnyFreeSpec with Matchers {

  "noPunycodeDomain" - {

    val constraint = Validation.noPunycodeDomain("error.invalid")

    "must return Valid for a standard domain" in {
      constraint("test@example.com") mustEqual Valid
    }

    "must return Valid for an email with no domain" in {
      constraint("nodomain") mustEqual Valid
    }

    "must return Invalid when a domain label starts with xn--" in {
      constraint("test@xn--mnchen-3ya.de") mustEqual Invalid("error.invalid")
    }

    "must return Invalid when only one label in a multi-label domain is xn--" in {
      constraint("test@mail.xn--mnchen-3ya.de") mustEqual Invalid("error.invalid")
    }
  }

  "noInvalidDomainCharacters" - {

    val constraint = Validation.noInvalidDomainCharacters("error.invalid")

    "must return Valid for a standard domain" in {
      constraint("test@example.com") mustEqual Valid
    }

    "must return Valid for a domain label with a hyphen" in {
      constraint("test@my-domain.com") mustEqual Valid
    }

    "must return Valid for a Punycode label that decodes to valid Unicode letters" in {
      constraint("test@xn--mnchen-3ya.de") mustEqual Valid
    }

    "must return Invalid for a Punycode label that decodes to non-letter/digit characters" in {
      constraint("test@xn--domain-8ia.com") mustEqual Invalid("error.invalid")
    }
  }
}

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

    "must return Invalid when a domain label starts with xn--" in {
      constraint("test@xn--mnchen-3ya.de") mustEqual Invalid("error.invalid")
    }

    "must return Invalid when only one label in a multi-label domain is xn--" in {
      constraint("test@mail.xn--mnchen-3ya.de") mustEqual Invalid("error.invalid")
    }
  }

  "emailRegex" - {

    def matches(email: String): Boolean = email.matches(Validation.emailRegex)

    "valid emails" - {
      "must match a standard address" in {
        matches("test@example.com") mustBe true
      }
      "must match a subdomain" in {
        matches("test@mail.example.com") mustBe true
      }
      "must match a ccTLD" in {
        matches("test@example.co.uk") mustBe true
      }
      "must match with special chars in local part" in {
        matches("test.name+tag@example.com") mustBe true
      }
      "must match a hyphenated domain" in {
        matches("user@my-domain.com") mustBe true
      }
      "must match a short address" in {
        matches("a@b.co") mustBe true
      }
    }

    "missing structure" - {
      "must not match with no local part" in {
        matches("@example.com") mustBe false
      }
      "must not match with no domain" in {
        matches("test@") mustBe false
      }
      "must not match with no at sign" in {
        matches("testexample.com") mustBe false
      }
      "must not match when domain starts with a dot" in {
        matches("test@.example.com") mustBe false
      }
      "must not match an empty string" in {
        matches("") mustBe false
      }
    }

    "non-ASCII and non-English characters" - {
      "must not match a non-ASCII character in the local part" in {
        matches("tëst@example.com") mustBe false
      }
      "must not match a German umlaut in the domain" in {
        matches("test@münchen.de") mustBe false
      }
      "must not match a Cyrillic domain" in {
        matches("test@пример.com") mustBe false
      }
      "must not match a Chinese character domain" in {
        matches("test@例子.com") mustBe false
      }
      "must not match non-ASCII in both local and domain parts" in {
        matches("tëst@münchen.de") mustBe false
      }
      "must not match an Arabic local part" in {
        matches("مستخدم@example.com") mustBe false
      }
    }

    "IP address domains" - {
      "must match an IPv4 domain" in {
        matches("test@192.168.1.1") mustBe true
      }
    }
  }
}

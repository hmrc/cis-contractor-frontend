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

class ValidationSpec extends AnyFreeSpec with Matchers {

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
      "must match a domain without a dot" in {
        matches("test@example") mustBe true
      }
      "must match an underscore in the local part" in {
        matches("test_name@example.com") mustBe true
      }
      "must match an apostrophe in the local part" in {
        matches("o'connor@example.com") mustBe true
      }
      "must match a curly apostrophe in the local part" in {
        matches("o’connor@example.com") mustBe true
      }
      "must match consecutive dots in the local part" in {
        matches("test..name@example.com") mustBe true
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
      "must not match an empty string" in {
        matches("") mustBe false
      }
      "must not match multiple at signs" in {
        matches("test@@example.com") mustBe false
      }
    }

    "invalid domain characters" - {
      "must not match an underscore in the domain" in {
        matches("test@example_domain.com") mustBe false
      }
      "must not match a comma in the domain" in {
        matches("test@example,com") mustBe false
      }
      "must not match a forward slash in the domain" in {
        matches("test@example/com") mustBe false
      }
      "must not match a plus sign in the domain" in {
        matches("test@example+test.com") mustBe false
      }
      "must not match an exclamation mark in the domain" in {
        matches("test@example!.com") mustBe false
      }
    }

    "invalid domain structure" - {
      "must not match consecutive dots in the domain" in {
        matches("test@example..com") mustBe false
      }
      "must not match a leading dot in the domain" in {
        matches("test@.example.com") mustBe false
      }
      "must not match a trailing dot in the domain" in {
        matches("test@example.com.") mustBe false
      }
    }

    "whitespace" - {
      "must not match leading whitespace" in {
        matches(" test@example.com") mustBe false
      }
      "must not match trailing whitespace" in {
        matches("test@example.com ") mustBe false
      }
      "must not match whitespace in the local part" in {
        matches("test name@example.com") mustBe false
      }
      "must not match whitespace in the domain" in {
        matches("test@example .com") mustBe false
      }
    }

    "non-ASCII and non-English characters" - {
      "must not match a non-ASCII letter in the local part" in {
        matches("tëst@example.com") mustBe false
      }
      "must not match a non-ASCII letter in the domain" in {
        matches("test@ëxample.com") mustBe false
      }
    }

    "IP address domains" - {
      "must match an IPv4 domain" in {
        matches("test@192.168.1.1") mustBe true
      }
    }
  }
}

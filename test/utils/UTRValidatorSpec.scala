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

package utils

import base.SpecBase

class UTRValidatorSpec extends SpecBase:

  private val UTR_WEIGHTS = Array(6, 7, 8, 9, 10, 5, 4, 3, 2)

  private def randomUTR(): String =
    val random      = new scala.util.Random
    val digits      = (1 to 9).map(_ => random.nextInt(10))
    val total       = (0 to 8).map(i => digits(i) * UTR_WEIGHTS(i)).sum
    var checkNumber = 11 - total % 11
    if (checkNumber > 9) checkNumber -= 9
    val res         = checkNumber.toString + digits.mkString
    println(s"Generated UTR: $res")
    res

  private val utrValidator: UTRValidator = new UTRValidatorImpl

  "UTRValidator" - {

    "return true for a valid existing UTR" in {
      val validUtr = "5860920998"
      utrValidator.isValidUTR(validUtr) mustBe true
    }

    "return true for a valid UTR" in {
      val validUtr = randomUTR() // "4980086463" // TODO: This is my UTR!! Replace with a generic valid UTR for testing
      utrValidator.isValidUTR(validUtr) mustBe true
    }

    "return true for all of a larger set of UTR numbers" in {
      val randomUTRs = (1 to 1000).map(_ => randomUTR())
      randomUTRs.foreach { utr =>
        utrValidator.isValidUTR(utr) mustBe true
      }
    }

    "return false for a UTR of expected length but the check digit is wrong" in {
      val shortUtr = "123456789"
      utrValidator.isValidUTR(shortUtr) mustBe false
    }

    "return false for a UTR that is too short" in {
      val shortUtr = "123456789"
      utrValidator.isValidUTR(shortUtr) mustBe false
    }

    "return false for a UTR that is too long" in {
      val longUtr = "12345678901"
      utrValidator.isValidUTR(longUtr) mustBe false
    }

    "return false for a UTR with non-numeric characters" in {
      val invalidUtr = "12345A7890"
      utrValidator.isValidUTR(invalidUtr) mustBe false
    }

    "return false for an empty UTR" in {
      val emptyUtr = ""
      utrValidator.isValidUTR(emptyUtr) mustBe false
    }
  }

end UTRValidatorSpec

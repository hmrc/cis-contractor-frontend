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

package rules.verify

import models.Subcontractor
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDateTime
import java.time.LocalDate

class ReverificationRulesSpec extends AnyWordSpec with Matchers {

  private def sub(
    verified: Option[String] = Some("Y"),
    verificationDate: Option[LocalDateTime] = None,
    lastMonthlyReturnDate: Option[LocalDateTime] = None,
    subcontractorType: Option[String] = None,
    subbieResourceRef: Option[Long] = None,
    utr: Option[String] = None,
    partnerUtr: Option[String] = None,
    crn: Option[String] = None,
    nino: Option[String] = None
  ): Subcontractor =
    Subcontractor(
      subcontractorId = 1L,
      firstName = None,
      secondName = None,
      surname = None,
      tradingName = None,
      partnershipTradingName = None,
      verified = verified,
      verificationNumber = None,
      taxTreatment = None,
      verificationDate = verificationDate,
      lastMonthlyReturnDate = lastMonthlyReturnDate,
      createDate = None,
      subcontractorType = subcontractorType,
      subbieResourceRef = subbieResourceRef,
      utr = utr,
      partnerUtr = partnerUtr,
      crn = crn,
      nino = nino
    )

  "ReverificationRules.startDate" should {

    "match the examples in the spec" in {
      ReverificationRules.startDate(LocalDate.of(2026, 1, 25)) mustBe LocalDate.of(2023, 4, 6)
      ReverificationRules.startDate(LocalDate.of(2026, 4, 5)) mustBe LocalDate.of(2023, 4, 6)
      ReverificationRules.startDate(LocalDate.of(2026, 4, 6)) mustBe LocalDate.of(2024, 4, 6)
      ReverificationRules.startDate(LocalDate.of(2026, 4, 25)) mustBe LocalDate.of(2024, 4, 6)
    }
  }

  "ReverificationRules.reverifyRequired" should {

    "return false when subcontractor is not previously verified (VERIFIED != Y)" in {
      val current = LocalDate.of(2026, 4, 25)
      ReverificationRules.reverifyRequired(sub(verified = Some("N")), current) mustBe false
      ReverificationRules.reverifyRequired(sub(verified = None), current) mustBe false
    }

    "AC1: require reVerification when VERIFIED == Y but verificationDate is missing" in {
      val current = LocalDate.of(2026, 1, 25)
      ReverificationRules.reverifyRequired(sub(verificationDate = None), current) mustBe true
    }

    "AC2: not require reVerification when verificationDate is between startDate and currentDate" in {
      val current = LocalDate.of(2026, 1, 25)
      val start   = ReverificationRules.startDate(current)

      val s = sub(verificationDate = Some(start.plusDays(1).atStartOfDay()))
      ReverificationRules.reverifyRequired(s, current) mustBe false

      val s2 = sub(verificationDate = Some(start.atStartOfDay()))
      ReverificationRules.reverifyRequired(s2, current) mustBe false
    }

    "AC3: not require reVerification when verificationDate is before startDate but lastMonthlyReturnDate is between startDate and currentDate" in {
      val current = LocalDate.of(2026, 1, 25)
      val start   = ReverificationRules.startDate(current)

      val s = sub(
        verificationDate = Some(start.minusDays(10).atStartOfDay()),
        lastMonthlyReturnDate = Some(start.plusDays(5).atStartOfDay())
      )

      ReverificationRules.reverifyRequired(s, current) mustBe false
    }

    "AC4: require reVerification when verificationDate is before startDate and lastMonthlyReturnDate is before startDate" in {
      val current = LocalDate.of(2026, 1, 25)
      val start   = ReverificationRules.startDate(current)

      val s = sub(
        verificationDate = Some(start.minusDays(10).atStartOfDay()),
        lastMonthlyReturnDate = Some(start.minusDays(1).atStartOfDay())
      )

      ReverificationRules.reverifyRequired(s, current) mustBe true
    }

    "require reVerification when verificationDate is before startDate and lastMonthlyReturnDate is missing" in {
      val current = LocalDate.of(2026, 1, 25)
      val start   = ReverificationRules.startDate(current)

      val s = sub(
        verificationDate = Some(start.minusDays(10).atStartOfDay()),
        lastMonthlyReturnDate = None
      )

      ReverificationRules.reverifyRequired(s, current) mustBe true
    }
  }
}

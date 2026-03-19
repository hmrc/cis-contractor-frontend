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

package navigation

import base.SpecBase
import models.{NormalMode, UserAnswers}
import navigation.add.{CompanyNavigator, IndividualNavigator, PartnershipNavigator, SharedNavigator, TrustNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.Page
import pages.add.company.CompanyPage
import pages.add.partnership.PartnershipPage
import pages.add.trust.TrustPage
import pages.add.IndividualPage
import play.api.mvc.Call

class NavigatorSpec extends SpecBase {

  private val ua = UserAnswers("id")
  private val mode = NormalMode

  "Navigator" - {

    "must delegate to IndividualNavigator when page is an IndividualPage" in {
      case object TestIndividualPage extends Page with IndividualPage

      val individual = mock[IndividualNavigator]
      val company = mock[CompanyNavigator]
      val partnership = mock[PartnershipNavigator]
      val trust = mock[TrustNavigator]
      val shared = mock[SharedNavigator]

      val expected = Call("GET", "/individual")
      when(individual.nextPage(any(), any(), any())).thenReturn(expected)

      val navigator =
        new Navigator(individual, company, partnership, trust, shared)

      navigator.nextPage(TestIndividualPage, mode, ua) mustBe expected

      verify(individual).nextPage(TestIndividualPage, mode, ua)
      verify(company, never()).nextPage(any(), any(), any())
      verify(partnership, never()).nextPage(any(), any(), any())
      verify(trust, never()).nextPage(any(), any(), any())
      verify(shared, never()).nextPage(any(), any(), any())
    }

    "must delegate to CompanyNavigator when page is a CompanyPage" in {
      case object TestCompanyPage extends Page with CompanyPage

      val individual = mock[IndividualNavigator]
      val company = mock[CompanyNavigator]
      val partnership = mock[PartnershipNavigator]
      val trust = mock[TrustNavigator]
      val shared = mock[SharedNavigator]

      val expected = Call("GET", "/company")
      when(company.nextPage(any(), any(), any())).thenReturn(expected)

      val navigator =
        new Navigator(individual, company, partnership, trust, shared)

      navigator.nextPage(TestCompanyPage, mode, ua) mustBe expected

      verify(company).nextPage(TestCompanyPage, mode, ua)
      verify(individual, never()).nextPage(any(), any(), any())
      verify(partnership, never()).nextPage(any(), any(), any())
      verify(trust, never()).nextPage(any(), any(), any())
      verify(shared, never()).nextPage(any(), any(), any())
    }

    "must delegate to PartnershipNavigator when page is a PartnershipPage" in {
      case object TestPartnershipPage extends Page with PartnershipPage

      val individual = mock[IndividualNavigator]
      val company = mock[CompanyNavigator]
      val partnership = mock[PartnershipNavigator]
      val trust = mock[TrustNavigator]
      val shared = mock[SharedNavigator]

      val expected = Call("GET", "/partnership")
      when(partnership.nextPage(any(), any(), any())).thenReturn(expected)

      val navigator =
        new Navigator(individual, company, partnership, trust, shared)

      navigator.nextPage(TestPartnershipPage, mode, ua) mustBe expected

      verify(partnership).nextPage(TestPartnershipPage, mode, ua)
      verify(individual, never()).nextPage(any(), any(), any())
      verify(company, never()).nextPage(any(), any(), any())
      verify(trust, never()).nextPage(any(), any(), any())
      verify(shared, never()).nextPage(any(), any(), any())
    }

    "must delegate to TrustNavigator when page is a TrustPage" in {
      case object TestTrustPage extends Page with TrustPage

      val individual = mock[IndividualNavigator]
      val company = mock[CompanyNavigator]
      val partnership = mock[PartnershipNavigator]
      val trust = mock[TrustNavigator]
      val shared = mock[SharedNavigator]

      val expected = Call("GET", "/trust")
      when(trust.nextPage(any(), any(), any())).thenReturn(expected)

      val navigator =
        new Navigator(individual, company, partnership, trust, shared)

      navigator.nextPage(TestTrustPage, mode, ua) mustBe expected

      verify(trust).nextPage(TestTrustPage, mode, ua)
      verify(individual, never()).nextPage(any(), any(), any())
      verify(company, never()).nextPage(any(), any(), any())
      verify(partnership, never()).nextPage(any(), any(), any())
      verify(shared, never()).nextPage(any(), any(), any())
    }

    "must delegate to SharedNavigator when page is not a journey-specific page" in {
      case object TestUnknownPage extends Page

      val individual = mock[IndividualNavigator]
      val company = mock[CompanyNavigator]
      val partnership = mock[PartnershipNavigator]
      val trust = mock[TrustNavigator]
      val shared = mock[SharedNavigator]

      val expected = Call("GET", "/shared")
      when(shared.nextPage(any(), any(), any())).thenReturn(expected)

      val navigator =
        new Navigator(individual, company, partnership, trust, shared)

      navigator.nextPage(TestUnknownPage, mode, ua) mustBe expected

      verify(shared).nextPage(TestUnknownPage, mode, ua)
      verify(individual, never()).nextPage(any(), any(), any())
      verify(company, never()).nextPage(any(), any(), any())
      verify(partnership, never()).nextPage(any(), any(), any())
      verify(trust, never()).nextPage(any(), any(), any())
    }
  }
}

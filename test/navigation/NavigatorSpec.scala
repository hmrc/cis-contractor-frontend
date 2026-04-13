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
import navigation.verify.VerifyNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.Page
import pages.add.company.CompanyJourney
import pages.add.partnership.PartnershipJourney
import pages.add.trust.TrustJourney
import pages.add.IndividualJourney
import play.api.mvc.Call

class NavigatorSpec extends SpecBase {

  private val ua   = UserAnswers("id")
  private val mode = NormalMode

  "Navigator" - {

    "must delegate to IndividualNavigator when page is an IndividualJourney" in {
      case object TestIndividualPage extends Page with IndividualJourney

      val individual      = mock[IndividualNavigator]
      val company         = mock[CompanyNavigator]
      val partnership     = mock[PartnershipNavigator]
      val trust           = mock[TrustNavigator]
      val shared          = mock[SharedNavigator]
      val verifyNavigator = mock[VerifyNavigator]

      val expected = Call("GET", "/individual")
      when(individual.nextPage(any(), any(), any())).thenReturn(expected)

      val navigator =
        new Navigator(individual, company, partnership, trust, shared, verifyNavigator)

      navigator.nextPage(TestIndividualPage, mode, ua) mustBe expected

      verify(individual).nextPage(TestIndividualPage, mode, ua)
      verify(company, never()).nextPage(any(), any(), any())
      verify(partnership, never()).nextPage(any(), any(), any())
      verify(trust, never()).nextPage(any(), any(), any())
      verify(shared, never()).nextPage(any(), any(), any())
    }

    "must delegate to CompanyNavigator when page is a CompanyJourney" in {
      case object TestCompanyPage extends Page with CompanyJourney

      val individual      = mock[IndividualNavigator]
      val company         = mock[CompanyNavigator]
      val partnership     = mock[PartnershipNavigator]
      val trust           = mock[TrustNavigator]
      val shared          = mock[SharedNavigator]
      val verifyNavigator = mock[VerifyNavigator]

      val expected = Call("GET", "/company")
      when(company.nextPage(any(), any(), any())).thenReturn(expected)

      val navigator =
        new Navigator(individual, company, partnership, trust, shared, verifyNavigator)

      navigator.nextPage(TestCompanyPage, mode, ua) mustBe expected

      verify(company).nextPage(TestCompanyPage, mode, ua)
      verify(individual, never()).nextPage(any(), any(), any())
      verify(partnership, never()).nextPage(any(), any(), any())
      verify(trust, never()).nextPage(any(), any(), any())
      verify(shared, never()).nextPage(any(), any(), any())
    }

    "must delegate to PartnershipNavigator when page is a PartnershipPage" in {
      case object TestPartnershipPage extends Page with PartnershipJourney

      val individual      = mock[IndividualNavigator]
      val company         = mock[CompanyNavigator]
      val partnership     = mock[PartnershipNavigator]
      val trust           = mock[TrustNavigator]
      val shared          = mock[SharedNavigator]
      val verifyNavigator = mock[VerifyNavigator]

      val expected = Call("GET", "/partnership")
      when(partnership.nextPage(any(), any(), any())).thenReturn(expected)

      val navigator =
        new Navigator(individual, company, partnership, trust, shared, verifyNavigator)

      navigator.nextPage(TestPartnershipPage, mode, ua) mustBe expected

      verify(partnership).nextPage(TestPartnershipPage, mode, ua)
      verify(individual, never()).nextPage(any(), any(), any())
      verify(company, never()).nextPage(any(), any(), any())
      verify(trust, never()).nextPage(any(), any(), any())
      verify(shared, never()).nextPage(any(), any(), any())
    }

    "must delegate to TrustNavigator when page is a TrustJourney" in {
      case object TestTrustPage extends Page with TrustJourney

      val individual      = mock[IndividualNavigator]
      val company         = mock[CompanyNavigator]
      val partnership     = mock[PartnershipNavigator]
      val trust           = mock[TrustNavigator]
      val shared          = mock[SharedNavigator]
      val verifyNavigator = mock[VerifyNavigator]

      val expected = Call("GET", "/trust")
      when(trust.nextPage(any(), any(), any())).thenReturn(expected)

      val navigator =
        new Navigator(individual, company, partnership, trust, shared, verifyNavigator)

      navigator.nextPage(TestTrustPage, mode, ua) mustBe expected

      verify(trust).nextPage(TestTrustPage, mode, ua)
      verify(individual, never()).nextPage(any(), any(), any())
      verify(company, never()).nextPage(any(), any(), any())
      verify(partnership, never()).nextPage(any(), any(), any())
      verify(shared, never()).nextPage(any(), any(), any())
    }

    "must delegate to VerifyNavigator when page is a VerifyJourney" in {
      case object TestVerifyPage extends Page with pages.verify.VerifyJourney

      val individual  = mock[IndividualNavigator]
      val company     = mock[CompanyNavigator]
      val partnership = mock[PartnershipNavigator]
      val trust       = mock[TrustNavigator]
      val shared      = mock[SharedNavigator]
      val verifyNav   = mock[VerifyNavigator]

      val expected = Call("GET", "/verify")
      when(verifyNav.nextPage(any(), any(), any())).thenReturn(expected)

      val navigator = new Navigator(individual, company, partnership, trust, shared, verifyNav)

      navigator.nextPage(TestVerifyPage, mode, ua) mustBe expected

      verify(verifyNav).nextPage(TestVerifyPage, mode, ua)
      verify(individual, never()).nextPage(any(), any(), any())
      verify(company, never()).nextPage(any(), any(), any())
      verify(partnership, never()).nextPage(any(), any(), any())
      verify(trust, never()).nextPage(any(), any(), any())
      verify(shared, never()).nextPage(any(), any(), any())
    }

    "must delegate to SharedNavigator when page is not a journey-specific page" in {
      case object TestUnknownPage extends Page

      val individual      = mock[IndividualNavigator]
      val company         = mock[CompanyNavigator]
      val partnership     = mock[PartnershipNavigator]
      val trust           = mock[TrustNavigator]
      val shared          = mock[SharedNavigator]
      val verifyNavigator = mock[VerifyNavigator]

      val expected = Call("GET", "/shared")
      when(shared.nextPage(any(), any(), any())).thenReturn(expected)

      val navigator =
        new Navigator(individual, company, partnership, trust, shared, verifyNavigator)

      navigator.nextPage(TestUnknownPage, mode, ua) mustBe expected

      verify(shared).nextPage(TestUnknownPage, mode, ua)
      verify(individual, never()).nextPage(any(), any(), any())
      verify(company, never()).nextPage(any(), any(), any())
      verify(partnership, never()).nextPage(any(), any(), any())
      verify(trust, never()).nextPage(any(), any(), any())
    }
  }
}

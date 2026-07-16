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

package controllers.add.company

import base.SpecBase
import controllers.routes
import models.NormalMode
import models.address.{Address, Country}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.company.CompanyNamePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.AddressLookupService
import models.UserAnswers
import queries.AddressLookupAmendReturnQuery

import scala.concurrent.Future

class CompanyAddressControllerSpec extends SpecBase with MockitoSugar {

  private val lookupUrl   = "/address-lookup-on-ramp"
  private val companyName = "Test Company"

  private val userAnswersWithName =
    emptyUserAnswers.set(CompanyNamePage, companyName).success.value

  private val testAddress = Address(
    addressLine1 = "line 1",
    addressLine2 = Some("line 2"),
    addressLine3 = Some("line 3"),
    addressLine4 = Some("line 4"),
    postcode = Some("NX1 1AA"),
    country = Some(Country(Some("GB"), Some("United Kingdom")))
  )

  private lazy val redirectRoute =
    controllers.add.company.routes.CompanyAddressController.redirectToAddressLookup().url

  private lazy val redirectChangeRoute =
    controllers.add.company.routes.CompanyAddressController.redirectToAddressLookup(Some("change")).url

  private lazy val callbackRoute =
    controllers.add.company.routes.CompanyAddressController.addressLookupCallback("addr-id").url

  private lazy val callbackChangeRoute =
    controllers.add.company.routes.CompanyAddressController.addressLookupCallbackChange("addr-id").url

  "CompanyAddress Controller" - {

    "redirectToAddressLookup" - {

      "must redirect to the address lookup on-ramp using the standard callback when session data exists and no changeRoute is provided" in {

        val mockSessionRepository    = mock[SessionRepository]
        val mockAddressLookupService = mock[AddressLookupService]

        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(userAnswersWithName))
        when(
          mockAddressLookupService
            .getJourneyUrl(any(), any(), any(), any(), any(), any(), any(), any(), any())(any(), any(), any())
        ) thenReturn Future.successful(Call("GET", lookupUrl))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithName))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[AddressLookupService].toInstance(mockAddressLookupService)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, redirectRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe lookupUrl

          val callbackCaptor = ArgumentCaptor.forClass(classOf[Call])
          val optNameCaptor  = ArgumentCaptor.forClass(classOf[Option[String]])
          verify(mockAddressLookupService)
            .getJourneyUrl(
              any(),
              callbackCaptor.capture(),
              any(),
              optNameCaptor.capture(),
              any(),
              any(),
              any(),
              any(),
              any()
            )(any(), any(), any())

          callbackCaptor.getValue.url mustBe
            controllers.add.company.routes.CompanyAddressController.addressLookupCallback().url
          optNameCaptor.getValue mustBe Some(companyName)
        }
      }

      "must redirect to the address lookup on-ramp using the change callback when a changeRoute is provided" in {

        val mockSessionRepository    = mock[SessionRepository]
        val mockAddressLookupService = mock[AddressLookupService]

        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(userAnswersWithName))
        when(
          mockAddressLookupService
            .getJourneyUrl(any(), any(), any(), any(), any(), any(), any(), any(), any())(any(), any(), any())
        ) thenReturn Future.successful(Call("GET", lookupUrl))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithName))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[AddressLookupService].toInstance(mockAddressLookupService)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, redirectChangeRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe lookupUrl

          val callbackCaptor = ArgumentCaptor.forClass(classOf[Call])
          val optNameCaptor  = ArgumentCaptor.forClass(classOf[Option[String]])
          verify(mockAddressLookupService)
            .getJourneyUrl(
              any(),
              callbackCaptor.capture(),
              any(),
              optNameCaptor.capture(),
              any(),
              any(),
              any(),
              any(),
              any()
            )(any(), any(), any())

          callbackCaptor.getValue.url mustBe
            controllers.add.company.routes.CompanyAddressController.addressLookupCallbackChange().url
          optNameCaptor.getValue mustBe Some(companyName)
        }
      }

      "must redirect to Journey Recovery when ALF is unavailable" in {

        val mockAddressLookupService = mock[AddressLookupService]

        when(
          mockAddressLookupService
            .getJourneyUrl(any(), any(), any(), any(), any(), any(), any(), any(), any())(any(), any(), any())
        ) thenReturn Future.failed(new RuntimeException("ALF unavailable"))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithName))
            .overrides(bind[AddressLookupService].toInstance(mockAddressLookupService))
            .build()

        running(application) {
          val request = FakeRequest(GET, redirectRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when no user answers exist" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, redirectRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when no company name can be resolved" in {

        val mockSessionRepository    = mock[SessionRepository]
        val mockAddressLookupService = mock[AddressLookupService]

        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[AddressLookupService].toInstance(mockAddressLookupService)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, redirectRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

          verify(mockAddressLookupService, never)
            .getJourneyUrl(any(), any(), any(), any(), any(), any(), any(), any(), any())(any(), any(), any())
        }
      }
    }

    "addressLookupCallback" - {

      "must retrieve and persist the address then redirect to AddCompanyContactMethodsYesNo page when the save succeeds" in {

        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(any())(any(), any())) thenReturn Future.successful(testAddress)
        when(mockAddressLookupService.saveAddressDetails(any(), any())(any(), any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithName))
            .overrides(bind[AddressLookupService].toInstance(mockAddressLookupService))
            .build()

        running(application) {
          val request = FakeRequest(GET, callbackRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe
            controllers.add.company.routes.AddCompanyContactMethodsYesNoController.onPageLoad(NormalMode).url

          val idCaptor = ArgumentCaptor.forClass(classOf[String])
          verify(mockAddressLookupService).getAddressById(idCaptor.capture())(any(), any())
          idCaptor.getValue mustBe "addr-id"
        }
      }

      "must redirect to Journey Recovery when ALF is unavailable" in {

        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(any())(any(), any())) thenReturn Future.failed(
          new RuntimeException("ALF unavailable")
        )

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithName))
            .overrides(bind[AddressLookupService].toInstance(mockAddressLookupService))
            .build()

        running(application) {
          val request = FakeRequest(GET, callbackRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when the address could not be saved" in {

        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(any())(any(), any())) thenReturn Future.successful(testAddress)
        when(mockAddressLookupService.saveAddressDetails(any(), any())(any(), any())) thenReturn Future.successful(
          false
        )

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithName))
            .overrides(bind[AddressLookupService].toInstance(mockAddressLookupService))
            .build()

        running(application) {
          val request = FakeRequest(GET, callbackRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when no user answers exist" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, callbackRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "addressLookupCallbackChange" - {

      "must retrieve and persist the address then redirect to Check Your Answers when the save succeeds" in {

        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(any())(any(), any())) thenReturn Future.successful(testAddress)
        when(mockAddressLookupService.saveAddressDetails(any(), any())(any(), any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithName))
            .overrides(bind[AddressLookupService].toInstance(mockAddressLookupService))
            .build()

        running(application) {
          val request = FakeRequest(GET, callbackChangeRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe
            controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when ALF is unavailable" in {

        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(any())(any(), any())) thenReturn Future.failed(
          new RuntimeException("ALF unavailable")
        )

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithName))
            .overrides(bind[AddressLookupService].toInstance(mockAddressLookupService))
            .build()

        running(application) {
          val request = FakeRequest(GET, callbackChangeRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when the address could not be saved" in {

        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(any())(any(), any())) thenReturn Future.successful(testAddress)
        when(mockAddressLookupService.saveAddressDetails(any(), any())(any(), any())) thenReturn Future.successful(
          false
        )

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithName))
            .overrides(bind[AddressLookupService].toInstance(mockAddressLookupService))
            .build()

        running(application) {
          val request = FakeRequest(GET, callbackChangeRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when no user answers exist" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, callbackChangeRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "redirectToAmendAddressLookup" - {

      "must set AddressLookupAmendReturnQuery and redirect to the change address lookup journey" in {

        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithName))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              controllers.add.company.routes.CompanyAddressController.redirectToAmendAddressLookup().url
            )

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.add.company.routes.CompanyAddressController
              .redirectToAddressLookup(Some("change"))
              .url

          verify(mockSessionRepository).set(captor.capture())

          captor.getValue
            .get(AddressLookupAmendReturnQuery)
            .value mustBe true
        }
      }

      "must redirect to Journey Recovery when saving fails" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.failed(new RuntimeException("DB unavailable")))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithName))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              controllers.add.company.routes.CompanyAddressController.redirectToAmendAddressLookup().url
            )

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when no user answers exist" in {

        val application =
          applicationBuilder(userAnswers = None)
            .build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              controllers.add.company.routes.CompanyAddressController.redirectToAmendAddressLookup().url
            )

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}

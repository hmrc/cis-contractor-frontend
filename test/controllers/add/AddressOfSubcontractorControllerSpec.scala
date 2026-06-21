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

package controllers.add

import base.SpecBase
import controllers.routes
import models.NormalMode
import models.add.SubcontractorName
import models.address.{Address, Country}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import pages.add.SubcontractorNamePage
import repositories.SessionRepository
import services.AddressLookupService

import scala.concurrent.Future

class AddressOfSubcontractorControllerSpec extends SpecBase with MockitoSugar {

  private val lookupUrl         = "/address-lookup-on-ramp"
  private val subcontractorName = "John Smith"

  private val userAnswersWithName =
    emptyUserAnswers
      .set(SubcontractorNamePage, SubcontractorName(firstName = "John", middleName = None, lastName = "Smith"))
      .success
      .value

  private val testAddress = Address(
    addressLine1 = "line 1",
    addressLine2 = Some("line 2"),
    addressLine3 = Some("line 3"),
    addressLine4 = Some("line 4"),
    postcode = Some("NX1 1AA"),
    country = Some(Country(Some("GB"), Some("United Kingdom")))
  )

  private lazy val redirectRoute =
    controllers.add.routes.AddressOfSubcontractorController.redirectToAddressLookup().url

  private lazy val redirectChangeRoute =
    controllers.add.routes.AddressOfSubcontractorController.redirectToAddressLookup(Some("change")).url

  private lazy val callbackRoute =
    controllers.add.routes.AddressOfSubcontractorController.addressLookupCallback("addr-id").url

  private lazy val callbackChangeRoute =
    controllers.add.routes.AddressOfSubcontractorController.addressLookupCallbackChange("addr-id").url

  "AddressOfSubcontractor Controller" - {

    "redirectToAddressLookup" - {

      "must redirect to the address lookup on-ramp using the standard callback when session data exists and no changeRoute is provided" in {

        val mockSessionRepository    = mock[SessionRepository]
        val mockAddressLookupService = mock[AddressLookupService]

        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(userAnswersWithName))
        when(
          mockAddressLookupService.getJourneyUrl(any(), any(), any(), any(), any())(any(), any(), any())
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
            .getJourneyUrl(any(), callbackCaptor.capture(), any(), optNameCaptor.capture(), any())(any(), any(), any())

          callbackCaptor.getValue.url mustBe
            controllers.add.routes.AddressOfSubcontractorController.addressLookupCallback().url
          optNameCaptor.getValue mustBe Some(subcontractorName)
        }
      }

      "must redirect to the address lookup on-ramp using the change callback when a changeRoute is provided" in {

        val mockSessionRepository    = mock[SessionRepository]
        val mockAddressLookupService = mock[AddressLookupService]

        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(userAnswersWithName))
        when(
          mockAddressLookupService.getJourneyUrl(any(), any(), any(), any(), any())(any(), any(), any())
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
            .getJourneyUrl(any(), callbackCaptor.capture(), any(), optNameCaptor.capture(), any())(any(), any(), any())

          callbackCaptor.getValue.url mustBe
            controllers.add.routes.AddressOfSubcontractorController.addressLookupCallbackChange().url
          optNameCaptor.getValue mustBe Some(subcontractorName)
        }
      }

      "must redirect to Journey Recovery when no session data is found" in {

        val mockSessionRepository    = mock[SessionRepository]
        val mockAddressLookupService = mock[AddressLookupService]

        when(mockSessionRepository.get(any())) thenReturn Future.successful(None)

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

      "must redirect to Journey Recovery when ALF is unavailable" in {

        val mockAddressLookupService = mock[AddressLookupService]

        when(
          mockAddressLookupService.getJourneyUrl(any(), any(), any(), any(), any())(any(), any(), any())
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

      "must redirect to Journey Recovery when no subcontractor name can be resolved" in {

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
            .getJourneyUrl(any(), any(), any(), any(), any())(any(), any(), any())
        }
      }
    }

    "addressLookupCallback" - {

      "must retrieve and persist the address then redirect to Individual Choose Contact Details when the save succeeds" in {

        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(any())(any(), any())) thenReturn Future.successful(testAddress)
        when(mockAddressLookupService.saveAddressDetails(any(), any())(any(), any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[AddressLookupService].toInstance(mockAddressLookupService))
            .build()

        running(application) {
          val request = FakeRequest(GET, callbackRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe
            controllers.add.routes.IndividualChooseContactDetailsController.onPageLoad(NormalMode).url

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
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
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
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
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
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[AddressLookupService].toInstance(mockAddressLookupService))
            .build()

        running(application) {
          val request = FakeRequest(GET, callbackChangeRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe
            controllers.add.routes.CheckYourAnswersController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when ALF is unavailable" in {

        val mockAddressLookupService = mock[AddressLookupService]

        when(mockAddressLookupService.getAddressById(any())(any(), any())) thenReturn Future.failed(
          new RuntimeException("ALF unavailable")
        )

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
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
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
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
  }
}

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

package controllers.amend.trust

import base.SpecBase
import forms.amend.trust.AmendTrustRemoveDetailYesNoFormProvider
import models.address.Address
import models.UserAnswers
import models.amend.trust.AmendTrustRemoveDetail
import models.contact.ContactMethodOptions
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.amend.trust.AmendTrustRemoveDetailYesNoPage
import org.scalatestplus.mockito.MockitoSugar
import pages.add.trust.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.amend.trust.AmendTrustRemoveDetailYesNoView

import scala.concurrent.Future

class AmendTrustRemoveDetailYesNoControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new AmendTrustRemoveDetailYesNoFormProvider()
  val form         = formProvider()

  private val trustName = "Test Trust"
  private val address   = Address("line 1", postcode = Some("AA1 1AA"))

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(TrustNamePage, trustName).success.value

  private def uaWithNameAndDetail(
    detail: String
  ): UserAnswers = {

    val userAnswers =
      detail match {

        case "address" =>
          uaWithName
            .set(TrustAddressPage, address)
            .success
            .value
            .set(TrustAddressYesNoPage, true)
            .success
            .value

        case "contact-details" =>
          uaWithName
            .set(
              TrustContactMethodOptionsPage,
              Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
            )
            .success
            .value
            .set(TrustEmailAddressPage, "old@email.com")
            .success
            .value
            .set(TrustPhoneNumberPage, "01234567890")
            .success
            .value
            .set(TrustMobileNumberPage, "07123456789")
            .success
            .value
            .set(AddTrustContactMethodsYesNoPage, true)
            .success
            .value

        case "utr" =>
          uaWithName
            .set(TrustUtrPage, "7777777777")
            .success
            .value
            .set(TrustUtrYesNoPage, true)
            .success
            .value

        case "works-reference-number" =>
          uaWithName
            .set(TrustWorksReferencePage, "WR-001")
            .success
            .value
            .set(TrustWorksReferenceYesNoPage, true)
            .success
            .value
      }

    userAnswers
  }

  private def assertDetailWasRemoved(
    userAnswers: UserAnswers,
    detail: AmendTrustRemoveDetail
  ): Unit =
    detail match {
      case AmendTrustRemoveDetail.Address =>
        userAnswers.get(TrustAddressPage) mustBe None
        userAnswers.get(TrustAddressYesNoPage) mustBe Some(false)

      case AmendTrustRemoveDetail.ContactDetails =>
        userAnswers.get(TrustContactMethodOptionsPage) mustBe None
        userAnswers.get(TrustEmailAddressPage) mustBe None
        userAnswers.get(TrustPhoneNumberPage) mustBe None
        userAnswers.get(TrustMobileNumberPage) mustBe None
        userAnswers.get(AddTrustContactMethodsYesNoPage) mustBe Some(false)

      case AmendTrustRemoveDetail.Utr =>
        userAnswers.get(TrustUtrPage) mustBe None
        userAnswers.get(TrustUtrYesNoPage) mustBe Some(false)

      case AmendTrustRemoveDetail.WorksReferenceNumber =>
        userAnswers.get(TrustWorksReferencePage) mustBe None
        userAnswers.get(TrustWorksReferenceYesNoPage) mustBe Some(false)
    }

  private def assertDetailWasRetained(
    userAnswers: UserAnswers,
    detail: AmendTrustRemoveDetail
  ): Unit =
    detail match {
      case AmendTrustRemoveDetail.Address =>
        userAnswers.get(TrustAddressPage) mustBe Some(address)
        userAnswers.get(TrustAddressYesNoPage) mustBe Some(true)

      case AmendTrustRemoveDetail.ContactDetails =>
        userAnswers.get(TrustContactMethodOptionsPage) mustBe Some(
          Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
        )
        userAnswers.get(TrustEmailAddressPage) mustBe Some("old@email.com")
        userAnswers.get(TrustPhoneNumberPage) mustBe Some("01234567890")
        userAnswers.get(TrustMobileNumberPage) mustBe Some("07123456789")
        userAnswers.get(AddTrustContactMethodsYesNoPage) mustBe Some(true)

      case AmendTrustRemoveDetail.Utr =>
        userAnswers.get(TrustUtrPage) mustBe Some("7777777777")
        userAnswers.get(TrustUtrYesNoPage) mustBe Some(true)

      case AmendTrustRemoveDetail.WorksReferenceNumber =>
        userAnswers.get(TrustWorksReferencePage) mustBe Some("WR-001")
        userAnswers.get(TrustWorksReferenceYesNoPage) mustBe Some(true)
    }

  "AmendTrustRemoveDetailYesNo Controller" - {
    Seq(
      ("address", "address"),
      ("contact-details", "contact-details"),
      ("unique-taxpayer-reference", "utr"),
      ("works-reference-number", "works-reference-number")
    ).foreach { case (subcontractorDetail, selectedDetail) =>
      s"when contractorDetail is '$subcontractorDetail'" - {
        val form = formProvider()

        val detailType =
          AmendTrustRemoveDetail
            .fromKey(selectedDetail)
            .value

        lazy val removeDetailYesNoRoute =
          controllers.amend.trust.routes.AmendTrustRemoveDetailYesNoController.onPageLoad(selectedDetail).url

        "must return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(uaWithNameAndDetail(selectedDetail))).build()

          val detailTitle =
            messages(application)(detailType.messageKey)

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[AmendTrustRemoveDetailYesNoView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(trustName, selectedDetail, detailTitle, form)(
              request,
              messages(application)
            ).toString
          }
        }

        "must redirect to the next page when valid data with value Yes is submitted" in {

          val mockSessionRepository = mock[SessionRepository]
          val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(uaWithNameAndDetail(selectedDetail)))
              .overrides(
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, removeDetailYesNoRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.amend.trust.routes.AmendTrustCheckYourAnswersController
              .onPageLoad()
              .url

            verify(mockSessionRepository).set(captor.capture())
            val savedAnswers = captor.getValue

            assertDetailWasRemoved(savedAnswers, detailType)
            savedAnswers.get(AmendTrustRemoveDetailYesNoPage(detailType)) mustBe None
          }
        }

        "must redirect to the next page when valid data with value No is submitted" in {

          val mockSessionRepository = mock[SessionRepository]
          val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(uaWithNameAndDetail(selectedDetail)))
              .overrides(
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, removeDetailYesNoRoute)
                .withFormUrlEncodedBody(("value", "false"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.amend.trust.routes.AmendTrustCheckYourAnswersController
              .onPageLoad()
              .url

            verify(mockSessionRepository).set(captor.capture())
            val savedAnswers = captor.getValue

            assertDetailWasRetained(savedAnswers, detailType)
            savedAnswers.get(AmendTrustRemoveDetailYesNoPage(detailType)) mustBe None
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          val application = applicationBuilder(userAnswers = Some(uaWithNameAndDetail(selectedDetail))).build()

          val detailTitle =
            messages(application)(detailType.messageKey)

          running(application) {
            val request =
              FakeRequest(POST, removeDetailYesNoRoute)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[AmendTrustRemoveDetailYesNoView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(trustName, selectedDetail, detailTitle, boundForm)(
              request,
              messages(application)
            ).toString
          }
        }

        "must redirect to Journey Recovery for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to Journey Recovery for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, removeDetailYesNoRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to JourneyRecovery if trustName is missing for a GET" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to JourneyRecovery if trustName is missing for a POST" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request =
              FakeRequest(POST, removeDetailYesNoRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to Journey Recovery for a GET if no detail data is found" in {

          val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to JourneyRecovery if detail data is missing for a POST" in {

          val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

          running(application) {
            val request =
              FakeRequest(POST, removeDetailYesNoRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to the JourneyRecovery when failed to save remove detail answer in session" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())).thenReturn(
            Future.failed(new RuntimeException(s"Failed to save remove detail answer for '$subcontractorDetail'"))
          )

          val application =
            applicationBuilder(userAnswers = Some(uaWithNameAndDetail(selectedDetail)))
              .overrides(
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, removeDetailYesNoRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }

    "when contractorDetail is neither 'address', 'contact-details', 'unique-taxpayer-reference' or 'works-reference-number'" - {

      "must redirect to Journey Recovery on GET" in {

        val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              controllers.amend.trust.routes.AmendTrustRemoveDetailYesNoController.onPageLoad("invalid").url
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery on POST" in {

        val application =
          applicationBuilder(userAnswers = Some(uaWithName)).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              controllers.amend.trust.routes.AmendTrustRemoveDetailYesNoController.onSubmit("invalid").url
            )
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}

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

package controllers.amend

import base.SpecBase
import forms.amend.AmendIndividualRemoveDetailYesNoFormProvider
import models.address.Address
import models.add.SubcontractorName
import models.amend.AmendIndividualRemoveDetail
import models.{AmendMode, UserAnswers}
import models.contact.ContactMethodOptions
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.*
import pages.amend.AmendIndividualRemoveDetailYesNoPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.amend.AmendIndividualRemoveDetailYesNoView

import scala.concurrent.Future

class AmendIndividualRemoveDetailYesNoControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new AmendIndividualRemoveDetailYesNoFormProvider()
  val form         = formProvider()

  private val subcontractorTradingName = "Test individual"
  private val address                  = Address("line 1", postcode = Some("AA1 1AA"))

  private def uaWithTradingName: UserAnswers =
    emptyUserAnswers.set(TradingNameOfSubcontractorPage, subcontractorTradingName).success.value

  private def uaWithTradingNameAndDetail(
    detail: String
  ): UserAnswers = {

    val userAnswers =
      detail match {

        case "trading-name" =>
          emptyUserAnswers
            .set(SubTradingNameYesNoPage, false)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, subcontractorTradingName)
            .success
            .value

        case "address" =>
          uaWithTradingName
            .set(AddressOfSubcontractorPage, address)
            .success
            .value
            .set(SubAddressYesNoPage, true)
            .success
            .value

        case "contact-details" =>
          uaWithTradingName
            .set(
              IndividualContactMethodOptionsPage,
              Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
            )
            .success
            .value
            .set(IndividualEmailAddressPage, "old@email.com")
            .success
            .value
            .set(IndividualPhoneNumberPage, "01234567890")
            .success
            .value
            .set(IndividualMobileNumberPage, "07123456789")
            .success
            .value
            .set(AddIndividualContactMethodsYesNoPage, true)
            .success
            .value

        case "utr" =>
          uaWithTradingName
            .set(SubcontractorsUniqueTaxpayerReferencePage, "7777777777")
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, true)
            .success
            .value

        case "national-insurance-number" =>
          uaWithTradingName
            .set(SubNationalInsuranceNumberPage, "AA123457A")
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, true)
            .success
            .value

        case "works-reference-number" =>
          uaWithTradingName
            .set(WorksReferenceNumberPage, "WR-001")
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, true)
            .success
            .value
      }

    userAnswers
  }

  private def assertDetailWasRemoved(
    userAnswers: UserAnswers,
    detail: AmendIndividualRemoveDetail
  ): Unit =
    detail match {
      case AmendIndividualRemoveDetail.TradingName =>
        userAnswers.get(TradingNameOfSubcontractorPage) mustBe None
        userAnswers.get(SubTradingNameYesNoPage) mustBe Some(false)

      case AmendIndividualRemoveDetail.SubcontractorName =>
        userAnswers.get(SubcontractorNamePage) mustBe None
        userAnswers.get(SubTradingNameYesNoPage) mustBe Some(true)

      case AmendIndividualRemoveDetail.Address =>
        userAnswers.get(AddressOfSubcontractorPage) mustBe None
        userAnswers.get(SubAddressYesNoPage) mustBe Some(false)

      case AmendIndividualRemoveDetail.ContactDetails =>
        userAnswers.get(IndividualContactMethodOptionsPage) mustBe None
        userAnswers.get(IndividualEmailAddressPage) mustBe None
        userAnswers.get(IndividualPhoneNumberPage) mustBe None
        userAnswers.get(IndividualMobileNumberPage) mustBe None
        userAnswers.get(AddIndividualContactMethodsYesNoPage) mustBe Some(false)

      case AmendIndividualRemoveDetail.Utr =>
        userAnswers.get(SubcontractorsUniqueTaxpayerReferencePage) mustBe None
        userAnswers.get(UniqueTaxpayerReferenceYesNoPage) mustBe Some(false)

      case AmendIndividualRemoveDetail.NationalInsuranceNumber =>
        userAnswers.get(SubNationalInsuranceNumberPage) mustBe None
        userAnswers.get(NationalInsuranceNumberYesNoPage) mustBe Some(false)

      case AmendIndividualRemoveDetail.WorksReferenceNumber =>
        userAnswers.get(WorksReferenceNumberPage) mustBe None
        userAnswers.get(WorksReferenceNumberYesNoPage) mustBe Some(false)
    }

  private def assertDetailWasRetained(
    userAnswers: UserAnswers,
    detail: AmendIndividualRemoveDetail
  ): Unit =
    detail match {

      case AmendIndividualRemoveDetail.TradingName =>
        userAnswers.get(TradingNameOfSubcontractorPage) mustBe Some(subcontractorTradingName)
        userAnswers.get(SubTradingNameYesNoPage) mustBe Some(true)

      case AmendIndividualRemoveDetail.SubcontractorName =>
        userAnswers.get(SubcontractorNamePage) mustBe Some(SubcontractorName("John", Some("Paul"), "Smith"))
        userAnswers.get(SubTradingNameYesNoPage) mustBe Some(false)

      case AmendIndividualRemoveDetail.Address =>
        userAnswers.get(AddressOfSubcontractorPage) mustBe Some(address)
        userAnswers.get(SubAddressYesNoPage) mustBe Some(true)

      case AmendIndividualRemoveDetail.ContactDetails =>
        userAnswers.get(IndividualContactMethodOptionsPage) mustBe Some(
          Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
        )
        userAnswers.get(IndividualEmailAddressPage) mustBe Some("old@email.com")
        userAnswers.get(IndividualPhoneNumberPage) mustBe Some("01234567890")
        userAnswers.get(IndividualMobileNumberPage) mustBe Some("07123456789")
        userAnswers.get(AddIndividualContactMethodsYesNoPage) mustBe Some(true)

      case AmendIndividualRemoveDetail.Utr =>
        userAnswers.get(SubcontractorsUniqueTaxpayerReferencePage) mustBe Some("7777777777")
        userAnswers.get(UniqueTaxpayerReferenceYesNoPage) mustBe Some(true)

      case AmendIndividualRemoveDetail.NationalInsuranceNumber =>
        userAnswers.get(SubNationalInsuranceNumberPage) mustBe Some("AA123457A")
        userAnswers.get(NationalInsuranceNumberYesNoPage) mustBe Some(true)

      case AmendIndividualRemoveDetail.WorksReferenceNumber =>
        userAnswers.get(WorksReferenceNumberPage) mustBe Some("WR-001")
        userAnswers.get(WorksReferenceNumberYesNoPage) mustBe Some(true)
    }

  private def uaWithSubcontractorNameAndDetail: UserAnswers =
    emptyUserAnswers
      .set(SubTradingNameYesNoPage, true)
      .success
      .value
      .set(SubcontractorNamePage, SubcontractorName("John", Some("Paul"), "Smith"))
      .success
      .value

  private def uaWithSubcontractorName: UserAnswers =
    emptyUserAnswers
      .set(SubcontractorNamePage, SubcontractorName("John", Some("Paul"), "Smith"))
      .success
      .value

  "AmendIndividualRemoveDetailYesNo Controller" - {
    Seq(
      ("address", "address"),
      ("contact-details", "contact-details"),
      ("utr", "utr"),
      ("national-insurance-number", "national-insurance-number"),
      ("works-reference-number", "works-reference-number")
    ).foreach { case (subcontractorDetail, selectedDetail) =>
      s"when subcontractorDetail is '$subcontractorDetail'" - {
        val form = formProvider()

        val detailType =
          AmendIndividualRemoveDetail
            .fromKey(selectedDetail)
            .value

        lazy val removeDetailYesNoRoute =
          controllers.amend.routes.AmendIndividualRemoveDetailYesNoController.onPageLoad(selectedDetail).url

        "must return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(uaWithTradingNameAndDetail(selectedDetail))).build()

          val detailTitle =
            messages(application)(detailType.messageKey)

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[AmendIndividualRemoveDetailYesNoView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(subcontractorTradingName, selectedDetail, detailTitle, form)(
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
            applicationBuilder(userAnswers = Some(uaWithTradingNameAndDetail(selectedDetail)))
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
            redirectLocation(result).value mustEqual controllers.amend.routes.AmendIndividualCheckYourAnswersController
              .onPageLoad()
              .url

            verify(mockSessionRepository).set(captor.capture())
            val savedAnswers = captor.getValue

            assertDetailWasRemoved(savedAnswers, detailType)
            savedAnswers.get(AmendIndividualRemoveDetailYesNoPage(detailType)) mustBe None
          }
        }

        "must redirect to the next page when valid data with value No is submitted" in {

          val mockSessionRepository = mock[SessionRepository]
          val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(uaWithTradingNameAndDetail(selectedDetail)))
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
            redirectLocation(result).value mustEqual controllers.amend.routes.AmendIndividualCheckYourAnswersController
              .onPageLoad()
              .url

            verify(mockSessionRepository).set(captor.capture())
            val savedAnswers = captor.getValue

            assertDetailWasRetained(savedAnswers, detailType)
            savedAnswers.get(AmendIndividualRemoveDetailYesNoPage(detailType)) mustBe None
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          val application = applicationBuilder(userAnswers = Some(uaWithTradingNameAndDetail(selectedDetail))).build()

          val detailTitle =
            messages(application)(detailType.messageKey)

          running(application) {
            val request =
              FakeRequest(POST, removeDetailYesNoRoute)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[AmendIndividualRemoveDetailYesNoView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(subcontractorTradingName, selectedDetail, detailTitle, boundForm)(
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

        "must redirect to JourneyRecovery if subcontractorTradingName is missing for a GET" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to JourneyRecovery if subcontractorTradingName is missing for a POST" in {

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

          val application = applicationBuilder(userAnswers = Some(uaWithTradingName)).build()

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to JourneyRecovery if detail data is missing for a POST" in {

          val application = applicationBuilder(userAnswers = Some(uaWithTradingName)).build()

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
            Future.failed(new RuntimeException(s"\"Failed to save remove detail answer for '$subcontractorDetail'\""))
          )

          val application =
            applicationBuilder(userAnswers = Some(uaWithTradingNameAndDetail(selectedDetail)))
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

    "when subcontractorDetail is subcontractor-name" - {
      val form = formProvider()

      val subcontractorName = "John Smith"

      val selectedDetail = "subcontractor-name"

      val detailType =
        AmendIndividualRemoveDetail
          .fromKey(selectedDetail)
          .value

      lazy val removeDetailYesNoRoute =
        controllers.amend.routes.AmendIndividualRemoveDetailYesNoController.onPageLoad("subcontractor-name").url

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(uaWithSubcontractorNameAndDetail)).build()

        val detailTitle =
          messages(application)(detailType.messageKey)

        running(application) {
          val request = FakeRequest(GET, removeDetailYesNoRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AmendIndividualRemoveDetailYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(subcontractorName, selectedDetail, detailTitle, form)(
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
          applicationBuilder(userAnswers = Some(uaWithSubcontractorNameAndDetail))
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
          redirectLocation(result).value mustEqual controllers.add.routes.TradingNameOfSubcontractorController
            .onPageLoad(AmendMode)
            .url

          verify(mockSessionRepository).set(captor.capture())
          val savedAnswers = captor.getValue

          assertDetailWasRemoved(savedAnswers, AmendIndividualRemoveDetail.SubcontractorName)
          savedAnswers.get(AmendIndividualRemoveDetailYesNoPage(detailType)) mustBe None
        }
      }

      "must redirect to the next page when valid data with value No is submitted" in {

        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(uaWithSubcontractorNameAndDetail))
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
          redirectLocation(result).value mustEqual controllers.amend.routes.AmendIndividualCheckYourAnswersController
            .onPageLoad()
            .url

          verify(mockSessionRepository).set(captor.capture())
          val savedAnswers = captor.getValue

          assertDetailWasRetained(savedAnswers, AmendIndividualRemoveDetail.SubcontractorName)
          savedAnswers.get(AmendIndividualRemoveDetailYesNoPage(detailType)) mustBe None
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(uaWithSubcontractorNameAndDetail)).build()

        val detailTitle =
          messages(application)(detailType.messageKey)

        running(application) {
          val request =
            FakeRequest(POST, removeDetailYesNoRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[AmendIndividualRemoveDetailYesNoView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(subcontractorName, selectedDetail, detailTitle, boundForm)(
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

      "must redirect to JourneyRecovery if subcontractorName is missing for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, removeDetailYesNoRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery if subcontractorName is missing for a POST" in {

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

      "must redirect to JourneyRecovery if detail is missing for a GET" in {

        val application = applicationBuilder(userAnswers = Some(uaWithSubcontractorName)).build()

        running(application) {
          val request = FakeRequest(GET, removeDetailYesNoRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery if detail is missing for a POST" in {

        val application = applicationBuilder(userAnswers = Some(uaWithSubcontractorName)).build()

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
          Future.failed(new RuntimeException("Failed to save remove detail answer for subcontractor-name"))
        )

        val application =
          applicationBuilder(userAnswers = Some(uaWithSubcontractorNameAndDetail))
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

    "when subcontractorDetail is trading-name" - {
      val form = formProvider()

      val selectedDetail = "trading-name"

      lazy val removeDetailYesNoRoute =
        controllers.amend.routes.AmendIndividualRemoveDetailYesNoController.onPageLoad(selectedDetail).url

      val detailType =
        AmendIndividualRemoveDetail
          .fromKey(selectedDetail)
          .value

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(uaWithTradingNameAndDetail(selectedDetail))).build()

        val detailTitle =
          messages(application)(detailType.messageKey)

        running(application) {
          val request = FakeRequest(GET, removeDetailYesNoRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AmendIndividualRemoveDetailYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(subcontractorTradingName, selectedDetail, detailTitle, form)(
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
          applicationBuilder(userAnswers = Some(uaWithTradingNameAndDetail(selectedDetail)))
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
          redirectLocation(result).value mustEqual controllers.add.routes.SubcontractorNameController
            .onPageLoad(AmendMode)
            .url

          verify(mockSessionRepository).set(captor.capture())
          val savedAnswers = captor.getValue

          assertDetailWasRemoved(savedAnswers, AmendIndividualRemoveDetail.TradingName)
          savedAnswers.get(AmendIndividualRemoveDetailYesNoPage(detailType)) mustBe None
        }
      }

      "must redirect to the next page when valid data with value No is submitted" in {

        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(uaWithTradingNameAndDetail(selectedDetail)))
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
          redirectLocation(result).value mustEqual controllers.amend.routes.AmendIndividualCheckYourAnswersController
            .onPageLoad()
            .url

          verify(mockSessionRepository).set(captor.capture())
          val savedAnswers = captor.getValue

          assertDetailWasRetained(savedAnswers, AmendIndividualRemoveDetail.TradingName)
          savedAnswers.get(AmendIndividualRemoveDetailYesNoPage(detailType)) mustBe None
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(uaWithTradingNameAndDetail(selectedDetail))).build()

        val detailTitle =
          messages(application)(detailType.messageKey)

        running(application) {
          val request =
            FakeRequest(POST, removeDetailYesNoRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[AmendIndividualRemoveDetailYesNoView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(subcontractorTradingName, selectedDetail, detailTitle, boundForm)(
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

      "must redirect to JourneyRecovery if subcontractorTradingName is missing for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, removeDetailYesNoRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery if subcontractorTradingName is missing for a POST" in {

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

      "must redirect to JourneyRecovery if detail is missing for a GET" in {

        val application = applicationBuilder(userAnswers = Some(uaWithTradingName)).build()

        running(application) {
          val request = FakeRequest(GET, removeDetailYesNoRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery if detail is missing for a POST" in {

        val application = applicationBuilder(userAnswers = Some(uaWithTradingName)).build()

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
          Future.failed(new RuntimeException("Failed to save remove detail answer for trading-name"))
        )

        val application =
          applicationBuilder(userAnswers = Some(uaWithTradingNameAndDetail(selectedDetail)))
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

    "when subcontractorDetail is neither 'subcontractor-name', 'trading-name', 'address', 'contact-details', 'unique-taxpayer-reference' or 'works-reference-number'" - {

      "must redirect to Journey Recovery on GET" in {

        val application = applicationBuilder(userAnswers = Some(uaWithSubcontractorNameAndDetail)).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              controllers.amend.routes.AmendIndividualRemoveDetailYesNoController.onPageLoad("invalid").url
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery on POST" in {

        val application =
          applicationBuilder(userAnswers = Some(uaWithSubcontractorNameAndDetail)).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              controllers.amend.routes.AmendIndividualRemoveDetailYesNoController.onSubmit("invalid").url
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

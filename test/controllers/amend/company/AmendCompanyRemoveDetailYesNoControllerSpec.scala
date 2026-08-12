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

package controllers.amend.company

import base.SpecBase
import forms.amend.company.AmendCompanyRemoveDetailYesNoFormProvider
import models.amend.company.AmendCompanyRemoveDetail
import models.UserAnswers
import models.address.Address
import models.contact.ContactMethodOptions
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.company.*
import pages.amend.company.AmendCompanyRemoveDetailYesNoPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.amend.company.AmendCompanyRemoveDetailYesNoView

import scala.concurrent.Future

class AmendCompanyRemoveDetailYesNoControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new AmendCompanyRemoveDetailYesNoFormProvider()

  private val companyName = "Test Company"
  private val address     = Address("line 1", postcode = Some("AA1 1AA"))

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(CompanyNamePage, companyName).success.value

  private def uaWithNameAndDetail(
    detail: String
  ): UserAnswers = {

    val userAnswers =
      detail match {

        case "address" =>
          uaWithName
            .set(CompanyAddressPage, address)
            .success
            .value
            .set(CompanyAddressYesNoPage, true)
            .success
            .value

        case "contact-details" =>
          uaWithName
            .set(
              CompanyContactMethodOptionsPage,
              Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
            )
            .success
            .value
            .set(CompanyEmailAddressPage, "old@email.com")
            .success
            .value
            .set(CompanyPhoneNumberPage, "01234567890")
            .success
            .value
            .set(CompanyMobileNumberPage, "07123456789")
            .success
            .value
            .set(AddCompanyContactMethodsYesNoPage, true)
            .success
            .value

        case "utr" =>
          uaWithName
            .set(CompanyUtrPage, "7777777777")
            .success
            .value
            .set(CompanyUtrYesNoPage, true)
            .success
            .value

        case "company-registration-number" =>
          uaWithName
            .set(CompanyCrnPage, "AA1234567A")
            .success
            .value
            .set(CompanyCrnYesNoPage, true)
            .success
            .value

        case "works-reference-number" =>
          uaWithName
            .set(CompanyWorksReferencePage, "WR-001")
            .success
            .value
            .set(CompanyWorksReferenceYesNoPage, true)
            .success
            .value
      }

    userAnswers
  }

  private def assertDetailWasRemoved(
    userAnswers: UserAnswers,
    detail: AmendCompanyRemoveDetail
  ): Unit =
    detail match {
      case AmendCompanyRemoveDetail.Address =>
        userAnswers.get(CompanyAddressPage) mustBe None
        userAnswers.get(CompanyAddressYesNoPage) mustBe Some(false)

      case AmendCompanyRemoveDetail.ContactDetails =>
        userAnswers.get(CompanyContactMethodOptionsPage) mustBe None
        userAnswers.get(CompanyEmailAddressPage) mustBe None
        userAnswers.get(CompanyPhoneNumberPage) mustBe None
        userAnswers.get(CompanyMobileNumberPage) mustBe None
        userAnswers.get(AddCompanyContactMethodsYesNoPage) mustBe Some(false)

      case AmendCompanyRemoveDetail.Utr =>
        userAnswers.get(CompanyUtrPage) mustBe None
        userAnswers.get(CompanyUtrYesNoPage) mustBe Some(false)

      case AmendCompanyRemoveDetail.CompanyRegistrationNumber =>
        userAnswers.get(CompanyCrnPage) mustBe None
        userAnswers.get(CompanyCrnYesNoPage) mustBe Some(false)

      case AmendCompanyRemoveDetail.WorksReferenceNumber =>
        userAnswers.get(CompanyWorksReferencePage) mustBe None
        userAnswers.get(CompanyWorksReferenceYesNoPage) mustBe Some(false)
    }

  private def assertDetailWasRetained(
    userAnswers: UserAnswers,
    detail: AmendCompanyRemoveDetail
  ): Unit =
    detail match {
      case AmendCompanyRemoveDetail.Address =>
        userAnswers.get(CompanyAddressPage) mustBe Some(address)
        userAnswers.get(CompanyAddressYesNoPage) mustBe Some(true)

      case AmendCompanyRemoveDetail.ContactDetails =>
        userAnswers.get(CompanyContactMethodOptionsPage) mustBe Some(
          Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
        )
        userAnswers.get(CompanyEmailAddressPage) mustBe Some("old@email.com")
        userAnswers.get(CompanyPhoneNumberPage) mustBe Some("01234567890")
        userAnswers.get(CompanyMobileNumberPage) mustBe Some("07123456789")
        userAnswers.get(AddCompanyContactMethodsYesNoPage) mustBe Some(true)

      case AmendCompanyRemoveDetail.Utr =>
        userAnswers.get(CompanyUtrPage) mustBe Some("7777777777")
        userAnswers.get(CompanyUtrYesNoPage) mustBe Some(true)

      case AmendCompanyRemoveDetail.CompanyRegistrationNumber =>
        userAnswers.get(CompanyCrnPage) mustBe Some("AA1234567A")
        userAnswers.get(CompanyCrnYesNoPage) mustBe Some(true)

      case AmendCompanyRemoveDetail.WorksReferenceNumber =>
        userAnswers.get(CompanyWorksReferencePage) mustBe Some("WR-001")
        userAnswers.get(CompanyWorksReferenceYesNoPage) mustBe Some(true)
    }

  "AmendCompanyRemoveDetailYesNo Controller" - {

    Seq(
      ("address", "address"),
      ("contact-details", "contact-details"),
      ("utr", "utr"),
      ("company-registration-number", "company-registration-number"),
      ("works-reference-number", "works-reference-number")
    ).foreach { case (subcontractorDetail, selectedDetail) =>
      s"when subcontractorDetail is '$subcontractorDetail'" - {
        val form = formProvider()

        val detailType =
          AmendCompanyRemoveDetail
            .fromKey(selectedDetail)
            .value

        lazy val removeDetailYesNoRoute =
          controllers.amend.company.routes.AmendCompanyRemoveDetailYesNoController.onPageLoad(selectedDetail).url

        "must return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(uaWithNameAndDetail(selectedDetail))).build()

          val detailTitle =
            messages(application)(detailType.messageKey)

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[AmendCompanyRemoveDetailYesNoView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(companyName, selectedDetail, detailTitle, form)(
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
            redirectLocation(
              result
            ).value mustEqual controllers.amend.company.routes.AmendCompanyCheckYourAnswersController
              .onPageLoad()
              .url

            verify(mockSessionRepository).set(captor.capture())
            val savedAnswers = captor.getValue

            assertDetailWasRemoved(savedAnswers, detailType)
            savedAnswers.get(AmendCompanyRemoveDetailYesNoPage(detailType)) mustBe None
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
            redirectLocation(
              result
            ).value mustEqual controllers.amend.company.routes.AmendCompanyCheckYourAnswersController
              .onPageLoad()
              .url

            verify(mockSessionRepository).set(captor.capture())
            val savedAnswers = captor.getValue

            assertDetailWasRetained(savedAnswers, detailType)
            savedAnswers.get(AmendCompanyRemoveDetailYesNoPage(detailType)) mustBe None
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

            val view = application.injector.instanceOf[AmendCompanyRemoveDetailYesNoView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(companyName, selectedDetail, detailTitle, boundForm)(
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

        "must redirect to JourneyRecovery if CompanyName is missing for a GET" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to JourneyRecovery if CompanyName is missing for a POST" in {

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

    "when subcontractorDetail is neither 'address', 'contact-details', 'utr', 'company-registration-number' or 'works-reference-number'" - {

      "must redirect to Journey Recovery on GET" in {

        val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              controllers.amend.company.routes.AmendCompanyRemoveDetailYesNoController.onPageLoad("invalid").url
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
              controllers.amend.company.routes.AmendCompanyRemoveDetailYesNoController.onSubmit("invalid").url
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

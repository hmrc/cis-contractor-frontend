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

package controllers.add

import base.SpecBase
import controllers.routes
import forms.add.SubcontractorNameFormProvider
import models.{AmendMode, NormalMode, UserAnswers}
import models.add.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.{IndividualNamesOptionsPage, SubcontractorNamePage}
import pages.amend.ShowVerificationDetailsPage
import play.api.inject.bind
import play.api.libs.json.{Json, OFormat}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.SubcontractorNameView

import scala.concurrent.Future

class SubcontractorNameControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new SubcontractorNameFormProvider()
  private val form = formProvider()

  private lazy val subcontractorNameRoute =
    controllers.add.routes.SubcontractorNameController.onPageLoad(NormalMode).url

  private lazy val subcontractorNameAmendRoute =
    controllers.add.routes.SubcontractorNameController.onPageLoad(AmendMode).url

  private def uaWithSubcontractorNameOption: UserAnswers =
    emptyUserAnswers
      .set(IndividualNamesOptionsPage, Set(IndividualNamesOptions.SubcontractorName))
      .success
      .value

  "SubcontractorName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(uaWithSubcontractorNameOption)).build()

      running(application) {
        val request = FakeRequest(GET, subcontractorNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubcontractorNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      implicit val subcontractorNameFormat: OFormat[SubcontractorName] =
        Json.format[SubcontractorName]

      val validName = SubcontractorName("John", Some("Paul"), "Smith")

      val userAnswers = uaWithSubcontractorNameOption.set(SubcontractorNamePage, validName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, subcontractorNameRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("value=\"John\"")
        contentAsString(result) must include("value=\"Paul\"")
        contentAsString(result) must include("value=\"Smith\"")
      }
    }

    "must redirect to the SubAddressYesNo page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(uaWithSubcontractorNameOption))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorNameRoute)
            .withFormUrlEncodedBody(
              "firstName"  -> "John",
              "middleName" -> "Paul",
              "lastName"   -> "Smith"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.SubAddressYesNoController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must redirect to TradingNameOfSubcontractorPage for a Post when TradingName is selected" in {

      def uaWithBothNamesOptions: UserAnswers =
        emptyUserAnswers
          .set(
            IndividualNamesOptionsPage,
            Set(IndividualNamesOptions.SubcontractorName, IndividualNamesOptions.TradingName)
          )
          .success
          .value

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(uaWithBothNamesOptions))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorNameRoute)
            .withFormUrlEncodedBody(
              "firstName"  -> "John",
              "middleName" -> "Paul",
              "lastName"   -> "Smith"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.TradingNameOfSubcontractorController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorNameRoute)
            .withFormUrlEncodedBody(
              "firstName"  -> "",
              "middleName" -> "1Paul",
              "lastName"   -> ""
            )

        val boundForm = form.bind(
          Map("firstName" -> "", "middleName" -> "1Paul", "lastName" -> "")
        )

        val view = application.injector.instanceOf[SubcontractorNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, subcontractorNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorNameRoute)
            .withFormUrlEncodedBody(
              "firstName"  -> "John",
              "middleName" -> "Paul",
              "lastName"   -> "Smith"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when subcontractor is verified" in {

      val userAnswers =
        emptyUserAnswers.set(ShowVerificationDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, subcontractorNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a Post when subcontractor is verified" in {
      val userAnswers =
        emptyUserAnswers.set(ShowVerificationDetailsPage, true).success.value

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorNameRoute)
            .withFormUrlEncodedBody(
              "firstName"  -> "John",
              "middleName" -> "Paul",
              "lastName"   -> "Smith"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the correct view for a GET when subcontractor is unverified in amend mode" in {

      val userAnswers =
        emptyUserAnswers
          .set(ShowVerificationDetailsPage, false)
          .success
          .value
          .set(IndividualNamesOptionsPage, Set(IndividualNamesOptions.SubcontractorName))
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, subcontractorNameAmendRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubcontractorNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, AmendMode)(request, messages(application)).toString
      }
    }

    "must redirect to the AmendIndividualCheckYourAnswers page when valid data is submitted when subcontractor is unverified in amend mode" in {
      val userAnswers =
        emptyUserAnswers.set(ShowVerificationDetailsPage, false).success.value

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorNameAmendRoute)
            .withFormUrlEncodedBody(
              "firstName"  -> "John",
              "middleName" -> "Paul",
              "lastName"   -> "Smith"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.amend.routes.AmendIndividualCheckYourAnswersController
          .onPageLoad()
          .url
      }
    }

    "must redirect to IndividualNamesOptionsPage for a GET when TradingName is not selected" in {

      def uaWithTradingNameOption: UserAnswers =
        emptyUserAnswers
          .set(IndividualNamesOptionsPage, Set(IndividualNamesOptions.TradingName))
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(uaWithTradingNameOption)).build()

      running(application) {
        val request = FakeRequest(GET, subcontractorNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.IndividualNamesOptionsController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must redirect to IndividualNamesOptionsPage for a GET when IndividualNamesOptions is missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, subcontractorNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.IndividualNamesOptionsController
          .onPageLoad(NormalMode)
          .url
      }
    }
  }
}

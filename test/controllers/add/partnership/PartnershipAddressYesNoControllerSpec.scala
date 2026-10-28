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

package controllers.add.partnership

import base.SpecBase
import controllers.routes
import forms.add.partnership.PartnershipAddressYesNoFormProvider
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.partnership.{PartnershipAddressYesNoPage, PartnershipNamePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.partnership.PartnershipAddressYesNoView

import scala.concurrent.Future

class PartnershipAddressYesNoControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider        = new PartnershipAddressYesNoFormProvider()
  val form: Form[Boolean] = formProvider()

  private val partnershipName = "Test Partnership"

  lazy val partnershipAddressYesNoRoute: String =
    controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(NormalMode).url

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(PartnershipNamePage, partnershipName).success.value

  "PartnershipAddressYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request = FakeRequest(GET, partnershipAddressYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnershipAddressYesNoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          partnershipName,
          controllers.add.partnership.routes.PartnershipAddressYesNoController.onSubmit(NormalMode)
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = uaWithName.set(PartnershipAddressYesNoPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnershipAddressYesNoRoute)

        val view = application.injector.instanceOf[PartnershipAddressYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(true),
          NormalMode,
          partnershipName,
          controllers.add.partnership.routes.PartnershipAddressYesNoController.onSubmit(NormalMode)
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockNavigator         = mock[Navigator]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockNavigator.nextPage(any(), any(), any())).thenReturn(onwardRoute)

      val application =
        applicationBuilder(userAnswers = Some(uaWithName))
          .overrides(
            bind[Navigator].toInstance(mockNavigator),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipAddressYesNoRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipAddressYesNoRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PartnershipAddressYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          partnershipName,
          controllers.add.partnership.routes.PartnershipAddressYesNoController.onSubmit(NormalMode)
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, partnershipAddressYesNoRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipAddressYesNoRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery if PartnershipName is missing for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnershipAddressYesNoRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery if PartnershipName is missing for a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipAddressYesNoRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return BadRequest when invalid data is submitted and name is present" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipAddressYesNoRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[PartnershipAddressYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            partnershipName,
            controllers.add.partnership.routes.PartnershipAddressYesNoController.onSubmit(NormalMode)
          )(request, messages(application)).toString
      }
    }

    "must return a Bad Request when no value is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipAddressYesNoRoute)
            .withFormUrlEncodedBody()

        val boundForm = form.bind(Map.empty)
        val view      = application.injector.instanceOf[PartnershipAddressYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            partnershipName,
            controllers.add.partnership.routes.PartnershipAddressYesNoController.onSubmit(NormalMode)
          )(request, messages(application)).toString
      }
    }

    "must return OK for a GET with id when the journey folder has a partnership name" in {
      val uuid    = java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")
      val id      = models.JourneyScope.subJourneyId(models.requests.CisPath.CisOrg, uuid)
      val answers = emptyUserAnswers
        .initialiseSubJourney(id)
        .success
        .value
        .set(PartnershipNamePage(id), partnershipName)
        .success
        .value
      val repo    = mockSessionRepository(Some(answers))

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[SessionRepository].toInstance(repo))
          .build()

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.add.partnership.routes.PartnershipAddressYesNoController
            .onPageLoadWithId(models.requests.CisPath.CisOrg, uuid, NormalMode)
            .url
        )

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must redirect to address lookup with id when yes is submitted" in {
      val uuid    = java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")
      val id      = models.JourneyScope.subJourneyId(models.requests.CisPath.CisOrg, uuid)
      val answers = emptyUserAnswers
        .initialiseSubJourney(id)
        .success
        .value
        .set(PartnershipNamePage(id), partnershipName)
        .success
        .value
      val repo    = mockSessionRepository(Some(answers))

      when(repo.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[SessionRepository].toInstance(repo))
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            controllers.add.partnership.routes.PartnershipAddressYesNoController
              .onSubmitWithId(models.requests.CisPath.CisOrg, uuid, NormalMode)
              .url
          ).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.add.partnership.routes.PartnershipAddressController
            .redirectToAddressLookupWithId(models.requests.CisPath.CisOrg, uuid, None)
            .url
      }
    }

    "must redirect to contact methods with id when no is submitted" in {
      val uuid    = java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")
      val id      = models.JourneyScope.subJourneyId(models.requests.CisPath.CisOrg, uuid)
      val answers = emptyUserAnswers
        .initialiseSubJourney(id)
        .success
        .value
        .set(PartnershipNamePage(id), partnershipName)
        .success
        .value
      val repo    = mockSessionRepository(Some(answers))

      when(repo.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[SessionRepository].toInstance(repo))
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            controllers.add.partnership.routes.PartnershipAddressYesNoController
              .onSubmitWithId(models.requests.CisPath.CisOrg, uuid, NormalMode)
              .url
          ).withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.add.partnership.routes.AddPartnershipContactMethodsYesNoController
            .onPageLoadWithId(models.requests.CisPath.CisOrg, uuid, NormalMode)
            .url
      }
    }
  }
}

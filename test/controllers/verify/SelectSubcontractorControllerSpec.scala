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

package controllers.verify

import base.SpecBase
import forms.verify.SelectSubcontractorFormProvider
import models.{NormalMode, SelectSubcontractor, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.Messages
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.SelectSubcontractorPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.PaginationService
import views.html.verify.SelectSubcontractorView

import scala.concurrent.Future

class SelectSubcontractorControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new SelectSubcontractorFormProvider()
  val form = formProvider()

  val paginationService = new PaginationService()

  def url(page: Int = 1) =
    controllers.verify.routes.SelectSubcontractorController.onPageLoad(NormalMode, page).url

  "SelectSubcontractor Controller" - {

    "must return OK and correct view for GET (page 1)" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, url(1))
        val result  = route(application, request).value

        implicit val msgs: Messages = messages(application)

        val view = application.injector.instanceOf[SelectSubcontractorView]

        val allItems = SelectSubcontractor.checkboxItems
        val paginationResult =
          paginationService.paginateCheckboxItems(allItems, 1, url(1))

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          paginationResult.paginatedData,
          paginationResult.paginationViewModel,
          1
        )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(SelectSubcontractorPage, SelectSubcontractor.values.toSet)
          .success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, url(1))
        val result = route(application, request).value

        implicit val msgs: Messages = messages(application)

        val view = application.injector.instanceOf[SelectSubcontractorView]

        val allItems = SelectSubcontractor.checkboxItems
        val paginationResult =
          paginationService.paginateCheckboxItems(allItems, 1, url(1))

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form.fill(SelectSubcontractor.values.toSet),
          NormalMode,
          paginationResult.paginatedData,
          paginationResult.paginationViewModel,
          1
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody(
              "value[0]" -> SelectSubcontractor.values.head.toString
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody("value" -> "invalid")

        val boundForm = form.bind(Map("value" -> "invalid"))

        val view = application.injector.instanceOf[SelectSubcontractorView]

        implicit val msgs: Messages = messages(application)

        val allItems = SelectSubcontractor.checkboxItems
        val paginationResult =
          paginationService.paginateCheckboxItems(allItems, 1, url(1))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          paginationResult.paginatedData,
          paginationResult.paginationViewModel,
          1
        )(request, messages(application)).toString
      }
    }

    "must support pagination (page 2)" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, url(2))
        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must render the page for a GET when no existing data is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, url(1))

      val result = route(application, request).value

      status(result) mustBe OK
    }

    "must return BadRequest for POST when no existing data and form has errors" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, url(1))
          .withFormUrlEncodedBody("value" -> "")

      val result = route(application, request).value

      status(result) mustBe BAD_REQUEST
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, url(1))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody(
              "value[0]" -> SelectSubcontractor.values.head.toString
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

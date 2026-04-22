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
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.SelectSubcontractorPage
import play.api.data.Forms.*
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.PaginationService
import views.html.verify.SelectSubcontractorView
import javax.inject.Inject

import scala.concurrent.Future

class SelectSubcontractorControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider                         = new SelectSubcontractorFormProvider()
  val form: Form[Set[SelectSubcontractor]] = formProvider()

  val paginationService = new PaginationService()

  def url(page: Int = 1): String =
    controllers.verify.routes.SelectSubcontractorController.onPageLoad(NormalMode, page).url

  "SelectSubcontractor Controller" - {

    "must return OK and correct view for GET (page 1)" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, url(1))
        val result  = route(application, request).value

        implicit val msgs: Messages = messages(application)

        val view = application.injector.instanceOf[SelectSubcontractorView]

        val allItems         = SelectSubcontractor.checkboxItems
        val paginationResult =
          paginationService.paginateCheckboxItems(allItems, 1)

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
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, url(1))
        val result  = route(application, request).value

        implicit val msgs: Messages = messages(application)

        val view = application.injector.instanceOf[SelectSubcontractorView]

        val allItems         = SelectSubcontractor.checkboxItems
        val paginationResult =
          paginationService.paginateCheckboxItems(allItems, 1)

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

        val allItems         = SelectSubcontractor.checkboxItems
        val paginationResult =
          paginationService.paginateCheckboxItems(allItems, 1)

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
        val result  = route(application, request).value

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

    "must redirect to target page and save selections when gotoPage field is present" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody(
              "value[0]" -> SelectSubcontractor.values.head.toString,
              "gotoPage" -> "2"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual url(2)
      }
    }

    "must redirect when gotoPage is present even if no selections exist anywhere" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody("gotoPage" -> "2")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual url(2)

        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())
        captor.getValue.get(SelectSubcontractorPage).value mustEqual Set.empty[SelectSubcontractor]
      }
    }

    "must redirect to target page when gotoPage is present and there are prior selections (even if current page submits none)" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val priorSelection: Set[SelectSubcontractor] = Set(SelectSubcontractor.BrodyMartin)
      val userAnswers                              =
        UserAnswers(userAnswersId)
          .set(SelectSubcontractorPage, priorSelection)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody("gotoPage" -> "2")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual url(2)
      }
    }

    "must redirect to next page when Continue is submitted on page 2 with no selections but prior page selections saved" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val page1Selection: Set[SelectSubcontractor] = Set(SelectSubcontractor.BrodyMartin)
      val userAnswers                              =
        UserAnswers(userAnswersId)
          .set(SelectSubcontractorPage, page1Selection)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, url(2))
            .withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must preserve selections from other pages when submitting from a given page" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val page1Selection: Set[SelectSubcontractor] = Set(SelectSubcontractor.BrodyMartin)
      val userAnswers                              =
        UserAnswers(userAnswersId)
          .set(SelectSubcontractorPage, page1Selection)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, url(2))
            .withFormUrlEncodedBody(
              "value[0]" -> SelectSubcontractor.EpsilonCarpentry.toString
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())
        captor.getValue.get(SelectSubcontractorPage).value mustEqual
          (page1Selection + SelectSubcontractor.EpsilonCarpentry)
      }
    }

    "must merge newly selected values with otherPageValues when mergedValues is empty and no gotoPage is provided (fold success path)" in {

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

        implicit val msgs: Messages = messages(application)

        val allItems = SelectSubcontractor.checkboxItems

        val page1 =
          paginationService.paginateCheckboxItems(allItems, 1)

        val page2 =
          paginationService.paginateCheckboxItems(allItems, 2)

        val page1Values: Set[SelectSubcontractor] =
          page1.paginatedData
            .flatMap(item => SelectSubcontractor.values.find(_.toString == item.value))
            .toSet

        val page2Values: Set[SelectSubcontractor] =
          page2.paginatedData
            .flatMap(item => SelectSubcontractor.values.find(_.toString == item.value))
            .toSet

        page2Values.nonEmpty mustBe true

        val otherPageSelection: Set[SelectSubcontractor] = Set(page2Values.head)

        val userAnswers =
          UserAnswers(userAnswersId)
            .set(SelectSubcontractorPage, otherPageSelection)
            .success
            .value

        val appWithAnswers =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(appWithAnswers) {

          val newSelectionOnPage1: SelectSubcontractor =
            page1Values.diff(otherPageSelection).head

          val request =
            FakeRequest(POST, url(1))
              .withFormUrlEncodedBody(
                "value[0]" -> newSelectionOnPage1.toString
              )

          val result = route(appWithAnswers, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url

          val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(captor.capture())

          captor.getValue.get(SelectSubcontractorPage).value mustEqual
            (otherPageSelection + newSelectionOnPage1)
        }
      }
    }

    "must hit fold success branch and save (value ++ otherPageValues) when mergedValues is empty (test-only form provider override)" in {

      // A test-only form provider that always binds successfully and returns a fixed non-empty Set
      class TestSelectSubcontractorFormProvider @Inject() () extends SelectSubcontractorFormProvider {
        override def apply(): Form[Set[SelectSubcontractor]] =
          Form(
            single(
              // ignore request content; always return a valid non-empty set
              "value" -> ignored(Set(SelectSubcontractor.values.head))
            )
          )
      }

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      // Ensure otherPageValues will be empty too (no prior selections),
      // so mergedValues = otherPageValues ++ currentSelectedValues stays empty
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SelectSubcontractorFormProvider].to[TestSelectSubcontractorFormProvider]
          )
          .build()

      running(application) {
        // no gotoPage, and no submitted value keys -> currentSelectedValues == empty
        val request = FakeRequest(POST, url(1)).withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        // verify it saved the value from the form (plus empty otherPageValues)
        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        captor.getValue.get(SelectSubcontractorPage).value mustEqual
          Set(SelectSubcontractor.values.head)
      }
    }

  }
}

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
import controllers.routes
import forms.verify.SelectSubcontractorsToReverifyFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.Mockito.verify
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.SelectSubcontractorsToReverifyPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import org.jsoup.Jsoup
import services.PaginationToReverifyService
import models.verify.SelectedSubcontractors
import viewmodels.verify.SubcontractorReverifyData
import pages.verify.UnverifiedSubcontractorsPage
import pages.verify.SelectedUnverifiedSubcontractorsPage
import views.html.verify.SelectSubcontractorsToReverifyView

import scala.concurrent.Future

class SelectSubcontractorsToReverifyControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val selectSubcontractorsToReverifyRoute =
    controllers.verify.routes.SelectSubcontractorsToReverifyController
      .onPageLoad(NormalMode)
      .url

  val formProvider = new SelectSubcontractorsToReverifyFormProvider()
  val form         = formProvider(requireSelection = true).bind(Map.empty)

  val paginationService = new PaginationToReverifyService()

  private val allRows = SubcontractorReverifyData.rows

  private val firstRow  = allRows.head
  private val secondRow = allRows(1)

  def url(page: Int = 1): String =
    controllers.verify.routes.SelectSubcontractorsToReverifyController
      .onPageLoad(NormalMode, page)
      .url

  "SubcontractorsToReverifyViewModel Controller" - {

    "must return OK and the correct view for a GET page 1" in {

      val ua =
        emptyUserAnswers
          .setOrException(
            SelectSubcontractorsToReverifyPage,
            Set(
              SelectedSubcontractors(allRows(0).id, allRows(0).name),
              SelectedSubcontractors(allRows(1).id, allRows(1).name)
            )
          )

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {

        val request = FakeRequest(
          GET,
          controllers.verify.routes.SelectSubcontractorsToReverifyController
            .onPageLoad(NormalMode, 1)
            .url
        )

        val result = route(application, request).value

        status(result) mustBe OK

        val body = contentAsString(result)

        body must include("Which subcontractors do you want to reverify?")
        body must include("Select the existing subcontractors you want to include in this verification request")
        body must include("""id="subcontractor-table"""")

        val total = allRows.size
        body must include(s"Showing 1 to 6 of $total results")

        val doc = Jsoup.parse(body)

        def inputValue(inputId: String): String =
          doc.selectFirst(s"input#$inputId").attr("value")

        def isChecked(inputId: String): Boolean =
          doc.selectFirst(s"input#$inputId").hasAttr("checked")

        inputValue("value-0") mustBe allRows(0).id
        isChecked("value-0") mustBe true

        inputValue("value-1") mustBe allRows(1).id
        isChecked("value-1") mustBe true

        // do NOT assert unchecked for other items if you refuse to change the view
        inputValue("value-2") mustBe allRows(2).id
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(
            SelectSubcontractorsToReverifyPage,
            Set(
              SelectedSubcontractors(firstRow.id, firstRow.name),
              SelectedSubcontractors(secondRow.id, secondRow.name)
            )
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, url(1))
        val result  = route(application, request).value

        val view = application.injector.instanceOf[SelectSubcontractorsToReverifyView]

        val baseUrl =
          controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(NormalMode).url

        val paginated =
          paginationService.paginate(
            allItems = allRows,
            currentPage = 1,
            recordsPerPage = 6,
            baseUrl = baseUrl
          )

        val expectedForm =
          formProvider(requireSelection = false).fill(Set(firstRow.id, secondRow.id))

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          expectedForm,
          NormalMode,
          paginated.items,
          paginated.pagination,
          1,
          paginated.startIndex,
          paginated.totalCount
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
          FakeRequest(POST, selectSubcontractorsToReverifyRoute)
            .withFormUrlEncodedBody(("value[0]", firstRow.id))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers =
        emptyUserAnswers
          .set(SelectedUnverifiedSubcontractorsPage, false)
          .success
          .value
          .set(UnverifiedSubcontractorsPage, List.empty)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody("value" -> "")

        // IMPORTANT: requireSelection MUST be TRUE
        val boundForm = formProvider(requireSelection = true).bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SelectSubcontractorsToReverifyView]

        val paginated =
          paginationService.paginate(
            allItems = allRows,
            currentPage = 1,
            recordsPerPage = 6,
            baseUrl = url(1)
          )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          paginated.items,
          paginated.pagination,
          1,
          paginated.startIndex,
          paginated.totalCount
        )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request = FakeRequest(GET, url(1))
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody(
              "value[0]" -> firstRow.id
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to target page when gotoPage field is present" in {

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
              "value[0]" -> firstRow.id,
              "gotoPage" -> "2"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual url(2)
      }
    }

    "must save selections when gotoPage is present" in {

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
              "value[0]" -> firstRow.id,
              "gotoPage" -> "2"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        val captor = org.mockito.ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        captor.getValue
          .get(SelectSubcontractorsToReverifyPage)
          .value must contain(SelectedSubcontractors(firstRow.id, firstRow.name))
      }
    }

    "must merge previous selections with current page selections" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val existingAnswers =
        UserAnswers(userAnswersId)
          .set(
            SelectSubcontractorsToReverifyPage,
            Set(SelectedSubcontractors(firstRow.id, firstRow.name))
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(existingAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody(
              "value[0]" -> secondRow.id
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val captor = org.mockito.ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        captor.getValue
          .get(SelectSubcontractorsToReverifyPage)
          .value mustEqual Set(SelectedSubcontractors(secondRow.id, secondRow.name))
      }
    }

    "must redirect to next page when Continue is submitted on page 2" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, url(2))
            .withFormUrlEncodedBody(
              "value[0]" -> firstRow.id,
              "gotoPage" -> "2"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
  }
}

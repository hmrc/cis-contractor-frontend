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
import models.response.GetNewestVerificationBatchResponse
import models.{NormalMode, Subcontractor, SubcontractorViewModel, UserAnswers, Verification}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.{NewestVerificationBatchResponsePage, SelectSubcontractorPage, UnverifiedSubcontractorsPage}
import play.api.data.Forms.*
import play.api.data.Form
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

  val formProvider            = new SelectSubcontractorFormProvider()
  val form: Form[Set[String]] = formProvider()
  val paginationService       = new PaginationService()

  def url(page: Int = 1): String =
    controllers.verify.routes.SelectSubcontractorController.onPageLoad(NormalMode, page).url

  private def generateSubcontractors(count: Int): Seq[Subcontractor] =
    (1 to count).map { subcontractorId =>
      Subcontractor(
        subcontractorId = subcontractorId.toLong,
        firstName = None,
        secondName = None,
        surname = None,
        tradingName = Some("ABC Construction Ltd"),
        partnershipTradingName = None,
        verified = None,
        verificationNumber = None,
        taxTreatment = None,
        verificationDate = None,
        lastMonthlyReturnDate = None,
        createDate = None,
        subcontractorType = Some("company"),
        subbieResourceRef = Some(9999L),
        utr = None,
        partnerUtr = None,
        crn = None,
        nino = None
      )
    }

  private val subcontractorCount = 10

  private val subcontractors: Seq[Subcontractor] = generateSubcontractors(subcontractorCount)

  private val getNewestVerificationBatchResponse: GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = None,
      subcontractors = subcontractors,
      verificationBatch = None,
      verifications = Seq(
        Verification(
          verificationId = 10L,
          matched = None,
          verificationNumber = None,
          taxTreatment = None,
          verificationBatchId = None,
          subcontractorId = None
        )
      ),
      submission = None,
      monthlyReturn = None
    )

  private def uaWithSubcontractors: UserAnswers =
    emptyUserAnswers
      .set(NewestVerificationBatchResponsePage, getNewestVerificationBatchResponse)
      .success
      .value
      .set(UnverifiedSubcontractorsPage, subcontractors)
      .success
      .value

  private val (_, allSubs)     = SubcontractorViewModel.fromSubcontractors(subcontractors)
  private val brodyMartin      = allSubs.head // first subcontractor, on page 1
  private val epsilonCarpentry = allSubs(6) // seventh subcontractor, on page 2 (pageSize = 6)

  "SelectSubcontractor Controller" - {

    "must return OK and correct view for GET (page 1)" in {

      val application = applicationBuilder(userAnswers = Some(uaWithSubcontractors)).build()

      running(application) {
        val request = FakeRequest(GET, url(1))
        val result  = route(application, request).value

        val view = application.injector.instanceOf[SelectSubcontractorView]

        val allItems         = SubcontractorViewModel.checkboxItems(allSubs)
        val paginationResult = paginationService.paginateCheckboxItems(allItems, 1)

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          paginationResult.paginatedData,
          paginationResult.paginationViewModel,
          1,
          paginationResult.startIndex,
          paginationResult.totalCount
        )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        uaWithSubcontractors
          .set(SelectSubcontractorPage, allSubs.toSet)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, url(1))
        val result  = route(application, request).value

        val view = application.injector.instanceOf[SelectSubcontractorView]

        val allItems         = SubcontractorViewModel.checkboxItems(allSubs)
        val paginationResult = paginationService.paginateCheckboxItems(allItems, 1)

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form.fill(allSubs.map(_.id).toSet),
          NormalMode,
          paginationResult.paginatedData,
          paginationResult.paginationViewModel,
          1,
          paginationResult.startIndex,
          paginationResult.totalCount
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(uaWithSubcontractors))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody("value[0]" -> allSubs.head.id)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithSubcontractors)).build()

      running(application) {
        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SelectSubcontractorView]

        val allItems         = SubcontractorViewModel.checkboxItems(allSubs)
        val paginationResult = paginationService.paginateCheckboxItems(allItems, 1)

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          paginationResult.paginatedData,
          paginationResult.paginationViewModel,
          1,
          paginationResult.startIndex,
          paginationResult.totalCount
        )(request, messages(application)).toString
      }
    }

    // todo even no page 2 still give ok
    "must support pagination (page 2)" in {

      val application = applicationBuilder(userAnswers = Some(uaWithSubcontractors)).build()

      running(application) {
        val request = FakeRequest(GET, url(2))
        val result  = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must render the page for a GET when no existing data is found" in {

      val application = applicationBuilder(userAnswers = Some(uaWithSubcontractors)).build()

      val request = FakeRequest(GET, url(1))
      val result  = route(application, request).value

      status(result) mustBe OK
    }

    "must return BadRequest for POST when no existing data and form has errors" in {

      val application = applicationBuilder(userAnswers = Some(uaWithSubcontractors)).build()

      val request =
        FakeRequest(POST, url(1))
          .withFormUrlEncodedBody("value" -> "")

      val result = route(application, request).value

      status(result) mustBe BAD_REQUEST
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, url(1))
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to NoSubcontractorsAdded on GET when subcontractors list is empty" in {

      val getNewestVerificationBatchResponse: GetNewestVerificationBatchResponse =
        GetNewestVerificationBatchResponse(
          scheme = None,
          subcontractors = Seq.empty[Subcontractor],
          verificationBatch = None,
          verifications = Seq(
            Verification(
              verificationId = 10L,
              matched = None,
              verificationNumber = None,
              taxTreatment = None,
              verificationBatchId = None,
              subcontractorId = None
            )
          ),
          submission = None,
          monthlyReturn = None
        )

      def uaWithNoSubcontractors: UserAnswers =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, getNewestVerificationBatchResponse)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(uaWithNoSubcontractors))
          .build()

      running(application) {
        val request = FakeRequest(GET, url(1))
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.verify.routes.NoSubcontractorsAddedController.onPageLoad().url
      }
    }

    "must redirect to VerifyYourSubcontractorsYesNo on GET when subcontractors list is not empty, unverifiedSubcontractors is empty" in {

      val subcontractors: Seq[Subcontractor] = Seq.empty[Subcontractor]

      def uaWithNoUnverifiedSubcontractors: UserAnswers =
        uaWithSubcontractors
          .set(UnverifiedSubcontractorsPage, subcontractors)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(uaWithNoUnverifiedSubcontractors))
          .build()

      running(application) {
        val request = FakeRequest(GET, url(1))
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.verify.routes.VerifyYourSubcontractorsYesNoController.onPageLoad.url
      }
    }

    "must redirect to Journey Recovery on GET when NewestVerificationBatchResponse is not found" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      running(application) {
        val request = FakeRequest(GET, url(1))
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery on GET when every unverified subcontractor has a missing or unknown type" in {

      val invalidSubcontractors: Seq[Subcontractor] = generateSubcontractors(3).map(_.copy(subcontractorType = None))

      val responseWithInvalid: GetNewestVerificationBatchResponse =
        getNewestVerificationBatchResponse.copy(subcontractors = invalidSubcontractors)

      def uaWithAllInvalid: UserAnswers =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, responseWithInvalid)
          .success
          .value
          .set(UnverifiedSubcontractorsPage, invalidSubcontractors)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(uaWithAllInvalid)).build()

      running(application) {
        val request = FakeRequest(GET, url(1))
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must render the page with only valid rows on GET when some unverified subcontractors have a missing type" in {

      val validSubs   = generateSubcontractors(2)
      val invalidSubs = generateSubcontractors(1).map(_.copy(subcontractorId = 99L, subcontractorType = None))
      val mixed       = validSubs ++ invalidSubs

      val responseWithMixed: GetNewestVerificationBatchResponse =
        getNewestVerificationBatchResponse.copy(subcontractors = mixed)

      def uaWithMixed: UserAnswers =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, responseWithMixed)
          .success
          .value
          .set(UnverifiedSubcontractorsPage, mixed)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(uaWithMixed)).build()

      running(application) {
        val request = FakeRequest(GET, url(1))
        val result  = route(application, request).value

        val (_, validViewModels) = SubcontractorViewModel.fromSubcontractors(mixed)
        val view                 = application.injector.instanceOf[SelectSubcontractorView]
        val items                = SubcontractorViewModel.checkboxItems(validViewModels)
        val paginationResult     = paginationService.paginateCheckboxItems(items, 1)

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          paginationResult.paginatedData,
          paginationResult.paginationViewModel,
          1,
          paginationResult.startIndex,
          paginationResult.totalCount
        )(request, messages(application)).toString

        validViewModels.size mustEqual 2
      }
    }

    "must redirect to Journey Recovery on GET when UnverifiedSubcontractors is not found" in {

      def uaWithNoUnverifiedSubcontractors: UserAnswers =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, getNewestVerificationBatchResponse)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(uaWithNoUnverifiedSubcontractors))
          .build()

      running(application) {
        val request = FakeRequest(GET, url(1))
        val result  = route(application, request).value

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
            .withFormUrlEncodedBody("value[0]" -> allSubs.head.id)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if UnverifiedSubcontractorsPage data is not found" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody("value[0]" -> allSubs.head.id)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if UnverifiedSubcontractors is empty" in {

      val subcontractorCount = 0

      def uaWithNoUnverifiedSubcontractor: UserAnswers =
        emptyUserAnswers
          .set(UnverifiedSubcontractorsPage, generateSubcontractors(subcontractorCount))
          .success
          .value

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(uaWithNoUnverifiedSubcontractor))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody("value[0]" -> allSubs.head.id)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST when every unverified subcontractor has a missing or unknown type" in {

      val invalidSubcontractors: Seq[Subcontractor] = generateSubcontractors(3).map(_.copy(subcontractorType = None))

      def uaWithNoUnverifiedSubcontractor: UserAnswers =
        emptyUserAnswers
          .set(UnverifiedSubcontractorsPage, invalidSubcontractors)
          .success
          .value

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(uaWithNoUnverifiedSubcontractor))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody("value[0]" -> allSubs.head.id)

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
        applicationBuilder(userAnswers = Some(uaWithSubcontractors))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody(
              "value[0]" -> allSubs.head.id,
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
        applicationBuilder(userAnswers = Some(uaWithSubcontractors))
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
        captor.getValue.get(SelectSubcontractorPage).value mustEqual Set.empty[SubcontractorViewModel]
      }
    }

    "must redirect to target page when gotoPage is present and there are prior selections (even if current page submits none)" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val priorSelection: Set[SubcontractorViewModel] = Set(brodyMartin)
      val userAnswers                                 =
        uaWithSubcontractors
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

      val page1Selection: Set[SubcontractorViewModel] = Set(brodyMartin)
      val userAnswers                                 =
        uaWithSubcontractors
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

      val page1Selection: Set[SubcontractorViewModel] = Set(brodyMartin)
      val userAnswers                                 =
        uaWithSubcontractors
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
            .withFormUrlEncodedBody("value[0]" -> epsilonCarpentry.id)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())
        captor.getValue.get(SelectSubcontractorPage).value mustEqual
          (page1Selection + epsilonCarpentry)
      }
    }

    "must merge newly selected values with otherPageValues when mergedValues is empty and no gotoPage is provided (fold success path)" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val allItems = SubcontractorViewModel.checkboxItems(allSubs)
      val page1    = paginationService.paginateCheckboxItems(allItems, 1)
      val page2    = paginationService.paginateCheckboxItems(allItems, 2)

      val page1Subs: Set[SubcontractorViewModel] =
        page1.paginatedData.flatMap(item => allSubs.find(_.id == item.value)).toSet

      val page2Subs: Set[SubcontractorViewModel] =
        page2.paginatedData.flatMap(item => allSubs.find(_.id == item.value)).toSet

      page2Subs.nonEmpty mustBe true

      val otherPageSelection: Set[SubcontractorViewModel] = Set(page2Subs.head)

      val userAnswers =
        uaWithSubcontractors
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

        val newSelectionOnPage1: SubcontractorViewModel =
          page1Subs.diff(otherPageSelection).head

        val request =
          FakeRequest(POST, url(1))
            .withFormUrlEncodedBody("value[0]" -> newSelectionOnPage1.id)

        val result = route(appWithAnswers, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        captor.getValue.get(SelectSubcontractorPage).value mustEqual
          (otherPageSelection + newSelectionOnPage1)
      }
    }

    "must hit fold success branch when form binds but submitted ids match no known subcontractor" in {

      class TestSelectSubcontractorFormProvider @Inject() () extends SelectSubcontractorFormProvider {
        override def apply(): Form[Set[String]] =
          Form(single("value" -> ignored(Set("non-existent-id"))))
      }

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(uaWithSubcontractors))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SelectSubcontractorFormProvider].to[TestSelectSubcontractorFormProvider]
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, url(1)).withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        captor.getValue.get(SelectSubcontractorPage).value mustEqual Set.empty[SubcontractorViewModel]
      }
    }
  }
}

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
import forms.add.TypeOfSubcontractorFormProvider
import models.{NormalMode, TypeOfSubcontractor, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.add.TypeOfSubcontractorPage
import pages.add.partnership.PartnershipNamePage
import queries.CisIdQuery
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import utils.DefaultSubcontractorCleanupService
import views.html.add.TypeOfSubcontractorView

import java.util.UUID
import scala.concurrent.Future
import scala.util.{Failure, Success}

class TypeOfSubcontractorControllerSpec extends SpecBase with MockitoSugar {

  private lazy val subcontractorTypesRoute =
    controllers.add.routes.TypeOfSubcontractorController
      .onPageLoad(NormalMode)
      .url

  private val formProvider = new TypeOfSubcontractorFormProvider()
  private val form         = formProvider()

  "TypeOfSubcontractorController" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, subcontractorTypesRoute)

        val result =
          route(application, request).value

        val view =
          application.injector.instanceOf[TypeOfSubcontractorView]

        status(result) mustBe OK

        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          controllers.add.routes.TypeOfSubcontractorController.onSubmit(NormalMode)
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(
            TypeOfSubcontractorPage,
            TypeOfSubcontractor.values.head
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, subcontractorTypesRoute)

        val view =
          application.injector.instanceOf[TypeOfSubcontractorView]

        val result =
          route(application, request).value

        status(result) mustBe OK

        contentAsString(result) mustEqual view(
          form.fill(TypeOfSubcontractor.values.head),
          NormalMode,
          controllers.add.routes.TypeOfSubcontractorController.onSubmit(NormalMode)
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the IndividualNamesOptions page when Individual or sole trader is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val mockCleanupService =
        mock[DefaultSubcontractorCleanupService]

      when(mockCleanupService.clean(any()))
        .thenReturn(Success(emptyUserAnswers))

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[DefaultSubcontractorCleanupService]
              .toInstance(mockCleanupService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorTypesRoute)
            .withFormUrlEncodedBody(
              "value" ->
                TypeOfSubcontractor.Individualorsoletrader.toString
            )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.add.routes.IndividualNamesOptionsController
            .onPageLoad(NormalMode)
            .url

        verify(mockCleanupService, times(1))
          .clean(any())

        verify(mockSessionRepository, times(1))
          .set(any())
      }
    }

    "must redirect to the CompanyName page when Limited company is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val mockCleanupService =
        mock[DefaultSubcontractorCleanupService]

      when(mockCleanupService.clean(any()))
        .thenReturn(Success(emptyUserAnswers))

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[DefaultSubcontractorCleanupService]
              .toInstance(mockCleanupService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorTypesRoute)
            .withFormUrlEncodedBody(
              "value" ->
                TypeOfSubcontractor.Limitedcompany.toString
            )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.add.company.routes.CompanyNameController
            .onPageLoad(NormalMode)
            .url

        verify(mockCleanupService, times(1))
          .clean(any())

        verify(mockSessionRepository, times(1))
          .set(any())
      }
    }

    "must redirect to the PartnershipName page when Partnership is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val mockCleanupService =
        mock[DefaultSubcontractorCleanupService]

      when(mockCleanupService.clean(any()))
        .thenReturn(Success(emptyUserAnswers))

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[DefaultSubcontractorCleanupService]
              .toInstance(mockCleanupService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorTypesRoute)
            .withFormUrlEncodedBody(
              "value" ->
                TypeOfSubcontractor.Partnership.toString
            )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.add.partnership.routes.PartnershipNameController
            .onPageLoad(NormalMode)
            .url

        verify(mockCleanupService, times(1))
          .clean(any())

        verify(mockSessionRepository, times(1))
          .set(any())
      }
    }

    "must redirect to the TrustName page when Trust is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val mockCleanupService =
        mock[DefaultSubcontractorCleanupService]

      when(mockCleanupService.clean(any()))
        .thenReturn(Success(emptyUserAnswers))

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[DefaultSubcontractorCleanupService]
              .toInstance(mockCleanupService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorTypesRoute)
            .withFormUrlEncodedBody(
              "value" ->
                TypeOfSubcontractor.Trust.toString
            )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.add.trust.routes.TrustNameController
            .onPageLoad(NormalMode)
            .url

        verify(mockCleanupService, times(1))
          .clean(any())

        verify(mockSessionRepository, times(1))
          .set(any())
      }
    }

    "must save the selected subcontractor type after cleaning the previous answers" in {

      val cleanedUserAnswers =
        UserAnswers(userAnswersId)

      val mockSessionRepository =
        mock[SessionRepository]

      val mockCleanupService =
        mock[DefaultSubcontractorCleanupService]

      when(mockCleanupService.clean(any()))
        .thenReturn(Success(cleanedUserAnswers))

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[DefaultSubcontractorCleanupService]
              .toInstance(mockCleanupService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorTypesRoute)
            .withFormUrlEncodedBody(
              "value" ->
                TypeOfSubcontractor.Limitedcompany.toString
            )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        val captor: ArgumentCaptor[UserAnswers] =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        verify(mockCleanupService, times(1))
          .clean(any())

        verify(mockSessionRepository, times(1))
          .set(captor.capture())

        captor.getValue
          .get(TypeOfSubcontractorPage) mustBe
          Some(TypeOfSubcontractor.Limitedcompany)
      }
    }

    "must redirect to JourneyRecovery and not persist answers when cleanup fails" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val mockCleanupService =
        mock[DefaultSubcontractorCleanupService]

      when(mockCleanupService.clean(any()))
        .thenReturn(
          Failure(
            new RuntimeException("Unable to clean user answers")
          )
        )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[DefaultSubcontractorCleanupService]
              .toInstance(mockCleanupService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorTypesRoute)
            .withFormUrlEncodedBody(
              "value" ->
                TypeOfSubcontractor.Limitedcompany.toString
            )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          routes.JourneyRecoveryController
            .onPageLoad()
            .url

        verify(mockCleanupService, times(1))
          .clean(any())

        verify(mockSessionRepository, never())
          .set(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val mockCleanupService =
        mock[DefaultSubcontractorCleanupService]

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[DefaultSubcontractorCleanupService]
              .toInstance(mockCleanupService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorTypesRoute)
            .withFormUrlEncodedBody(
              "value" -> "invalid value"
            )

        val boundForm =
          form.bind(
            Map("value" -> "invalid value")
          )

        val view =
          application.injector.instanceOf[TypeOfSubcontractorView]

        val result =
          route(application, request).value

        status(result) mustBe BAD_REQUEST

        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          controllers.add.routes.TypeOfSubcontractorController.onSubmit(NormalMode)
        )(
          request,
          messages(application)
        ).toString

        verifyNoInteractions(mockCleanupService)
        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must return a Bad Request and errors when no value is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val mockCleanupService =
        mock[DefaultSubcontractorCleanupService]

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[DefaultSubcontractorCleanupService]
              .toInstance(mockCleanupService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, subcontractorTypesRoute)
            .withFormUrlEncodedBody()

        val boundForm =
          form.bind(Map.empty[String, String])

        val view =
          application.injector.instanceOf[TypeOfSubcontractorView]

        val result =
          route(application, request).value

        status(result) mustBe BAD_REQUEST

        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          controllers.add.routes.TypeOfSubcontractorController.onSubmit(NormalMode)
        )(
          request,
          messages(application)
        ).toString

        contentAsString(result) must include(
          messages(application)(
            "typeOfSubcontractor.error.required"
          )
        )

        verifyNoInteractions(mockCleanupService)
        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must store the type and cisId under a new UUID when Partnership is submitted on start" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.add.routes.TypeOfSubcontractorController.onSubmitStart(NormalMode).url)
            .withFormUrlEncodedBody("value" -> TypeOfSubcontractor.Partnership.toString)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.add.partnership.routes.PartnershipNameController.onPageLoad(NormalMode).url

        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        val saved = captor.getValue
        val id    = saved.data.value.keys.find(key => scala.util.Try(UUID.fromString(key)).isSuccess).value
        saved.get(TypeOfSubcontractorPage(id)) mustBe Some(TypeOfSubcontractor.Partnership)
        saved.get(CisIdQuery(id)) mustBe Some("1")
        saved.get(CisIdQuery) mustBe Some("1")
      }
    }

    "must clean previous answers before storing the type under a new UUID on start" in {

      val leftoverAnswers =
        userAnswersWithCisId
          .set(PartnershipNamePage, "Old name")
          .success
          .value

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(leftoverAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.add.routes.TypeOfSubcontractorController.onSubmitStart(NormalMode).url)
            .withFormUrlEncodedBody("value" -> TypeOfSubcontractor.Partnership.toString)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER

        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        captor.getValue.get(PartnershipNamePage) mustBe None
        captor.getValue.get(CisIdQuery) mustBe Some("1")
      }
    }

    "must redirect to Journey Recovery on start when cleanup fails" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockCleanupService    = mock[DefaultSubcontractorCleanupService]
      when(mockCleanupService.clean(any())).thenReturn(Failure(new RuntimeException("Unable to clean user answers")))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[DefaultSubcontractorCleanupService].toInstance(mockCleanupService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.add.routes.TypeOfSubcontractorController.onSubmitStart(NormalMode).url)
            .withFormUrlEncodedBody("value" -> TypeOfSubcontractor.Partnership.toString)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must redirect to Journey Recovery on start when cisId is missing" in {
      val mockSessionRepository = mock[SessionRepository]
      val application           =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.add.routes.TypeOfSubcontractorController.onSubmitStart(NormalMode).url)
            .withFormUrlEncodedBody("value" -> TypeOfSubcontractor.Partnership.toString)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must return OK and the correct view for a GET on start" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.routes.TypeOfSubcontractorController.onPageLoadStart(NormalMode).url)

        val result =
          route(application, request).value

        val view =
          application.injector.instanceOf[TypeOfSubcontractorView]

        status(result) mustBe OK

        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          controllers.add.routes.TypeOfSubcontractorController.onSubmitStart(NormalMode)
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET with id" in {

      val id          = UUID.fromString("11111111-1111-1111-1111-111111111111")
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.routes.TypeOfSubcontractorController.onPageLoadWithId(id, NormalMode).url)

        val result =
          route(application, request).value

        val view =
          application.injector.instanceOf[TypeOfSubcontractorView]

        status(result) mustBe OK

        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          controllers.add.routes.TypeOfSubcontractorController.onSubmitWithId(id, NormalMode)
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET with id when the question has previously been answered" in {

      val id          = UUID.fromString("11111111-1111-1111-1111-111111111111")
      val userAnswers =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage(id.toString), TypeOfSubcontractor.Partnership)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.routes.TypeOfSubcontractorController.onPageLoadWithId(id, NormalMode).url)

        val view =
          application.injector.instanceOf[TypeOfSubcontractorView]

        val result =
          route(application, request).value

        status(result) mustBe OK

        contentAsString(result) mustEqual view(
          form.fill(TypeOfSubcontractor.Partnership),
          NormalMode,
          controllers.add.routes.TypeOfSubcontractorController.onSubmitWithId(id, NormalMode)
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must save the selected type under the id when Partnership is submitted" in {

      val id                    = UUID.fromString("11111111-1111-1111-1111-111111111111")
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.add.routes.TypeOfSubcontractorController.onSubmitWithId(id, NormalMode).url)
            .withFormUrlEncodedBody("value" -> TypeOfSubcontractor.Partnership.toString)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.add.partnership.routes.PartnershipNameController.onPageLoad(NormalMode).url

        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        val saved = captor.getValue
        saved.get(TypeOfSubcontractorPage(id.toString)) mustBe Some(TypeOfSubcontractor.Partnership)
        saved.get(TypeOfSubcontractorPage) mustBe None
        saved.get(CisIdQuery) mustBe Some("1")
      }
    }

    "must clean previous answers before saving the type under the id" in {

      val id              = UUID.fromString("11111111-1111-1111-1111-111111111111")
      val leftoverAnswers =
        userAnswersWithCisId
          .set(PartnershipNamePage, "Old name")
          .success
          .value

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(leftoverAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.add.routes.TypeOfSubcontractorController.onSubmitWithId(id, NormalMode).url)
            .withFormUrlEncodedBody("value" -> TypeOfSubcontractor.Partnership.toString)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER

        val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        captor.getValue.get(PartnershipNamePage) mustBe None
        captor.getValue.get(TypeOfSubcontractorPage(id.toString)) mustBe Some(TypeOfSubcontractor.Partnership)
      }
    }

    "must redirect to Journey Recovery on submit with id when cleanup fails" in {

      val id                    = UUID.fromString("11111111-1111-1111-1111-111111111111")
      val mockSessionRepository = mock[SessionRepository]
      val mockCleanupService    = mock[DefaultSubcontractorCleanupService]
      when(mockCleanupService.clean(any())).thenReturn(Failure(new RuntimeException("Unable to clean user answers")))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithCisId))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[DefaultSubcontractorCleanupService].toInstance(mockCleanupService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.add.routes.TypeOfSubcontractorController.onSubmitWithId(id, NormalMode).url)
            .withFormUrlEncodedBody("value" -> TypeOfSubcontractor.Partnership.toString)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted with id" in {

      val id          = UUID.fromString("11111111-1111-1111-1111-111111111111")
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.add.routes.TypeOfSubcontractorController.onSubmitWithId(id, NormalMode).url)
            .withFormUrlEncodedBody("value" -> "invalid value")

        val boundForm =
          form.bind(Map("value" -> "invalid value"))

        val view =
          application.injector.instanceOf[TypeOfSubcontractorView]

        val result =
          route(application, request).value

        status(result) mustBe BAD_REQUEST

        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          controllers.add.routes.TypeOfSubcontractorController.onSubmitWithId(id, NormalMode)
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery on a GET with id when there is no session data" in {

      val id          = UUID.fromString("11111111-1111-1111-1111-111111111111")
      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.routes.TypeOfSubcontractorController.onPageLoadWithId(id, NormalMode).url)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

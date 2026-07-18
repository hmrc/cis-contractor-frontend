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

import controllers.routes
import models.{TypeOfSubcontractor, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.TypeOfSubcontractorPage
import pages.add.trust._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.SubContractorVerifiedQuery
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future

class AmendTrustCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

  private val minUa =
    emptyUserAnswers
      .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
      .success
      .value
      .set(TrustNamePage, "Test Trust")
      .success
      .value
      .set(TrustAddressYesNoPage, false)
      .success
      .value
      .set(AddTrustContactMethodsYesNoPage, false)
      .success
      .value
      .set(TrustUtrYesNoPage, false)
      .success
      .value
      .set(TrustWorksReferenceYesNoPage, false)
      .success
      .value
      .set(SubContractorVerifiedQuery, false)
      .success
      .value

  "AmendTrustCheckYourAnswersController" - {

    "must return OK for GET when validation succeeds" in {

      val application =
        applicationBuilder(userAnswers = Some(minUa)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Test Trust")
      }
    }

    "must redirect to Journey Recovery when validation fails" in {

      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(invalidUa)).build()

      running(application) {

        val request =
          FakeRequest(GET, controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect back to amend CYA after successful submit" in {

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]

      when(mockSubcontractorService.createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(minUa))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onPageLoad().url
      }

      verify(mockSubcontractorService)
        .createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])

      verifyNoMoreInteractions(mockSubcontractorService)
    }

    "must redirect to Journey Recovery when the service fails" in {

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]

      when(mockSubcontractorService.createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application =
        applicationBuilder(userAnswers = Some(minUa))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockSubcontractorService)
        .createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
    }

    "must redirect to Journey Recovery when POST validation fails" in {

      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
          .success
          .value

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]

      val application =
        applicationBuilder(userAnswers = Some(invalidUa))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockSubcontractorService, never())
        .createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
    }

    "must clear answers and redirect to Index on cancel" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(minUa))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(GET, controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onCancel().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.IndexController.onPageLoad().url
      }

      verify(mockSessionRepository).set(any[UserAnswers])
    }
  }
}

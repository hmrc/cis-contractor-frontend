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

import controllers.routes
import models.{TypeOfSubcontractor, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.TypeOfSubcontractorPage
import pages.add.company._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.SubContractorVerifiedQuery
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future

class AmendCompanyCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

  private val minUa =
    emptyUserAnswers
      .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
      .success
      .value
      .set(CompanyNamePage, "Test Company")
      .success
      .value
      .set(CompanyAddressYesNoPage, false)
      .success
      .value
      .set(AddCompanyContactMethodsYesNoPage, false)
      .success
      .value
      .set(CompanyUtrYesNoPage, false)
      .success
      .value
      .set(CompanyWorksReferenceYesNoPage, false)
      .success
      .value
      .set(SubContractorVerifiedQuery, false)
      .success
      .value

  "AmendCompanyCheckYourAnswersController" - {

    "must return OK for GET when validation succeeds" in {

      val application =
        applicationBuilder(userAnswers = Some(minUa)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        val doc = contentAsString(result)

        doc must include(controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onSubmit().url)
        doc must include(controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onCancel().url)
        doc must include("Test Company")
      }
    }

    "must redirect to Journey Recovery when validation fails" in {

      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(invalidUa)).build()

      running(application) {

        val request =
          FakeRequest(GET, controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onPageLoad().url)

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
          FakeRequest(POST, controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onPageLoad().url
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
          FakeRequest(POST, controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onSubmit().url)

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
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
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
          FakeRequest(POST, controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onSubmit().url)

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
          FakeRequest(GET, controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onCancel().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.IndexController.onPageLoad().url
      }

      verify(mockSessionRepository).set(any[UserAnswers])
    }
  }
}

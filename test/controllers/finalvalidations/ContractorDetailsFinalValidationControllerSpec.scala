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

package controllers.finalvalidations

import base.SpecBase
import models.Scheme
import models.UserAnswers
import models.contractordetails.{ContractorDetailsFinalValidation, ContractorDetailsValidationTarget}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.contractordetails.{ContractorDetailsValidationTargetPage, ContractorSchemePage, ContractorUtrPage, EnterContractorEmailAddressPage, SchemeNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import repositories.SessionRepository
import services.{CisManageService, ContractorDetailsFinalValidationService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ContractorDetailsFinalValidationControllerSpec extends SpecBase with MockitoSugar {

  private val scheme =
    Scheme(
      schemeId = 1,
      instanceId = "cisId",
      accountsOfficeReference = "123 PA 87654321",
      taxOfficeNumber = "123",
      taxOfficeReference = "45678"
    )

  private def app(
    userAnswers: UserAnswers,
    finalValidationService: ContractorDetailsFinalValidationService
  ) = {
    val mockCisManageService  = mock[CisManageService]
    val mockSessionRepository = mock[SessionRepository]

    when(mockCisManageService.ensureCisIdInUserAnswers(any[UserAnswers])(any[HeaderCarrier]))
      .thenReturn(Future.successful(userAnswers))
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

    applicationBuilder(userAnswers = Some(userAnswers))
      .configure("urls.cisReturnDashboard" -> "http://localhost:9557/return-dashboard")
      .overrides(
        bind[ContractorDetailsFinalValidationService].toInstance(finalValidationService),
        bind[CisManageService].toInstance(mockCisManageService),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
      .build()
  }

  "ContractorDetailsFinalValidationController" - {

    "must redirect to review contractor details when file monthly return validations fail" in {
      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, "cisId")
          .success
          .value

      val mockFinalValidationService =
        mock[ContractorDetailsFinalValidationService]

      when(
        mockFinalValidationService.refreshAndValidate(
          any[UserAnswers],
          any[ContractorDetailsValidationTarget]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful((userAnswers, ContractorDetailsFinalValidation(false, true, true)))
      )

      val application = app(userAnswers, mockFinalValidationService)

      running(application) {
        val result =
          route(
            application,
            FakeRequest(GET, routes.ContractorDetailsFinalValidationController.startFileMonthlyReturn().url)
          ).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          routes.ContractorDetailsFinalValidationController.onPageLoad().url
      }
    }

    "must redirect to return target when file nil return validations pass" in {
      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, "cisId")
          .success
          .value

      val mockFinalValidationService =
        mock[ContractorDetailsFinalValidationService]

      when(
        mockFinalValidationService.refreshAndValidate(
          any[UserAnswers],
          any[ContractorDetailsValidationTarget]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful((userAnswers, ContractorDetailsFinalValidation(true, true, true)))
      )

      val application = app(userAnswers, mockFinalValidationService)

      running(application) {
        val result =
          route(
            application,
            FakeRequest(GET, routes.ContractorDetailsFinalValidationController.startFileNilReturn().url)
          ).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe "http://localhost:9557/return-dashboard"
      }
    }

    "must render incomplete tasks with links and disable the final action" in {
      val userAnswers =
        emptyUserAnswers
          .set(ContractorDetailsValidationTargetPage, ContractorDetailsValidationTarget.VerifySubcontractors)
          .success
          .value

      val mockFinalValidationService =
        mock[ContractorDetailsFinalValidationService]

      when(mockFinalValidationService.validate(any[UserAnswers]))
        .thenReturn(ContractorDetailsFinalValidation(false, false, false))

      val application = app(userAnswers, mockFinalValidationService)

      running(application) {
        val result =
          route(
            application,
            FakeRequest(GET, routes.ContractorDetailsFinalValidationController.onPageLoad().url)
          ).value

        val body = contentAsString(result)

        status(result) mustBe OK
        body must include("Unique Taxpayer Reference")
        body must include("Scheme name")
        body must include("Email address")
        body must include("Cannot start yet")
        body must include("/contractor-details/enter-contractors-utr")
      }
    }

    "must update scheme and redirect to verification target when all tasks are complete" in {
      val userAnswers =
        emptyUserAnswers
          .set(ContractorDetailsValidationTargetPage, ContractorDetailsValidationTarget.VerifySubcontractors)
          .success
          .value
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(ContractorUtrPage, "1234567890")
          .success
          .value
          .set(SchemeNamePage, "Scheme")
          .success
          .value
          .set(EnterContractorEmailAddressPage, "test@example.com")
          .success
          .value

      val mockFinalValidationService =
        mock[ContractorDetailsFinalValidationService]

      when(mockFinalValidationService.validate(any[UserAnswers]))
        .thenReturn(ContractorDetailsFinalValidation(true, true, true))
      when(mockFinalValidationService.updateSchemeFromAnswers(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(()))

      val application = app(userAnswers, mockFinalValidationService)

      running(application) {
        val result =
          route(
            application,
            FakeRequest(GET, routes.ContractorDetailsFinalValidationController.onContinue().url)
          ).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.verify.routes.NewestVerificationBatchController.onPageLoad().url
      }

      verify(mockFinalValidationService).updateSchemeFromAnswers(any[UserAnswers])(any[HeaderCarrier])
    }
  }
}

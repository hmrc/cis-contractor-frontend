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

package controllers.contractordetails

import base.SpecBase
import connectors.ConstructionIndustrySchemeConnector
import models.Scheme
import models.requests.UpdateContractorSchemeParams
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.contractordetails.{ContractorSchemePage, ContractorUtrPage, EnterContractorEmailAddressPage, SchemeNamePage}
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ContractorDetailsService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ContractorDetailsCheckAnswersControllerSpec extends SpecBase with MockitoSugar {

  private val scheme = Scheme(
    schemeId = 123,
    instanceId = "cisId",
    accountsOfficeReference = "123 PA 87654321",
    taxOfficeNumber = "123",
    taxOfficeReference = "45678",
    utr = Some("1234567890"),
    name = Some("Scheme ABC"),
    emailAddress = Some("test@mail.com")
  )

  "ContractorDetailsCheckAnswersController" - {

    "must return OK and the correct view for a GET when answers exist" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(ContractorUtrPage, "1234567890")
          .success
          .value
          .set(SchemeNamePage, "Scheme ABC")
          .success
          .value
          .set(EnterContractorEmailAddressPage, "test@mail.com")
          .success
          .value

      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(
          GET,
          controllers.contractordetails.routes.ContractorDetailsCheckAnswersController.onPageLoad().url
        )

        val result = route(application, request).value
        val body   = contentAsString(result)

        status(result) mustEqual OK

        body must include("123 PA 87654321")
        body must include("1234567890")
        body must include("Scheme ABC")
        body must include("test@mail.com")
      }
    }

    "must redirect to JourneyRecovery when ContractorSchemePage is missing" in {

      val application =
        applicationBuilder(Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(
          GET,
          routes.ContractorDetailsCheckAnswersController.onPageLoad().url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must render the page when only ContractorSchemePage exists" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value

      val application =
        applicationBuilder(Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(
          GET,
          routes.ContractorDetailsCheckAnswersController.onPageLoad().url
        )

        val result = route(application, request).value

        status(result) mustEqual OK

        val body = contentAsString(result)

        body must include("123 PA 87654321")
      }
    }

    "must submit contractor details and redirect to Contractor Details Updated page" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(ContractorUtrPage, "1234567890")
          .success
          .value
          .set(SchemeNamePage, "Scheme ABC")
          .success
          .value
          .set(EnterContractorEmailAddressPage, "test@mail.com")
          .success
          .value
      val mockService =
        mock[ContractorDetailsService]

      val application =
        applicationBuilder(Some(userAnswers))
          .overrides(
            inject
              .bind[ContractorDetailsService]
              .toInstance(mockService)
          )
          .build()

      when(
        mockService.updateContractorDetails(
          any[UpdateContractorSchemeParams]
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(()))

      running(application) {

        val request = FakeRequest(
          POST,
          routes.ContractorDetailsCheckAnswersController.onSubmit().url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.ContractorDetailsUpdatedController.onPageLoad().url

        val captor =
          ArgumentCaptor.forClass(classOf[UpdateContractorSchemeParams])

        verify(mockService)
          .updateContractorDetails(
            captor.capture()
          )(any[HeaderCarrier])

        captor.getValue mustBe UpdateContractorSchemeParams(
          schemeId = 123,
          instanceId = "cisId",
          accountsOfficeReference = "123 PA 87654321",
          taxOfficeNumber = "123",
          taxOfficeReference = "45678",
          utr = Some("1234567890"),
          name = Some("Scheme ABC"),
          emailAddress = Some("test@mail.com"),
          version = scheme.version,
          displayWelcomePage = scheme.displayWelcomePage,
          prePopCount = scheme.prePopCount,
          prePopSuccessful = scheme.prePopSuccessful
        )
      }
    }
    "must redirect to journey recovery on submit when ContractorSchemePage is missing" in {

      val mockService =
        mock[ContractorDetailsService]

      val application =
        applicationBuilder(Some(emptyUserAnswers))
          .overrides(
            inject
              .bind[ContractorDetailsService]
              .toInstance(mockService)
          )
          .build()

      running(application) {

        val request = FakeRequest(
          POST,
          routes.ContractorDetailsCheckAnswersController.onSubmit().url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, never())
          .updateContractorDetails(any())(any())
      }
    }

    "must redirect to journey recovery when submitContractorDetails fails" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value

      val mockService =
        mock[ContractorDetailsService]

      val application =
        applicationBuilder(Some(userAnswers))
          .overrides(
            inject
              .bind[ContractorDetailsService]
              .toInstance(mockService)
          )
          .build()

      when(
        mockService.updateContractorDetails(
          any[UpdateContractorSchemeParams]
        )(any())
      ).thenReturn(
        Future.failed(new RuntimeException("boom"))
      )

      running(application) {

        val request = FakeRequest(
          POST,
          routes.ContractorDetailsCheckAnswersController.onSubmit().url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}

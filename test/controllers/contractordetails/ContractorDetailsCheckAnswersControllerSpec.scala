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

import base.WithFakeCisTaxpayer
import models.{Scheme, UserAnswers}
import models.requests.UpdateContractorSchemeParams
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.contractordetails.{AddEmailAddressYesNoPage, AddSchemeNameYesNoPage, ContractorSchemePage, ContractorUtrPage, EnterContractorEmailAddressPage, SchemeNamePage}
import pages.CisIdPage
import play.api.inject
import navigation.{FakeNavigator, Navigator}
import queries.CisIdQuery
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ContractorDetailsService
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import repositories.SessionRepository
import scala.concurrent.Future

class ContractorDetailsCheckAnswersControllerSpec extends WithFakeCisTaxpayer with MockitoSugar {

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

  private val flowId =
    UUID.fromString("11111111-1111-1111-1111-111111111111")

  private val id = flowId.toString

  private def answersWithCisId: UserAnswers =
    emptyUserAnswers
      .set(CisIdQuery(id), "AB12")
      .success
      .value

  private def completeAnswersWithId: UserAnswers =
    answersWithCisId
      .set(ContractorSchemePage(id), scheme)
      .success
      .value
      .set(ContractorUtrPage(id), "1234567890")
      .success
      .value
      .set(AddSchemeNameYesNoPage(id), true)
      .success
      .value
      .set(SchemeNamePage(id), "Test Contractor Scheme")
      .success
      .value
      .set(AddEmailAddressYesNoPage(id), true)
      .success
      .value
      .set(EnterContractorEmailAddressPage(id), "contractor@test.com")
      .success
      .value

  "ContractorDetailsCheckAnswersController" - {

    "must return OK and the correct view for a GET when all required answers exist" in {
      Seq(
        (
          "AGENT",
          applicationConfig.constructionIndustryAgentAccountUrl + "1",
          true,
          Some(
            emptyUserAnswers
              .set(ContractorSchemePage, scheme)
              .success
              .value
              .set(CisIdPage, "1")
              .success
              .value
              .set(AddSchemeNameYesNoPage, false)
              .success
              .value
              .set(AddEmailAddressYesNoPage, false)
              .success
              .value
          )
        ),
        (
          "ORGANISATION",
          applicationConfig.constructionIndustryOrgAccountUrl,
          false,
          Some(
            emptyUserAnswers
              .set(ContractorSchemePage, scheme)
              .success
              .value
              .set(AddSchemeNameYesNoPage, false)
              .success
              .value
              .set(AddEmailAddressYesNoPage, false)
              .success
              .value
          )
        )
      ).foreach { case (_, cisAccountUrl, isAgent, userAnswers) =>
        val application =
          applicationBuilder(
            userAnswers = userAnswers,
            isAgent = isAgent
          ).build()

        running(application) {
          val request =
            FakeRequest(
              GET,
              routes.ContractorDetailsCheckAnswersController.onPageLoad().url
            )

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include(cisAccountUrl)
        }
      }
    }

    "must return OK and the correct view for a GET when answers exist" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(ContractorUtrPage, "1234567890")
          .success
          .value
          .set(AddSchemeNameYesNoPage, true)
          .success
          .value
          .set(SchemeNamePage, "Scheme ABC")
          .success
          .value
          .set(AddEmailAddressYesNoPage, true)
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

    "must render Continue when a valid target is supplied" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(AddSchemeNameYesNoPage, false)
          .success
          .value
          .set(AddEmailAddressYesNoPage, false)
          .success
          .value

      Seq(
        "manageYourCisReturn",
        "subcontractors"
      ).foreach { target =>

        val application =
          applicationBuilder(Some(userAnswers)).build()

        running(application) {

          val request = FakeRequest(
            GET,
            routes.ContractorDetailsCheckAnswersController
              .onPageLoad(Some(target))
              .url
          )

          val result = route(application, request).value
          val body   = contentAsString(result)

          status(result) mustEqual OK

          body must include("Continue")
          body must not include "Save and continue"
        }
      }
    }

    "must render Save and continue when an invalid target is supplied" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(AddSchemeNameYesNoPage, false)
          .success
          .value
          .set(AddEmailAddressYesNoPage, false)
          .success
          .value

      val application =
        applicationBuilder(Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(
          GET,
          routes.ContractorDetailsCheckAnswersController
            .onPageLoad(Some("invalidTarget"))
            .url
        )

        val result = route(application, request).value
        val body   = contentAsString(result)

        status(result) mustEqual OK

        body must include("Save and continue")
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

    "must redirect to JourneyRecovery when required details are missing" in {

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

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must render the page when scheme name and email address are not required" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(AddSchemeNameYesNoPage, false)
          .success
          .value
          .set(AddEmailAddressYesNoPage, false)
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

    "must redirect to JourneyRecovery when AddSchemeNameYesNoPage is true but SchemeNamePage is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(AddSchemeNameYesNoPage, true)
          .success
          .value
          .set(AddEmailAddressYesNoPage, false)
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

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when AddEmailAddressYesNoPage is false but EnterContractorEmailAddressPage is set (url-hopping)" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(AddSchemeNameYesNoPage, false)
          .success
          .value
          .set(AddEmailAddressYesNoPage, false)
          .success
          .value
          .set(EnterContractorEmailAddressPage, "stale@test.com")
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

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when AddEmailAddressYesNoPage is true but EnterContractorEmailAddressPage is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(AddSchemeNameYesNoPage, false)
          .success
          .value
          .set(AddEmailAddressYesNoPage, true)
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

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
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
          .set(AddSchemeNameYesNoPage, true)
          .success
          .value
          .set(SchemeNamePage, "Scheme ABC")
          .success
          .value
          .set(AddEmailAddressYesNoPage, true)
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

    "must redirect to target without updating contractor details when a valid target is supplied" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(AddSchemeNameYesNoPage, false)
          .success
          .value
          .set(AddEmailAddressYesNoPage, false)
          .success
          .value

      Seq(
        (
          "manageYourCisReturn",
          applicationConfig.manageCisReturnUrl(scheme.instanceId)
        ),
        (
          "subcontractors",
          applicationConfig.manageSubcontractorsLandingUrl(scheme.instanceId)
        )
      ).foreach { case (target, expectedUrl) =>
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

        running(application) {

          val request = FakeRequest(
            POST,
            routes.ContractorDetailsCheckAnswersController
              .onSubmit(Some(target))
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual expectedUrl

          verify(mockService, never())
            .updateContractorDetails(any())(any())
        }
      }
    }

    "must submit contractor details when an invalid target is supplied" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(AddSchemeNameYesNoPage, false)
          .success
          .value
          .set(AddEmailAddressYesNoPage, false)
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
          routes.ContractorDetailsCheckAnswersController
            .onSubmit(Some("invalidTarget"))
            .url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.ContractorDetailsUpdatedController.onPageLoad().url

        verify(mockService)
          .updateContractorDetails(
            any[UpdateContractorSchemeParams]
          )(any[HeaderCarrier])
      }
    }

    "must redirect to JourneyRecovery on submit when ContractorSchemePage is missing" in {

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

    "must redirect to JourneyRecovery on submit when required details are missing" in {

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

    "must redirect to JourneyRecovery on submit when AddSchemeNameYesNoPage is true but SchemeNamePage is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(AddSchemeNameYesNoPage, true)
          .success
          .value
          .set(AddEmailAddressYesNoPage, false)
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

    "must redirect to JourneyRecovery on submit when AddEmailAddressYesNoPage is true but EnterContractorEmailAddressPage is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(AddSchemeNameYesNoPage, false)
          .success
          .value
          .set(AddEmailAddressYesNoPage, true)
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

    "must redirect to JourneyRecovery when submitContractorDetails fails" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(AddSchemeNameYesNoPage, true)
          .success
          .value
          .set(SchemeNamePage, "Scheme ABC")
          .success
          .value
          .set(AddEmailAddressYesNoPage, true)
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

    "ContractorDetailsCheckAnswers Controller with id" - {

      "must return OK and display contractor details for a GET with id" in {

        val userAnswers = completeAnswersWithId
        val repo        = mockSessionRepository(Some(userAnswers))

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              inject.bind[SessionRepository].toInstance(repo)
            )
            .build()

        running(application) {
          val request = FakeRequest(
            GET,
            routes.ContractorDetailsCheckAnswersController
              .onPageLoadWithId(flowId)
              .url
          )

          val result = route(application, request).value
          val body   = contentAsString(result)

          status(result) mustEqual OK

          body must include("123 PA 87654321")
          body must include("1234567890")
          body must include("Test Contractor Scheme")
          body must include("contractor@test.com")
        }
      }

      "must render Continue when a valid target is supplied for a GET with id" in {

        val userAnswers =
          answersWithCisId
            .set(ContractorSchemePage(id), scheme)
            .success
            .value
            .set(AddSchemeNameYesNoPage(id), false)
            .success
            .value
            .set(AddEmailAddressYesNoPage(id), false)
            .success
            .value

        val repo = mockSessionRepository(Some(userAnswers))

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              inject.bind[SessionRepository].toInstance(repo)
            )
            .build()

        running(application) {
          val request = FakeRequest(
            GET,
            routes.ContractorDetailsCheckAnswersController
              .onPageLoadWithId(flowId, Some("manageYourCisReturn"))
              .url
          )

          val result = route(application, request).value
          val body   = contentAsString(result)

          status(result) mustEqual OK
          body must include("Continue")
          body must not include "Save and continue"
        }
      }

      "must update contractor details and redirect to the updated page for a POST with id" in {

        val userAnswers = completeAnswersWithId
        val repo        = mockSessionRepository(Some(userAnswers))
        val mockService = mock[ContractorDetailsService]

        when(
          mockService.updateContractorDetails(
            any[UpdateContractorSchemeParams]
          )(any[HeaderCarrier])
        ).thenReturn(Future.successful(()))

        val expectedRoute =
          routes.ContractorDetailsUpdatedController.onPageLoadWithId(flowId)

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              inject.bind[SessionRepository].toInstance(repo),
              inject.bind[ContractorDetailsService].toInstance(mockService),
              inject.bind[Navigator].toInstance(new FakeNavigator(expectedRoute))
            )
            .build()

        running(application) {
          val request = FakeRequest(
            POST,
            routes.ContractorDetailsCheckAnswersController
              .onSubmitWithId(flowId)
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual expectedRoute.url
        }

        val captor =
          ArgumentCaptor.forClass(classOf[UpdateContractorSchemeParams])

        verify(mockService).updateContractorDetails(
          captor.capture()
        )(any[HeaderCarrier])

        captor.getValue mustBe UpdateContractorSchemeParams(
          schemeId = 123,
          instanceId = "cisId",
          accountsOfficeReference = "123 PA 87654321",
          taxOfficeNumber = "123",
          taxOfficeReference = "45678",
          utr = Some("1234567890"),
          name = Some("Test Contractor Scheme"),
          emailAddress = Some("contractor@test.com"),
          version = scheme.version,
          displayWelcomePage = scheme.displayWelcomePage,
          prePopCount = scheme.prePopCount,
          prePopSuccessful = scheme.prePopSuccessful
        )
      }

      "must redirect to JourneyRecovery when the scheme is missing for a POST with id" in {

        val userAnswers = answersWithCisId
        val repo        = mockSessionRepository(Some(userAnswers))
        val mockService = mock[ContractorDetailsService]

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              inject.bind[SessionRepository].toInstance(repo),
              inject.bind[ContractorDetailsService].toInstance(mockService)
            )
            .build()

        running(application) {
          val request = FakeRequest(
            POST,
            routes.ContractorDetailsCheckAnswersController
              .onSubmitWithId(flowId)
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }

        verify(mockService, never())
          .updateContractorDetails(any[UpdateContractorSchemeParams])(any[HeaderCarrier])
      }
    }
  }
}

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

package controllers.amend

import base.SpecBase
import generators.ModelGenerators
import models.TypeOfSubcontractor.{Individualorsoletrader, Limitedcompany, Partnership, Trust}
import models.UserAnswers
import models.amend.AmendJourneyType
import models.response.{GetSubcontractorResponse, SubcontractorResponse}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{atLeastOnce, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.add.TypeOfSubcontractorPage
import pages.amend.AmendJourneyTypePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{CisManageService, SubcontractorService}
import uk.gov.hmrc.http.HeaderCarrier
import queries.CisIdQuery

import scala.jdk.CollectionConverters.*
import scala.concurrent.Future

class AmendSubcontractorControllerSpec
    extends SpecBase
    with MockitoSugar
    with ModelGenerators
    with GuiceOneAppPerSuite {

  private val cisId             = "INST-123"
  private val subbieResourceRef = 1001L

  private lazy val amendSubcontractorRoute =
    controllers.amend.routes.AmendSubcontractorController
      .onPageLoad(
        subbieResourceRef,
        "standard"
      )
      .url

  private val baseSubcontractor =
    SubcontractorResponse(
      subcontractorId = 1L,
      utr = Some("1123456789"),
      pageVisited = Some(2),
      partnerUtr = Some("2234567890"),
      crn = Some("12345678"),
      firstName = Some("John"),
      nino = Some("AA123456A"),
      secondName = Some("Middle"),
      surname = Some("Smith"),
      partnershipTradingName = Some("Test Partnership"),
      tradingName = Some("Test Trading Name"),
      subcontractorType = Some("soletrader"),
      addressLine1 = Some("12 Harbor View Road"),
      addressLine2 = Some("Amity Island"),
      addressLine3 = Some("Bodmin"),
      addressLine4 = Some("Cornwall"),
      country = Some("England"),
      postcode = Some("PL31 2HL"),
      emailAddress = Some("test@example.com"),
      phoneNumber = Some("02070000000"),
      mobilePhoneNumber = Some("07123456789"),
      worksReferenceNumber = Some("XLS345-MM"),
      createDate = None,
      lastUpdate = None,
      subbieResourceRef = Some(subbieResourceRef),
      matched = Some("Y"),
      autoVerified = Some("N"),
      verified = Some("Y"),
      verificationNumber = Some("V1234567890"),
      taxTreatment = Some("gross"),
      verificationDate = None,
      version = Some(3),
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = Some(0)
    )

  private def responseWith(
    subcontractor: Option[SubcontractorResponse]
  ): GetSubcontractorResponse =
    GetSubcontractorResponse(
      scheme = None,
      subcontractor = subcontractor
    )

  private def applicationWith(
    mockService: SubcontractorService,
    mockSessionRepository: SessionRepository
  ): GuiceApplicationBuilder = {

    val mockCisManagerService = mock[CisManageService]

    val answersWithCisId =
      emptyUserAnswers
        .set(CisIdQuery, cisId)
        .success
        .value

    when(mockCisManagerService.ensureCisIdInUserAnswers(any[UserAnswers])(any[HeaderCarrier]))
      .thenReturn(Future.successful(answersWithCisId))

    applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(
        bind[SubcontractorService].toInstance(mockService),
        bind[CisManageService].toInstance(mockCisManagerService),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
  }

  "AmendSubcontractorController" - {

    "onPageLoad" - {

      "must retrieve an individual subcontractor, save populated answers and redirect to individual CYA" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractor =
          baseSubcontractor.copy(
            subcontractorType = Some("soletrader")
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(responseWith(Some(subcontractor)))
        )

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationWith(mockService, mockSessionRepository).build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendSubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.amend.routes.AmendIndividualCheckYourAnswersController
              .onPageLoad(subbieResourceRef)
              .url

          verify(mockSessionRepository, times(2))
            .set(captor.capture())

          captor.getValue
            .get(TypeOfSubcontractorPage)
            .value mustBe Individualorsoletrader
        }
      }

      "must retrieve a company subcontractor, save populated answers and redirect to company CYA" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractor =
          baseSubcontractor.copy(
            subcontractorType = Some("company")
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(responseWith(Some(subcontractor)))
        )

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationWith(mockService, mockSessionRepository).build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendSubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.amend.company.routes.AmendCompanyCheckYourAnswersController
              .onPageLoad(subbieResourceRef)
              .url

          verify(mockSessionRepository, times(2))
            .set(captor.capture())

          captor.getValue
            .get(TypeOfSubcontractorPage)
            .value mustBe Limitedcompany
        }
      }

      "must retrieve a partnership subcontractor, save populated answers and redirect to partnership CYA" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractor =
          baseSubcontractor.copy(
            subcontractorType = Some("partnership")
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(responseWith(Some(subcontractor)))
        )

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationWith(mockService, mockSessionRepository).build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendSubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController
              .onPageLoad(subbieResourceRef)
              .url

          verify(mockSessionRepository, times(2))
            .set(captor.capture())

          captor.getValue
            .get(TypeOfSubcontractorPage)
            .value mustBe Partnership
        }
      }

      "must retrieve a trust subcontractor, save populated answers and redirect to trust CYA" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractor =
          baseSubcontractor.copy(
            subcontractorType = Some("trust")
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(responseWith(Some(subcontractor)))
        )

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application = applicationWith(mockService, mockSessionRepository).build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendSubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.amend.trust.routes.AmendTrustCheckYourAnswersController
              .onPageLoad(subbieResourceRef)
              .url

          verify(mockSessionRepository, times(2))
            .set(captor.capture())

          captor.getValue
            .get(TypeOfSubcontractorPage)
            .value mustBe Trust
        }
      }

      "must redirect to JourneyRecovery and not save when no subcontractor is returned" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            GetSubcontractorResponse(
              scheme = None,
              subcontractor = None
            )
          )
        )

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationWith(mockService, mockSessionRepository).build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendSubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(mockSessionRepository, atLeastOnce())
            .set(captor.capture())

          val savedAnswers = captor.getAllValues

          savedAnswers.forEach { answers =>
            answers.get(TypeOfSubcontractorPage) mustBe None
          }
        }
      }

      "must redirect to JourneyRecovery when getSubcontractor is missing" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        )
          .thenReturn(Future.failed(new RuntimeException("current batch failed")))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationWith(mockService, mockSessionRepository).build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendSubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(mockSessionRepository, atLeastOnce())
            .set(captor.capture())

          val savedAnswers = captor.getAllValues

          savedAnswers.forEach { answers =>
            answers.get(TypeOfSubcontractorPage) mustBe None
          }
        }
      }

      "must redirect to JourneyRecovery and not save when subcontractor type is missing" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

        val invalidSubcontractor =
          baseSubcontractor.copy(
            subcontractorType = None
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            responseWith(Some(invalidSubcontractor))
          )
        )

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationWith(mockService, mockSessionRepository).build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendSubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(mockSessionRepository, atLeastOnce())
            .set(captor.capture())

          captor.getAllValues.asScala.exists(
            _.get(TypeOfSubcontractorPage).isDefined
          ) mustBe false
        }
      }

      "must redirect to JourneyRecovery and not save when subcontractor type is unsupported" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

        val invalidSubcontractor =
          baseSubcontractor.copy(
            subcontractorType = Some("unsupported")
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            responseWith(Some(invalidSubcontractor))
          )
        )

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationWith(mockService, mockSessionRepository).build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendSubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(mockSessionRepository, atLeastOnce())
            .set(captor.capture())

          captor.getAllValues.asScala.exists(
            _.get(TypeOfSubcontractorPage).isDefined
          ) mustBe false
        }
      }

      "must redirect to JourneyRecovery when journey type is invalid" in {

        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        val invalidRoute =
          controllers.amend.routes.AmendSubcontractorController
            .onPageLoad(
              subbieResourceRef,
              "invalid-journey-type"
            )
            .url

        val application =
          applicationWith(mockService, mockSessionRepository).build()

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, invalidRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url
        }
      }

      "must save AmendJourneyTypePage as Standard when started from the standard amend journey" in {

        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractor =
          baseSubcontractor.copy(
            subcontractorType = Some("soletrader")
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            responseWith(Some(subcontractor))
          )
        )

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        val application =
          applicationWith(
            mockService,
            mockSessionRepository
          ).build()

        running(application) {

          val result =
            route(
              application,
              FakeRequest(
                GET,
                amendSubcontractorRoute
              )
            ).value

          status(result) mustBe SEE_OTHER

          verify(mockSessionRepository, atLeastOnce())
            .set(captor.capture())

          val savedAnswers =
            captor.getAllValues.asScala.toSeq

          savedAnswers.exists(
            _.get(AmendJourneyTypePage).contains(AmendJourneyType.Standard)
          ) mustBe true
        }
      }

    }
  }
}

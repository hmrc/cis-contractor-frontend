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

package controllers.info

import base.SpecBase
import models.UserAnswers
import models.response.{GetSubcontractorResponse, SubcontractorResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class SubcontractorControllerSpec extends SpecBase with MockitoSugar {

  private val cisId             = "INST-123"
  private val subbieResourceRef = 1001L

  private lazy val viewOnlySubcontractorRoute =
    controllers.info.routes.SubcontractorController
      .onPageLoad(cisId, subbieResourceRef)
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
    mockSessionRepository: SessionRepository,
    userAnswers: Option[UserAnswers] = Some(emptyUserAnswers)
  ) =
    applicationBuilder(userAnswers = userAnswers)
      .overrides(
        bind[SubcontractorService].toInstance(mockService),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
      .build()

  "SubcontractorController" - {

    "onPageLoad" - {

      "must retrieve an individual subcontractor, save populated answers and redirect to individual CYA" in {

        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        when(
          mockService.getSubcontractor(
            any[String],
            any[Long]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            responseWith(
              Some(
                baseSubcontractor.copy(
                  subcontractorType = Some("soletrader")
                )
              )
            )
          )
        )

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(Future.successful(true))

        val application =
          applicationWith(mockService, mockSessionRepository)

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, viewOnlySubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe defined

          redirectLocation(result).value mustBe
            controllers.info.routes.IndividualCheckYourAnswersController
              .onPageLoad()
              .url

          verify(mockService, times(1))
            .getSubcontractor(
              any[String],
              any[Long]
            )(any[HeaderCarrier])

          verify(mockSessionRepository, times(1))
            .set(any[UserAnswers])
        }
      }

      "must retrieve a company subcontractor, save populated answers and redirect to company CYA" in {

        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        when(
          mockService.getSubcontractor(
            any[String],
            any[Long]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            responseWith(
              Some(
                baseSubcontractor.copy(
                  subcontractorType = Some("company")
                )
              )
            )
          )
        )

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(Future.successful(true))

        val application =
          applicationWith(mockService, mockSessionRepository)

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, viewOnlySubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe defined

          redirectLocation(result).value mustBe
            controllers.info.company.routes.CompanyCheckYourAnswersController
              .onPageLoad()
              .url

          verify(mockService, times(1))
            .getSubcontractor(
              any[String],
              any[Long]
            )(any[HeaderCarrier])

          verify(mockSessionRepository, times(1))
            .set(any[UserAnswers])
        }
      }

      "must retrieve a partnership subcontractor, save populated answers and redirect to partnership CYA" in {

        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        when(
          mockService.getSubcontractor(
            any[String],
            any[Long]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            responseWith(
              Some(
                baseSubcontractor.copy(
                  subcontractorType = Some("partnership")
                )
              )
            )
          )
        )

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(Future.successful(true))

        val application =
          applicationWith(mockService, mockSessionRepository)

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, viewOnlySubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe defined

          redirectLocation(result).value mustBe
            controllers.info.partnership.routes.PartnershipCheckYourAnswersController
              .onPageLoad()
              .url

          verify(mockService, times(1))
            .getSubcontractor(
              any[String],
              any[Long]
            )(any[HeaderCarrier])

          verify(mockSessionRepository, times(1))
            .set(any[UserAnswers])
        }
      }

      "must retrieve a trust subcontractor, save populated answers and redirect to trust CYA" in {

        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        when(
          mockService.getSubcontractor(
            any[String],
            any[Long]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            responseWith(
              Some(
                baseSubcontractor.copy(
                  subcontractorType = Some("trust")
                )
              )
            )
          )
        )

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(Future.successful(true))

        val application =
          applicationWith(mockService, mockSessionRepository)

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, viewOnlySubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe defined

          redirectLocation(result).value mustBe
            controllers.info.trust.routes.TrustCheckYourAnswersController
              .onPageLoad()
              .url

          verify(mockService, times(1))
            .getSubcontractor(
              any[String],
              any[Long]
            )(any[HeaderCarrier])

          verify(mockSessionRepository, times(1))
            .set(any[UserAnswers])
        }
      }

      "must redirect to JourneyRecovery and not save when no subcontractor is returned" in {

        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        when(
          mockService.getSubcontractor(
            any[String],
            any[Long]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(responseWith(None))
        )

        val application =
          applicationWith(
            mockService,
            mockSessionRepository,
            Some(emptyUserAnswers)
          )

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, viewOnlySubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe defined

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(mockSessionRepository, never())
            .set(any[UserAnswers])
        }
      }

      "must redirect to JourneyRecovery and not save when subcontractor type is missing" in {

        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        val subcontractor =
          baseSubcontractor.copy(
            subcontractorType = None
          )

        when(
          mockService.getSubcontractor(
            any[String],
            any[Long]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            responseWith(Some(subcontractor))
          )
        )

        val application =
          applicationWith(
            mockService,
            mockSessionRepository,
            Some(emptyUserAnswers)
          )

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, viewOnlySubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe defined

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(mockSessionRepository, never())
            .set(any[UserAnswers])
        }
      }

      "must redirect to JourneyRecovery and not save when subcontractor type is unsupported" in {

        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        val subcontractor =
          baseSubcontractor.copy(
            subcontractorType = Some("unsupported")
          )

        when(
          mockService.getSubcontractor(
            any[String],
            any[Long]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            responseWith(Some(subcontractor))
          )
        )

        val application =
          applicationWith(
            mockService,
            mockSessionRepository,
            Some(emptyUserAnswers)
          )

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, viewOnlySubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe defined

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(mockSessionRepository, never())
            .set(any[UserAnswers])
        }
      }

      "must redirect to JourneyRecovery and not save when service fails" in {

        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        when(
          mockService.getSubcontractor(
            any[String],
            any[Long]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.failed(
            new RuntimeException("service failure")
          )
        )

        val application =
          applicationWith(
            mockService,
            mockSessionRepository,
            Some(emptyUserAnswers)
          )

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, viewOnlySubcontractorRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe defined

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(mockSessionRepository, never())
            .set(any[UserAnswers])
        }
      }
    }
  }
}

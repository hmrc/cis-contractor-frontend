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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.TypeOfSubcontractor.Trust
import models.UserAnswers
import models.address.{Address, Country}
import models.amend.trust.OriginalTrustAnswers
import models.contact.ContactMethodOptions.{Email, Mobile, Phone}
import models.response.{GetSubcontractorResponse, SubcontractorResponse}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.TypeOfSubcontractorPage
import pages.add.trust.*
import play.api.inject.bind
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalTrustAnswersQuery}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class AmendTrustControllerSpec extends SpecBase with MockitoSugar {

  private val cisId             = "INST-123"
  private val subbieResourceRef = 1001L

  private lazy val amendTrustRoute =
    controllers.amend.trust.routes.AmendTrustController
      .onPageLoad(cisId, subbieResourceRef)
      .url

  private val trustName      = "Test Trust"
  private val emailAddress   = "trust@example.com"
  private val phoneNumber    = "02070000000"
  private val mobileNumber   = "07123456789"
  private val utr            = "1123456789"
  private val worksReference = "XLS345-MM"

  private val expectedAddress =
    Address(
      addressLine1 = "12 Harbor View Road",
      addressLine2 = Some("Amity Island"),
      addressLine3 = Some("Bodmin"),
      addressLine4 = Some("Cornwall"),
      postcode = Some("PL31 2HL"),
      country = Some(
        Country(
          code = None,
          name = Some("England")
        )
      )
    )

  private val subcontractor =
    SubcontractorResponse(
      subcontractorId = 1L,
      utr = Some(utr),
      pageVisited = Some(2),
      partnerUtr = None,
      crn = None,
      firstName = None,
      nino = None,
      secondName = None,
      surname = None,
      partnershipTradingName = None,
      tradingName = Some(trustName),
      subcontractorType = Some("trust"),
      addressLine1 = Some("12 Harbor View Road"),
      addressLine2 = Some("Amity Island"),
      addressLine3 = Some("Bodmin"),
      addressLine4 = Some("Cornwall"),
      country = Some("England"),
      postcode = Some("PL31 2HL"),
      emailAddress = Some(emailAddress),
      phoneNumber = Some(phoneNumber),
      mobilePhoneNumber = Some(mobileNumber),
      worksReferenceNumber = Some(worksReference),
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
      pendingVerifications = Some(1)
    )

  private val response =
    GetSubcontractorResponse(
      scheme = None,
      subcontractor = Some(subcontractor),
      otherInfo = Seq.empty
    )

  private val expectedOriginal =
    OriginalTrustAnswers(
      trustName = Some(trustName),
      addressYesNo = Some(true),
      address = Some(expectedAddress),
      trustContactMethodsYesNo = Some(true),
      trustContactMethod = Set(Email, Phone, Mobile),
      email = Some(emailAddress),
      phone = Some(phoneNumber),
      mobile = Some(mobileNumber),
      utrYesNo = Some(true),
      utr = Some(utr),
      worksReferenceYesNo = Some(true),
      worksReference = Some(worksReference)
    )

  "AmendTrustController" - {

    "onPageLoad" - {

      "must retrieve the subcontractor, save populated answers and redirect to the trust details page" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(Future.successful(response))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendTrustRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.amend.trust.routes.AmendTrustCheckYourAnswersController
              .onPageLoad()
              .url

          verify(mockService, times(1))
            .getSubcontractor(
              eqTo(cisId),
              eqTo(subbieResourceRef)
            )(any[HeaderCarrier])

          verify(mockSessionRepository, times(1))
            .set(any[UserAnswers])
        }
      }

      "must save all trust answers returned by the backend" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(Future.successful(response))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendTrustRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(TypeOfSubcontractorPage)
            .value mustBe Trust

          savedAnswers
            .get(TrustNamePage)
            .value mustBe trustName

          savedAnswers
            .get(TrustAddressYesNoPage)
            .value mustBe true

          savedAnswers
            .get(TrustAddressPage)
            .value mustBe expectedAddress

          savedAnswers
            .get(AddTrustContactMethodsYesNoPage)
            .value mustBe true

          savedAnswers
            .get(TrustContactMethodOptionsPage)
            .value mustBe Set(Email, Phone, Mobile)

          savedAnswers
            .get(TrustEmailAddressPage)
            .value mustBe emailAddress

          savedAnswers
            .get(TrustPhoneNumberPage)
            .value mustBe phoneNumber

          savedAnswers
            .get(TrustMobileNumberPage)
            .value mustBe mobileNumber

          savedAnswers
            .get(TrustUtrYesNoPage)
            .value mustBe true

          savedAnswers
            .get(TrustUtrPage)
            .value mustBe utr

          savedAnswers
            .get(TrustWorksReferenceYesNoPage)
            .value mustBe true

          savedAnswers
            .get(TrustWorksReferencePage)
            .value mustBe worksReference

          savedAnswers
            .get(CisIdQuery)
            .value mustBe cisId

          savedAnswers
            .get(OriginalTrustAnswersQuery)
            .value mustBe expectedOriginal
        }
      }

      "must use tradingName as the trust name when it is present" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val updatedSubcontractor =
          subcontractor.copy(
            tradingName = Some("Trading Trust"),
            partnershipTradingName = Some("Partnership Fallback")
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            response.copy(
              subcontractor = Some(updatedSubcontractor)
            )
          )
        )

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendTrustRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(TrustNamePage)
            .value mustBe "Trading Trust"

          savedAnswers
            .get(OriginalTrustAnswersQuery)
            .value
            .trustName mustBe Some("Trading Trust")
        }
      }

      "must use partnershipTradingName when tradingName is missing" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val updatedSubcontractor =
          subcontractor.copy(
            tradingName = None,
            partnershipTradingName = Some("Fallback Trust")
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            response.copy(
              subcontractor = Some(updatedSubcontractor)
            )
          )
        )

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendTrustRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(TrustNamePage)
            .value mustBe "Fallback Trust"

          savedAnswers
            .get(OriginalTrustAnswersQuery)
            .value
            .trustName mustBe Some("Fallback Trust")
        }
      }

      "must not populate the trust name when both name fields are missing" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val updatedSubcontractor =
          subcontractor.copy(
            tradingName = None,
            partnershipTradingName = None
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            response.copy(
              subcontractor = Some(updatedSubcontractor)
            )
          )
        )

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendTrustRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers.get(TrustNamePage) mustBe None

          savedAnswers
            .get(OriginalTrustAnswersQuery)
            .value
            .trustName mustBe None
        }
      }

      "must set only email as the contact method when only email is returned" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val updatedSubcontractor =
          subcontractor.copy(
            phoneNumber = None,
            mobilePhoneNumber = None
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            response.copy(
              subcontractor = Some(updatedSubcontractor)
            )
          )
        )

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendTrustRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(AddTrustContactMethodsYesNoPage)
            .value mustBe true

          savedAnswers
            .get(TrustContactMethodOptionsPage)
            .value mustBe Set(Email)

          savedAnswers
            .get(TrustEmailAddressPage)
            .value mustBe emailAddress

          savedAnswers.get(TrustPhoneNumberPage) mustBe None
          savedAnswers.get(TrustMobileNumberPage) mustBe None

          savedAnswers
            .get(OriginalTrustAnswersQuery)
            .value
            .trustContactMethod mustBe Set(Email)
        }
      }

      "must set phone and mobile as contact methods when email is missing" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val updatedSubcontractor =
          subcontractor.copy(
            emailAddress = None
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            response.copy(
              subcontractor = Some(updatedSubcontractor)
            )
          )
        )

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendTrustRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(TrustContactMethodOptionsPage)
            .value mustBe Set(Phone, Mobile)

          savedAnswers.get(TrustEmailAddressPage) mustBe None

          savedAnswers
            .get(TrustPhoneNumberPage)
            .value mustBe phoneNumber

          savedAnswers
            .get(TrustMobileNumberPage)
            .value mustBe mobileNumber
        }
      }

      "must set contact-method answer to false and leave contact pages empty when no contact details are returned" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val updatedSubcontractor =
          subcontractor.copy(
            emailAddress = None,
            phoneNumber = None,
            mobilePhoneNumber = None
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            response.copy(
              subcontractor = Some(updatedSubcontractor)
            )
          )
        )

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendTrustRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(AddTrustContactMethodsYesNoPage)
            .value mustBe false

          savedAnswers.get(TrustContactMethodOptionsPage) mustBe None
          savedAnswers.get(TrustEmailAddressPage) mustBe None
          savedAnswers.get(TrustPhoneNumberPage) mustBe None
          savedAnswers.get(TrustMobileNumberPage) mustBe None

          val original =
            savedAnswers
              .get(OriginalTrustAnswersQuery)
              .value

          original.trustContactMethodsYesNo mustBe Some(false)
          original.trustContactMethod mustBe Set.empty
          original.email mustBe None
          original.phone mustBe None
          original.mobile mustBe None
        }
      }

      "must set address answer to false and leave the address page empty when address line 1 is missing" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val updatedSubcontractor =
          subcontractor.copy(
            addressLine1 = None,
            addressLine2 = None,
            addressLine3 = None,
            addressLine4 = None,
            postcode = None,
            country = None
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            response.copy(
              subcontractor = Some(updatedSubcontractor)
            )
          )
        )

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendTrustRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(TrustAddressYesNoPage)
            .value mustBe false

          savedAnswers.get(TrustAddressPage) mustBe None

          val original =
            savedAnswers
              .get(OriginalTrustAnswersQuery)
              .value

          original.addressYesNo mustBe Some(false)
          original.address mustBe None
        }
      }

      "must create the country with no country code" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(Future.successful(response))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendTrustRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val country =
            captor.getValue
              .get(TrustAddressPage)
              .value
              .country
              .value

          country.code mustBe None
          country.name mustBe Some("England")
        }
      }

      "must set UTR and works-reference answers to false when identifiers are missing" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val updatedSubcontractor =
          subcontractor.copy(
            utr = None,
            worksReferenceNumber = None
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            response.copy(
              subcontractor = Some(updatedSubcontractor)
            )
          )
        )

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendTrustRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(TrustUtrYesNoPage)
            .value mustBe false

          savedAnswers.get(TrustUtrPage) mustBe None

          savedAnswers
            .get(TrustWorksReferenceYesNoPage)
            .value mustBe false

          savedAnswers.get(TrustWorksReferencePage) mustBe None

          val original =
            savedAnswers
              .get(OriginalTrustAnswersQuery)
              .value

          original.utrYesNo mustBe Some(false)
          original.utr mustBe None
          original.worksReferenceYesNo mustBe Some(false)
          original.worksReference mustBe None
        }
      }

      "must redirect to JourneyRecovery and not save when no subcontractor is returned" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            response.copy(subcontractor = None)
          )
        )

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendTrustRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(mockSessionRepository, never())
            .set(any[UserAnswers])
        }
      }

      "must redirect to JourneyRecovery when the service fails" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.failed(new RuntimeException("Backend unavailable"))
        )

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendTrustRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(mockSessionRepository, never())
            .set(any[UserAnswers])
        }
      }

      "must redirect to JourneyRecovery when saving the session fails" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(Future.successful(response))

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(
            Future.failed(new RuntimeException("DB unavailable"))
          )

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendTrustRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(mockSessionRepository, times(1))
            .set(any[UserAnswers])
        }
      }

      "must redirect to JourneyRecovery and not call the service when no user answers exist" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        val application =
          applicationBuilder(userAnswers = None)
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendTrustRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(mockService, never())
            .getSubcontractor(
              any[String],
              any[Long]
            )(any[HeaderCarrier])

          verify(mockSessionRepository, never())
            .set(any[UserAnswers])
        }
      }

      "must redirect to JourneyRecovery and not save when populating user answers fails" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(Future.successful(response))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SubcontractorService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[AmendTrustController]
                .to[FailingPopulateAmendTrustController]
            )
            .build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendTrustRoute)
            ).value

          status(result) mustBe SEE_OTHER

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

class FailingPopulateAmendTrustController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subcontractorService: SubcontractorService,
  sessionRepository: SessionRepository,
  controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends AmendTrustController(
      identify,
      getData,
      requireData,
      subcontractorService,
      sessionRepository,
      controllerComponents
    ) {

  override protected def populateUserAnswers(
    userAnswers: UserAnswers,
    cisId: String,
    subcontractor: SubcontractorResponse
  ): Try[UserAnswers] =
    Failure(new RuntimeException("intentional population failure"))
}

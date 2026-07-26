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

package controllers.amend.partnership

import base.SpecBase
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.TypeOfSubcontractor.Partnership
import models.UserAnswers
import models.address.{Address, Country}
import models.amend.partnership.OriginalPartnershipAnswers
import models.contact.ContactMethodOptions.{Email, Mobile, Phone}
import models.response.{GetSubcontractorResponse, SubcontractorResponse}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.*
import pages.add.partnership.*
import play.api.inject.bind
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalPartnershipAnswersQuery}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class AmendPartnershipControllerSpec extends SpecBase with MockitoSugar {

  private val cisId             = "INST-123"
  private val subbieResourceRef = 1001L

  private lazy val amendPartnershipRoute =
    controllers.amend.partnership.routes.AmendPartnershipController
      .onPageLoad(cisId, subbieResourceRef)
      .url

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
      utr = Some("7777777777"),
      pageVisited = Some(2),
      partnerUtr = Some("8777777777"),
      crn = Some("AC012345"),
      firstName = Some("Martin"),
      nino = Some("QQ123456C"),
      secondName = Some("James"),
      surname = Some("Brody"),
      partnershipTradingName = Some("Brody Partnership"),
      tradingName = Some("Fallback Partnership"),
      subcontractorType = Some("partnership"),
      addressLine1 = Some("12 Harbor View Road"),
      addressLine2 = Some("Amity Island"),
      addressLine3 = Some("Bodmin"),
      addressLine4 = Some("Cornwall"),
      country = Some("England"),
      postcode = Some("PL31 2HL"),
      emailAddress = Some("partnership@example.com"),
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
      pendingVerifications = Some(1)
    )

  private val response =
    GetSubcontractorResponse(
      scheme = None,
      subcontractor = Some(subcontractor),
      otherInfo = Seq.empty
    )

  private val expectedOriginal =
    OriginalPartnershipAnswers(
      partnershipName = Some("Brody Partnership"),
      addressYesNo = Some(true),
      address = Some(expectedAddress),
      partnershipContactMethodsYesNo = Some(true),
      partnershipContactMethodOptions = Some(Set(Email, Phone, Mobile)),
      email = Some("partnership@example.com"),
      phone = Some("02070000000"),
      mobile = Some("07123456789"),
      hasUtrYesNo = Some(true),
      utr = Some("7777777777"),
      nominatedPartnerName = Some("Martin James Brody"),
      nominatedPartnerUtrYesNo = Some(true),
      nominatedPartnerUtr = Some("8777777777"),
      nominatedPartnerNinoYesNo = Some(true),
      nominatedPartnerNino = Some("QQ123456C"),
      nominatedPartnerCrnYesNo = Some(true),
      nominatedPartnerCrn = Some("AC012345"),
      nominatedPartnerWorksReferenceYesNo = Some(true),
      nominatedPartnerWorksReference = Some("XLS345-MM")
    )

  "AmendPartnershipController" - {

    "onPageLoad" - {

      "must retrieve the subcontractor, save populated answers and redirect to the partnership details page" in {
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
              FakeRequest(GET, amendPartnershipRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController
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

      "must save all partnership answers returned by the backend" in {
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
            FakeRequest(GET, amendPartnershipRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(TypeOfSubcontractorPage)
            .value mustBe Partnership

          savedAnswers
            .get(PartnershipNamePage)
            .value mustBe "Brody Partnership"

          savedAnswers
            .get(PartnershipAddressYesNoPage)
            .value mustBe true

          savedAnswers
            .get(PartnershipAddressPage)
            .value mustBe expectedAddress

          savedAnswers
            .get(AddPartnershipContactMethodsYesNoPage)
            .value mustBe true

          savedAnswers
            .get(PartnershipContactMethodOptionsPage)
            .value mustBe Set(Email, Phone, Mobile)

          savedAnswers
            .get(PartnershipEmailAddressPage)
            .value mustBe "partnership@example.com"

          savedAnswers
            .get(PartnershipPhoneNumberPage)
            .value mustBe "02070000000"

          savedAnswers
            .get(PartnershipMobileNumberPage)
            .value mustBe "07123456789"

          savedAnswers
            .get(PartnershipHasUtrYesNoPage)
            .value mustBe true

          savedAnswers
            .get(PartnershipUniqueTaxpayerReferencePage)
            .value mustBe "7777777777"

          savedAnswers
            .get(PartnershipNominatedPartnerNamePage)
            .value mustBe "Martin James Brody"

          savedAnswers
            .get(PartnershipNominatedPartnerUtrYesNoPage)
            .value mustBe true

          savedAnswers
            .get(PartnershipNominatedPartnerUtrPage)
            .value mustBe "8777777777"

          savedAnswers
            .get(PartnershipNominatedPartnerNinoYesNoPage)
            .value mustBe true

          savedAnswers
            .get(PartnershipNominatedPartnerNinoPage)
            .value mustBe "QQ123456C"

          savedAnswers
            .get(PartnershipNominatedPartnerCrnYesNoPage)
            .value mustBe true

          savedAnswers
            .get(PartnershipNominatedPartnerCrnPage)
            .value mustBe "AC012345"

          savedAnswers
            .get(PartnershipWorksReferenceNumberYesNoPage)
            .value mustBe true

          savedAnswers
            .get(PartnershipWorksReferenceNumberPage)
            .value mustBe "XLS345-MM"

          savedAnswers
            .get(CisIdQuery)
            .value mustBe cisId

          savedAnswers
            .get(OriginalPartnershipAnswersQuery)
            .value mustBe expectedOriginal
        }
      }

      "must prefer partnershipTradingName over tradingName" in {
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
            FakeRequest(GET, amendPartnershipRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          captor.getValue
            .get(PartnershipNamePage)
            .value mustBe "Brody Partnership"
        }
      }

      "must use tradingName when partnershipTradingName is missing" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithoutPartnershipName =
          subcontractor.copy(
            partnershipTradingName = None,
            tradingName = Some("Fallback Partnership")
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            response.copy(
              subcontractor = Some(subcontractorWithoutPartnershipName)
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
            FakeRequest(GET, amendPartnershipRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(PartnershipNamePage)
            .value mustBe "Fallback Partnership"

          savedAnswers
            .get(OriginalPartnershipAnswersQuery)
            .value
            .partnershipName mustBe Some("Fallback Partnership")
        }
      }

      "must build the nominated partner name from first, second and surname" in {
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
            FakeRequest(GET, amendPartnershipRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          captor.getValue
            .get(PartnershipNominatedPartnerNamePage)
            .value mustBe "Martin James Brody"
        }
      }

      "must build the nominated partner name without extra spaces when second name is missing" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithoutSecondName =
          subcontractor.copy(secondName = None)

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            response.copy(
              subcontractor = Some(subcontractorWithoutSecondName)
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
            FakeRequest(GET, amendPartnershipRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          captor.getValue
            .get(PartnershipNominatedPartnerNamePage)
            .value mustBe "Martin Brody"
        }
      }

      "must not populate the nominated partner name when all name fields are missing" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithoutName =
          subcontractor.copy(
            firstName = None,
            secondName = None,
            surname = None
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            response.copy(
              subcontractor = Some(subcontractorWithoutName)
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
            FakeRequest(GET, amendPartnershipRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(PartnershipNominatedPartnerNamePage) mustBe None

          savedAnswers
            .get(OriginalPartnershipAnswersQuery)
            .value
            .nominatedPartnerName mustBe None
        }
      }

      "must set only the contact methods returned by the backend" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithEmailOnly =
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
              subcontractor = Some(subcontractorWithEmailOnly)
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
            FakeRequest(GET, amendPartnershipRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(AddPartnershipContactMethodsYesNoPage)
            .value mustBe true

          savedAnswers
            .get(PartnershipContactMethodOptionsPage)
            .value mustBe Set(Email)

          savedAnswers
            .get(PartnershipEmailAddressPage)
            .value mustBe "partnership@example.com"

          savedAnswers
            .get(PartnershipPhoneNumberPage) mustBe None

          savedAnswers
            .get(PartnershipMobileNumberPage) mustBe None
        }
      }

      "must set contact methods to false and leave contact pages empty when none are returned" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithoutContacts =
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
              subcontractor = Some(subcontractorWithoutContacts)
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
            FakeRequest(GET, amendPartnershipRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(AddPartnershipContactMethodsYesNoPage)
            .value mustBe false

          savedAnswers
            .get(PartnershipContactMethodOptionsPage) mustBe None

          savedAnswers
            .get(PartnershipEmailAddressPage) mustBe None

          savedAnswers
            .get(PartnershipPhoneNumberPage) mustBe None

          savedAnswers
            .get(PartnershipMobileNumberPage) mustBe None

          savedAnswers
            .get(OriginalPartnershipAnswersQuery)
            .value
            .partnershipContactMethodOptions mustBe None
        }
      }

      "must set identifier yes-no answers to false and leave value pages empty when identifiers are absent" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithoutIdentifiers =
          subcontractor.copy(
            utr = None,
            partnerUtr = None,
            nino = None,
            crn = None,
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
              subcontractor = Some(subcontractorWithoutIdentifiers)
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
            FakeRequest(GET, amendPartnershipRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(PartnershipHasUtrYesNoPage)
            .value mustBe false

          savedAnswers
            .get(PartnershipUniqueTaxpayerReferencePage) mustBe None

          savedAnswers
            .get(PartnershipNominatedPartnerUtrYesNoPage)
            .value mustBe false

          savedAnswers
            .get(PartnershipNominatedPartnerUtrPage) mustBe None

          savedAnswers
            .get(PartnershipNominatedPartnerNinoYesNoPage)
            .value mustBe false

          savedAnswers
            .get(PartnershipNominatedPartnerNinoPage) mustBe None

          savedAnswers
            .get(PartnershipNominatedPartnerCrnYesNoPage)
            .value mustBe false

          savedAnswers
            .get(PartnershipNominatedPartnerCrnPage) mustBe None

          savedAnswers
            .get(PartnershipWorksReferenceNumberYesNoPage)
            .value mustBe false

          savedAnswers
            .get(PartnershipWorksReferenceNumberPage) mustBe None
        }
      }

      "must set address yes-no to false and leave address page empty when address line 1 is absent" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithoutAddress =
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
              subcontractor = Some(subcontractorWithoutAddress)
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
            FakeRequest(GET, amendPartnershipRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(PartnershipAddressYesNoPage)
            .value mustBe false

          savedAnswers
            .get(PartnershipAddressPage) mustBe None

          savedAnswers
            .get(OriginalPartnershipAnswersQuery)
            .value
            .address mustBe None
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
              FakeRequest(GET, amendPartnershipRoute)
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
              FakeRequest(GET, amendPartnershipRoute)
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
              FakeRequest(GET, amendPartnershipRoute)
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
              FakeRequest(GET, amendPartnershipRoute)
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
              bind[AmendPartnershipController]
                .to[FailingPopulateAmendPartnershipController]
            )
            .build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendPartnershipRoute)
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

class FailingPopulateAmendPartnershipController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subcontractorService: SubcontractorService,
  sessionRepository: SessionRepository,
  controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends AmendPartnershipController(
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
    Failure(new RuntimeException("Unable to populate UserAnswers"))
}

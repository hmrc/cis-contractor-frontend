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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.TypeOfSubcontractor.Individualorsoletrader
import models.UserAnswers
import models.add.SubcontractorName
import models.address.{Address, Country}
import models.amend.OriginalIndividualAnswers
import models.contact.ContactMethodOptions.{Email, Mobile, Phone}
import models.response.{GetSubcontractorResponse, SubcontractorResponse}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.*
import play.api.inject.bind
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalIndividualAnswersQuery}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class AmendIndividualControllerSpec extends SpecBase with MockitoSugar {

  private val cisId             = "INST-123"
  private val subbieResourceRef = 1001L

  private lazy val amendIndividualRoute =
    controllers.amend.routes.AmendIndividualController
      .onPageLoad(cisId, subbieResourceRef)
      .url

  private val emailAddress = "martin@example.com"
  private val phoneNumber  = "02071234567"
  private val mobileNumber = "07123456789"

  private val expectedName =
    SubcontractorName(
      firstName = "Martin",
      middleName = None,
      lastName = "Brody"
    )

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
      utr = Some("3992651526"),
      pageVisited = Some(1),
      partnerUtr = None,
      crn = None,
      firstName = Some("Martin"),
      nino = Some("QQ123456C"),
      secondName = None,
      surname = Some("Brody"),
      partnershipTradingName = None,
      tradingName = None,
      subcontractorType = Some("soletrader"),
      addressLine1 = Some("12 Harbor View Road"),
      addressLine2 = Some("Amity Island"),
      addressLine3 = Some("Bodmin"),
      addressLine4 = Some("Cornwall"),
      country = Some("England"),
      postcode = Some("PL31 2HL"),
      emailAddress = Some(emailAddress),
      phoneNumber = Some(phoneNumber),
      mobilePhoneNumber = Some(mobileNumber),
      worksReferenceNumber = Some("XLS345-MM"),
      createDate = None,
      lastUpdate = None,
      subbieResourceRef = Some(subbieResourceRef),
      matched = None,
      autoVerified = None,
      verified = None,
      verificationNumber = None,
      taxTreatment = None,
      verificationDate = None,
      version = Some(1),
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = None
    )

  private val response =
    GetSubcontractorResponse(
      scheme = None,
      subcontractor = Some(subcontractor),
      otherInfo = Seq.empty
    )

  private val expectedOriginal =
    OriginalIndividualAnswers(
      usesTradingName = Some(false),
      tradingName = None,
      subcontractorName = Some(expectedName),
      address = Some(expectedAddress),
      individualContactMethod = Some(Set(Email, Phone, Mobile)),
      email = Some(emailAddress),
      phone = Some(phoneNumber),
      mobile = Some(mobileNumber),
      utr = Some("3992651526"),
      nino = Some("QQ123456C"),
      worksReference = Some("XLS345-MM")
    )

  private def mockSuccessfulService(
    mockService: SubcontractorService,
    returnedResponse: GetSubcontractorResponse = response
  ): Unit =
    when(
      mockService.getSubcontractor(
        eqTo(cisId),
        eqTo(subbieResourceRef)
      )(any[HeaderCarrier])
    ).thenReturn(Future.successful(returnedResponse))

  private def mockSuccessfulRepository(
    mockSessionRepository: SessionRepository
  ): Unit =
    when(
      mockSessionRepository.set(any[UserAnswers])
    ).thenReturn(Future.successful(true))

  "AmendIndividualController" - {

    "onPageLoad" - {

      "must retrieve the subcontractor, save populated answers and redirect to the individual details page" in {
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        mockSuccessfulService(mockService)
        mockSuccessfulRepository(mockSessionRepository)

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendIndividualRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.amend.routes.AmendIndividualCheckYourAnswersController
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

      "must save all individual answers returned by the backend" in {
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        val captor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        mockSuccessfulService(mockService)
        mockSuccessfulRepository(mockSessionRepository)

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository)
            .set(captor.capture())

          val savedAnswers =
            captor.getValue

          savedAnswers
            .get(TypeOfSubcontractorPage)
            .value mustBe Individualorsoletrader

          savedAnswers
            .get(SubTradingNameYesNoPage)
            .value mustBe false

          savedAnswers
            .get(TradingNameOfSubcontractorPage) mustBe None

          savedAnswers
            .get(SubcontractorNamePage)
            .value mustBe expectedName

          savedAnswers
            .get(SubAddressYesNoPage)
            .value mustBe true

          savedAnswers
            .get(AddressOfSubcontractorPage)
            .value mustBe expectedAddress

          savedAnswers
            .get(AddIndividualContactMethodsYesNoPage)
            .value mustBe true

          savedAnswers
            .get(IndividualContactMethodOptionsPage)
            .value mustBe Set(Email, Phone, Mobile)

          savedAnswers
            .get(IndividualEmailAddressPage)
            .value mustBe emailAddress

          savedAnswers
            .get(IndividualPhoneNumberPage)
            .value mustBe phoneNumber

          savedAnswers
            .get(IndividualMobileNumberPage)
            .value mustBe mobileNumber

          savedAnswers
            .get(UniqueTaxpayerReferenceYesNoPage)
            .value mustBe true

          savedAnswers
            .get(SubcontractorsUniqueTaxpayerReferencePage)
            .value mustBe "3992651526"

          savedAnswers
            .get(NationalInsuranceNumberYesNoPage)
            .value mustBe true

          savedAnswers
            .get(SubNationalInsuranceNumberPage)
            .value mustBe "QQ123456C"

          savedAnswers
            .get(WorksReferenceNumberYesNoPage)
            .value mustBe true

          savedAnswers
            .get(WorksReferenceNumberPage)
            .value mustBe "XLS345-MM"

          savedAnswers
            .get(CisIdQuery)
            .value mustBe cisId

          savedAnswers
            .get(OriginalIndividualAnswersQuery)
            .value mustBe expectedOriginal
        }
      }

      "must populate trading-name answers when a trading name is returned" in {
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        val captor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithTradingName =
          subcontractor.copy(
            tradingName = Some("Brody Construction")
          )

        mockSuccessfulService(
          mockService,
          response.copy(
            subcontractor = Some(subcontractorWithTradingName)
          )
        )

        mockSuccessfulRepository(mockSessionRepository)

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository)
            .set(captor.capture())

          val savedAnswers =
            captor.getValue

          savedAnswers
            .get(SubTradingNameYesNoPage)
            .value mustBe true

          savedAnswers
            .get(TradingNameOfSubcontractorPage)
            .value mustBe "Brody Construction"

          val original =
            savedAnswers
              .get(OriginalIndividualAnswersQuery)
              .value

          original.usesTradingName mustBe Some(true)
          original.tradingName mustBe
            Some("Brody Construction")
        }
      }

      "must set uses-trading-name to false when a trading name contains only whitespace" in {
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        val captor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithBlankTradingName =
          subcontractor.copy(
            tradingName = Some("   ")
          )

        mockSuccessfulService(
          mockService,
          response.copy(
            subcontractor = Some(subcontractorWithBlankTradingName)
          )
        )

        mockSuccessfulRepository(mockSessionRepository)

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository)
            .set(captor.capture())

          val savedAnswers =
            captor.getValue

          savedAnswers
            .get(SubTradingNameYesNoPage)
            .value mustBe false

          savedAnswers
            .get(TradingNameOfSubcontractorPage)
            .value mustBe "   "

          savedAnswers
            .get(OriginalIndividualAnswersQuery)
            .value
            .usesTradingName mustBe Some(false)
        }
      }

      "must populate only email contact details when only email is returned" in {
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        val captor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val emailOnlySubcontractor =
          subcontractor.copy(
            phoneNumber = None,
            mobilePhoneNumber = None
          )

        mockSuccessfulService(
          mockService,
          response.copy(
            subcontractor = Some(emailOnlySubcontractor)
          )
        )

        mockSuccessfulRepository(mockSessionRepository)

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository)
            .set(captor.capture())

          val savedAnswers =
            captor.getValue

          savedAnswers
            .get(AddIndividualContactMethodsYesNoPage)
            .value mustBe true

          savedAnswers
            .get(IndividualContactMethodOptionsPage)
            .value mustBe Set(Email)

          savedAnswers
            .get(IndividualEmailAddressPage)
            .value mustBe emailAddress

          savedAnswers
            .get(IndividualPhoneNumberPage) mustBe None

          savedAnswers
            .get(IndividualMobileNumberPage) mustBe None

          val original =
            savedAnswers
              .get(OriginalIndividualAnswersQuery)
              .value

          original.individualContactMethod mustBe
            Some(Set(Email))

          original.email mustBe Some(emailAddress)
          original.phone mustBe None
          original.mobile mustBe None
        }
      }

      "must populate phone and mobile contact details when email is absent" in {
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        val captor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val phoneAndMobileSubcontractor =
          subcontractor.copy(
            emailAddress = None
          )

        mockSuccessfulService(
          mockService,
          response.copy(
            subcontractor = Some(phoneAndMobileSubcontractor)
          )
        )

        mockSuccessfulRepository(mockSessionRepository)

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository)
            .set(captor.capture())

          val savedAnswers =
            captor.getValue

          savedAnswers
            .get(AddIndividualContactMethodsYesNoPage)
            .value mustBe true

          savedAnswers
            .get(IndividualContactMethodOptionsPage)
            .value mustBe Set(Phone, Mobile)

          savedAnswers
            .get(IndividualEmailAddressPage) mustBe None

          savedAnswers
            .get(IndividualPhoneNumberPage)
            .value mustBe phoneNumber

          savedAnswers
            .get(IndividualMobileNumberPage)
            .value mustBe mobileNumber

          val original =
            savedAnswers
              .get(OriginalIndividualAnswersQuery)
              .value

          original.individualContactMethod mustBe
            Some(Set(Phone, Mobile))

          original.email mustBe None
          original.phone mustBe Some(phoneNumber)
          original.mobile mustBe Some(mobileNumber)
        }
      }

      "must set contact-method answer to false and leave contact pages empty when no contact details are returned" in {
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        val captor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithoutContacts =
          subcontractor.copy(
            emailAddress = None,
            phoneNumber = None,
            mobilePhoneNumber = None
          )

        mockSuccessfulService(
          mockService,
          response.copy(
            subcontractor = Some(subcontractorWithoutContacts)
          )
        )

        mockSuccessfulRepository(mockSessionRepository)

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository)
            .set(captor.capture())

          val savedAnswers =
            captor.getValue

          savedAnswers
            .get(AddIndividualContactMethodsYesNoPage)
            .value mustBe false

          savedAnswers
            .get(IndividualContactMethodOptionsPage) mustBe None

          savedAnswers
            .get(IndividualEmailAddressPage) mustBe None

          savedAnswers
            .get(IndividualPhoneNumberPage) mustBe None

          savedAnswers
            .get(IndividualMobileNumberPage) mustBe None

          val original =
            savedAnswers
              .get(OriginalIndividualAnswersQuery)
              .value

          original.individualContactMethod mustBe None
          original.email mustBe None
          original.phone mustBe None
          original.mobile mustBe None
        }
      }

      "must set optional-answer pages to false and leave their value pages empty when values are absent" in {
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        val captor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithMissingOptionalData =
          subcontractor.copy(
            utr = None,
            nino = None,
            worksReferenceNumber = None,
            addressLine1 = None,
            addressLine2 = None,
            addressLine3 = None,
            addressLine4 = None,
            country = None,
            postcode = None
          )

        mockSuccessfulService(
          mockService,
          response.copy(
            subcontractor = Some(subcontractorWithMissingOptionalData)
          )
        )

        mockSuccessfulRepository(mockSessionRepository)

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository)
            .set(captor.capture())

          val savedAnswers =
            captor.getValue

          savedAnswers
            .get(SubAddressYesNoPage)
            .value mustBe false

          savedAnswers
            .get(AddressOfSubcontractorPage) mustBe None

          savedAnswers
            .get(UniqueTaxpayerReferenceYesNoPage)
            .value mustBe false

          savedAnswers
            .get(SubcontractorsUniqueTaxpayerReferencePage) mustBe None

          savedAnswers
            .get(NationalInsuranceNumberYesNoPage)
            .value mustBe false

          savedAnswers
            .get(SubNationalInsuranceNumberPage) mustBe None

          savedAnswers
            .get(WorksReferenceNumberYesNoPage)
            .value mustBe false

          savedAnswers
            .get(WorksReferenceNumberPage) mustBe None

          val original =
            savedAnswers
              .get(OriginalIndividualAnswersQuery)
              .value

          original.address mustBe None
          original.utr mustBe None
          original.nino mustBe None
          original.worksReference mustBe None
        }
      }

      "must preserve the address fields returned by the backend" in {
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        val captor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        mockSuccessfulService(mockService)
        mockSuccessfulRepository(mockSessionRepository)

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository)
            .set(captor.capture())

          val address =
            captor.getValue
              .get(AddressOfSubcontractorPage)
              .value

          address.addressLine1 mustBe
            "12 Harbor View Road"

          address.addressLine2 mustBe
            Some("Amity Island")

          address.addressLine3 mustBe
            Some("Bodmin")

          address.addressLine4 mustBe
            Some("Cornwall")

          address.addressLine5 mustBe None
          address.postcode mustBe Some("PL31 2HL")

          address.country.value.code mustBe None

          address.country.value.name mustBe
            Some("England")
        }
      }

      "must not populate the name page when first name is missing" in {
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        val captor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithoutFirstName =
          subcontractor.copy(
            firstName = None
          )

        mockSuccessfulService(
          mockService,
          response.copy(
            subcontractor = Some(subcontractorWithoutFirstName)
          )
        )

        mockSuccessfulRepository(mockSessionRepository)

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository)
            .set(captor.capture())

          val savedAnswers =
            captor.getValue

          savedAnswers
            .get(SubcontractorNamePage) mustBe None

          savedAnswers
            .get(OriginalIndividualAnswersQuery)
            .value
            .subcontractorName mustBe None
        }
      }

      "must not populate the name page when surname is missing" in {
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        val captor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithoutSurname =
          subcontractor.copy(
            surname = None
          )

        mockSuccessfulService(
          mockService,
          response.copy(
            subcontractor = Some(subcontractorWithoutSurname)
          )
        )

        mockSuccessfulRepository(mockSessionRepository)

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository)
            .set(captor.capture())

          val savedAnswers =
            captor.getValue

          savedAnswers
            .get(SubcontractorNamePage) mustBe None

          savedAnswers
            .get(OriginalIndividualAnswersQuery)
            .value
            .subcontractorName mustBe None
        }
      }

      "must include the middle name when it is returned" in {
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        val captor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithMiddleName =
          subcontractor.copy(
            secondName = Some("James")
          )

        mockSuccessfulService(
          mockService,
          response.copy(
            subcontractor = Some(subcontractorWithMiddleName)
          )
        )

        mockSuccessfulRepository(mockSessionRepository)

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          route(
            application,
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository)
            .set(captor.capture())

          captor.getValue
            .get(SubcontractorNamePage)
            .value mustBe
            SubcontractorName(
              firstName = "Martin",
              middleName = Some("James"),
              lastName = "Brody"
            )
        }
      }

      "must redirect to JourneyRecovery and not save when no subcontractor is returned" in {
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        mockSuccessfulService(
          mockService,
          response.copy(
            subcontractor = None
          )
        )

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendIndividualRoute)
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
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.failed(
            new RuntimeException("Backend unavailable")
          )
        )

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendIndividualRoute)
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

      "must redirect to JourneyRecovery when the session repository fails to save" in {
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        mockSuccessfulService(mockService)

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.failed(
            new RuntimeException("DB unavailable")
          )
        )

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendIndividualRoute)
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
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        val application =
          applicationBuilder(
            userAnswers = None
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendIndividualRoute)
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
        val mockService =
          mock[SubcontractorService]

        val mockSessionRepository =
          mock[SessionRepository]

        mockSuccessfulService(mockService)

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          )
            .overrides(
              bind[SubcontractorService]
                .toInstance(mockService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository),
              bind[AmendIndividualController]
                .to[
                  FailingPopulateAmendIndividualController
                ]
            )
            .build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendIndividualRoute)
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

class FailingPopulateAmendIndividualController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subcontractorService: SubcontractorService,
  sessionRepository: SessionRepository,
  controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends AmendIndividualController(
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
    Failure(
      new RuntimeException(
        "Unable to populate UserAnswers"
      )
    )
}

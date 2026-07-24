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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.TypeOfSubcontractor.Limitedcompany
import models.UserAnswers
import models.address.{Address, Country}
import models.amend.company.OriginalCompanyAnswers
import models.contact.ContactMethodOptions
import models.contact.ContactMethodOptions.{Email, Mobile, Phone}
import models.response.{GetSubcontractorResponse, SubcontractorResponse}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.TypeOfSubcontractorPage
import pages.add.company.*
import play.api.inject.bind
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalCompanyAnswersQuery}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class AmendCompanyControllerSpec extends SpecBase with MockitoSugar {

  private val cisId             = "INST-123"
  private val subbieResourceRef = 1001L

  private lazy val amendCompanyRoute =
    controllers.amend.company.routes.AmendCompanyController
      .onPageLoad(cisId, subbieResourceRef)
      .url

  private val companyName    = "Test Company Ltd"
  private val emailAddress   = "test@example.com"
  private val phoneNumber    = "02070000000"
  private val mobileNumber   = "07123456789"
  private val utr            = "7777777777"
  private val crn            = "AC012345"
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
      crn = Some(crn),
      firstName = None,
      nino = None,
      secondName = None,
      surname = None,
      partnershipTradingName = None,
      tradingName = Some(companyName),
      subcontractorType = Some("company"),
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
    OriginalCompanyAnswers(
      companyName = Some(companyName),
      address = Some(expectedAddress),
      companyContactMethod = Some(Set(Email, Phone, Mobile)),
      email = Some(emailAddress),
      phone = Some(phoneNumber),
      mobile = Some(mobileNumber),
      crn = Some(crn),
      utr = Some(utr),
      worksReference = Some(worksReference)
    )

  "AmendCompanyController" - {

    "onPageLoad" - {

      "must retrieve the subcontractor, save populated answers and redirect to the company details page" in {
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
              FakeRequest(GET, amendCompanyRoute)
            ).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.amend.company.routes.AmendCompanyCheckYourAnswersController
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

      "must save all company answers returned by the backend" in {
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
            FakeRequest(GET, amendCompanyRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(TypeOfSubcontractorPage)
            .value mustBe Limitedcompany

          savedAnswers
            .get(CompanyNamePage)
            .value mustBe companyName

          savedAnswers
            .get(CompanyAddressYesNoPage)
            .value mustBe true

          savedAnswers
            .get(CompanyAddressPage)
            .value mustBe expectedAddress

          savedAnswers
            .get(AddCompanyContactMethodsYesNoPage)
            .value mustBe true

          savedAnswers
            .get(CompanyContactMethodOptionsPage)
            .value mustBe Set(Email, Phone, Mobile)

          savedAnswers
            .get(CompanyEmailAddressPage)
            .value mustBe emailAddress

          savedAnswers
            .get(CompanyPhoneNumberPage)
            .value mustBe phoneNumber

          savedAnswers
            .get(CompanyMobileNumberPage)
            .value mustBe mobileNumber

          savedAnswers
            .get(CompanyUtrYesNoPage)
            .value mustBe true

          savedAnswers
            .get(CompanyUtrPage)
            .value mustBe utr

          savedAnswers
            .get(CompanyCrnYesNoPage)
            .value mustBe true

          savedAnswers
            .get(CompanyCrnPage)
            .value mustBe crn

          savedAnswers
            .get(CompanyWorksReferenceYesNoPage)
            .value mustBe true

          savedAnswers
            .get(CompanyWorksReferencePage)
            .value mustBe worksReference

          savedAnswers
            .get(CisIdQuery)
            .value mustBe cisId

          savedAnswers
            .get(OriginalCompanyAnswersQuery)
            .value mustBe expectedOriginal
        }
      }

      "must set only email as the contact method when only an email is returned" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val emailOnlySubcontractor =
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
              subcontractor = Some(emailOnlySubcontractor)
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
            FakeRequest(GET, amendCompanyRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(AddCompanyContactMethodsYesNoPage)
            .value mustBe true

          savedAnswers
            .get(CompanyContactMethodOptionsPage)
            .value mustBe Set(Email)

          savedAnswers
            .get(CompanyEmailAddressPage)
            .value mustBe emailAddress

          savedAnswers.get(CompanyPhoneNumberPage) mustBe None
          savedAnswers.get(CompanyMobileNumberPage) mustBe None

          savedAnswers
            .get(OriginalCompanyAnswersQuery)
            .value
            .companyContactMethod mustBe Some(Set(Email))
        }
      }

      "must set phone and mobile contact methods when no email is returned" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val phoneAndMobileSubcontractor =
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
              subcontractor = Some(phoneAndMobileSubcontractor)
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
            FakeRequest(GET, amendCompanyRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(AddCompanyContactMethodsYesNoPage)
            .value mustBe true

          savedAnswers
            .get(CompanyContactMethodOptionsPage)
            .value mustBe Set(Phone, Mobile)

          savedAnswers.get(CompanyEmailAddressPage) mustBe None

          savedAnswers
            .get(CompanyPhoneNumberPage)
            .value mustBe phoneNumber

          savedAnswers
            .get(CompanyMobileNumberPage)
            .value mustBe mobileNumber
        }
      }

      "must set contact methods to false and leave contact pages empty when no contact details are returned" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithoutContactDetails =
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
              subcontractor = Some(subcontractorWithoutContactDetails)
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
            FakeRequest(GET, amendCompanyRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(AddCompanyContactMethodsYesNoPage)
            .value mustBe false

          savedAnswers.get(CompanyContactMethodOptionsPage) mustBe None
          savedAnswers.get(CompanyEmailAddressPage) mustBe None
          savedAnswers.get(CompanyPhoneNumberPage) mustBe None
          savedAnswers.get(CompanyMobileNumberPage) mustBe None

          val original =
            savedAnswers
              .get(OriginalCompanyAnswersQuery)
              .value

          original.companyContactMethod mustBe None
          original.email mustBe None
          original.phone mustBe None
          original.mobile mustBe None
        }
      }

      "must set address yes-no to false and leave the address page empty when address line 1 is missing" in {
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
            FakeRequest(GET, amendCompanyRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(CompanyAddressYesNoPage)
            .value mustBe false

          savedAnswers.get(CompanyAddressPage) mustBe None

          savedAnswers
            .get(OriginalCompanyAnswersQuery)
            .value
            .address mustBe None
        }
      }

      "must populate country without a country code" in {
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
            FakeRequest(GET, amendCompanyRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val country =
            captor.getValue
              .get(CompanyAddressPage)
              .value
              .country
              .value

          country.code mustBe None
          country.name mustBe Some("England")
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
            FakeRequest(GET, amendCompanyRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers
            .get(CompanyUtrYesNoPage)
            .value mustBe false

          savedAnswers.get(CompanyUtrPage) mustBe None

          savedAnswers
            .get(CompanyCrnYesNoPage)
            .value mustBe false

          savedAnswers.get(CompanyCrnPage) mustBe None

          savedAnswers
            .get(CompanyWorksReferenceYesNoPage)
            .value mustBe false

          savedAnswers.get(CompanyWorksReferencePage) mustBe None

          val original =
            savedAnswers
              .get(OriginalCompanyAnswersQuery)
              .value

          original.utr mustBe None
          original.crn mustBe None
          original.worksReference mustBe None
        }
      }

      "must not populate the company name page when trading name is missing" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithoutTradingName =
          subcontractor.copy(
            tradingName = None
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            response.copy(
              subcontractor = Some(subcontractorWithoutTradingName)
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
            FakeRequest(GET, amendCompanyRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers.get(CompanyNamePage) mustBe None

          savedAnswers
            .get(OriginalCompanyAnswersQuery)
            .value
            .companyName mustBe None
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
              FakeRequest(GET, amendCompanyRoute)
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
              FakeRequest(GET, amendCompanyRoute)
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
              FakeRequest(GET, amendCompanyRoute)
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
              FakeRequest(GET, amendCompanyRoute)
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
              bind[AmendCompanyController]
                .to[FailingPopulateAmendCompanyController]
            )
            .build()

        running(application) {
          val result =
            route(
              application,
              FakeRequest(GET, amendCompanyRoute)
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

class FailingPopulateAmendCompanyController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subcontractorService: SubcontractorService,
  sessionRepository: SessionRepository,
  controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends AmendCompanyController(
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

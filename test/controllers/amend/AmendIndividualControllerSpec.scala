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
import models.contact.ContactOptions.NoDetails
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
      emailAddress = None,
      phoneNumber = None,
      mobilePhoneNumber = None,
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
      contactMethod = Some(NoDetails),
      contactValue = None,
      utr = Some("3992651526"),
      nino = Some("QQ123456C"),
      worksReference = Some("XLS345-MM")
    )

  "AmendIndividualController" - {

    "onPageLoad" - {

      "must retrieve the subcontractor, save populated answers and redirect to the individual details page" in {
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

      "must save user answers with the retrieved individual data" in {
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
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

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
            .get(IndividualChooseContactDetailsPage)
            .value mustBe NoDetails

          savedAnswers
            .get(AddIndividualContactMethodsYesNoPage)
            .value mustBe false

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
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithTradingName =
          subcontractor.copy(
            tradingName = Some("Brody Construction")
          )

        val responseWithTradingName =
          response.copy(
            subcontractor = Some(subcontractorWithTradingName)
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(Future.successful(responseWithTradingName))

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
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

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
          original.tradingName mustBe Some("Brody Construction")
        }
      }

      "must set optional-answer pages to false and leave their value pages empty when values are absent" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
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

        val responseWithMissingOptionalData =
          response.copy(
            subcontractor = Some(subcontractorWithMissingOptionalData)
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(Future.successful(responseWithMissingOptionalData))

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
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

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
        }
      }

      "must not populate the name page when first name is missing" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithoutFirstName =
          subcontractor.copy(firstName = None)

        val responseWithoutFirstName =
          response.copy(
            subcontractor = Some(subcontractorWithoutFirstName)
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(Future.successful(responseWithoutFirstName))

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
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers.get(SubcontractorNamePage) mustBe None

          savedAnswers
            .get(OriginalIndividualAnswersQuery)
            .value
            .subcontractorName mustBe None
        }
      }

      "must not populate the name page when surname is missing" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]
        val captor                =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        val subcontractorWithoutSurname =
          subcontractor.copy(surname = None)

        val responseWithoutSurname =
          response.copy(
            subcontractor = Some(subcontractorWithoutSurname)
          )

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(Future.successful(responseWithoutSurname))

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
            FakeRequest(GET, amendIndividualRoute)
          ).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers.get(SubcontractorNamePage) mustBe None

          savedAnswers
            .get(OriginalIndividualAnswersQuery)
            .value
            .subcontractorName mustBe None
        }
      }

      "must redirect to JourneyRecovery and not save when no subcontractor is returned" in {
        val mockService           = mock[SubcontractorService]
        val mockSessionRepository = mock[SessionRepository]

        val responseWithoutSubcontractor =
          response.copy(subcontractor = None)

        when(
          mockService.getSubcontractor(
            eqTo(cisId),
            eqTo(subbieResourceRef)
          )(any[HeaderCarrier])
        ).thenReturn(Future.successful(responseWithoutSubcontractor))

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
              bind[AmendIndividualController]
                .to[FailingPopulateAmendIndividualController]
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
    Failure(new RuntimeException("Unable to populate UserAnswers"))
}

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
import controllers.routes
import models.TypeOfSubcontractor.Individualorsoletrader
import models.UserAnswers
import models.add.SubcontractorName
import models.address.{Address, Country}
import models.amend.OriginalIndividualAnswers
import models.contact.ContactOptions.NoDetails
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.*
import play.api.inject.bind
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalIndividualAnswersQuery}
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class FailingPopulateAmendIndividualController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  override val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends AmendIndividualController(identify, getData, requireData, sessionRepository, controllerComponents) {

  override protected def populateUserAnswers(ua: UserAnswers): Try[UserAnswers] =
    Failure(new RuntimeException("intentional population failure"))
}

class AmendIndividualControllerSpec extends SpecBase with MockitoSugar {

  private lazy val amendIndividualRoute = controllers.amend.routes.AmendIndividualController.onPageLoad().url

  private val expectedName = SubcontractorName(firstName = "Martin", middleName = None, lastName = "Brody")

  private val expectedAddress = Address(
    addressLine1 = "12 Harbor View Road",
    addressLine2 = Some("Amity Island"),
    addressLine3 = Some("Bodmin"),
    addressLine4 = Some("Cornwall"),
    postcode = Some("PL31 2HL"),
    country = Some(Country(code = None, name = Some("England")))
  )

  private val expectedOriginal = OriginalIndividualAnswers(
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

      "must save populated user answers and redirect to JourneyRecovery" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request = FakeRequest(GET, amendIndividualRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          verify(mockSessionRepository, times(1)).set(any())
        }
      }

      "must save user answers with the expected individual data" in {

        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request = FakeRequest(GET, amendIndividualRoute)
          route(application, request).value.futureValue

          verify(mockSessionRepository).set(captor.capture())
          val savedAnswers = captor.getValue

          savedAnswers.get(TypeOfSubcontractorPage).value mustEqual Individualorsoletrader
          savedAnswers.get(SubTradingNameYesNoPage).value mustEqual false
          savedAnswers.get(SubcontractorNamePage).value mustEqual expectedName
          savedAnswers.get(SubAddressYesNoPage).value mustEqual true
          savedAnswers.get(AddressOfSubcontractorPage).value mustEqual expectedAddress
          savedAnswers.get(IndividualChooseContactDetailsPage).value mustEqual NoDetails
          savedAnswers.get(UniqueTaxpayerReferenceYesNoPage).value mustEqual true
          savedAnswers.get(SubcontractorsUniqueTaxpayerReferencePage).value mustEqual "3992651526"
          savedAnswers.get(NationalInsuranceNumberYesNoPage).value mustEqual true
          savedAnswers.get(SubNationalInsuranceNumberPage).value mustEqual "QQ123456C"
          savedAnswers.get(WorksReferenceNumberYesNoPage).value mustEqual true
          savedAnswers.get(WorksReferenceNumberPage).value mustEqual "XLS345-MM"
          savedAnswers.get(CisIdQuery).value mustEqual "1"
          savedAnswers.get(OriginalIndividualAnswersQuery).value mustEqual expectedOriginal
        }
      }

      "must redirect to JourneyRecovery when no user answers exist" in {

        val mockSessionRepository = mock[SessionRepository]

        val application =
          applicationBuilder(userAnswers = None)
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request = FakeRequest(GET, amendIndividualRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery and not save when populating user answers fails" in {

        val mockSessionRepository = mock[SessionRepository]

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[AmendIndividualController].to[FailingPopulateAmendIndividualController]
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, amendIndividualRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          verify(mockSessionRepository, never()).set(any())
        }
      }

      "must propagate the exception when the session repository fails to save" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.failed(new Exception("DB unavailable"))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request = FakeRequest(GET, amendIndividualRoute)
          val result  = route(application, request).value

          whenReady(result.failed) { ex =>
            ex.getMessage mustEqual "DB unavailable"
          }
        }
      }
    }
  }
}

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
import controllers.routes
import models.TypeOfSubcontractor.Partnership
import models.UserAnswers
import models.address.{Address, Country}
import models.amend.partnership.OriginalPartnershipAnswers
import models.contact.ContactMethodOptions.Email
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.TypeOfSubcontractorPage
import pages.add.partnership.*
import play.api.inject.bind
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalPartnershipAnswersQuery}
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class FailingPopulateAmendPartnershipController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  override val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends AmendPartnershipController(
      identify,
      getData,
      requireData,
      sessionRepository,
      controllerComponents
    ) {

  override protected def populateUserAnswers(ua: UserAnswers): Try[UserAnswers] =
    Failure(new RuntimeException("intentional population failure"))
}

class AmendPartnershipControllerSpec extends SpecBase with MockitoSugar {

  private lazy val amendPartnershipRoute =
    controllers.amend.partnership.routes.AmendPartnershipController.onPageLoad().url
  private val partnershipName            = "test partnership"
  private val emailAddress               = "test@example.com"
  private val utr                        = "7777777777"
  private val crn                        = "AC012345"
  private val worksReference             = "XLS345-MM"
  private val expectedAddress            = Address(
    addressLine1 = "12 Harbor View Road",
    addressLine2 = Some("Amity Island"),
    addressLine3 = Some("Bodmin"),
    addressLine4 = Some("Cornwall"),
    postcode = Some("PL31 2HL"),
    country = Some(Country(code = None, name = Some("England")))
  )
  private val nominatedPartnerUtr        = "8777777777"
  private val nominatedPartnerName       = "test nominated partner"
  private val nominatedPartnerNino       = "QQ123456C"

  private val expectedOriginal =
    OriginalPartnershipAnswers(
      partnershipName = Some(partnershipName),
      addressYesNo = Some(true),
      address = Some(expectedAddress),
      partnershipContactMethodsYesNo = Some(true),
      partnershipContactMethodOptions = Some(Set(Email)),
      email = Some(emailAddress),
      phone = None,
      mobile = None,
      hasUtrYesNo = Some(true),
      utr = Some(utr),
      nominatedPartnerName = Some(nominatedPartnerName),
      nominatedPartnerUtrYesNo = Some(false),
      nominatedPartnerUtr = None,
      nominatedPartnerNinoYesNo = Some(true),
      nominatedPartnerNino = Some(nominatedPartnerNino),
      nominatedPartnerCrnYesNo = Some(true),
      nominatedPartnerCrn = Some(crn),
      nominatedPartnerWorksReferenceYesNo = Some(true),
      nominatedPartnerWorksReference = Some(worksReference)
    )

  "AmendPartnershipController" - {

    "onPageLoad" - {

      "must save populated user answers and redirect to JourneyRecovery" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {

          val request = FakeRequest(GET, amendPartnershipRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

          verify(mockSessionRepository, times(1)).set(any())
        }
      }

      "must save user answers with the expected partnership data" in {

        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {

          val request = FakeRequest(GET, amendPartnershipRoute)
          route(application, request).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers.get(TypeOfSubcontractorPage).value mustBe Partnership
          savedAnswers.get(PartnershipNamePage).value mustBe partnershipName

          savedAnswers.get(PartnershipAddressYesNoPage).value mustBe true
          savedAnswers.get(PartnershipAddressPage).value mustBe expectedAddress

          savedAnswers.get(AddPartnershipContactMethodsYesNoPage).value mustBe true
          savedAnswers.get(PartnershipContactMethodOptionsPage).value mustBe Set(Email)
          savedAnswers.get(PartnershipEmailAddressPage).value mustBe emailAddress

          savedAnswers.get(PartnershipHasUtrYesNoPage).value mustBe true
          savedAnswers.get(PartnershipUniqueTaxpayerReferencePage).value mustBe utr

          savedAnswers.get(PartnershipNominatedPartnerNamePage).value mustBe nominatedPartnerName

          savedAnswers.get(PartnershipNominatedPartnerUtrYesNoPage).value mustBe true
          savedAnswers.get(PartnershipNominatedPartnerUtrPage).value mustBe nominatedPartnerUtr

          savedAnswers.get(PartnershipNominatedPartnerNinoYesNoPage).value mustBe true
          savedAnswers.get(PartnershipNominatedPartnerNinoPage).value mustBe nominatedPartnerNino

          savedAnswers.get(PartnershipNominatedPartnerCrnYesNoPage).value mustBe true
          savedAnswers.get(PartnershipNominatedPartnerCrnPage).value mustBe crn

          savedAnswers.get(PartnershipWorksReferenceNumberYesNoPage).value mustBe true
          savedAnswers.get(PartnershipWorksReferenceNumberPage).value mustBe worksReference

          savedAnswers.get(CisIdQuery).value mustBe "1"
          savedAnswers.get(OriginalPartnershipAnswersQuery).value mustBe expectedOriginal
        }
      }

      "must redirect to JourneyRecovery when no user answers exist" in {

        val mockSessionRepository = mock[SessionRepository]

        val application =
          applicationBuilder(userAnswers = None)
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {

          val request = FakeRequest(GET, amendPartnershipRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery and not save when populating user answers fails" in {

        val mockSessionRepository = mock[SessionRepository]

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[AmendPartnershipController].to[FailingPopulateAmendPartnershipController]
            )
            .build()

        running(application) {

          val request = FakeRequest(GET, amendPartnershipRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

          verify(mockSessionRepository, never()).set(any())
        }
      }

      "must propagate the exception when the session repository fails to save" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.failed(new Exception("DB unavailable")))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {

          val request = FakeRequest(GET, amendPartnershipRoute)
          val result  = route(application, request).value

          whenReady(result.failed) { ex =>
            ex.getMessage mustBe "DB unavailable"
          }
        }
      }
    }
  }
}

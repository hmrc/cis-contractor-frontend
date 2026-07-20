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
import controllers.routes
import models.TypeOfSubcontractor.Trust
import models.UserAnswers
import models.address.{Address, Country}
import models.amend.trust.OriginalTrustAnswers
import models.contact.ContactMethodOptions.{Email, Mobile, Phone}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.TypeOfSubcontractorPage
import pages.add.trust.*
import play.api.inject.bind
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalTrustAnswersQuery, SubContractorVerificationNumberQuery, SubContractorVerifiedQuery}
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class FailingPopulateAmendTrustController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  override val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends AmendTrustController(
      identify,
      getData,
      requireData,
      sessionRepository,
      controllerComponents
    ) {

  override protected def populateUserAnswers(ua: UserAnswers): Try[UserAnswers] =
    Failure(new RuntimeException("intentional population failure"))
}

class AmendTrustControllerSpec extends SpecBase with MockitoSugar {

  private lazy val amendTrustRoute =
    controllers.amend.trust.routes.AmendTrustController.onPageLoad().url
  private val trustName            = "test trust"
  private val emailAddress         = "test@example.com"
  private val phoneNumber          = "1234567890"
  private val mobileNumber         = "6454543667"
  private val utr                  = "1123456789"
  private val worksReference       = "XLS345-MM"
  private val expectedAddress      = Address(
    addressLine1 = "12 Harbor View Road",
    addressLine2 = Some("Amity Island"),
    addressLine3 = Some("Bodmin"),
    addressLine4 = Some("Cornwall"),
    postcode = Some("PL31 2HL"),
    country = Some(Country(code = None, name = Some("England")))
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

      "must save populated user answers and redirect to JourneyRecovery" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {

          val request = FakeRequest(GET, amendTrustRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

          verify(mockSessionRepository, times(1)).set(any())
        }
      }

      "must save user answers with the expected trust data" in {

        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {

          val request = FakeRequest(GET, amendTrustRoute)

          route(application, request).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers.get(TypeOfSubcontractorPage).value mustBe Trust

          savedAnswers.get(TrustNamePage).value mustBe trustName

          savedAnswers.get(TrustAddressYesNoPage).value mustBe true
          savedAnswers.get(TrustAddressPage).value mustBe expectedAddress

          savedAnswers.get(AddTrustContactMethodsYesNoPage).value mustBe true
          savedAnswers.get(TrustContactMethodOptionsPage).value mustBe Set(Email, Phone, Mobile)

          savedAnswers.get(TrustEmailAddressPage).value mustBe emailAddress
          savedAnswers.get(TrustPhoneNumberPage).value mustBe phoneNumber
          savedAnswers.get(TrustMobileNumberPage).value mustBe mobileNumber

          savedAnswers.get(TrustUtrYesNoPage).value mustBe true
          savedAnswers.get(TrustUtrPage).value mustBe utr

          savedAnswers.get(TrustWorksReferenceYesNoPage).value mustBe true
          savedAnswers.get(TrustWorksReferencePage).value mustBe worksReference

          savedAnswers.get(CisIdQuery).value mustBe "1"
          savedAnswers.get(OriginalTrustAnswersQuery).value mustBe expectedOriginal

          savedAnswers.get(SubContractorVerifiedQuery).value mustBe false
          savedAnswers.get(SubContractorVerificationNumberQuery).value mustBe "V0004528765"
        }
      }

      "must redirect to JourneyRecovery when no user answers exist" in {

        val mockSessionRepository = mock[SessionRepository]

        val application =
          applicationBuilder(userAnswers = None)
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {

          val request = FakeRequest(GET, amendTrustRoute)
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
              bind[AmendTrustController].to[FailingPopulateAmendTrustController]
            )
            .build()

        running(application) {

          val request = FakeRequest(GET, amendTrustRoute)
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

          val request = FakeRequest(GET, amendTrustRoute)
          val result  = route(application, request).value

          whenReady(result.failed) { ex =>
            ex.getMessage mustBe "DB unavailable"
          }
        }
      }
    }
  }
}

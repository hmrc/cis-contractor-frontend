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
import controllers.routes
import models.UserAnswers
import models.address.{Address, Country}
import models.amend.company.OriginalCompanyAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.company.*
import play.api.inject.bind
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalCompanyAnswersQuery}
import repositories.SessionRepository
import models.TypeOfSubcontractor.Limitedcompany
import models.contact.ContactMethodOptions
import pages.add.TypeOfSubcontractorPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class FailingPopulateAmendCompanyController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  override val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends AmendCompanyController(
      identify,
      getData,
      requireData,
      sessionRepository,
      controllerComponents
    ) {

  override protected def populateUserAnswers(ua: UserAnswers): Try[UserAnswers] =
    Failure(new RuntimeException("intentional population failure"))
}

class AmendCompanyControllerSpec extends SpecBase with MockitoSugar {

  private lazy val amendCompanyRoute =
    controllers.amend.company.routes.AmendCompanyController.onPageLoad().url
  private val companyName            = "test company"
  private val emailAddress           = "test@example.com"
  private val utr                    = "7777777777"
  private val crn                    = "AC012345"
  private val worksReference         = "XLS345-MM"
  private val expectedAddress        = Address(
    addressLine1 = "12 Harbor View Road",
    addressLine2 = Some("Amity Island"),
    addressLine3 = Some("Bodmin"),
    addressLine4 = Some("Cornwall"),
    postcode = Some("PL31 2HL"),
    country = Some(Country(code = None, name = Some("England")))
  )

  private val expectedOriginal =
    OriginalCompanyAnswers(
      companyName = Some(companyName),
      address = Some(expectedAddress),
      companyContactMethod = Some(Set(ContactMethodOptions.Email)),
      email = Some(emailAddress),
      phone = None,
      mobile = None,
      utr = Some(utr),
      crn = Some(crn),
      worksReference = Some(worksReference)
    )

  "AmendCompanyController" - {

    "onPageLoad" - {

      "must save populated user answers and redirect to JourneyRecovery" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {

          val request = FakeRequest(GET, amendCompanyRoute)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

          verify(mockSessionRepository, times(1)).set(any())
        }
      }

      "must save user answers with the expected company data" in {

        val mockSessionRepository = mock[SessionRepository]
        val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {

          val request = FakeRequest(GET, amendCompanyRoute)
          route(application, request).value.futureValue

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers.get(TypeOfSubcontractorPage).value mustEqual Limitedcompany
          savedAnswers.get(CompanyNamePage).value mustBe companyName
          savedAnswers.get(CompanyAddressYesNoPage).value mustBe true
          savedAnswers.get(CompanyAddressPage).value mustBe expectedAddress

          savedAnswers.get(AddCompanyContactMethodsYesNoPage).value mustBe true
          savedAnswers.get(CompanyContactMethodOptionsPage).value mustBe Set(ContactMethodOptions.Email)
          savedAnswers.get(CompanyEmailAddressPage).value mustBe emailAddress

          savedAnswers.get(CompanyUtrYesNoPage).value mustBe true
          savedAnswers.get(CompanyUtrPage).value mustBe utr

          savedAnswers.get(CompanyCrnYesNoPage).value mustBe true
          savedAnswers.get(CompanyCrnPage).value mustBe crn

          savedAnswers.get(CompanyWorksReferenceYesNoPage).value mustBe true
          savedAnswers.get(CompanyWorksReferencePage).value mustBe worksReference

          savedAnswers.get(CisIdQuery).value mustBe "1"
          savedAnswers.get(OriginalCompanyAnswersQuery).value mustBe expectedOriginal
        }
      }

      "must redirect to JourneyRecovery when no user answers exist" in {

        val mockSessionRepository = mock[SessionRepository]

        val application =
          applicationBuilder(userAnswers = None)
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {

          val request = FakeRequest(GET, amendCompanyRoute)
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
              bind[AmendCompanyController].to[FailingPopulateAmendCompanyController]
            )
            .build()

        running(application) {

          val request = FakeRequest(GET, amendCompanyRoute)
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

          val request = FakeRequest(GET, amendCompanyRoute)
          val result  = route(application, request).value

          whenReady(result.failed) { ex =>
            ex.getMessage mustBe "DB unavailable"
          }
        }
      }
    }
  }
}

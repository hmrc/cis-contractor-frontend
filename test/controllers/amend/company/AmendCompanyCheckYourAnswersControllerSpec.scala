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
import controllers.routes
import models.address.{Address, Country}
import models.amend.company.OriginalCompanyAnswers
import models.contact.ContactMethodOptions
import models.{TypeOfSubcontractor, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, verifyNoInteractions, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.TypeOfSubcontractorPage
import pages.add.company.*
import pages.amend.{AmendCheckYourAnswersSubmittedPage, ShowVerificationDetailsPage}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.OriginalCompanyAnswersQuery
import repositories.SessionRepository
import services.SubcontractorService
import pages.amend.AmendCheckYourAnswersSubmittedPage
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class AmendCompanyCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {
  private val address =
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
  private val minUa   =
    emptyUserAnswers
      .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
      .success
      .value
      .set(CompanyNamePage, "Test Company Ltd")
      .success
      .value
      .set(CompanyAddressYesNoPage, true)
      .success
      .value
      .set(CompanyAddressPage, address)
      .success
      .value
      .set(AddCompanyContactMethodsYesNoPage, true)
      .success
      .value
      .set(CompanyContactMethodOptionsPage, Set(ContactMethodOptions.Email))
      .success
      .value
      .set(CompanyEmailAddressPage, "test@test.com")
      .success
      .value
      .set(CompanyUtrYesNoPage, true)
      .success
      .value
      .set(CompanyUtrPage, "11111111")
      .success
      .value
      .set(CompanyCrnYesNoPage, true)
      .success
      .value
      .set(CompanyCrnPage, "12345678")
      .success
      .value
      .set(CompanyWorksReferenceYesNoPage, true)
      .success
      .value
      .set(CompanyWorksReferencePage, "WRN-1")
      .success
      .value
      .set(ShowVerificationDetailsPage, false)
      .success
      .value
      .set(
        OriginalCompanyAnswersQuery,
        OriginalCompanyAnswers(
          companyName = Some("Test Company Ltd"),
          addressYesNo = Some(true),
          address = Some(address),
          companyContactMethodsYesNo = Some(true),
          companyContactMethod = Set(ContactMethodOptions.Email),
          email = Some("test@test.com"),
          phone = None,
          mobile = None,
          crnYesNo = Some(true),
          crn = Some("12345678"),
          utrYesNo = Some(true),
          utr = Some("11111111"),
          worksReferenceYesNo = Some(true),
          worksReference = Some("WRN-1"),
          verificationNumber = None
        )
      )
      .success
      .value

  "AmendCompanyCheckYourAnswersController" - {

    "must return OK and render the page with the correct summary list for GET when validation succeeds for unverified company" in {
      val application =
        applicationBuilder(userAnswers = Some(minUa)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onPageLoad().url)
        val msg     = app.injector.instanceOf[MessagesApi].preferred(request)
        val result  = route(application, request).value

        status(result) mustEqual OK

        val page = contentAsString(result)

        page must include(msg("typeOfSubcontractor.checkYourAnswersLabel"))
        page must include(msg("companyName.checkYourAnswersLabel"))
        page must include(msg("companyCrnYesNo.checkYourAnswersLabel"))
        page must include(msg("companyCrn.checkYourAnswersLabel"))
        page must include(msg("companyWorksReferenceYesNo.checkYourAnswersLabel"))
        page must include(msg("companyWorksReference.checkYourAnswersLabel"))
        page must include(msg("companyAddressYesNo.checkYourAnswersLabel"))
        page must include(msg("companyAddress.checkYourAnswersLabel"))
        page must include(msg("addCompanyContactMethodsYesNo.checkYourAnswersLabel"))
        page must include(msg("companyContactMethodOptions.checkYourAnswersLabel"))
        page must include(msg("companyEmailAddress.checkYourAnswersLabel"))

        page must not include (msg("amendCheckYourAnswers.verificationNumber.label"))

        page must include("Company")
        page must include("Test Company Ltd")
        page must include("12345678")
        page must include("WRN-1")
        page must include("test@test.com")
        page must include("12 Harbor View Road")
        page must include("Amity Island")
        page must include("Bodmin")
        page must include("Cornwall")
        page must include("PL31 2HL")
        page must include("England")

        page must not include "VRN123456"
      }
    }

    "must render the correct summary for a verified company" in {

      val verifiedUa  =
        minUa
          .set(ShowVerificationDetailsPage, true)
          .success
          .value
          .set(
            OriginalCompanyAnswersQuery,
            OriginalCompanyAnswers(
              companyName = Some("Test Company Ltd"),
              addressYesNo = Some(false),
              address = None,
              companyContactMethodsYesNo = Some(false),
              companyContactMethod = Set.empty,
              email = None,
              phone = None,
              mobile = None,
              crnYesNo = Some(false),
              crn = None,
              utrYesNo = Some(false),
              utr = None,
              worksReferenceYesNo = Some(false),
              worksReference = None,
              verificationNumber = Some("VRN123456")
            )
          )
          .success
          .value
      val application =
        applicationBuilder(userAnswers = Some(verifiedUa)).build()

      running(application) {

        val request =
          FakeRequest(GET, controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onPageLoad().url)
        val msg     = app.injector.instanceOf[MessagesApi].preferred(request)
        val result  = route(application, request).value

        status(result) mustEqual OK

        val page = contentAsString(result)

        page must include(msg("amendCheckYourAnswers.verificationNumber.label"))
        page must include("VRN123456")

        page must not include (msg("companyName.checkYourAnswersLabel"))
        page must not include (msg("companyRegistrationNumberYesNo.checkYourAnswersLabel"))
        page must not include (msg("companyRegistrationNumber.change.hidden"))

        page must include(msg("typeOfSubcontractor.checkYourAnswersLabel"))
        page must include("Company")

        page must include(msg("companyAddressYesNo.checkYourAnswersLabel"))
        page must include(msg("companyAddress.checkYourAnswersLabel"))
        page must include(msg("site.yes"))
        page must include("12 Harbor View Road")
        page must include("Amity Island")
        page must include("Bodmin")
        page must include("Cornwall")
        page must include("PL31 2HL")
        page must include("England")

        page must include(msg("addCompanyContactMethodsYesNo.checkYourAnswersLabel"))
        page must include(msg("companyContactMethodOptions.checkYourAnswersLabel"))
        page must include(msg("companyEmailAddress.checkYourAnswersLabel"))
        page must include("test@test.com")

        page must include(msg("companyWorksReferenceYesNo.checkYourAnswersLabel"))
        page must include(msg("companyWorksReference.checkYourAnswersLabel"))
        page must include(msg("site.no"))
        page must include("WRN-1")
      }
    }

    "must redirect to Journey Recovery when validation fails" in {

      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(invalidUa)).build()

      running(application) {

        val request =
          FakeRequest(GET, controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect back to amend CYA after successful submit" in {

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]
      val captor                   = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSubcontractorService.createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(()))
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))
      val application              =
        applicationBuilder(userAnswers = Some(minUa))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onPageLoad().url
      }

      verify(mockSubcontractorService)
        .createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])

      verify(mockSessionRepository).set(captor.capture())
      captor.getValue.get(AmendCheckYourAnswersSubmittedPage) mustBe Some(true)
      verifyNoMoreInteractions(mockSubcontractorService, mockSessionRepository)
    }

    "must redirect to Journey Recovery when the check your answers page has already been submitted" in {

      val ua = minUa
        .set(AmendCheckYourAnswersSubmittedPage, true)
        .success
        .value

      val mockSubcontractorService = mock[SubcontractorService]

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
      verifyNoInteractions(mockSubcontractorService)
    }

    "must redirect to Journey Recovery when the service fails" in {

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]

      when(mockSubcontractorService.createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application =
        applicationBuilder(userAnswers = Some(minUa))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockSubcontractorService)
        .createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
    }

    "must redirect to Journey Recovery when POST validation fails" in {

      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
          .success
          .value

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]

      val application =
        applicationBuilder(userAnswers = Some(invalidUa))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockSubcontractorService, never())
        .createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])
    }

    "must clear answers and redirect to Index on cancel" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(minUa))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(GET, controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onCancel().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.IndexController.onPageLoad().url
      }

      verify(mockSessionRepository).set(any[UserAnswers])
    }
  }
}

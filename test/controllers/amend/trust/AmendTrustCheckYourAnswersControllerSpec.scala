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
import controllers.routes
import models.address.{Address, Country}
import models.amend.trust.OriginalTrustAnswers
import models.{TypeOfSubcontractor, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.TypeOfSubcontractorPage
import pages.add.trust.*
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.OriginalTrustAnswersQuery
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import models.contact.ContactMethodOptions

class AmendTrustCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {
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
      .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
      .success
      .value
      .set(TrustNamePage, "Test Trust")
      .success
      .value
      .set(TrustAddressYesNoPage, true)
      .success
      .value
      .set(TrustAddressPage, address)
      .success
      .value
      .set(AddTrustContactMethodsYesNoPage, true)
      .success
      .value
      .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email))
      .success
      .value
      .set(TrustEmailAddressPage, "test@test.com")
      .success
      .value
      .set(TrustUtrYesNoPage, true)
      .success
      .value
      .set(TrustUtrPage, "11111111")
      .success
      .value
      .set(TrustWorksReferenceYesNoPage, true)
      .success
      .value
      .set(TrustWorksReferencePage, "WRN-1")
      .success
      .value
      .set(
        OriginalTrustAnswersQuery,
        OriginalTrustAnswers(
          trustName = Some("Test Trust"),
          addressYesNo = Some(true),
          address = Some(address),
          trustContactMethodsYesNo = Some(true),
          trustContactMethod = Set(ContactMethodOptions.Email),
          email = Some("test@test.com"),
          phone = None,
          mobile = None,
          utrYesNo = Some(true),
          utr = Some("11111111"),
          worksReferenceYesNo = Some(true),
          worksReference = Some("WRN-1"),
          verificationNumber = None,
          isVerified = Some(false)
        )
      )
      .success
      .value

  "AmendTrustCheckYourAnswersController" - {

    "must return OK and render the page with the correct summary list for GET when validation succeeds for unverified trust" in {
      val application =
        applicationBuilder(userAnswers = Some(minUa)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onPageLoad().url)
        val msg     = app.injector.instanceOf[MessagesApi].preferred(request)
        val result  = route(application, request).value

        status(result) mustEqual OK

        val page = contentAsString(result)

        page must include(msg("typeOfSubcontractor.checkYourAnswersLabel"))
        page must include(msg("trustName.checkYourAnswersLabel"))
        page must include(msg("trustUtrYesNo.checkYourAnswersLabel"))
        page must include(msg("trustUtr.checkYourAnswersLabel"))
        page must include(msg("trustWorksReferenceYesNo.checkYourAnswersLabel"))
        page must include(msg("trustWorksReference.checkYourAnswersLabel"))
        page must include(msg("trustAddressYesNo.checkYourAnswersLabel"))
        page must include(msg("trustAddress.checkYourAnswersLabel"))
        page must include(msg("addTrustContactMethodsYesNo.checkYourAnswersLabel"))
        page must include(msg("trustContactMethodOptions.checkYourAnswersLabel"))
        page must include(msg("trustEmailAddress.checkYourAnswersLabel"))

        page must not include msg("amendCheckYourAnswers.verificationNumber.label")

        page must include("Trust")
        page must include("Test Trust")
        page must include("11111111")
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

    "must render the correct summary for a verified trust" in {

      val verifiedUa =
        minUa
          .set(
            OriginalTrustAnswersQuery,
            OriginalTrustAnswers(
              trustName = Some("Test Trust"),
              addressYesNo = Some(false),
              address = None,
              trustContactMethodsYesNo = Some(false),
              trustContactMethod = Set.empty,
              email = None,
              phone = None,
              mobile = None,
              utrYesNo = Some(false),
              utr = None,
              worksReferenceYesNo = Some(false),
              worksReference = None,
              verificationNumber = Some("VRN123456"),
              isVerified = Some(true)
            )
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(verifiedUa)).build()

      running(application) {

        val request =
          FakeRequest(GET, controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onPageLoad().url)
        val msg     = app.injector.instanceOf[MessagesApi].preferred(request)
        val result  = route(application, request).value

        status(result) mustEqual OK

        val page = contentAsString(result)

        page must include(msg("amendCheckYourAnswers.verificationNumber.label"))
        page must include("VRN123456")

        page must not include msg("trustName.checkYourAnswersLabel")
        page must not include msg("trustUtrYesNo.checkYourAnswersLabel")
        page must not include msg("trustUtr.change.hidden")

        page must include(msg("typeOfSubcontractor.checkYourAnswersLabel"))
        page must include("Trust")

        page must include(msg("trustUtr.checkYourAnswersLabel"))
        page must include("11111111")
        page must include(msg("trustAddressYesNo.checkYourAnswersLabel"))
        page must include(msg("trustAddress.checkYourAnswersLabel"))
        page must include(msg("site.yes"))
        page must include("12 Harbor View Road")
        page must include("Amity Island")
        page must include("Bodmin")
        page must include("Cornwall")
        page must include("PL31 2HL")
        page must include("England")

        page must include(msg("addTrustContactMethodsYesNo.checkYourAnswersLabel"))
        page must include(msg("trustContactMethodOptions.checkYourAnswersLabel"))
        page must include(msg("trustEmailAddress.checkYourAnswersLabel"))
        page must include("test@test.com")

        page must include(msg("trustWorksReferenceYesNo.checkYourAnswersLabel"))
        page must include(msg("trustWorksReference.checkYourAnswersLabel"))
        page must include(msg("site.no"))
        page must include("WRN-1")
      }
    }

    "must redirect to Journey Recovery when validation fails" in {

      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(invalidUa)).build()

      running(application) {

        val request =
          FakeRequest(GET, controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect back to amend CYA after successful submit" in {

      val mockSubcontractorService = mock[SubcontractorService]
      val mockSessionRepository    = mock[SessionRepository]

      when(mockSubcontractorService.createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier]))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(minUa))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onPageLoad().url
      }

      verify(mockSubcontractorService)
        .createAndUpdateSubcontractor(any[UserAnswers])(any[HeaderCarrier])

      verifyNoMoreInteractions(mockSubcontractorService)
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
          FakeRequest(POST, controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onSubmit().url)

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
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
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
          FakeRequest(POST, controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onSubmit().url)

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
          FakeRequest(GET, controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onCancel().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.IndexController.onPageLoad().url
      }

      verify(mockSessionRepository).set(any[UserAnswers])
    }
  }
}

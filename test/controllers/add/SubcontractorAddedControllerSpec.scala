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

package controllers.add

import base.SpecBase
import controllers.routes
import models.UserAnswers
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentCaptor
import org.scalatestplus.mockito.MockitoSugar
import pages.add.company.{CompanyAddressYesNoPage, CompanyNamePage}
import pages.add.{CheckYourAnswersSubmittedPage, SubAddressYesNoPage, TradingNameOfSubcontractorPage}
import pages.add.partnership.{PartnershipAddressYesNoPage, PartnershipNamePage}
import pages.add.trust.{TrustAddressYesNoPage, TrustNamePage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.add.SubcontractorAddedView

import scala.concurrent.Future
import play.api.inject.bind
import queries.CisIdQuery

class SubcontractorAddedControllerSpec extends SpecBase with MockitoSugar {

  private val subcontractorName = "Test subcontractor"

  "SubcontractorAddedController.individualSubcontractorAdded" - {

    lazy val individualSubcontractorAddedRoute =
      controllers.add.routes.SubcontractorAddedController.individualSubcontractorAdded().url

    "must return Ok and the correct view for a GET when CheckYourAnswersSubmittedPage(true) and subcontractorName(individual) are in ua" in {

      def ua: UserAnswers =
        emptyUserAnswers
          .set(TradingNameOfSubcontractorPage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, individualSubcontractorAddedRoute)

        val view = application.injector.instanceOf[SubcontractorAddedView]

        val result = route(application, request).value
        status(result) mustBe OK
        contentAsString(result) mustEqual view(subcontractorName, "Individual")(request, messages(application)).toString

        verify(mockRepo, times(1)).set(any())
      }
    }

    "must clear all subcontractor journey ua (not CisId) for a GET when CheckYourAnswersSubmittedPage(true) and subcontractorName(individual) are in ua" in {

      def ua: UserAnswers =
        emptyUserAnswers
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, "10")
          .success
          .value
          .set(TradingNameOfSubcontractorPage, subcontractorName)
          .success
          .value
          .set(SubAddressYesNoPage, true)
          .success
          .value
          .set(CompanyAddressYesNoPage, true)
          .success
          .value
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value
          .set(TrustAddressYesNoPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, individualSubcontractorAddedRoute)

        val view = application.injector.instanceOf[SubcontractorAddedView]

        val result = route(application, request).value
        status(result) mustBe OK
        contentAsString(result) mustEqual view(subcontractorName, "Individual")(request, messages(application)).toString

        val captor: ArgumentCaptor[models.UserAnswers] = ArgumentCaptor.forClass(classOf[models.UserAnswers])
        verify(mockRepo, atLeastOnce()).set(captor.capture())

        val savedAnswers = captor.getValue
        savedAnswers.get(CisIdQuery) mustBe Some("10")
        savedAnswers.get(TradingNameOfSubcontractorPage) mustBe None
        savedAnswers.get(SubAddressYesNoPage) mustBe None
        savedAnswers.get(CompanyAddressYesNoPage) mustBe None
        savedAnswers.get(PartnershipAddressYesNoPage) mustBe None
        savedAnswers.get(TrustAddressYesNoPage) mustBe None

        verify(mockRepo, times(1)).set(any())
      }
    }

    "must change checkYourAnswersSubmitted in ua to false for a GET when CheckYourAnswersSubmittedPage(true) and subcontractorName(individual) are in ua" in {

      def ua: UserAnswers =
        emptyUserAnswers
          .set(TradingNameOfSubcontractorPage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, individualSubcontractorAddedRoute)

        val result = route(application, request).value
        status(result) mustBe OK

        val captor: ArgumentCaptor[models.UserAnswers] = ArgumentCaptor.forClass(classOf[models.UserAnswers])
        verify(mockRepo, atLeastOnce()).set(captor.capture())

        val savedAnswers = captor.getValue
        savedAnswers.get(CheckYourAnswersSubmittedPage) mustBe Some(false)

        verify(mockRepo, times(1)).set(any())
      }
    }

    "must redirect to JourneyRecovery for a GET when CheckYourAnswersSubmittedPage is not in ua" in {
      def ua: UserAnswers =
        emptyUserAnswers
          .set(TradingNameOfSubcontractorPage, subcontractorName)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, individualSubcontractorAddedRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockRepo, times(0)).set(any())
      }
    }

    "must redirect to JourneyRecovery for a GET when CheckYourAnswersSubmittedPage(false) is in ua" in {
      def ua: UserAnswers =
        emptyUserAnswers
          .set(TradingNameOfSubcontractorPage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, false)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, individualSubcontractorAddedRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockRepo, times(0)).set(any())
      }
    }

    "must redirect to JourneyRecovery for a GET when CheckYourAnswersSubmittedPage(true) in ua and subcontractorName(individual) is not in ua " in {
      def ua: UserAnswers =
        emptyUserAnswers
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, individualSubcontractorAddedRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockRepo, times(0)).set(any())
      }
    }
  }

  "SubcontractorAddedController.companySubcontractorAdded" - {

    lazy val companySubcontractorAddedRoute =
      controllers.add.routes.SubcontractorAddedController.companySubcontractorAdded().url

    "must return Ok and the correct view for a GET when CheckYourAnswersSubmittedPage(true) and subcontractorName(company) are in ua" in {

      def ua: UserAnswers =
        emptyUserAnswers
          .set(CompanyNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, companySubcontractorAddedRoute)

        val view = application.injector.instanceOf[SubcontractorAddedView]

        val result = route(application, request).value
        status(result) mustBe OK
        contentAsString(result) mustEqual view(subcontractorName, "Company")(
          request,
          messages(application)
        ).toString

        verify(mockRepo, times(1)).set(any())
      }
    }

    "must clear all subcontractor journey ua (not CisId) for a GET when CheckYourAnswersSubmittedPage(true) and subcontractorName(company) are in ua" in {

      def ua: UserAnswers =
        emptyUserAnswers
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, "10")
          .success
          .value
          .set(CompanyNamePage, subcontractorName)
          .success
          .value
          .set(SubAddressYesNoPage, true)
          .success
          .value
          .set(CompanyAddressYesNoPage, true)
          .success
          .value
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value
          .set(TrustAddressYesNoPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, companySubcontractorAddedRoute)

        val view = application.injector.instanceOf[SubcontractorAddedView]

        val result = route(application, request).value
        status(result) mustBe OK
        contentAsString(result) mustEqual view(subcontractorName, "Company")(request, messages(application)).toString

        val captor: ArgumentCaptor[models.UserAnswers] = ArgumentCaptor.forClass(classOf[models.UserAnswers])
        verify(mockRepo, atLeastOnce()).set(captor.capture())

        val savedAnswers = captor.getValue
        savedAnswers.get(CisIdQuery) mustBe Some("10")
        savedAnswers.get(CompanyNamePage) mustBe None
        savedAnswers.get(SubAddressYesNoPage) mustBe None
        savedAnswers.get(CompanyAddressYesNoPage) mustBe None
        savedAnswers.get(PartnershipAddressYesNoPage) mustBe None
        savedAnswers.get(TrustAddressYesNoPage) mustBe None

        verify(mockRepo, times(1)).set(any())
      }
    }

    "must change checkYourAnswersSubmitted in ua to false for a GET when CheckYourAnswersSubmittedPage(true) and subcontractorName(company) are in ua" in {

      def ua: UserAnswers =
        emptyUserAnswers
          .set(CompanyNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, companySubcontractorAddedRoute)

        val result = route(application, request).value
        status(result) mustBe OK

        val captor: ArgumentCaptor[models.UserAnswers] = ArgumentCaptor.forClass(classOf[models.UserAnswers])
        verify(mockRepo, atLeastOnce()).set(captor.capture())

        val savedAnswers = captor.getValue
        savedAnswers.get(CheckYourAnswersSubmittedPage) mustBe Some(false)

        verify(mockRepo, times(1)).set(any())
      }
    }

    "must redirect to JourneyRecovery for a GET when CheckYourAnswersSubmittedPage is not in ua" in {
      def ua: UserAnswers =
        emptyUserAnswers
          .set(CompanyNamePage, subcontractorName)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, companySubcontractorAddedRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockRepo, times(0)).set(any())
      }
    }

    "must redirect to JourneyRecovery for a GET when CheckYourAnswersSubmittedPage(false) is in ua" in {
      def ua: UserAnswers =
        emptyUserAnswers
          .set(CompanyNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, false)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, companySubcontractorAddedRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockRepo, times(0)).set(any())
      }
    }

    "must redirect to JourneyRecovery for a GET when CheckYourAnswersSubmittedPage(true) in ua and subcontractorName(company) is not in ua " in {
      def ua: UserAnswers =
        emptyUserAnswers
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, companySubcontractorAddedRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockRepo, times(0)).set(any())
      }
    }
  }

  "SubcontractorAddedController.partnershipSubcontractorAdded" - {

    lazy val partnershipSubcontractorAddedRoute =
      controllers.add.routes.SubcontractorAddedController.partnershipSubcontractorAdded().url

    "must return Ok and the correct view for a GET when CheckYourAnswersSubmittedPage(true) and subcontractorName(partnership) are in ua" in {

      def ua: UserAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, partnershipSubcontractorAddedRoute)

        val view = application.injector.instanceOf[SubcontractorAddedView]

        val result = route(application, request).value
        status(result) mustBe OK
        contentAsString(result) mustEqual view(subcontractorName, "Partnership")(
          request,
          messages(application)
        ).toString

        verify(mockRepo, times(1)).set(any())
      }
    }

    "must clear all subcontractor journey ua (not CisId) for a GET when CheckYourAnswersSubmittedPage(true) and subcontractorName(partnership) are in ua" in {

      def ua: UserAnswers =
        emptyUserAnswers
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, "10")
          .success
          .value
          .set(PartnershipNamePage, subcontractorName)
          .success
          .value
          .set(SubAddressYesNoPage, true)
          .success
          .value
          .set(CompanyAddressYesNoPage, true)
          .success
          .value
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value
          .set(TrustAddressYesNoPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, partnershipSubcontractorAddedRoute)

        val view = application.injector.instanceOf[SubcontractorAddedView]

        val result = route(application, request).value
        status(result) mustBe OK
        contentAsString(result) mustEqual view(subcontractorName, "Partnership")(
          request,
          messages(application)
        ).toString

        val captor: ArgumentCaptor[models.UserAnswers] = ArgumentCaptor.forClass(classOf[models.UserAnswers])
        verify(mockRepo, atLeastOnce()).set(captor.capture())

        val savedAnswers = captor.getValue
        savedAnswers.get(CisIdQuery) mustBe Some("10")
        savedAnswers.get(PartnershipNamePage) mustBe None
        savedAnswers.get(SubAddressYesNoPage) mustBe None
        savedAnswers.get(CompanyAddressYesNoPage) mustBe None
        savedAnswers.get(PartnershipAddressYesNoPage) mustBe None
        savedAnswers.get(TrustAddressYesNoPage) mustBe None

        verify(mockRepo, times(1)).set(any())
      }
    }

    "must change checkYourAnswersSubmitted in ua to false for a GET when CheckYourAnswersSubmittedPage(true) and subcontractorName(partnership) are in ua" in {

      def ua: UserAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, partnershipSubcontractorAddedRoute)

        val result = route(application, request).value
        status(result) mustBe OK

        val captor: ArgumentCaptor[models.UserAnswers] = ArgumentCaptor.forClass(classOf[models.UserAnswers])
        verify(mockRepo, atLeastOnce()).set(captor.capture())

        val savedAnswers = captor.getValue
        savedAnswers.get(CheckYourAnswersSubmittedPage) mustBe Some(false)

        verify(mockRepo, times(1)).set(any())
      }
    }

    "must redirect to JourneyRecovery for a GET when CheckYourAnswersSubmittedPage is not in ua" in {
      def ua: UserAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, subcontractorName)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, partnershipSubcontractorAddedRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockRepo, times(0)).set(any())
      }
    }

    "must redirect to JourneyRecovery for a GET when CheckYourAnswersSubmittedPage(false) is in ua" in {
      def ua: UserAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, false)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, partnershipSubcontractorAddedRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockRepo, times(0)).set(any())
      }
    }

    "must redirect to JourneyRecovery for a GET when CheckYourAnswersSubmittedPage(true) in ua and subcontractorName(partnership) is not in ua " in {
      def ua: UserAnswers =
        emptyUserAnswers
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, partnershipSubcontractorAddedRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockRepo, times(0)).set(any())
      }
    }
  }

  "SubcontractorAddedController.trustSubcontractorAdded" - {

    lazy val trustSubcontractorAddedRoute =
      controllers.add.routes.SubcontractorAddedController.trustSubcontractorAdded().url

    "must return Ok and the correct view for a GET when CheckYourAnswersSubmittedPage(true) and subcontractorName(trust) are in ua" in {

      def ua: UserAnswers =
        emptyUserAnswers
          .set(TrustNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, trustSubcontractorAddedRoute)

        val view = application.injector.instanceOf[SubcontractorAddedView]

        val result = route(application, request).value
        status(result) mustBe OK
        contentAsString(result) mustEqual view(subcontractorName, "Trust")(
          request,
          messages(application)
        ).toString

        verify(mockRepo, times(1)).set(any())
      }
    }

    "must clear all subcontractor journey ua (not CisId) for a GET when CheckYourAnswersSubmittedPage(true) and subcontractorName(trust) are in ua" in {

      def ua: UserAnswers =
        emptyUserAnswers
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value
          .set(CisIdQuery, "10")
          .success
          .value
          .set(TrustNamePage, subcontractorName)
          .success
          .value
          .set(SubAddressYesNoPage, true)
          .success
          .value
          .set(CompanyAddressYesNoPage, true)
          .success
          .value
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value
          .set(TrustAddressYesNoPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, trustSubcontractorAddedRoute)

        val view = application.injector.instanceOf[SubcontractorAddedView]

        val result = route(application, request).value
        status(result) mustBe OK
        contentAsString(result) mustEqual view(subcontractorName, "Trust")(request, messages(application)).toString

        val captor: ArgumentCaptor[models.UserAnswers] = ArgumentCaptor.forClass(classOf[models.UserAnswers])
        verify(mockRepo, atLeastOnce()).set(captor.capture())

        val savedAnswers = captor.getValue
        savedAnswers.get(CisIdQuery) mustBe Some("10")
        savedAnswers.get(TrustNamePage) mustBe None
        savedAnswers.get(SubAddressYesNoPage) mustBe None
        savedAnswers.get(CompanyAddressYesNoPage) mustBe None
        savedAnswers.get(PartnershipAddressYesNoPage) mustBe None
        savedAnswers.get(TrustAddressYesNoPage) mustBe None

        verify(mockRepo, times(1)).set(any())
      }
    }

    "must change checkYourAnswersSubmitted in ua to false for a GET when CheckYourAnswersSubmittedPage(true) and subcontractorName(trust) are in ua" in {

      def ua: UserAnswers =
        emptyUserAnswers
          .set(TrustNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, trustSubcontractorAddedRoute)

        val result = route(application, request).value
        status(result) mustBe OK

        val captor: ArgumentCaptor[models.UserAnswers] = ArgumentCaptor.forClass(classOf[models.UserAnswers])
        verify(mockRepo, atLeastOnce()).set(captor.capture())

        val savedAnswers = captor.getValue
        savedAnswers.get(CheckYourAnswersSubmittedPage) mustBe Some(false)

        verify(mockRepo, times(1)).set(any())
      }
    }

    "must redirect to JourneyRecovery for a GET when CheckYourAnswersSubmittedPage is not in ua" in {
      def ua: UserAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, subcontractorName)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, trustSubcontractorAddedRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockRepo, times(0)).set(any())
      }
    }

    "must redirect to JourneyRecovery for a GET when CheckYourAnswersSubmittedPage(false) is in ua" in {
      def ua: UserAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, subcontractorName)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, false)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, trustSubcontractorAddedRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockRepo, times(0)).set(any())
      }
    }

    "must redirect to JourneyRecovery for a GET when CheckYourAnswersSubmittedPage(true) in ua and subcontractorName(trust) is not in ua " in {
      def ua: UserAnswers =
        emptyUserAnswers
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value

      val mockRepo = mock[SessionRepository]

      when(mockRepo.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, trustSubcontractorAddedRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockRepo, times(0)).set(any())
      }
    }
  }
}

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

package controllers.verify

import base.SpecBase
import models.{ContractorScheme, SubcontractorViewModel}
import models.response.{CreateSubmissionForVerificationResponse, GetCurrentVerificationBatchResponse, GetNewestVerificationBatchResponse}
import models.{SubcontractorCurrentVerification, VerificationBatchCurrentVerification, VerificationCurrentVerification}
import models.requests.CreateSubmissionForVerificationRequest
import models.verify.ContractorEmailConfirmationStored.{CurrentEmail, DoNotSend}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.verify._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import services.VerificationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class SubmissionSendingControllerSpec extends SpecBase with MockitoSugar {

  private lazy val onPageLoadRoute =
    controllers.verify.routes.SubmissionSendingController.onPageLoad().url

  private val instanceId = "INST-123"

  private val currentBatch: GetCurrentVerificationBatchResponse =
    GetCurrentVerificationBatchResponse(
      subcontractors = Seq(
        SubcontractorCurrentVerification(
          subcontractorId = 10L,
          subbieResourceRef = Some(1111L),
          firstName = None,
          secondName = None,
          surname = None,
          tradingName = None,
          utr = None,
          nino = None,
          crn = None,
          partnerUtr = None,
          partnershipTradingName = None
        )
      ),
      verificationBatch = Some(
        VerificationBatchCurrentVerification(
          verificationBatchId = 99L,
          verifBatchResourceRef = Some(7777L)
        )
      ),
      verifications = Seq(
        VerificationCurrentVerification(
          verificationId = 1L,
          verificationBatchId = Some(99L),
          subcontractorId = Some(10L),
          verificationResourceRef = Some(111L)
        )
      )
    )

  private def newestWithSchemeEmail(email: String): GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = Some(ContractorScheme(accountsOfficeReference = None, emailAddress = Some(email))),
      subcontractors = Nil,
      verificationBatch = None,
      verifications = Nil,
      submission = None,
      monthlyReturn = None
    )

  "SubmissionSendingController.onPageLoad" - {

    "must call service and return OK when valid and email is resolved from scheme (CurrentEmail) without EmailAddressPage" in {
      val mockService = mock[VerificationService]
      when(mockService.createSubmissionForVerification(any[CreateSubmissionForVerificationRequest])(any[HeaderCarrier]))
        .thenReturn(Future.successful(CreateSubmissionForVerificationResponse(123L)))

      val ua0 =
        emptyUserAnswers
          .setOrException(SelectSubcontractorPage, Set(SubcontractorViewModel("10", "Name 10")))
          .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
          .setOrException(NewestVerificationBatchResponsePage, newestWithSchemeEmail("scheme@example.com"))
          .setOrException(CurrentVerificationBatchResponsePage, currentBatch)

      val ua =
        ua0.set(CisIdQuery, instanceId).success.value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, onPageLoadRoute)).value

        status(result) mustBe OK

        val reqCaptor = ArgumentCaptor.forClass(classOf[CreateSubmissionForVerificationRequest])
        verify(mockService).createSubmissionForVerification(reqCaptor.capture())(any[HeaderCarrier])

        val req = reqCaptor.getValue
        req.instanceId mustBe instanceId
        req.verificationBatchId mustBe 99L
        req.verificationBatchResourceRef mustBe 7777L
        req.emailRecipient mustBe "scheme@example.com"
        req.verifications.map(_.verificationResourceRef) mustBe Seq(111L)
      }
    }

    "must redirect to JourneyRecovery when CisIdQuery is missing (buildSubmissionRequest fails)" in {
      val mockService = mock[VerificationService]

      val ua =
        emptyUserAnswers
          .setOrException(SelectSubcontractorPage, Set(SubcontractorViewModel("10", "Name 10")))
          .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
          .setOrException(NewestVerificationBatchResponsePage, newestWithSchemeEmail("scheme@example.com"))
          .setOrException(CurrentVerificationBatchResponsePage, currentBatch)

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, onPageLoadRoute)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, never()).createSubmissionForVerification(any())(any())
      }
    }

    "must redirect to JourneyRecovery when CurrentVerificationBatchResponsePage is missing (buildSubmissionRequest fails)" in {
      val mockService = mock[VerificationService]

      val ua0 =
        emptyUserAnswers
          .setOrException(SelectSubcontractorPage, Set(SubcontractorViewModel("10", "Name 10")))
          .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
          .setOrException(NewestVerificationBatchResponsePage, newestWithSchemeEmail("scheme@example.com"))

      val ua =
        ua0.set(CisIdQuery, instanceId).success.value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, onPageLoadRoute)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, never()).createSubmissionForVerification(any())(any())
      }
    }

    "must redirect to JourneyRecovery when verificationBatchResourceRef is missing" in {
      val mockService = mock[VerificationService]

      val currentNoBatchRef =
        currentBatch.copy(
          verificationBatch = currentBatch.verificationBatch.map(_.copy(verifBatchResourceRef = None))
        )

      val ua0 =
        emptyUserAnswers
          .setOrException(SelectSubcontractorPage, Set(SubcontractorViewModel("10", "Name 10")))
          .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
          .setOrException(NewestVerificationBatchResponsePage, newestWithSchemeEmail("scheme@example.com"))
          .setOrException(CurrentVerificationBatchResponsePage, currentNoBatchRef)

      val ua =
        ua0.set(CisIdQuery, instanceId).success.value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, onPageLoadRoute)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, never()).createSubmissionForVerification(any())(any())
      }
    }

    "must redirect to JourneyRecovery when no email is resolved (e.g. DoNotSend)" in {
      val mockService = mock[VerificationService]

      val ua0 =
        emptyUserAnswers
          .setOrException(SelectSubcontractorPage, Set(SubcontractorViewModel("10", "Name 10")))
          .setOrException(ContractorEmailConfirmationStoredPage, DoNotSend)
          .setOrException(CurrentVerificationBatchResponsePage, currentBatch)

      val ua =
        ua0.set(CisIdQuery, instanceId).success.value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, onPageLoadRoute)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, never()).createSubmissionForVerification(any())(any())
      }
    }
  }
}

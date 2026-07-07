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
import controllers.routes
import models.*
import models.requests.{CreateVerifications, DeleteVerifications, ModifyVerificationsRequest}
import models.response.GetCurrentVerificationBatchResponse
import models.verify.SelectedSubcontractors
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.verify.{CurrentVerificationBatchResponsePage, SelectSubcontractorPage, SelectSubcontractorsToReverifyPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import services.VerificationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ModifyVerificationBatchAndVerificationsControllerSpec extends SpecBase with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val hc: HeaderCarrier    = HeaderCarrier()

  private val instanceId = "INST-123"

  private def currentSubcontractor(
    id: Long,
    ref: Option[Long]
  ): SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = id,
      subbieResourceRef = ref,
      firstName = None,
      secondName = None,
      surname = None,
      tradingName = None,
      utr = None,
      nino = None,
      crn = None,
      partnerUtr = None,
      partnershipTradingName = None,
      subcontractorType = None,
      addressLine1 = None,
      addressLine2 = None,
      addressLine3 = None,
      addressLine4 = None,
      country = None,
      postcode = None,
      worksReferenceNumber = None
    )

  private val currentBatch: GetCurrentVerificationBatchResponse =
    GetCurrentVerificationBatchResponse(
      subcontractors = Seq(
        currentSubcontractor(10L, Some(1111L)),
        currentSubcontractor(20L, Some(2222L)),
        currentSubcontractor(30L, Some(3333L))
      ),
      verificationBatch = Some(
        VerificationBatchCurrentVerification(
          verificationBatchId = 999L,
          verifBatchResourceRef = Some(7777L)
        )
      ),
      verifications = Seq(
        VerificationCurrentVerification(
          verificationId = 1L,
          verificationBatchId = Some(999L),
          subcontractorId = Some(10L),
          verificationResourceRef = Some(1111L)
        ),
        VerificationCurrentVerification(
          verificationId = 2L,
          verificationBatchId = Some(999L),
          subcontractorId = Some(30L),
          verificationResourceRef = Some(3333L)
        )
      )
    )

  "ModifyVerificationBatchAndVerificationsController.modifyVerificationBatch" - {

    "must call service with correct ModifyVerificationsRequest (create + delete) and redirect to Index" in {
      val mockService = mock[VerificationService]

      // selected: 10, 20 => refs 1111,2222
      // existing: 1111,3333
      // createRefs = 2222 ; deleteRefs = 3333
      val ua =
        emptyUserAnswers
          .set(CisIdQuery, instanceId)
          .success
          .value
          .set(CurrentVerificationBatchResponsePage, currentBatch)
          .success
          .value
          .set(
            SelectSubcontractorPage,
            Set(
              SubcontractorViewModel("10", "Name 10"),
              SubcontractorViewModel("20", "Name 20")
            )
          )
          .success
          .value
          .set(SelectSubcontractorsToReverifyPage, Set.empty[SelectedSubcontractors])
          .success
          .value

      when(
        mockService.modifyVerificationBatchAndVerifications(any[UserAnswers], any[ModifyVerificationsRequest])(any())
      )
        .thenReturn(Future.successful(ua))

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val controller = app.injector.instanceOf[ModifyVerificationBatchAndVerificationsController]
        val result     = controller.modifyVerificationBatch()(FakeRequest(POST, "/test-only"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.IndexController.onPageLoad().url

        val reqCaptor = ArgumentCaptor.forClass(classOf[ModifyVerificationsRequest])
        verify(mockService).modifyVerificationBatchAndVerifications(eqTo(ua), reqCaptor.capture())(any())

        reqCaptor.getValue mustBe ModifyVerificationsRequest(
          instanceId = instanceId,
          deleteVerifications = Some(DeleteVerifications(Seq(3333L))),
          createVerifications =
            Some(CreateVerifications(verificationBatchResourceRef = 7777L, verificationResourceReferences = Seq(2222L)))
        )
      }
    }

    "must not call service when no changes required and redirect to Index" in {
      val mockService = mock[VerificationService]

      // selected: 10,30 => refs 1111,3333
      // existing: 1111,3333 => so no changes required
      val ua =
        emptyUserAnswers
          .set(CisIdQuery, instanceId)
          .success
          .value
          .set(CurrentVerificationBatchResponsePage, currentBatch)
          .success
          .value
          .set(
            SelectSubcontractorPage,
            Set(
              SubcontractorViewModel("10", "Name 10"),
              SubcontractorViewModel("30", "Name 30")
            )
          )
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val controller = app.injector.instanceOf[ModifyVerificationBatchAndVerificationsController]
        val result     = controller.modifyVerificationBatch()(FakeRequest(POST, "/test-only"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.IndexController.onPageLoad().url

        verify(mockService, never()).modifyVerificationBatchAndVerifications(any(), any())(any())
      }
    }

    "must redirect to JourneyRecovery when invalid subcontractor id found (parseIds Left)" in {
      val mockService = mock[VerificationService]

      val ua =
        emptyUserAnswers
          .set(CisIdQuery, instanceId)
          .success
          .value
          .set(CurrentVerificationBatchResponsePage, currentBatch)
          .success
          .value
          .set(SelectSubcontractorPage, Set(SubcontractorViewModel("not-a-long", "Bad")))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val controller = app.injector.instanceOf[ModifyVerificationBatchAndVerificationsController]
        val result     = controller.modifyVerificationBatch()(FakeRequest(POST, "/test-only"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, never()).modifyVerificationBatchAndVerifications(any(), any())(any())
      }
    }

    "must redirect to JourneyRecovery when CisIdQuery is missing" in {
      val mockService = mock[VerificationService]

      val ua =
        emptyUserAnswers
          .set(CurrentVerificationBatchResponsePage, currentBatch)
          .success
          .value
          .set(SelectSubcontractorPage, Set(SubcontractorViewModel("10", "Name 10")))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val controller = app.injector.instanceOf[ModifyVerificationBatchAndVerificationsController]
        val result     = controller.modifyVerificationBatch()(FakeRequest(POST, "/test-only"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, never()).modifyVerificationBatchAndVerifications(any(), any())(any())
      }
    }

    "must redirect to JourneyRecovery when CurrentVerificationBatchResponsePage is missing" in {
      val mockService = mock[VerificationService]

      val ua =
        emptyUserAnswers
          .set(CisIdQuery, instanceId)
          .success
          .value
          .set(SelectSubcontractorPage, Set(SubcontractorViewModel("10", "Name 10")))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val controller = app.injector.instanceOf[ModifyVerificationBatchAndVerificationsController]
        val result     = controller.modifyVerificationBatch()(FakeRequest(POST, "/test-only"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, never()).modifyVerificationBatchAndVerifications(any(), any())(any())
      }
    }

    "must redirect to JourneyRecovery when selected subcontractor id has no subbieResourceRef in current batch" in {
      val mockService = mock[VerificationService]

      // select 40 which doesn't exist in currentBatch.subcontractors so thsi should fail
      val ua =
        emptyUserAnswers
          .set(CisIdQuery, instanceId)
          .success
          .value
          .set(CurrentVerificationBatchResponsePage, currentBatch)
          .success
          .value
          .set(SelectSubcontractorPage, Set(SubcontractorViewModel("40", "Name 40")))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val controller = app.injector.instanceOf[ModifyVerificationBatchAndVerificationsController]
        val result     = controller.modifyVerificationBatch()(FakeRequest(POST, "/test-only"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, never()).modifyVerificationBatchAndVerifications(any(), any())(any())
      }
    }

    "must redirect to JourneyRecovery when create is required but verificationBatchResourceRef missing" in {
      val mockService = mock[VerificationService]

      val currentWithoutBatchRef =
        currentBatch.copy(
          verificationBatch = currentBatch.verificationBatch.map(_.copy(verifBatchResourceRef = None))
        )

      // select 20 => selectedRefs = 2222; existingRefs = 1111,3333 => create needed
      // but verificationBatchResourceRef missing => so we fail and redirect to journeyRecovery
      val ua =
        emptyUserAnswers
          .set(CisIdQuery, instanceId)
          .success
          .value
          .set(CurrentVerificationBatchResponsePage, currentWithoutBatchRef)
          .success
          .value
          .set(SelectSubcontractorPage, Set(SubcontractorViewModel("20", "Name 20")))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val controller = app.injector.instanceOf[ModifyVerificationBatchAndVerificationsController]
        val result     = controller.modifyVerificationBatch()(FakeRequest(POST, "/test-only"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

        verify(mockService, never()).modifyVerificationBatchAndVerifications(any(), any())(any())
      }
    }

    "must default missing SelectSubcontractorPage to Seq.empty and use only SelectSubcontractorsToReverifyPage ids" in {
      val mockService = mock[VerificationService]

      val ua =
        emptyUserAnswers
          .set(CisIdQuery, instanceId)
          .success
          .value
          .set(CurrentVerificationBatchResponsePage, currentBatch)
          .success
          .value
          .set(
            SelectSubcontractorsToReverifyPage,
            Set(SelectedSubcontractors("20", "Name 20"))
          )
          .success
          .value

      when(
        mockService.modifyVerificationBatchAndVerifications(any[UserAnswers], any[ModifyVerificationsRequest])(any())
      ).thenReturn(Future.successful(ua))

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[VerificationService].toInstance(mockService))
          .build()

      running(app) {
        val controller = app.injector.instanceOf[ModifyVerificationBatchAndVerificationsController]
        val result     = controller.modifyVerificationBatch()(FakeRequest(POST, "/test-only"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.IndexController.onPageLoad().url

        val reqCaptor = ArgumentCaptor.forClass(classOf[ModifyVerificationsRequest])
        verify(mockService).modifyVerificationBatchAndVerifications(eqTo(ua), reqCaptor.capture())(any())

        // selected = only 20 => selectedRefs = 2222
        // existing = 1111,3333
        // create = 2222
        // delete = 1111,3333
        reqCaptor.getValue mustBe ModifyVerificationsRequest(
          instanceId = instanceId,
          deleteVerifications = Some(DeleteVerifications(Seq(1111L, 3333L))),
          createVerifications =
            Some(CreateVerifications(verificationBatchResourceRef = 7777L, verificationResourceReferences = Seq(2222L)))
        )
      }
    }
  }
}

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

package controllers.insufficient

import base.SpecBase
import controllers.routes
import controllers.verify.CheckVerificationBatchReadinessController
import forms.insufficient.RemoveInsufficientSubcontractorNameYesNoFormProvider
import models.{NormalMode, Subcontractor, SubcontractorCurrentVerification, UserAnswers, VerificationBatchCurrentVerification, VerificationCurrentVerification}
import models.response.{DeleteVerificationResponse, GetCurrentVerificationBatchResponse}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.insufficient.RemoveInsufficientSubcontractorNameYesNoPage
import pages.verify.{CurrentVerificationBatchResponsePage, UnverifiedSubcontractorsPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.VerificationService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.insufficient.RemoveInsufficientSubcontractorNameYesNoView

import scala.concurrent.Future

class RemoveInsufficientSubcontractorNameYesNoControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider =
    new RemoveInsufficientSubcontractorNameYesNoFormProvider()

  private val form =
    formProvider()

  private val mode =
    NormalMode

  private val subcontractorName =
    "Test Subcontractor"

  private val verificationResourceRef =
    12345L

  private val subcontractorId =
    10L

  private val currentBatchResponse: GetCurrentVerificationBatchResponse =
    GetCurrentVerificationBatchResponse(
      subcontractors = Seq(
        SubcontractorCurrentVerification(
          subcontractorId = subcontractorId,
          subbieResourceRef = Some(1111L),
          firstName = None,
          secondName = None,
          surname = None,
          tradingName = Some(subcontractorName),
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
          emailAddress = None,
          phoneNumber = None,
          mobilePhoneNumber = None,
          worksReferenceNumber = None,
          matched = None,
          autoVerified = None,
          verified = None,
          verificationNumber = None,
          taxTreatment = None,
          verificationDate = None,
          version = None,
          updatedTaxTreatment = None,
          lastMonthlyReturnDate = None,
          pendingVerifications = None
        )
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
          subcontractorId = Some(subcontractorId),
          verificationResourceRef = Some(verificationResourceRef),
          subcontractorName = None,
          verificationNumber = None,
          taxTreatment = None,
          actionIndicator = None,
          proceed = None,
          matched = None
        )
      )
    )

  private val unverifiedSubcontractor =
    Subcontractor(
      subcontractorId = 1L,
      firstName = None,
      secondName = None,
      surname = None,
      tradingName = Some("Subcontractor Ltd"),
      partnershipTradingName = None,
      verified = Some("N"),
      verificationNumber = None,
      taxTreatment = None,
      verificationDate = None,
      lastMonthlyReturnDate = None,
      createDate = None,
      subcontractorType = None,
      subbieResourceRef = Some(1L),
      utr = None,
      partnerUtr = None,
      crn = None,
      nino = None
    )

  private def userAnswersWithCurrentBatch: UserAnswers =
    emptyUserAnswers
      .set(
        CurrentVerificationBatchResponsePage,
        currentBatchResponse
      )
      .success
      .value

  private def getRoute(
    ref: Long = verificationResourceRef
  ): String =
    controllers.insufficient.routes.RemoveInsufficientSubcontractorNameYesNoController
      .onPageLoad(ref)
      .url

  private def postRoute(
    ref: Long = verificationResourceRef
  ): String =
    controllers.insufficient.routes.RemoveInsufficientSubcontractorNameYesNoController
      .onSubmit(ref)
      .url

  "RemoveInsufficientSubcontractorNameYesNoController" - {

    "onPageLoad" - {

      "must return OK and the correct view when the subcontractor name exists in the current batch" in {

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithCurrentBatch)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              getRoute()
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[
                RemoveInsufficientSubcontractorNameYesNoView
              ]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              form,
              mode,
              subcontractorName,
              verificationResourceRef
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must populate the form when the question has previously been answered" in {

        val userAnswers =
          userAnswersWithCurrentBatch
            .set(
              RemoveInsufficientSubcontractorNameYesNoPage(
                verificationResourceRef
              ),
              true
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              getRoute()
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[
                RemoveInsufficientSubcontractorNameYesNoView
              ]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              form.fill(true),
              mode,
              subcontractorName,
              verificationResourceRef
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must not populate the form with an answer saved for a different verification reference" in {

        val otherVerificationResourceRef =
          67890L

        val userAnswers =
          userAnswersWithCurrentBatch
            .set(
              RemoveInsufficientSubcontractorNameYesNoPage(
                otherVerificationResourceRef
              ),
              false
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              getRoute()
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[
                RemoveInsufficientSubcontractorNameYesNoView
              ]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              form,
              mode,
              subcontractorName,
              verificationResourceRef
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must display verify.noName when the current batch is missing" in {

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              getRoute()
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[
                RemoveInsufficientSubcontractorNameYesNoView
              ]

          val expectedName =
            messages(application)("verify.noName")

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              form,
              mode,
              expectedName,
              verificationResourceRef
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must display verify.noName when the verification reference is not present in the current batch" in {

        val unknownVerificationResourceRef =
          99999L

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithCurrentBatch)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              getRoute(unknownVerificationResourceRef)
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[
                RemoveInsufficientSubcontractorNameYesNoView
              ]

          val expectedName =
            messages(application)("verify.noName")

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              form,
              mode,
              expectedName,
              unknownVerificationResourceRef
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must display verify.noName when the default negative verification reference is used" in {

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithCurrentBatch)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              controllers.insufficient.routes.RemoveInsufficientSubcontractorNameYesNoController
                .onPageLoad()
                .url
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[
                RemoveInsufficientSubcontractorNameYesNoView
              ]

          val expectedName =
            messages(application)("verify.noName")

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              form,
              mode,
              expectedName,
              -1L
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must redirect to Journey Recovery when no user answers exist" in {

        val application =
          applicationBuilder(
            userAnswers = None
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              getRoute()
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController
              .onPageLoad()
              .url
        }
      }
    }

    "onSubmit" - {

      "must return a Bad Request and display errors when invalid data is submitted" in {

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithCurrentBatch)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              postRoute()
            ).withFormUrlEncodedBody(
              "value" -> ""
            )

          val boundForm =
            form.bind(
              Map(
                "value" -> ""
              )
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[
                RemoveInsufficientSubcontractorNameYesNoView
              ]

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(
              boundForm,
              mode,
              subcontractorName,
              verificationResourceRef
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must use verify.noName on an invalid submission when the current batch is missing" in {

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              postRoute()
            ).withFormUrlEncodedBody(
              "value" -> ""
            )

          val boundForm =
            form.bind(
              Map(
                "value" -> ""
              )
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[
                RemoveInsufficientSubcontractorNameYesNoView
              ]

          val expectedName =
            messages(application)("verify.noName")

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(
              boundForm,
              mode,
              expectedName,
              verificationResourceRef
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must redirect to the insufficient subcontractors review page without deleting when the user selects no" in {

        val mockSessionRepository =
          mock[SessionRepository]

        val mockVerificationService =
          mock[VerificationService]

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithCurrentBatch)
          ).overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[VerificationService]
              .toInstance(mockVerificationService)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              postRoute()
            ).withFormUrlEncodedBody(
              "value" -> "false"
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
              .onPageLoad()
              .url

          verify(
            mockVerificationService,
            never()
          ).deleteVerification(
            any[UserAnswers],
            any[Long]
          )(
            any[HeaderCarrier]
          )

          val savedAnswersCaptor =
            ArgumentCaptor.forClass(
              classOf[UserAnswers]
            )

          verify(mockSessionRepository)
            .set(savedAnswersCaptor.capture())

          savedAnswersCaptor.getValue
            .get(
              RemoveInsufficientSubcontractorNameYesNoPage(
                verificationResourceRef
              )
            ) mustBe None
        }
      }

      "must delete the verification and return to the insufficient review page when verifications remain" in {

        val mockSessionRepository =
          mock[SessionRepository]

        val mockVerificationService =
          mock[VerificationService]

        val mockReadinessController =
          mock[CheckVerificationBatchReadinessController]

        val answersAfterReadiness =
          userAnswersWithCurrentBatch

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        when(
          mockSessionRepository.get(eqTo(userAnswersId))
        ).thenReturn(
          Future.successful(
            Some(userAnswersWithCurrentBatch)
          )
        )

        when(
          mockVerificationService.deleteVerification(
            any[UserAnswers],
            eqTo(verificationResourceRef)
          )(
            any[HeaderCarrier]
          )
        ).thenReturn(
          Future.successful(
            DeleteVerificationResponse(Some(1L))
          )
        )

        when(
          mockReadinessController
            .updateVerificationBatchReadiness(
              any[UserAnswers]
            )
        ).thenReturn(
          Future.successful(
            Some(answersAfterReadiness)
          )
        )

        when(
          mockVerificationService
            .refreshNewestVerificationBatch(
              any[UserAnswers]
            )(
              any[HeaderCarrier]
            )
        ).thenReturn(
          Future.successful(answersAfterReadiness)
        )

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithCurrentBatch)
          ).overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[VerificationService]
              .toInstance(mockVerificationService),
            bind[CheckVerificationBatchReadinessController]
              .toInstance(mockReadinessController)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              postRoute()
            ).withFormUrlEncodedBody(
              "value" -> "true"
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController
              .onPageLoad()
              .url

          verify(mockVerificationService)
            .deleteVerification(
              any[UserAnswers],
              eqTo(verificationResourceRef)
            )(
              any[HeaderCarrier]
            )

          verify(mockReadinessController)
            .updateVerificationBatchReadiness(
              any[UserAnswers]
            )

          verify(mockVerificationService)
            .refreshNewestVerificationBatch(
              any[UserAnswers]
            )(
              any[HeaderCarrier]
            )

          val savedAnswersCaptor =
            ArgumentCaptor.forClass(
              classOf[UserAnswers]
            )

          verify(mockSessionRepository)
            .set(savedAnswersCaptor.capture())

          savedAnswersCaptor.getValue
            .get(
              RemoveInsufficientSubcontractorNameYesNoPage(
                verificationResourceRef
              )
            ) mustBe None
        }
      }

      "must redirect to Journey Recovery when verifications remain but updated session answers cannot be found" in {

        val mockSessionRepository =
          mock[SessionRepository]

        val mockVerificationService =
          mock[VerificationService]

        val mockReadinessController =
          mock[CheckVerificationBatchReadinessController]

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        when(
          mockSessionRepository.get(eqTo(userAnswersId))
        ).thenReturn(
          Future.successful(None)
        )

        when(
          mockVerificationService.deleteVerification(
            any[UserAnswers],
            eqTo(verificationResourceRef)
          )(
            any[HeaderCarrier]
          )
        ).thenReturn(
          Future.successful(
            DeleteVerificationResponse(Some(1L))
          )
        )

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithCurrentBatch)
          ).overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[VerificationService]
              .toInstance(mockVerificationService),
            bind[CheckVerificationBatchReadinessController]
              .toInstance(mockReadinessController)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              postRoute()
            ).withFormUrlEncodedBody(
              "value" -> "true"
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(
            mockReadinessController,
            never()
          ).updateVerificationBatchReadiness(
            any[UserAnswers]
          )

          verify(
            mockVerificationService,
            never()
          ).refreshNewestVerificationBatch(
            any[UserAnswers]
          )(
            any[HeaderCarrier]
          )
        }
      }

      "must redirect to Journey Recovery when the readiness update returns no answers" in {

        val mockSessionRepository =
          mock[SessionRepository]

        val mockVerificationService =
          mock[VerificationService]

        val mockReadinessController =
          mock[CheckVerificationBatchReadinessController]

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        when(
          mockSessionRepository.get(eqTo(userAnswersId))
        ).thenReturn(
          Future.successful(
            Some(userAnswersWithCurrentBatch)
          )
        )

        when(
          mockVerificationService.deleteVerification(
            any[UserAnswers],
            eqTo(verificationResourceRef)
          )(
            any[HeaderCarrier]
          )
        ).thenReturn(
          Future.successful(
            DeleteVerificationResponse(Some(1L))
          )
        )

        when(
          mockReadinessController
            .updateVerificationBatchReadiness(
              any[UserAnswers]
            )
        ).thenReturn(
          Future.successful(None)
        )

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithCurrentBatch)
          ).overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[VerificationService]
              .toInstance(mockVerificationService),
            bind[CheckVerificationBatchReadinessController]
              .toInstance(mockReadinessController)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              postRoute()
            ).withFormUrlEncodedBody(
              "value" -> "true"
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(
            mockVerificationService,
            never()
          ).refreshNewestVerificationBatch(
            any[UserAnswers]
          )(
            any[HeaderCarrier]
          )
        }
      }

      "must redirect to select subcontractors to reverify when no verifications and no unverified subcontractors remain" in {

        val mockSessionRepository =
          mock[SessionRepository]

        val mockVerificationService =
          mock[VerificationService]

        val mockReadinessController =
          mock[CheckVerificationBatchReadinessController]

        val refreshedAnswers =
          emptyUserAnswers
            .set(
              UnverifiedSubcontractorsPage,
              Seq.empty[Subcontractor]
            )
            .success
            .value

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        when(
          mockVerificationService.deleteVerification(
            any[UserAnswers],
            eqTo(verificationResourceRef)
          )(
            any[HeaderCarrier]
          )
        ).thenReturn(
          Future.successful(
            DeleteVerificationResponse(Some(0L))
          )
        )

        when(
          mockVerificationService
            .getCurrentVerificationBatch(
              any[UserAnswers]
            )(
              any[HeaderCarrier]
            )
        ).thenReturn(
          Future.successful(emptyUserAnswers)
        )

        when(
          mockVerificationService
            .refreshNewestVerificationBatch(
              any[UserAnswers]
            )(
              any[HeaderCarrier]
            )
        ).thenReturn(
          Future.successful(refreshedAnswers)
        )

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithCurrentBatch)
          ).overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[VerificationService]
              .toInstance(mockVerificationService),
            bind[CheckVerificationBatchReadinessController]
              .toInstance(mockReadinessController)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              postRoute()
            ).withFormUrlEncodedBody(
              "value" -> "true"
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.verify.routes.SelectSubcontractorsToReverifyController
              .onPageLoad(NormalMode)
              .url

          verify(
            mockReadinessController,
            never()
          ).updateVerificationBatchReadiness(
            any[UserAnswers]
          )

          verify(mockVerificationService)
            .getCurrentVerificationBatch(
              any[UserAnswers]
            )(
              any[HeaderCarrier]
            )

          verify(mockVerificationService)
            .refreshNewestVerificationBatch(
              any[UserAnswers]
            )(
              any[HeaderCarrier]
            )
        }
      }

      "must redirect to select subcontractor when no verifications remain and unverified subcontractors exist" in {

        val mockSessionRepository =
          mock[SessionRepository]

        val mockVerificationService =
          mock[VerificationService]

        val mockReadinessController =
          mock[CheckVerificationBatchReadinessController]

        val refreshedAnswers =
          emptyUserAnswers
            .set(
              UnverifiedSubcontractorsPage,
              Seq(unverifiedSubcontractor)
            )
            .success
            .value

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        when(
          mockVerificationService.deleteVerification(
            any[UserAnswers],
            eqTo(verificationResourceRef)
          )(
            any[HeaderCarrier]
          )
        ).thenReturn(
          Future.successful(
            DeleteVerificationResponse(Some(0L))
          )
        )

        when(
          mockVerificationService
            .getCurrentVerificationBatch(
              any[UserAnswers]
            )(
              any[HeaderCarrier]
            )
        ).thenReturn(
          Future.successful(emptyUserAnswers)
        )

        when(
          mockVerificationService
            .refreshNewestVerificationBatch(
              any[UserAnswers]
            )(
              any[HeaderCarrier]
            )
        ).thenReturn(
          Future.successful(refreshedAnswers)
        )

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithCurrentBatch)
          ).overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[VerificationService]
              .toInstance(mockVerificationService),
            bind[CheckVerificationBatchReadinessController]
              .toInstance(mockReadinessController)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              postRoute()
            ).withFormUrlEncodedBody(
              "value" -> "true"
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.verify.routes.SelectSubcontractorController
              .onPageLoad(NormalMode)
              .url

          verify(
            mockReadinessController,
            never()
          ).updateVerificationBatchReadiness(
            any[UserAnswers]
          )

          verify(mockVerificationService)
            .getCurrentVerificationBatch(
              any[UserAnswers]
            )(
              any[HeaderCarrier]
            )

          verify(mockVerificationService)
            .refreshNewestVerificationBatch(
              any[UserAnswers]
            )(
              any[HeaderCarrier]
            )
        }
      }

      "must redirect to Journey Recovery when the delete response does not contain a verification count" in {

        val mockSessionRepository =
          mock[SessionRepository]

        val mockVerificationService =
          mock[VerificationService]

        val mockReadinessController =
          mock[CheckVerificationBatchReadinessController]

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        when(
          mockVerificationService.deleteVerification(
            any[UserAnswers],
            eqTo(verificationResourceRef)
          )(
            any[HeaderCarrier]
          )
        ).thenReturn(
          Future.successful(
            DeleteVerificationResponse(None)
          )
        )

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithCurrentBatch)
          ).overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[VerificationService]
              .toInstance(mockVerificationService),
            bind[CheckVerificationBatchReadinessController]
              .toInstance(mockReadinessController)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              postRoute()
            ).withFormUrlEncodedBody(
              "value" -> "true"
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(
            mockReadinessController,
            never()
          ).updateVerificationBatchReadiness(
            any[UserAnswers]
          )

          verify(
            mockVerificationService,
            never()
          ).getCurrentVerificationBatch(
            any[UserAnswers]
          )(
            any[HeaderCarrier]
          )

          verify(
            mockVerificationService,
            never()
          ).refreshNewestVerificationBatch(
            any[UserAnswers]
          )(
            any[HeaderCarrier]
          )
        }
      }

      "must redirect to Journey Recovery when a negative verification reference is submitted" in {

        val mockSessionRepository =
          mock[SessionRepository]

        val mockVerificationService =
          mock[VerificationService]

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          ).overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[VerificationService]
              .toInstance(mockVerificationService)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              postRoute(-1L)
            ).withFormUrlEncodedBody(
              "value" -> "true"
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(
            mockVerificationService,
            never()
          ).deleteVerification(
            any[UserAnswers],
            any[Long]
          )(
            any[HeaderCarrier]
          )
        }
      }

      "must redirect to Journey Recovery when deleting the verification fails" in {

        val mockSessionRepository =
          mock[SessionRepository]

        val mockVerificationService =
          mock[VerificationService]

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        when(
          mockVerificationService.deleteVerification(
            any[UserAnswers],
            eqTo(verificationResourceRef)
          )(
            any[HeaderCarrier]
          )
        ).thenReturn(
          Future.failed(
            new RuntimeException("Delete failed")
          )
        )

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithCurrentBatch)
          ).overrides(
            bind[SessionRepository]
              .toInstance(mockSessionRepository),
            bind[VerificationService]
              .toInstance(mockVerificationService)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              postRoute()
            ).withFormUrlEncodedBody(
              "value" -> "true"
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController
              .onPageLoad()
              .url
        }
      }

      "must redirect to Journey Recovery when no user answers exist" in {

        val application =
          applicationBuilder(
            userAnswers = None
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              postRoute()
            ).withFormUrlEncodedBody(
              "value" -> "true"
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController
              .onPageLoad()
              .url
        }
      }
    }
  }
}

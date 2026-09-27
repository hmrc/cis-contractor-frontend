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
import models.response.GetCurrentVerificationBatchResponse
import models.verify.SelectedSubcontractors
import models.{NormalMode, SubcontractorCurrentVerification, SubcontractorViewModel, UserAnswers, VerificationCurrentVerification}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import pages.verify.{CurrentVerificationBatchResponsePage, ReverifyExistingSubcontractorsYesNoPage, SelectSubcontractorPage, SelectSubcontractorsToReverifyPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository

import scala.concurrent.Future

class ContinueVerificationSubmissionControllerSpec extends SpecBase {

  private lazy val endpointUrl =
    controllers.verify.routes.ContinueVerificationSubmissionController.onSubmit().url

  private def subcontractor(id: Long): SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = id,
      subbieResourceRef = None,
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

  private def verification(
    id: Long,
    name: String,
    proceed: Option[String] = Some("Y"),
    actionIndicator: Option[String] = None
  ): VerificationCurrentVerification =
    VerificationCurrentVerification(
      verificationId = id,
      verificationBatchId = None,
      subcontractorId = Some(id),
      verificationResourceRef = Some(id + 1000L),
      subcontractorName = Some(name),
      verificationNumber = None,
      taxTreatment = None,
      actionIndicator = actionIndicator,
      proceed = proceed,
      matched = None
    )

  private val readyCurrentBatch =
    GetCurrentVerificationBatchResponse(
      subcontractors = Seq(
        subcontractor(1L),
        subcontractor(2L)
      ),
      verificationBatch = None,
      verifications = Seq(
        verification(
          id = 1L,
          name = "Subcontractor One"
        ),
        verification(
          id = 2L,
          name = "Subcontractor Two"
        )
      )
    )

  private val notReadyCurrentBatch =
    GetCurrentVerificationBatchResponse(
      subcontractors = Seq(
        subcontractor(1L),
        subcontractor(2L)
      ),
      verificationBatch = None,
      verifications = Seq(
        verification(
          id = 1L,
          name = "Subcontractor One",
          proceed = None
        ),
        verification(
          id = 2L,
          name = "Subcontractor Two",
          proceed = None
        )
      )
    )

  "ContinueVerificationSubmissionController" - {

    "when there are no existing verification selections" - {

      "must prepare the ready unmatched batch as subcontractors to reverify" in {

        val userAnswers =
          emptyUserAnswers
            .setOrException(
              CurrentVerificationBatchResponsePage,
              readyCurrentBatch
            )

        val sessionRepository =
          mockSessionRepository(Some(userAnswers))

        when(sessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers),
            additionalBindings = Seq(
              bind[SessionRepository].toInstance(sessionRepository)
            )
          ).build()

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, endpointUrl)
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.verify.routes.CheckVerificationBatchReadinessController
              .checkVerificationBatchReadiness(NormalMode)
              .url

          val captor =
            ArgumentCaptor.forClass(classOf[UserAnswers])

          verify(sessionRepository)
            .set(captor.capture())

          val savedAnswers =
            captor.getValue

          savedAnswers
            .get(ReverifyExistingSubcontractorsYesNoPage) mustBe Some(true)

          savedAnswers
            .get(SelectSubcontractorsToReverifyPage) mustBe Some(
            Set(
              SelectedSubcontractors(
                "1",
                "Subcontractor One"
              ),
              SelectedSubcontractors(
                "2",
                "Subcontractor Two"
              )
            )
          )

          savedAnswers
            .get(SelectSubcontractorPage) mustBe None
        }
      }

      "must continue to CheckVerificationBatchReadiness without performing a second unmatched batch readiness check" in {

        val userAnswers =
          emptyUserAnswers
            .setOrException(
              CurrentVerificationBatchResponsePage,
              notReadyCurrentBatch
            )

        val sessionRepository =
          mockSessionRepository(Some(userAnswers))

        when(sessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers),
            additionalBindings = Seq(
              bind[SessionRepository].toInstance(sessionRepository)
            )
          ).build()

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, endpointUrl)
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.verify.routes.CheckVerificationBatchReadinessController
              .checkVerificationBatchReadiness(NormalMode)
              .url

          verify(sessionRepository)
            .set(any[UserAnswers])
        }
      }
    }

    "when existing verification selections are present" - {

      "must preserve selections when they match the current verification batch" in {

        val selectedToVerify =
          SubcontractorViewModel(
            id = "1",
            name = "Subcontractor One"
          )

        val selectedToReverify =
          SelectedSubcontractors(
            id = "2",
            name = "Subcontractor Two"
          )

        val userAnswers =
          emptyUserAnswers
            .setOrException(
              CurrentVerificationBatchResponsePage,
              readyCurrentBatch
            )
            .setOrException(
              SelectSubcontractorPage,
              Set(selectedToVerify)
            )
            .setOrException(
              SelectSubcontractorsToReverifyPage,
              Set(selectedToReverify)
            )

        val sessionRepository =
          mockSessionRepository(Some(userAnswers))

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers),
            additionalBindings = Seq(
              bind[SessionRepository].toInstance(sessionRepository)
            )
          ).build()

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, endpointUrl)
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.verify.routes.CheckVerificationBatchReadinessController
              .checkVerificationBatchReadiness(NormalMode)
              .url

          verify(sessionRepository, never())
            .set(any[UserAnswers])
        }
      }

      "must redirect to Journey Recovery when selections do not match the current verification batch" in {

        val selectedToVerify =
          SubcontractorViewModel(
            id = "1",
            name = "Subcontractor One"
          )

        val userAnswers =
          emptyUserAnswers
            .setOrException(
              CurrentVerificationBatchResponsePage,
              readyCurrentBatch
            )
            .setOrException(
              SelectSubcontractorPage,
              Set(selectedToVerify)
            )

        val sessionRepository =
          mockSessionRepository(Some(userAnswers))

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers),
            additionalBindings = Seq(
              bind[SessionRepository].toInstance(sessionRepository)
            )
          ).build()

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, endpointUrl)
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verify(sessionRepository, never())
            .set(any[UserAnswers])
        }
      }
    }

    "when CurrentVerificationBatchResponsePage is missing" - {

      "must redirect to Journey Recovery" in {

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          ).build()

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, endpointUrl)
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url
        }
      }
    }

    "when the current verification batch is empty" - {

      "must redirect to Journey Recovery" in {

        val emptyCurrentBatch =
          GetCurrentVerificationBatchResponse(
            subcontractors = Seq.empty,
            verificationBatch = None,
            verifications = Seq.empty
          )

        val userAnswers =
          emptyUserAnswers
            .setOrException(
              CurrentVerificationBatchResponsePage,
              emptyCurrentBatch
            )

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          ).build()

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, endpointUrl)
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url
        }
      }
    }

    "when UserAnswers are missing" - {

      "must redirect to Journey Recovery" in {

        val application =
          applicationBuilder(
            userAnswers = None
          ).build()

        running(application) {

          val result =
            route(
              application,
              FakeRequest(GET, endpointUrl)
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url
        }
      }
    }
  }
}

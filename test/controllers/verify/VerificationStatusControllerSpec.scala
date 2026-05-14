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
import models.{NormalMode, Subcontractor}
import models.response.GetNewestVerificationBatchResponse
import pages.verify.NewestVerificationBatchResponsePage
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class VerificationStatusControllerSpec extends SpecBase {

  private lazy val goToReverificationDecisionUrl =
    controllers.verify.routes.VerificationStatusController.goToReverificationDecision().url

  private lazy val goToSelectSubcontractorsToReverifyUrl =
    controllers.verify.routes.VerificationStatusController.goToSelectSubcontractorsToReverify().url

  private def newestResponse(subs: Seq[Subcontractor]): GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = None,
      subcontractors = subs,
      verificationBatch = None,
      verifications = Nil,
      submission = None,
      monthlyReturn = None
    )

  private def mkSub(id: Long, verified: Option[String]): Subcontractor =
    Subcontractor(
      subcontractorId = id,
      firstName = None,
      secondName = None,
      surname = None,
      tradingName = None,
      partnershipTradingName = None,
      verified = verified,
      verificationNumber = None,
      taxTreatment = None,
      verificationDate = None,
      lastMonthlyReturnDate = None,
      createDate = None,
      subcontractorType = None,
      subbieResourceRef = None,
      utr = None,
      partnerUtr = None,
      crn = None,
      nino = None
    )

  "VerificationStatusController.goToReverificationDecision" - {

    "must redirect to ReverifyExistingSubcontractorsYesNo when verified subcontractors exist" in {
      val ua =
        emptyUserAnswers
          .set(
            NewestVerificationBatchResponsePage,
            newestResponse(
              Seq(
                mkSub(1L, Some("N")),
                mkSub(2L, Some("Y"))
              )
            )
          )
          .success
          .value

      val app = applicationBuilder(userAnswers = Some(ua)).build()

      running(app) {
        val request = FakeRequest(GET, goToReverificationDecisionUrl)
        val controller = app.injector.instanceOf[VerificationStatusController]
        val result     = controller.goToReverificationDecision()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.verify.routes.ReverifyExistingSubcontractorsYesNoController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to JourneyRecovery when no verified subcontractors exist" in {
      val ua =
        emptyUserAnswers
          .set(
            NewestVerificationBatchResponsePage,
            newestResponse(
              Seq(
                mkSub(1L, Some("N")),
                mkSub(2L, None)
              )
            )
          )
          .success
          .value

      val app = applicationBuilder(userAnswers = Some(ua)).build()

      running(app) {
        val request = FakeRequest(GET, goToReverificationDecisionUrl)
        val controller = app.injector.instanceOf[VerificationStatusController]
        val result     = controller.goToReverificationDecision()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when NewestVerificationBatchResponsePage is missing" in {
      val app = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(app) {
        val request = FakeRequest(GET, goToReverificationDecisionUrl)
        val controller = app.injector.instanceOf[VerificationStatusController]
        val result     = controller.goToReverificationDecision()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when no UserAnswers exist" in {
      val app = applicationBuilder(userAnswers = None).build()

      running(app) {
        val request = FakeRequest(GET, goToReverificationDecisionUrl)
        val controller = app.injector.instanceOf[VerificationStatusController]
        val result     = controller.goToReverificationDecision()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "VerificationStatusController.goToSelectSubcontractorsToReverify" - {

    "must redirect to SelectSubcontractorsToReverify when verified subcontractors exist" in {
      val ua =
        emptyUserAnswers
          .set(
            NewestVerificationBatchResponsePage,
            newestResponse(
              Seq(
                mkSub(10L, Some("Y"))
              )
            )
          )
          .success
          .value

      val app = applicationBuilder(userAnswers = Some(ua)).build()

      running(app) {
        val request = FakeRequest(GET, goToSelectSubcontractorsToReverifyUrl)
        val controller = app.injector.instanceOf[VerificationStatusController]
        val result     = controller.goToSelectSubcontractorsToReverify()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to JourneyRecovery when no verified subcontractors exist" in {
      val ua =
        emptyUserAnswers
          .set(
            NewestVerificationBatchResponsePage,
            newestResponse(
              Seq(
                mkSub(10L, Some("N"))
              )
            )
          )
          .success
          .value

      val app = applicationBuilder(userAnswers = Some(ua)).build()

      running(app) {
        val request = FakeRequest(GET, goToSelectSubcontractorsToReverifyUrl)
        val controller = app.injector.instanceOf[VerificationStatusController]
        val result     = controller.goToSelectSubcontractorsToReverify()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when NewestVerificationBatchResponsePage is missing" in {
      val app = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(app) {
        val request = FakeRequest(GET, goToSelectSubcontractorsToReverifyUrl)
        val controller = app.injector.instanceOf[VerificationStatusController]
        val result     = controller.goToSelectSubcontractorsToReverify()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to JourneyRecovery when no UserAnswers exist" in {
      val app = applicationBuilder(userAnswers = None).build()

      running(app) {
        val request = FakeRequest(GET, goToSelectSubcontractorsToReverifyUrl)
        val controller = app.injector.instanceOf[VerificationStatusController]
        val result     = controller.goToSelectSubcontractorsToReverify()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
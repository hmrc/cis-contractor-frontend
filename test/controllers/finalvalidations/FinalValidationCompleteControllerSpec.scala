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

package controllers.finalvalidations

import base.SpecBase
import config.FrontendAppConfig
import models.UserAnswers
import models.finalvalidation.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.finalvalidation.*
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.FinalValidationHandoffService
import services.finalvalidation.*
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.util.Success

class FinalValidationCompleteControllerSpec extends SpecBase {

  private val instanceId       = "1"
  private val draftId          = "draft-id"
  private val otherDraftId     = "other-draft-id"
  private val handoffId        = "handoff-id"
  private val subcontractorId  = 101L
  private val subbieResourceRef = 100L

  private val payload =
    FinalValidationHandoffPayload(
      draftId = draftId,
      instanceId = instanceId,
      subcontractorId = subcontractorId,
      subbieResourceRef = subbieResourceRef,
      field = FinalValidationField.TradingName,
      changeTarget = FinalValidationChangeTarget.TradingName
    )

  private val correction =
    FinalValidationCorrection(
      subcontractorId = subcontractorId,
      changeTarget = FinalValidationChangeTarget.TradingName,
      patch = FinalValidationSubcontractorPatch(
        tradingName = Some("Updated Trading Name")
      )
    )

  private val updatedDraft =
    Json
      .obj(
        "subcontractors" -> Json.arr()
      )
      .as[FinalValidationDraft]

  private def finalValidationCompleteRoute =
    routes.FinalValidationCompleteController
      .onPageLoad()
      .url

  private def applicationWith(
                               userAnswers: Option[UserAnswers],
                               finalValidationHandoffService: FinalValidationHandoffService,
                               finalValidationDraftService: FinalValidationDraftService,
                               correctionBuilder: FinalValidationCorrectionBuilder
                             ) =
    applicationBuilder(userAnswers = userAnswers)
      .configure(
        "play.http.context" -> "/"
      )
      .overrides(
        bind[FinalValidationHandoffService]
          .toInstance(finalValidationHandoffService),
        bind[FinalValidationDraftService]
          .toInstance(finalValidationDraftService),
        bind[FinalValidationCorrectionBuilder]
          .toInstance(correctionBuilder)
      )
      .build()

  "FinalValidationCompleteController.onPageLoad" - {

    "when the context is VerifySubcontractor" - {

      "must update the correction and redirect to Update Subcontractor Details" in {
        val finalValidationHandoffService =
          mock[FinalValidationHandoffService]

        val finalValidationDraftService =
          mock[FinalValidationDraftService]

        val correctionBuilder =
          mock[FinalValidationCorrectionBuilder]

        val userAnswers =
          emptyUserAnswers
            .setOrException(
              FinalValidationContextPage,
              FinalValidationContext.VerifySubcontractor
            )
            .setOrException(
              VerifyFinalValidationPayloadPage,
              payload
            )
            .setOrException(
              FinalValidationDraftIdPage,
              draftId
            )

        when(
          correctionBuilder.build(
            userAnswers,
            payload
          )
        ).thenReturn(
          Success(correction)
        )

        when(
          finalValidationDraftService.updateCorrection(
            any[String],
            any[String],
            any[FinalValidationCorrection]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(updatedDraft)
        )

        val application =
          applicationWith(
            userAnswers = Some(userAnswers),
            finalValidationHandoffService = finalValidationHandoffService,
            finalValidationDraftService = finalValidationDraftService,
            correctionBuilder = correctionBuilder
          )

        running(application) {
          val request =
            FakeRequest(
              GET,
              finalValidationCompleteRoute
            )

          val result =
            route(application, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            routes.UpdateSubcontractorDetailsController
              .onPageLoad(subcontractorId)
              .url

          verify(correctionBuilder)
            .build(
              userAnswers,
              payload
            )

          verify(finalValidationDraftService)
            .updateCorrection(
              any[String],
              any[String],
              any[FinalValidationCorrection]
            )(any[HeaderCarrier])
        }
      }

      "must redirect to Journey Recovery when the payload draft id does not match the session draft id" in {
        val finalValidationHandoffService =
          mock[FinalValidationHandoffService]

        val finalValidationDraftService =
          mock[FinalValidationDraftService]

        val correctionBuilder =
          mock[FinalValidationCorrectionBuilder]

        val userAnswers =
          emptyUserAnswers
            .setOrException(
              FinalValidationContextPage,
              FinalValidationContext.VerifySubcontractor
            )
            .setOrException(
              VerifyFinalValidationPayloadPage,
              payload
            )
            .setOrException(
              FinalValidationDraftIdPage,
              otherDraftId
            )

        val application =
          applicationWith(
            userAnswers = Some(userAnswers),
            finalValidationHandoffService = finalValidationHandoffService,
            finalValidationDraftService = finalValidationDraftService,
            correctionBuilder = correctionBuilder
          )

        running(application) {
          val request =
            FakeRequest(
              GET,
              finalValidationCompleteRoute
            )

          val result =
            route(application, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verifyNoInteractions(
            correctionBuilder,
            finalValidationDraftService
          )
        }
      }

      "must redirect to Journey Recovery when the payload is missing" in {
        val finalValidationHandoffService =
          mock[FinalValidationHandoffService]

        val finalValidationDraftService =
          mock[FinalValidationDraftService]

        val correctionBuilder =
          mock[FinalValidationCorrectionBuilder]

        val userAnswers =
          emptyUserAnswers
            .setOrException(
              FinalValidationContextPage,
              FinalValidationContext.VerifySubcontractor
            )
            .setOrException(
              FinalValidationDraftIdPage,
              draftId
            )

        val application =
          applicationWith(
            userAnswers = Some(userAnswers),
            finalValidationHandoffService = finalValidationHandoffService,
            finalValidationDraftService = finalValidationDraftService,
            correctionBuilder = correctionBuilder
          )

        running(application) {
          val request =
            FakeRequest(
              GET,
              finalValidationCompleteRoute
            )

          val result =
            route(application, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verifyNoInteractions(
            correctionBuilder,
            finalValidationDraftService
          )
        }
      }

      "must redirect to Journey Recovery when the draft id is missing" in {
        val finalValidationHandoffService =
          mock[FinalValidationHandoffService]

        val finalValidationDraftService =
          mock[FinalValidationDraftService]

        val correctionBuilder =
          mock[FinalValidationCorrectionBuilder]

        val userAnswers =
          emptyUserAnswers
            .setOrException(
              FinalValidationContextPage,
              FinalValidationContext.VerifySubcontractor
            )
            .setOrException(
              VerifyFinalValidationPayloadPage,
              payload
            )

        val application =
          applicationWith(
            userAnswers = Some(userAnswers),
            finalValidationHandoffService = finalValidationHandoffService,
            finalValidationDraftService = finalValidationDraftService,
            correctionBuilder = correctionBuilder
          )

        running(application) {
          val request =
            FakeRequest(
              GET,
              finalValidationCompleteRoute
            )

          val result =
            route(application, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verifyNoInteractions(
            correctionBuilder,
            finalValidationDraftService
          )
        }
      }
    }

    "when the context is MonthlyReturn" - {

      "must update the correction and redirect to the CIS frontend return URL" in {
        val finalValidationHandoffService =
          mock[FinalValidationHandoffService]

        val finalValidationDraftService =
          mock[FinalValidationDraftService]

        val correctionBuilder =
          mock[FinalValidationCorrectionBuilder]

        val userAnswers =
          emptyUserAnswers
            .setOrException(
              FinalValidationContextPage,
              FinalValidationContext.MonthlyReturn
            )
            .setOrException(
              FinalValidationHandoffPage,
              handoffId
            )
            .setOrException(
              FinalValidationDraftIdPage,
              draftId
            )

        when(
          finalValidationHandoffService.getPayload(
            any[String]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            Some(payload)
          )
        )

        when(
          correctionBuilder.build(
            userAnswers,
            payload
          )
        ).thenReturn(
          Success(correction)
        )

        when(
          finalValidationDraftService.updateCorrection(
            any[String],
            any[String],
            any[FinalValidationCorrection]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(updatedDraft)
        )

        val application =
          applicationWith(
            userAnswers = Some(userAnswers),
            finalValidationHandoffService = finalValidationHandoffService,
            finalValidationDraftService = finalValidationDraftService,
            correctionBuilder = correctionBuilder
          )

        running(application) {
          val request =
            FakeRequest(
              GET,
              finalValidationCompleteRoute
            )

          val result =
            route(application, request).value

          val appConfig =
            application.injector
              .instanceOf[FrontendAppConfig]

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            appConfig.cisFrontendFinalValidationReturnUrl(
              handoffId
            )

          verify(correctionBuilder)
            .build(
              userAnswers,
              payload
            )

          verify(finalValidationDraftService)
            .updateCorrection(
              any[String],
              any[String],
              any[FinalValidationCorrection]
            )(any[HeaderCarrier])
        }
      }

      "must redirect to Journey Recovery when the handoff does not exist" in {
        val finalValidationHandoffService =
          mock[FinalValidationHandoffService]

        val finalValidationDraftService =
          mock[FinalValidationDraftService]

        val correctionBuilder =
          mock[FinalValidationCorrectionBuilder]

        val userAnswers =
          emptyUserAnswers
            .setOrException(
              FinalValidationContextPage,
              FinalValidationContext.MonthlyReturn
            )
            .setOrException(
              FinalValidationHandoffPage,
              handoffId
            )
            .setOrException(
              FinalValidationDraftIdPage,
              draftId
            )

        when(
          finalValidationHandoffService.getPayload(
            any[String]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(None)
        )

        val application =
          applicationWith(
            userAnswers = Some(userAnswers),
            finalValidationHandoffService = finalValidationHandoffService,
            finalValidationDraftService = finalValidationDraftService,
            correctionBuilder = correctionBuilder
          )

        running(application) {
          val request =
            FakeRequest(
              GET,
              finalValidationCompleteRoute
            )

          val result =
            route(application, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verifyNoInteractions(
            correctionBuilder,
            finalValidationDraftService
          )
        }
      }

      "must redirect to Journey Recovery when the payload draft id does not match the session draft id" in {
        val finalValidationHandoffService =
          mock[FinalValidationHandoffService]

        val finalValidationDraftService =
          mock[FinalValidationDraftService]

        val correctionBuilder =
          mock[FinalValidationCorrectionBuilder]

        val userAnswers =
          emptyUserAnswers
            .setOrException(
              FinalValidationContextPage,
              FinalValidationContext.MonthlyReturn
            )
            .setOrException(
              FinalValidationHandoffPage,
              handoffId
            )
            .setOrException(
              FinalValidationDraftIdPage,
              otherDraftId
            )

        when(
          finalValidationHandoffService.getPayload(
            any[String]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            Some(payload)
          )
        )

        val application =
          applicationWith(
            userAnswers = Some(userAnswers),
            finalValidationHandoffService = finalValidationHandoffService,
            finalValidationDraftService = finalValidationDraftService,
            correctionBuilder = correctionBuilder
          )

        running(application) {
          val request =
            FakeRequest(
              GET,
              finalValidationCompleteRoute
            )

          val result =
            route(application, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verifyNoInteractions(
            correctionBuilder,
            finalValidationDraftService
          )
        }
      }

      "must redirect to Journey Recovery when the draft id is missing" in {
        val finalValidationHandoffService =
          mock[FinalValidationHandoffService]

        val finalValidationDraftService =
          mock[FinalValidationDraftService]

        val correctionBuilder =
          mock[FinalValidationCorrectionBuilder]

        val userAnswers =
          emptyUserAnswers
            .setOrException(
              FinalValidationContextPage,
              FinalValidationContext.MonthlyReturn
            )
            .setOrException(
              FinalValidationHandoffPage,
              handoffId
            )

        when(
          finalValidationHandoffService.getPayload(
            any[String]
          )(any[HeaderCarrier])
        ).thenReturn(
          Future.successful(
            Some(payload)
          )
        )

        val application =
          applicationWith(
            userAnswers = Some(userAnswers),
            finalValidationHandoffService = finalValidationHandoffService,
            finalValidationDraftService = finalValidationDraftService,
            correctionBuilder = correctionBuilder
          )

        running(application) {
          val request =
            FakeRequest(
              GET,
              finalValidationCompleteRoute
            )

          val result =
            route(application, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verifyNoInteractions(
            correctionBuilder,
            finalValidationDraftService
          )
        }
      }

      "must redirect to Journey Recovery when the handoff id is missing" in {
        val finalValidationHandoffService =
          mock[FinalValidationHandoffService]

        val finalValidationDraftService =
          mock[FinalValidationDraftService]

        val correctionBuilder =
          mock[FinalValidationCorrectionBuilder]

        val userAnswers =
          emptyUserAnswers
            .setOrException(
              FinalValidationContextPage,
              FinalValidationContext.MonthlyReturn
            )
            .setOrException(
              FinalValidationDraftIdPage,
              draftId
            )

        val application =
          applicationWith(
            userAnswers = Some(userAnswers),
            finalValidationHandoffService = finalValidationHandoffService,
            finalValidationDraftService = finalValidationDraftService,
            correctionBuilder = correctionBuilder
          )

        running(application) {
          val request =
            FakeRequest(
              GET,
              finalValidationCompleteRoute
            )

          val result =
            route(application, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url

          verifyNoInteractions(
            finalValidationHandoffService,
            correctionBuilder,
            finalValidationDraftService
          )
        }
      }
    }

    "must redirect to Journey Recovery when the Final Validation context is missing" in {
      val finalValidationHandoffService =
        mock[FinalValidationHandoffService]

      val finalValidationDraftService =
        mock[FinalValidationDraftService]

      val correctionBuilder =
        mock[FinalValidationCorrectionBuilder]

      val application =
        applicationWith(
          userAnswers = Some(emptyUserAnswers),
          finalValidationHandoffService = finalValidationHandoffService,
          finalValidationDraftService = finalValidationDraftService,
          correctionBuilder = correctionBuilder
        )

      running(application) {
        val request =
          FakeRequest(
            GET,
            finalValidationCompleteRoute
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url

        verifyNoInteractions(
          finalValidationHandoffService,
          correctionBuilder,
          finalValidationDraftService
        )
      }
    }

    "must redirect to Journey Recovery when no UserAnswers exist" in {
      val finalValidationHandoffService =
        mock[FinalValidationHandoffService]

      val finalValidationDraftService =
        mock[FinalValidationDraftService]

      val correctionBuilder =
        mock[FinalValidationCorrectionBuilder]

      val application =
        applicationWith(
          userAnswers = None,
          finalValidationHandoffService = finalValidationHandoffService,
          finalValidationDraftService = finalValidationDraftService,
          correctionBuilder = correctionBuilder
        )

      running(application) {
        val request =
          FakeRequest(
            GET,
            finalValidationCompleteRoute
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url

        verifyNoInteractions(
          finalValidationHandoffService,
          correctionBuilder,
          finalValidationDraftService
        )
      }
    }
  }
}

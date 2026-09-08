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
import models.UserAnswers
import models.finalvalidation.*
import navigation.finalvalidation.FinalValidationNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.FinalValidationHandoffService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class FinalValidationHandoffControllerSpec extends SpecBase {

  private val handoffId       = "handoff-id"
  private val draftId         = "draft-id"
  private val instanceId      = "1"
  private val subcontractorId = 101L

  private val payload =
    FinalValidationHandoffPayload(
      draftId = draftId,
      instanceId = instanceId,
      subcontractorId = subcontractorId,
      subbieResourceRef = 100L,
      field = FinalValidationField.Utr,
      changeTarget = FinalValidationChangeTarget.TradingName
    )

  private val updatedAnswers =
    UserAnswers(userAnswersId)

  private val onwardRoute =
    Call("GET", "/foo")

  private def finalValidationHandoffRoute =
    routes.FinalValidationHandoffController
      .onPageLoad(handoffId)
      .url

  private def applicationWith(
    finalValidationHandoffService: FinalValidationHandoffService,
    finalValidationNavigator: FinalValidationNavigator
  ) =
    applicationBuilder()
      .configure(
        "play.http.context" -> "/"
      )
      .overrides(
        bind[FinalValidationHandoffService]
          .toInstance(finalValidationHandoffService),
        bind[FinalValidationNavigator]
          .toInstance(finalValidationNavigator)
      )
      .build()

  "FinalValidationHandoffController.onPageLoad" - {

    "must prepare the monthly return journey and redirect to the Final Validation start page" in {
      val finalValidationHandoffService =
        mock[FinalValidationHandoffService]

      val finalValidationNavigator =
        mock[FinalValidationNavigator]

      when(
        finalValidationHandoffService.prepareMonthlyReturnJourney(
          any[UserAnswers],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(
          Some(
            (
              updatedAnswers,
              payload
            )
          )
        )
      )

      when(
        finalValidationNavigator.startPage(
          payload.changeTarget,
          updatedAnswers
        )
      ).thenReturn(
        onwardRoute
      )

      val application =
        applicationWith(
          finalValidationHandoffService,
          finalValidationNavigator
        )

      running(application) {
        val request =
          FakeRequest(
            GET,
            finalValidationHandoffRoute
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          onwardRoute.url

        verify(finalValidationHandoffService)
          .prepareMonthlyReturnJourney(
            any[UserAnswers],
            any[String]
          )(any[HeaderCarrier])

        verify(finalValidationNavigator)
          .startPage(
            payload.changeTarget,
            updatedAnswers
          )
      }
    }

    "must redirect to Journey Recovery when the handoff cannot be found" in {
      val finalValidationHandoffService =
        mock[FinalValidationHandoffService]

      val finalValidationNavigator =
        mock[FinalValidationNavigator]

      when(
        finalValidationHandoffService.prepareMonthlyReturnJourney(
          any[UserAnswers],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(None)
      )

      val application =
        applicationWith(
          finalValidationHandoffService,
          finalValidationNavigator
        )

      running(application) {
        val request =
          FakeRequest(
            GET,
            finalValidationHandoffRoute
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url

        verifyNoInteractions(
          finalValidationNavigator
        )
      }
    }

    "must redirect to Journey Recovery when preparing the monthly return journey fails" in {
      val finalValidationHandoffService =
        mock[FinalValidationHandoffService]

      val finalValidationNavigator =
        mock[FinalValidationNavigator]

      when(
        finalValidationHandoffService.prepareMonthlyReturnJourney(
          any[UserAnswers],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.failed(
          new RuntimeException(
            "failed to prepare journey"
          )
        )
      )

      val application =
        applicationWith(
          finalValidationHandoffService,
          finalValidationNavigator
        )

      running(application) {
        val request =
          FakeRequest(
            GET,
            finalValidationHandoffRoute
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url

        verifyNoInteractions(
          finalValidationNavigator
        )
      }
    }
  }
}

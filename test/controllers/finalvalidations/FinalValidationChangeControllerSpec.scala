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
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.finalvalidation.*
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.FinalValidationSubcontractorService
import services.finalvalidation.FinalValidationDraftService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.util.{Failure, Success}

class FinalValidationChangeControllerSpec extends SpecBase {

  private val cisId = "1"
  private val draftId = "draft-id"
  private val subcontractorId = 1L
  private val subbieResourceRef = 100L

  private val field =
    FinalValidationField.Utr

  private val changeTarget =
    FinalValidationChangeTarget.TradingName

  private val onwardRoute =
    Call("GET", "/foo")

  private val userAnswers =
    userAnswersWithCisId
      .setOrException(
        FinalValidationDraftIdPage,
        draftId
      )

  private def finalValidationChangeRoute(
                                          fieldKey: String = field.key,
                                          targetKey: String = changeTarget.key
                                        ) =
    routes.FinalValidationChangeController
      .onPageLoad(
        subcontractorId,
        fieldKey,
        targetKey
      )
      .url

  private def draft(
                     id: Long = subcontractorId,
                     readiness: String = "Incomplete",
                     issues: Seq[FinalValidationDraftIssue] = Seq(
                       FinalValidationDraftIssue(
                         fieldKey = field.key,
                         value = Some("1234567890")
                       )
                     )
                   ): FinalValidationDraft =
    Json
      .obj(
        "subcontractors" -> Json.arr(
          Json.obj(
            "subcontractorId" -> id,
            "subbieResourceRef" -> subbieResourceRef,
            "baseVersion" -> 1,
            "subcontractorType" -> "soletrader",
            "displayName" -> "Test Subcontractor",
            "base" -> Json.obj(),
            "proposed" -> Json.obj(),
            "changedTargets" -> Json.arr(),
            "issues" -> Json.toJson(issues),
            "readiness" -> readiness
          )
        )
      )
      .as[FinalValidationDraft]

  private def applicationWith(
                               userAnswers: Option[UserAnswers],
                               finalValidationDraftService: FinalValidationDraftService,
                               finalValidationSubcontractorService: FinalValidationSubcontractorService,
                               finalValidationNavigator: FinalValidationNavigator,
                               sessionRepository: SessionRepository
                             ) =
    applicationBuilder(userAnswers = userAnswers)
      .configure(
        "play.http.context" -> "/"
      )
      .overrides(
        bind[FinalValidationDraftService]
          .toInstance(finalValidationDraftService),
        bind[FinalValidationSubcontractorService]
          .toInstance(finalValidationSubcontractorService),
        bind[FinalValidationNavigator]
          .toInstance(finalValidationNavigator),
        bind[SessionRepository]
          .toInstance(sessionRepository)
      )
      .build()

  "FinalValidationChangeController.onPageLoad" - {

    "must populate and save the Final Validation answers and redirect to the start page" in {
      val finalValidationDraftService =
        mock[FinalValidationDraftService]

      val finalValidationSubcontractorService =
        mock[FinalValidationSubcontractorService]

      val finalValidationNavigator =
        mock[FinalValidationNavigator]

      val sessionRepository =
        mock[SessionRepository]

      val currentDraft =
        draft()

      val subcontractor =
        currentDraft
          .subcontractor(subcontractorId)
          .value

      val payload =
        FinalValidationHandoffPayload(
          draftId = draftId,
          instanceId = cisId,
          subcontractorId = subcontractorId,
          subbieResourceRef = subbieResourceRef,
          field = field,
          changeTarget = changeTarget
        )

      val populatedAnswers =
        userAnswers

      val expectedAnswers =
        populatedAnswers
          .setOrException(
            VerifyFinalValidationPayloadPage,
            payload
          )
          .setOrException(
            FinalValidationContextPage,
            FinalValidationContext.VerifySubcontractor
          )

      when(
        finalValidationDraftService.get(
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(currentDraft)
      )

      when(
        finalValidationSubcontractorService.populateFinalValidationUserAnswers(
          userAnswers,
          cisId,
          subcontractor,
          changeTarget
        )
      ).thenReturn(
        Success(populatedAnswers)
      )

      when(
        finalValidationNavigator.startPage(
          changeTarget,
          expectedAnswers
        )
      ).thenReturn(
        onwardRoute
      )

      when(
        sessionRepository.set(expectedAnswers)
      ).thenReturn(
        Future.successful(true)
      )

      val application =
        applicationWith(
          userAnswers = Some(userAnswers),
          finalValidationDraftService = finalValidationDraftService,
          finalValidationSubcontractorService = finalValidationSubcontractorService,
          finalValidationNavigator = finalValidationNavigator,
          sessionRepository = sessionRepository
        )

      running(application) {
        val request =
          FakeRequest(
            GET,
            finalValidationChangeRoute()
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe onwardRoute.url

        verify(finalValidationSubcontractorService)
          .populateFinalValidationUserAnswers(
            userAnswers,
            cisId,
            subcontractor,
            changeTarget
          )

        verify(finalValidationNavigator)
          .startPage(
            changeTarget,
            expectedAnswers
          )

        verify(sessionRepository)
          .set(expectedAnswers)
      }
    }

    "must redirect to Journey Recovery when the draft id is missing" in {
      val finalValidationDraftService =
        mock[FinalValidationDraftService]

      val finalValidationSubcontractorService =
        mock[FinalValidationSubcontractorService]

      val finalValidationNavigator =
        mock[FinalValidationNavigator]

      val sessionRepository =
        mock[SessionRepository]

      val answersWithoutDraftId =
        userAnswersWithCisId

      val application =
        applicationWith(
          userAnswers = Some(answersWithoutDraftId),
          finalValidationDraftService = finalValidationDraftService,
          finalValidationSubcontractorService = finalValidationSubcontractorService,
          finalValidationNavigator = finalValidationNavigator,
          sessionRepository = sessionRepository
        )

      running(application) {
        val request =
          FakeRequest(
            GET,
            finalValidationChangeRoute()
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must redirect to Journey Recovery when the subcontractor is not in the draft" in {
      val finalValidationDraftService =
        mock[FinalValidationDraftService]

      val finalValidationSubcontractorService =
        mock[FinalValidationSubcontractorService]

      val finalValidationNavigator =
        mock[FinalValidationNavigator]

      val sessionRepository =
        mock[SessionRepository]

      when(
        finalValidationDraftService.get(
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(
          draft(id = 999L)
        )
      )

      val application =
        applicationWith(
          userAnswers = Some(userAnswers),
          finalValidationDraftService = finalValidationDraftService,
          finalValidationSubcontractorService = finalValidationSubcontractorService,
          finalValidationNavigator = finalValidationNavigator,
          sessionRepository = sessionRepository
        )

      running(application) {
        val request =
          FakeRequest(
            GET,
            finalValidationChangeRoute()
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must redirect to Journey Recovery when the subcontractor is already complete" in {
      val finalValidationDraftService =
        mock[FinalValidationDraftService]

      val finalValidationSubcontractorService =
        mock[FinalValidationSubcontractorService]

      val finalValidationNavigator =
        mock[FinalValidationNavigator]

      val sessionRepository =
        mock[SessionRepository]

      when(
        finalValidationDraftService.get(
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(
          draft(readiness = "Complete")
        )
      )

      val application =
        applicationWith(
          userAnswers = Some(userAnswers),
          finalValidationDraftService = finalValidationDraftService,
          finalValidationSubcontractorService = finalValidationSubcontractorService,
          finalValidationNavigator = finalValidationNavigator,
          sessionRepository = sessionRepository
        )

      running(application) {
        val request =
          FakeRequest(
            GET,
            finalValidationChangeRoute()
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must redirect to Journey Recovery when the requested field is not an issue" in {
      val finalValidationDraftService =
        mock[FinalValidationDraftService]

      val finalValidationSubcontractorService =
        mock[FinalValidationSubcontractorService]

      val finalValidationNavigator =
        mock[FinalValidationNavigator]

      val sessionRepository =
        mock[SessionRepository]

      when(
        finalValidationDraftService.get(
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(
          draft(issues = Seq.empty)
        )
      )

      val application =
        applicationWith(
          userAnswers = Some(userAnswers),
          finalValidationDraftService = finalValidationDraftService,
          finalValidationSubcontractorService = finalValidationSubcontractorService,
          finalValidationNavigator = finalValidationNavigator,
          sessionRepository = sessionRepository
        )

      running(application) {
        val request =
          FakeRequest(
            GET,
            finalValidationChangeRoute()
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must redirect to Journey Recovery when the issue field is invalid" in {
      val finalValidationDraftService =
        mock[FinalValidationDraftService]

      val finalValidationSubcontractorService =
        mock[FinalValidationSubcontractorService]

      val finalValidationNavigator =
        mock[FinalValidationNavigator]

      val sessionRepository =
        mock[SessionRepository]

      val invalidField =
        "invalid-field"

      val issues =
        Seq(
          FinalValidationDraftIssue(
            fieldKey = invalidField,
            value = None
          )
        )

      when(
        finalValidationDraftService.get(
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(
          draft(issues = issues)
        )
      )

      val application =
        applicationWith(
          userAnswers = Some(userAnswers),
          finalValidationDraftService = finalValidationDraftService,
          finalValidationSubcontractorService = finalValidationSubcontractorService,
          finalValidationNavigator = finalValidationNavigator,
          sessionRepository = sessionRepository
        )

      running(application) {
        val request =
          FakeRequest(
            GET,
            finalValidationChangeRoute(
              fieldKey = invalidField
            )
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must redirect to Journey Recovery when the change target is invalid" in {
      val finalValidationDraftService =
        mock[FinalValidationDraftService]

      val finalValidationSubcontractorService =
        mock[FinalValidationSubcontractorService]

      val finalValidationNavigator =
        mock[FinalValidationNavigator]

      val sessionRepository =
        mock[SessionRepository]

      when(
        finalValidationDraftService.get(
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(
          draft()
        )
      )

      val application =
        applicationWith(
          userAnswers = Some(userAnswers),
          finalValidationDraftService = finalValidationDraftService,
          finalValidationSubcontractorService = finalValidationSubcontractorService,
          finalValidationNavigator = finalValidationNavigator,
          sessionRepository = sessionRepository
        )

      running(application) {
        val request =
          FakeRequest(
            GET,
            finalValidationChangeRoute(
              targetKey = "invalid-target"
            )
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must redirect to Journey Recovery when populating the Final Validation answers fails" in {
      val finalValidationDraftService =
        mock[FinalValidationDraftService]

      val finalValidationSubcontractorService =
        mock[FinalValidationSubcontractorService]

      val finalValidationNavigator =
        mock[FinalValidationNavigator]

      val sessionRepository =
        mock[SessionRepository]

      val currentDraft =
        draft()

      val subcontractor =
        currentDraft
          .subcontractor(subcontractorId)
          .value

      when(
        finalValidationDraftService.get(
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(currentDraft)
      )

      when(
        finalValidationSubcontractorService.populateFinalValidationUserAnswers(
          userAnswers,
          cisId,
          subcontractor,
          changeTarget
        )
      ).thenReturn(
        Failure(
          new RuntimeException(
            "failed to populate answers"
          )
        )
      )

      val application =
        applicationWith(
          userAnswers = Some(userAnswers),
          finalValidationDraftService = finalValidationDraftService,
          finalValidationSubcontractorService = finalValidationSubcontractorService,
          finalValidationNavigator = finalValidationNavigator,
          sessionRepository = sessionRepository
        )

      running(application) {
        val request =
          FakeRequest(
            GET,
            finalValidationChangeRoute()
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }

    "must redirect to Journey Recovery when loading the draft fails" in {
      val finalValidationDraftService =
        mock[FinalValidationDraftService]

      val finalValidationSubcontractorService =
        mock[FinalValidationSubcontractorService]

      val finalValidationNavigator =
        mock[FinalValidationNavigator]

      val sessionRepository =
        mock[SessionRepository]

      when(
        finalValidationDraftService.get(
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.failed(
          new RuntimeException(
            "failed to load draft"
          )
        )
      )

      val application =
        applicationWith(
          userAnswers = Some(userAnswers),
          finalValidationDraftService = finalValidationDraftService,
          finalValidationSubcontractorService = finalValidationSubcontractorService,
          finalValidationNavigator = finalValidationNavigator,
          sessionRepository = sessionRepository
        )

      running(application) {
        val request =
          FakeRequest(
            GET,
            finalValidationChangeRoute()
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
            .url
      }
    }
  }
}

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

package services

import base.SpecBase
import connectors.ConstructionIndustrySchemeConnector
import models.UserAnswers
import models.finalvalidation.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.finalvalidation.*
import play.api.libs.json.{JsNull, Json}
import repositories.SessionRepository
import services.finalvalidation.FinalValidationDraftService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

class FinalValidationHandoffServiceSpec extends SpecBase {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "FinalValidationHandoffService" - {

    "getPayload" - {

      "must get the Final Validation handoff payload" in {

        val connector                           = mock[ConstructionIndustrySchemeConnector]
        val sessionRepository                   = mock[SessionRepository]
        val finalValidationDraftService         = mock[FinalValidationDraftService]
        val finalValidationSubcontractorService = mock[FinalValidationSubcontractorService]

        val service =
          new FinalValidationHandoffService(
            connector,
            sessionRepository,
            finalValidationDraftService,
            finalValidationSubcontractorService
          )

        val handoffId = "handoff-id"

        val payload =
          FinalValidationHandoffPayload(
            draftId = "draft-id",
            instanceId = "instance-id",
            subcontractorId = 1L,
            subbieResourceRef = 2L,
            field = FinalValidationField.Utr,
            changeTarget = FinalValidationChangeTarget.Utr
          )

        when(
          connector.getFinalValidationJourneyHandoff(
            any[String],
            any[String]
          )(using any[HeaderCarrier])
        ).thenReturn(Future.successful(Some(payload)))

        service.getPayload(handoffId).futureValue mustBe Some(payload)

        verify(connector).getFinalValidationJourneyHandoff(
          any[String],
          any[String]
        )(using any[HeaderCarrier])
      }
    }

    "prepareMonthlyReturnJourney" - {

      "must return None when the handoff does not exist" in {

        val connector                           = mock[ConstructionIndustrySchemeConnector]
        val sessionRepository                   = mock[SessionRepository]
        val finalValidationDraftService         = mock[FinalValidationDraftService]
        val finalValidationSubcontractorService = mock[FinalValidationSubcontractorService]

        val service =
          new FinalValidationHandoffService(
            connector,
            sessionRepository,
            finalValidationDraftService,
            finalValidationSubcontractorService
          )

        val handoffId = "handoff-id"

        when(
          connector.getFinalValidationJourneyHandoff(
            any[String],
            any[String]
          )(using any[HeaderCarrier])
        ).thenReturn(Future.successful(None))

        service
          .prepareMonthlyReturnJourney(
            emptyUserAnswers,
            handoffId
          )
          .futureValue mustBe None
      }

      "must prepare and store the Monthly Return Final Validation journey" in {

        val connector                           = mock[ConstructionIndustrySchemeConnector]
        val sessionRepository                   = mock[SessionRepository]
        val finalValidationDraftService         = mock[FinalValidationDraftService]
        val finalValidationSubcontractorService = mock[FinalValidationSubcontractorService]

        val service =
          new FinalValidationHandoffService(
            connector,
            sessionRepository,
            finalValidationDraftService,
            finalValidationSubcontractorService
          )

        val handoffId = "handoff-id"

        val payload =
          FinalValidationHandoffPayload(
            draftId = "draft-id",
            instanceId = "instance-id",
            subcontractorId = 1L,
            subbieResourceRef = 2L,
            field = FinalValidationField.Utr,
            changeTarget = FinalValidationChangeTarget.Utr
          )

        val draft =
          Json
            .obj(
              "subcontractors" -> Json.arr(
                Json.obj(
                  "subcontractorId"   -> 1L,
                  "subbieResourceRef" -> 2L,
                  "baseVersion"       -> JsNull,
                  "subcontractorType" -> JsNull,
                  "displayName"       -> "Test Subcontractor",
                  "base"              -> Json.obj(),
                  "proposed"          -> Json.obj(),
                  "changedTargets"    -> Json.arr(),
                  "issues"            -> Json.arr(),
                  "readiness"         -> "Incomplete"
                )
              )
            )
            .as[FinalValidationDraft]

        val subcontractor =
          draft.subcontractor(1L).value

        val userAnswers =
          UserAnswers("id")

        when(
          connector.getFinalValidationJourneyHandoff(
            any[String],
            any[String]
          )(using any[HeaderCarrier])
        ).thenReturn(Future.successful(Some(payload)))

        when(
          finalValidationDraftService.get(
            any[String],
            any[String]
          )(using any[HeaderCarrier])
        ).thenReturn(Future.successful(draft))

        when(
          finalValidationSubcontractorService.populateFinalValidationUserAnswers(
            userAnswers = userAnswers,
            instanceId = payload.instanceId,
            subcontractor = subcontractor,
            changeTarget = payload.changeTarget
          )
        ).thenReturn(Success(userAnswers))

        when(sessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val result =
          service
            .prepareMonthlyReturnJourney(
              userAnswers,
              handoffId
            )
            .futureValue
            .value

        val updatedAnswers =
          result._1

        result._2 mustBe payload

        updatedAnswers
          .get(FinalValidationContextPage)
          .value mustBe FinalValidationContext.MonthlyReturn

        updatedAnswers
          .get(FinalValidationDraftIdPage)
          .value mustBe payload.draftId

        updatedAnswers
          .get(FinalValidationHandoffPage)
          .value mustBe handoffId

        verify(finalValidationDraftService).get(
          any[String],
          any[String]
        )(using any[HeaderCarrier])

        verify(finalValidationSubcontractorService)
          .populateFinalValidationUserAnswers(
            userAnswers = userAnswers,
            instanceId = payload.instanceId,
            subcontractor = subcontractor,
            changeTarget = payload.changeTarget
          )

        verify(sessionRepository).set(updatedAnswers)
      }

      "must fail when the subcontractor is not present in the draft" in {

        val connector                           = mock[ConstructionIndustrySchemeConnector]
        val sessionRepository                   = mock[SessionRepository]
        val finalValidationDraftService         = mock[FinalValidationDraftService]
        val finalValidationSubcontractorService = mock[FinalValidationSubcontractorService]

        val service =
          new FinalValidationHandoffService(
            connector,
            sessionRepository,
            finalValidationDraftService,
            finalValidationSubcontractorService
          )

        val handoffId = "handoff-id"

        val payload =
          FinalValidationHandoffPayload(
            draftId = "draft-id",
            instanceId = "instance-id",
            subcontractorId = 1L,
            subbieResourceRef = 2L,
            field = FinalValidationField.Utr,
            changeTarget = FinalValidationChangeTarget.Utr
          )

        val draft =
          Json
            .obj(
              "subcontractors" -> Json.arr()
            )
            .as[FinalValidationDraft]

        when(
          connector.getFinalValidationJourneyHandoff(
            any[String],
            any[String]
          )(using any[HeaderCarrier])
        ).thenReturn(Future.successful(Some(payload)))

        when(
          finalValidationDraftService.get(
            any[String],
            any[String]
          )(using any[HeaderCarrier])
        ).thenReturn(Future.successful(draft))

        val exception =
          service
            .prepareMonthlyReturnJourney(
              emptyUserAnswers,
              handoffId
            )
            .failed
            .futureValue

        exception.getMessage mustBe
          "Subcontractor 1 not found in Final Validation draft draft-id"
      }

      "must fail when the updated UserAnswers cannot be stored" in {

        val connector                           = mock[ConstructionIndustrySchemeConnector]
        val sessionRepository                   = mock[SessionRepository]
        val finalValidationDraftService         = mock[FinalValidationDraftService]
        val finalValidationSubcontractorService = mock[FinalValidationSubcontractorService]

        val service =
          new FinalValidationHandoffService(
            connector,
            sessionRepository,
            finalValidationDraftService,
            finalValidationSubcontractorService
          )

        val handoffId = "handoff-id"

        val payload =
          FinalValidationHandoffPayload(
            draftId = "draft-id",
            instanceId = "instance-id",
            subcontractorId = 1L,
            subbieResourceRef = 2L,
            field = FinalValidationField.Utr,
            changeTarget = FinalValidationChangeTarget.Utr
          )

        val draft =
          Json
            .obj(
              "subcontractors" -> Json.arr(
                Json.obj(
                  "subcontractorId"   -> 1L,
                  "subbieResourceRef" -> 2L,
                  "baseVersion"       -> JsNull,
                  "subcontractorType" -> JsNull,
                  "displayName"       -> "Test Subcontractor",
                  "base"              -> Json.obj(),
                  "proposed"          -> Json.obj(),
                  "changedTargets"    -> Json.arr(),
                  "issues"            -> Json.arr(),
                  "readiness"         -> "Incomplete"
                )
              )
            )
            .as[FinalValidationDraft]

        val subcontractor =
          draft.subcontractor(1L).value

        val userAnswers =
          UserAnswers("id")

        when(
          connector.getFinalValidationJourneyHandoff(
            any[String],
            any[String]
          )(using any[HeaderCarrier])
        ).thenReturn(Future.successful(Some(payload)))

        when(
          finalValidationDraftService.get(
            any[String],
            any[String]
          )(using any[HeaderCarrier])
        ).thenReturn(Future.successful(draft))

        when(
          finalValidationSubcontractorService.populateFinalValidationUserAnswers(
            userAnswers = userAnswers,
            instanceId = payload.instanceId,
            subcontractor = subcontractor,
            changeTarget = payload.changeTarget
          )
        ).thenReturn(Success(userAnswers))

        when(sessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(false))

        val exception =
          service
            .prepareMonthlyReturnJourney(
              userAnswers,
              handoffId
            )
            .failed
            .futureValue

        exception.getMessage mustBe
          "Failed to store updated UserAnswers for handoffId: handoff-id"
      }
    }
  }
}

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

package controllers.actions

import base.SpecBase
import models.JourneyScope
import models.requests.{IdentifierRequest, SchemeRequest}
import models.response.CisTaxpayerResponse
import models.{EmployerReference, UserAnswers}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JourneyActionSpec extends SpecBase with MockitoSugar {

  private val uuid = UUID.fromString("11111111-1111-1111-1111-111111111111")

  private val taxpayer = CisTaxpayerResponse(
    uniqueId = "AB12",
    taxOfficeNumber = "123",
    taxOfficeRef = "AB456",
    aoDistrict = None,
    aoPayType = None,
    aoCheckCode = None,
    aoReference = None,
    validBusinessAddr = None,
    correlation = None,
    ggAgentId = None,
    employerName1 = None,
    employerName2 = None,
    agentOwnRef = None,
    schemeName = None,
    utr = None,
    enrolledSig = None
  )

  private val schemeRequest = new SchemeRequest(
    IdentifierRequest(FakeRequest(), "user-1", Some(EmployerReference("123", "AB456")), None),
    taxpayer
  )

  private val subJourneyId = JourneyScope.subJourneyId(schemeRequest.cisPath, uuid)

  "JourneyAction" - {

    "loads the login document when the folder exists" in {
      val answers    = UserAnswers("user-1").initialiseSubJourney(subJourneyId).success.value
      val repository = mock[SessionRepository]
      when(repository.get("user-1")).thenReturn(Future.successful(Some(answers)))

      val result = new JourneyActionImpl(repository)(uuid).invokeBlock(
        schemeRequest,
        journey => Future.successful(Results.Ok(journey.subJourneyId))
      )

      status(result) mustBe OK
      contentAsString(result) mustBe subJourneyId
    }

    "sends the user to journey recovery when the folder is missing" in {
      val repository = mock[SessionRepository]
      when(repository.get("user-1")).thenReturn(Future.successful(Some(UserAnswers("user-1"))))

      val result = new JourneyActionImpl(repository)(uuid).invokeBlock(
        schemeRequest,
        _ => Future.successful(Results.Ok)
      )

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
    }
  }
}

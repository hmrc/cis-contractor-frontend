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
import models.requests.SchemeRequest
import models.response.CisTaxpayerResponse
import models.{EmployerReference, UserAnswers}
import models.requests.IdentifierRequest
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JourneyServiceSpec extends SpecBase with MockitoSugar {

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

  "JourneyService" - {

    "saves a new journey under the login, the client and a UUID" in {
      val repository = mock[SessionRepository]
      when(repository.set(any[UserAnswers]())).thenReturn(Future.successful(true))

      val journeyId = new JourneyService(repository).create(schemeRequest).futureValue

      journeyId.asString must startWith("user-1/org/")

      val saved: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      org.mockito.Mockito.verify(repository).set(saved.capture())
      saved.getValue.id mustBe journeyId.asString
    }

    "does not return a journey id when the save fails" in {
      val repository = mock[SessionRepository]
      when(repository.set(any[UserAnswers]())).thenReturn(Future.successful(false))

      val failure = new JourneyService(repository).create(schemeRequest).failed.futureValue

      failure mustBe a[IllegalStateException]
    }
  }
}

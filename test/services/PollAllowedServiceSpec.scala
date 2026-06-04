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
import pages.verify.{LastGatewayMessagePage, PollIntervalPage}

import java.time.Instant

class PollAllowedServiceSpec extends SpecBase {

  private val service = new PollAllowedService

  "PollAllowedService.isPollAllowed" - {

    "must return true when currentDateTime is after nextPollDateTime" in {

      val pastTimestamp = Instant.now().minusSeconds(60).toEpochMilli
      val userAnswers   = emptyUserAnswers
        .set(LastGatewayMessagePage, pastTimestamp)
        .success
        .value
        .set(PollIntervalPage, 5)
        .success
        .value

      service.isPollAllowed(userAnswers) mustEqual true
    }

    "must return false when currentDateTime is before nextPollDateTime" in {

      val recentTimestamp = Instant.now().toEpochMilli
      val userAnswers     = emptyUserAnswers
        .set(LastGatewayMessagePage, recentTimestamp)
        .success
        .value
        .set(PollIntervalPage, 300)
        .success
        .value

      service.isPollAllowed(userAnswers) mustEqual false
    }

    "must return false when lastGatewayMessage is missing" in {

      val userAnswers = emptyUserAnswers
        .set(PollIntervalPage, 5)
        .success
        .value

      service.isPollAllowed(userAnswers) mustEqual false
    }

    "must return false when pollInterval is missing" in {

      val userAnswers = emptyUserAnswers
        .set(LastGatewayMessagePage, Instant.now().toEpochMilli)
        .success
        .value

      service.isPollAllowed(userAnswers) mustEqual false
    }

    "must return false when both values are missing" in {

      service.isPollAllowed(emptyUserAnswers) mustEqual false
    }
  }
}

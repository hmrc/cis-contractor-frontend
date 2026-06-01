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

import models.UserAnswers
import pages.verify.{LastGatewayMessagePage, PollIntervalPage}

import java.time.Instant
import javax.inject.Singleton

@Singleton
class PollAllowedService {

  def isPollAllowed(userAnswers: UserAnswers): Boolean = {
    val result = for {
      lastGatewayMillis <- userAnswers.get(LastGatewayMessagePage)
      intervalSeconds   <- userAnswers.get(PollIntervalPage)
    } yield {
      val nextPollDateTime = Instant.ofEpochMilli(lastGatewayMillis).plusSeconds(intervalSeconds.toLong)
      Instant.now().isAfter(nextPollDateTime)
    }
    result.getOrElse(false)
  }
}

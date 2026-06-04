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
import play.api.Logging

import java.time.Instant
import javax.inject.Singleton

@Singleton
class PollAllowedService extends Logging {

  def isPollAllowed(userAnswers: UserAnswers): Boolean = {
    val lastGatewayMillis = userAnswers.get(LastGatewayMessagePage)
    val intervalSeconds   = userAnswers.get(PollIntervalPage)
    (lastGatewayMillis, intervalSeconds) match {
      case (Some(lastGatewayMillis), Some(intervalSeconds)) =>
        val now              = Instant.now()
        val lastGateway      = Instant.ofEpochMilli(lastGatewayMillis)
        val nextPollDateTime = lastGateway.plusSeconds(intervalSeconds.toLong)
        val allowed          = now.isAfter(nextPollDateTime)
        logger.debug(
          s"[PollAllowedService] lastGateway=$lastGateway, intervalSeconds=$intervalSeconds, nextPollAt=$nextPollDateTime, now=$now, pollAllowed=$allowed"
        )
        allowed
      case _                                                =>
        logger.warn(
          s"[PollAllowedService] Missing session data — lastGatewayMillis=$lastGatewayMillis, pollInterval=$intervalSeconds — defaulting to false"
        )
        false
    }
  }
}

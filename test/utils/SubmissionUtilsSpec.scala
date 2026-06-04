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

package utils

import com.typesafe.config.ConfigFactory
import config.FrontendAppConfig
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration

import java.time.LocalDateTime

class SubmissionUtilsSpec extends AnyWordSpec with Matchers {

  private val appConfig       = new FrontendAppConfig(Configuration(ConfigFactory.load()))
  private val submissionUtils = new SubmissionUtils(appConfig)

  "calculateTimeoutDateTime" should {

    "add the configured submissionPollTimeoutSeconds to the submittedAt time" in {
      val submittedAt     = LocalDateTime.parse("2026-01-01T00:00:00")
      val timeoutDateTime = submissionUtils.calculateTimeoutDateTime(submittedAt)

      timeoutDateTime shouldBe submittedAt.plusSeconds(appConfig.submissionPollTimeoutSeconds)
    }
  }

}

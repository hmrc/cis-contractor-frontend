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

package models.verify

import base.SpecBase
import models.verify.SubmissionStatus.*
import play.api.libs.json.{JsString, Json}

class SubmissionStatusSpec extends SpecBase {

  "SubmissionStatus" - {

    "must convert known string to status" in {
      SubmissionStatus.fromString("SUBMITTED") mustBe SUBMITTED
    }

    "must convert unknown string to Unknown" in {
      SubmissionStatus.fromString("NEW_STATUS") mustBe Unknown("NEW_STATUS")
    }

    "must read from JSON string" in {
      JsString("ACCEPTED").as[SubmissionStatus] mustBe ACCEPTED
    }

    "must write to JSON string" in {
      Json.toJson[SubmissionStatus](TIMED_OUT) mustBe JsString("TIMED_OUT")
    }
  }
}

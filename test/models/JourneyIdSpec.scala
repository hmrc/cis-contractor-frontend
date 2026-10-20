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

package models

import base.SpecBase
import models.requests.CisPath.{CisId, CisOrg}

import java.util.UUID

class JourneyIdSpec extends SpecBase {

  "JourneyId" - {
    "stores answers under the login, the client and the UUID" in {
      val uuid = UUID.fromString("11111111-1111-1111-1111-111111111111")

      JourneyId("user-1", CisOrg, uuid).asString mustBe s"user-1/org/$uuid"
      JourneyId("user-1", CisId("AB12"), uuid).asString mustBe s"user-1/AB12/$uuid"
    }
  }
}

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

class JourneyScopeSpec extends SpecBase {

  private val flowId =
    UUID.fromString("12345678-1234-1234-1234-123456789abc")

  "JourneyScope.subJourneyId" - {

    "create an organisation-scoped journey ID" in {
      JourneyScope.subJourneyId(CisOrg, flowId) mustBe "org_12345678-1234-1234-1234-123456789abc"
    }

    "create an agent client-scoped journey ID" in {
      JourneyScope.subJourneyId(CisId("123456789"), flowId) mustBe "123456789_12345678-1234-1234-1234-123456789abc"
    }

    "produce different IDs for different flowIds" in {
      val firstId  = JourneyScope.subJourneyId(CisOrg, UUID.fromString("12345678-1234-1234-1234-123456789abc"))
      val secondId = JourneyScope.subJourneyId(CisOrg, UUID.fromString("87654321-4321-4321-4321-cba987654321"))

      firstId must not be secondId
    }

    "produce different IDs for different CIS paths using the same flowId" in {
      JourneyScope.subJourneyId(CisOrg, flowId) must not be JourneyScope.subJourneyId(CisId("123456789"), flowId)
    }

    "produce the same ID for the same CIS path and flowId" in {
      JourneyScope.subJourneyId(CisOrg, flowId) mustBe JourneyScope.subJourneyId(CisOrg, flowId)
    }
  }
}

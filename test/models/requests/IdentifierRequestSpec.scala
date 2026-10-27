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

package models.requests

import base.SpecBase
import models.EmployerReference
import play.api.test.FakeRequest

class IdentifierRequestSpec extends SpecBase {

  "IdentifierRequest.user" - {

    "names an organisation by login" in {
      val request = IdentifierRequest(
        FakeRequest(),
        "user-1",
        Some(EmployerReference("123", "AB456")),
        None
      )

      request.user mustBe "Organisation <userId: user-1>"
    }

    "names an agent by login" in {
      val request = IdentifierRequest(
        FakeRequest(),
        "agent-1",
        None,
        Some("A123"),
        isAgent = true,
        agentCode = Some("C1")
      )

      request.user mustBe "Agent <userId: agent-1>"
    }
  }
}

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

package models.subcontractor

import models.add.TypeOfSubcontractor.Individualorsoletrader
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class CreateSubcontractorRequestSpec extends AnyWordSpec with Matchers {

  "CreateSubcontractorRequest JSON format" should {

    "round-trip (writes -> reads) with all fields populated" in {
      val model = CreateSubcontractorRequest(
        schemeId = 1,
        subcontractorType = Individualorsoletrader,
        version = 0
      )

      val js = Json.toJson(model)
      js.as[CreateSubcontractorRequest] mustBe model
    }

    "fail to parse when a required field is missing" in {
      val jsonMissing =
        Json.parse(
          """
            |{
            |  "schemeId": 1,
            |  "version": 0
            |}
            """.stripMargin
        )

      jsonMissing.validate[CreateSubcontractorRequest].isError mustBe true
    }
  }
}

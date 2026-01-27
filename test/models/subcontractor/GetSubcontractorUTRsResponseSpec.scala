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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class GetSubcontractorUTRsResponseSpec extends AnyWordSpec with Matchers {

  "GetSubcontractorUTRResponse JSON format" should {

    val subcontractorUTRs: Seq[String] = Seq("1111111111", "2222222222")

    "round-trip (writes -> reads) with all fields populated" in {
      val model = GetSubcontractorUTRsResponse(
        subcontractorUTRs = subcontractorUTRs
      )

      val js = Json.toJson(model)
      js.as[GetSubcontractorUTRsResponse] mustBe model
    }

    "fail to parse when a required field is missing" in {
      val jsonMissing =
        Json.parse(
          """
            |{
            |}
              """.stripMargin
        )

      jsonMissing.validate[GetSubcontractorUTRsResponse].isError mustBe true
    }
  }

}

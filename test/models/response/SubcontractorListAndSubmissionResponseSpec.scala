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

package models.response

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.*

class SubcontractorListAndSubmissionResponseSpec extends AnyFreeSpec with Matchers {

  "SubcontractorListItem" - {
    "round-trip with a subbie resource ref" in {
      val model = SubcontractorListItem(1L, Some(2L))
      Json.toJson(model).validate[SubcontractorListItem] mustBe JsSuccess(model)
    }
    "read a missing subbieResourceRef as None" in {
      Json.obj("subcontractorId" -> 1).as[SubcontractorListItem] mustBe SubcontractorListItem(1L, None)
    }
    "fail when subcontractorId is missing" in {
      Json.obj("subbieResourceRef" -> 2).validate[SubcontractorListItem].isError mustBe true
    }
  }

  "GetSubcontractorListResponse" - {
    "round-trip with items" in {
      val model =
        GetSubcontractorListResponse(Seq(SubcontractorListItem(1L, Some(2L)), SubcontractorListItem(3L, None)))
      Json.toJson(model).validate[GetSubcontractorListResponse] mustBe JsSuccess(model)
    }
    "round-trip empty" in {
      Json.toJson(GetSubcontractorListResponse(Nil)).as[GetSubcontractorListResponse] mustBe
        GetSubcontractorListResponse(Nil)
    }
    "fail when the list is missing" in {
      Json.obj().validate[GetSubcontractorListResponse].isError mustBe true
    }
  }

  "CreateSubmissionForVerificationResponse" - {
    "round-trip" in {
      Json.toJson(CreateSubmissionForVerificationResponse(9L)) mustBe Json.obj("submissionId" -> 9)
      Json.obj("submissionId" -> 9).as[CreateSubmissionForVerificationResponse] mustBe
        CreateSubmissionForVerificationResponse(9L)
    }
    "fail when submissionId is not a number" in {
      Json.obj("submissionId" -> "x").validate[CreateSubmissionForVerificationResponse].isError mustBe true
    }
  }
}

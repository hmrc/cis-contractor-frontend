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

package models.finalvalidation

import base.SpecBase

class ReviewSubcontractorDetailsPageModelSpec extends SpecBase {

  "ReviewSubcontractorDetailsRow" - {

    "must contain the supplied subcontractor details" in {

      val row =
        ReviewSubcontractorDetailsRow(
          subcontractorId = 1L,
          name = "John Smith",
          hasErrors = true
        )

      row.subcontractorId mustBe 1L
      row.name mustBe "John Smith"
      row.hasErrors mustBe true
    }
  }

  "ReviewSubcontractorDetailsPageModel" - {

    "must contain the supplied page model details" in {

      val subcontractors =
        Seq(
          ReviewSubcontractorDetailsRow(
            subcontractorId = 1L,
            name = "John Smith",
            hasErrors = true
          ),
          ReviewSubcontractorDetailsRow(
            subcontractorId = 2L,
            name = "Jane Smith",
            hasErrors = false
          )
        )

      val model =
        ReviewSubcontractorDetailsPageModel(
          subcontractors = subcontractors,
          canContinue = false,
          backUrl = "/back"
        )

      model.subcontractors mustBe subcontractors
      model.canContinue mustBe false
      model.backUrl mustBe "/back"
    }

    "must support an empty subcontractor list" in {

      val model =
        ReviewSubcontractorDetailsPageModel(
          subcontractors = Seq.empty,
          canContinue = true,
          backUrl = "/back"
        )

      model mustBe ReviewSubcontractorDetailsPageModel(
        subcontractors = Seq.empty,
        canContinue = true,
        backUrl = "/back"
      )
    }
  }
}

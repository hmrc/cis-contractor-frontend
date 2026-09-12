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

class UpdateSubcontractorDetailsPageModelSpec extends SpecBase {

  "UpdateSubcontractorDetailsRow" - {

    "must contain the supplied row details" in {

      val row =
        UpdateSubcontractorDetailsRow(
          field = FinalValidationField.Utr,
          labelKey = "finalValidation.utr",
          value = Some("1234567890"),
          changeUrl = "/change-utr"
        )

      row.field mustBe FinalValidationField.Utr
      row.labelKey mustBe "finalValidation.utr"
      row.value mustBe Some("1234567890")
      row.changeUrl mustBe "/change-utr"
    }

    "must support an empty value" in {

      val row =
        UpdateSubcontractorDetailsRow(
          field = FinalValidationField.EmailAddress,
          labelKey = "finalValidation.emailAddress",
          value = None,
          changeUrl = "/change-email"
        )

      row.value mustBe None
    }
  }

  "UpdateSubcontractorDetailsPageModel" - {

    "must contain the supplied page model details" in {

      val rows =
        Seq(
          UpdateSubcontractorDetailsRow(
            field = FinalValidationField.Utr,
            labelKey = "finalValidation.utr",
            value = Some("1234567890"),
            changeUrl = "/change-utr"
          ),
          UpdateSubcontractorDetailsRow(
            field = FinalValidationField.EmailAddress,
            labelKey = "finalValidation.emailAddress",
            value = Some("test@example.com"),
            changeUrl = "/change-email"
          )
        )

      val model =
        UpdateSubcontractorDetailsPageModel(
          subcontractorId = 1L,
          subcontractorName = "John Smith",
          rows = rows
        )

      model.subcontractorId mustBe 1L
      model.subcontractorName mustBe "John Smith"
      model.rows mustBe rows
    }

    "must support an empty row list" in {

      val model =
        UpdateSubcontractorDetailsPageModel(
          subcontractorId = 1L,
          subcontractorName = "John Smith",
          rows = Seq.empty
        )

      model.rows mustBe Seq.empty
    }
  }
}

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
import viewmodels.verify.SubcontractorReverifyRow
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class SubcontractorsToReverifyViewModelSpec extends SpecBase {

  "checkboxItems" - {

    "must create checkbox items with correct id, value and text" in {

      val rows = Seq(
        SubcontractorReverifyRow(
          id = "id-1",
          name = "Brightwell Partners",
          utr = "123",
          verified = "No",
          verificationNumber = "V1",
          taxTreatment = "Standard rate",
          dateAdded = "1 Jan 2024"
        ),
        SubcontractorReverifyRow(
          id = "id-2",
          name = "Carterfields Ltd",
          utr = "456",
          verified = "Yes",
          verificationNumber = "V2",
          taxTreatment = "Gross",
          dateAdded = "2 Jan 2024"
        )
      )

      val result =
        SubcontractorsToReverifyViewModel.checkboxItems(
          rows = rows,
          selected = Set.empty
        )

      result.size mustBe 2

      result.head.id mustBe Some("value-0_0")
      result.head.value mustBe "id-1"
      result.head.content mustBe Text("Brightwell Partners")

      result(1).id mustBe Some("value-1_1")
      result(1).value mustBe "id-2"
      result(1).content mustBe Text("Carterfields Ltd")
    }
  }

  "extractSelected" - {

    "must extract selected values from form data" in {

      val formData = Map(
        "value" -> "id-1,id-2"
      )

      val result =
        SubcontractorsToReverifyViewModel.extractSelected(formData)

      result mustBe Set("id-1", "id-2")
    }

    "must trim whitespace when extracting values" in {

      val formData = Map(
        "value" -> " id-1 ,  id-2 "
      )

      val result =
        SubcontractorsToReverifyViewModel.extractSelected(formData)

      result mustBe Set("id-1", "id-2")
    }

    "must return empty set when value key is missing" in {

      val result =
        SubcontractorsToReverifyViewModel.extractSelected(Map.empty)

      result mustBe empty
    }

    "must ignore empty values" in {

      val formData = Map(
        "value" -> "id-1,,"
      )

      val result =
        SubcontractorsToReverifyViewModel.extractSelected(formData)

      result mustBe Set("id-1")
    }
  }
}

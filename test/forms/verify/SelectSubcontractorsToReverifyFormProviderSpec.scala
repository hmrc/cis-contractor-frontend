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

package forms.verify

import forms.behaviours.CheckboxFieldBehaviours
import play.api.data.FormError

class SelectSubcontractorsToReverifyFormProviderSpec extends CheckboxFieldBehaviours {

  private val requiredKey = "verify.selectSubcontractorsToReverify.error.required"
  private val fieldName   = "value"

  "when selection IS required" - {

    val form = new SelectSubcontractorsToReverifyFormProvider()(requireSelection = true)

    "fail when nothing selected" in {
      val result = form.bind(Map.empty[String, String])

      result.errors must contain(
        FormError("value", requiredKey)
      )
    }

    "bind multiple selected values correctly" in {
      val data = Map(
        "value[0]" -> "Grantalan",
        "value[1]" -> "Hammondhouse"
      )

      val result = form.bind(data)

      result.value.value mustBe Set("Grantalan", "Hammondhouse")
    }
  }

  "when selection is NOT required" - {

    val form = new SelectSubcontractorsToReverifyFormProvider()(requireSelection = false)

    "allow empty submission" in {
      val result = form.bind(Map.empty[String, String])

      result.errors mustBe empty
      result.value mustBe Some(Set.empty)
    }

    "bind multiple selected values correctly" in {
      val data = Map(
        "value[0]" -> "Grantalan",
        "value[1]" -> "Hammondhouse"
      )

      val result = form.bind(data)

      result.value.value mustBe Set("Grantalan", "Hammondhouse")
    }
  }
}

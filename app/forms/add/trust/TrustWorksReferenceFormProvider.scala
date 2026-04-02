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

package forms.add.trust

import forms.mappings.Mappings
import play.api.data.Form
import forms.mappings.Constants.MaxLength20
import forms.Validation

import javax.inject.Inject

class TrustWorksReferenceFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("trustWorksReference.error.required")
        .transform(
          _.trim.replaceAll("""[\t\r\n]+""", ""),
          identity
        )
        .verifying(
          firstError(
            maxLength(MaxLength20, "trustWorksReference.error.length"),
            regexp(Validation.worksRefRegex, "trustWorksReference.error.invalid")
          )
        )
    )
}

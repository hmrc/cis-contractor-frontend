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

package forms.contractordetails

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class SchemeNameFormProvider @Inject() extends Mappings {

  private val maxLengthSchemeName = 56
  private val regexSchemeName =
    "^[A-Za-z0-9\"\\~\\!\\@\\#\\$\\%\\*\\+\\:\\;\\=\\?\\s,\\.\\[\\]\\_\\{\\}\\(\\)/\\&\\'\\-\\^\\\\£€]+$"

  def apply(): Form[String] =
    Form(
      "value" -> text("contractordetails.schemeName.error.required")
        .verifying(
          maxLength(maxLengthSchemeName, "contractordetails.schemeName.error.length"),
          regexp(regexSchemeName, "contractordetails.schemeName.error.invalidCharacters")
        )
    )
}

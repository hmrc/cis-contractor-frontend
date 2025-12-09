/*
 * Copyright 2025 HM Revenue & Customs
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

package forms

import forms.mappings.Mappings
import models.add.SubcontractorName
import play.api.data.Form
import play.api.data.Forms.*

import javax.inject.Inject

class SubcontractorNameFormProvider @Inject() extends Mappings {

  private val nameRegexLettersOnly = """^[A-Za-z'\-]+$"""
  private val lastNameRegex        = """^[A-Za-z0-9\s,\.\(\)/&'\-]+$"""
  private val firstCharLetterRegex = """^[a-zA-Z]{1}.*"""

  def apply(): Form[SubcontractorName] = Form(
    mapping(
      "firstName"  -> text("subcontractorName.firstName.error.required")
        .transform[String](_.trim, identity)
        .verifying(
          firstError(
            regexp(nameRegexLettersOnly, "subcontractorName.firstName.error.invalidCharacters"),
            regexp(firstCharLetterRegex, "subcontractorName.firstName.error.firstChar"),
            maxLength(35, "subcontractorName.firstName.error.length")
          )
        ),
      "middleName" -> optional(
        text()
          .transform[String](_.trim, identity)
          .verifying(
            firstError(
              regexp(nameRegexLettersOnly, "subcontractorName.middleName.error.invalidCharacters"),
              regexp(firstCharLetterRegex, "subcontractorName.middleName.error.firstChar"),
              maxLength(35, "subcontractorName.middleName.error.length")
            )
          )
      ),
      "lastName"   -> text("subcontractorName.lastName.error.required")
        .transform[String](_.trim, identity)
        .verifying(
          firstError(
            regexp(lastNameRegex, "subcontractorName.lastName.error.invalidCharacters"),
            regexp(firstCharLetterRegex, "subcontractorName.lastName.error.firstChar"),
            maxLength(35, "subcontractorName.lastName.error.length")
          )
        )
    )((firstName, middleName, lastName) => SubcontractorName(firstName, middleName, lastName))(name =>
      Some((name.firstName, name.middleName, name.lastName))
    )
  )
}

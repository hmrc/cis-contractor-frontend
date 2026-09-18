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

package pages.validation

import models.validation.{FieldValidationFailure, SubcontractorValidationFailure, SubcontractorValidationField}
import org.scalacheck.{Arbitrary, Gen}

import base.SpecBase
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class SubcontractorValidationFailuresPageSpec extends SpecBase with PageBehaviours {

  private implicit val arbitraryValidationField: Arbitrary[SubcontractorValidationField] =
    Arbitrary(
      Gen.oneOf(
        SubcontractorValidationField.values
      )
    )

  private implicit val arbitraryFieldValidationFailure: Arbitrary[FieldValidationFailure] =
    Arbitrary {
      for {
        field <-
          arbitrary[SubcontractorValidationField]
        value <-
          Gen.option(Gen.alphaNumStr)
      } yield FieldValidationFailure(
        field = field,
        value = value
      )
    }

  private implicit val arbitrarySubcontractorValidationFailure: Arbitrary[SubcontractorValidationFailure] =
    Arbitrary {
      for {
        subcontractorId <-
          Gen.posNum[Long]
        failedFields    <-
          Gen.listOf(
            arbitrary[FieldValidationFailure]
          )
      } yield SubcontractorValidationFailure(
        subcontractorId = subcontractorId,
        failedFields = failedFields
      )
    }

  "SubcontractorValidationFailuresPage" - {

    beRetrievable[
      List[SubcontractorValidationFailure]
    ](
      SubcontractorValidationFailuresPage
    )

    beSettable[
      List[SubcontractorValidationFailure]
    ](
      SubcontractorValidationFailuresPage
    )

    beRemovable[
      List[SubcontractorValidationFailure]
    ](
      SubcontractorValidationFailuresPage
    )
  }
}

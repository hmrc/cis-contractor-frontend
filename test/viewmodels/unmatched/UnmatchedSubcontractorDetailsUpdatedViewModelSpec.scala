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

package viewmodels.unmatched

import base.SpecBase
import models.unmatched.UnmatchedSubcontractorDetailsUpdated
import models.unmatched.UnmatchedSubcontractorDetailsUpdatedReturnTo
import models.unmatched.UnmatchedSubcontractorName
import models.unmatched.UnmatchedSubcontractorUpdate
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class UnmatchedSubcontractorDetailsUpdatedViewModelSpec extends SpecBase {

  private implicit val msgs: Messages =
    messages(app)

  private def confirmationData(
    updates: Seq[UnmatchedSubcontractorUpdate]
  ): UnmatchedSubcontractorDetailsUpdated =
    UnmatchedSubcontractorDetailsUpdated(
      subcontractorName = UnmatchedSubcontractorName(
        firstName = Some("Martin"),
        lastName = Some("Brody")
      ),
      updates = updates,
      returnTo = UnmatchedSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
    )

  "UnmatchedSubcontractorDetailsUpdatedViewModel" - {

    "must create table rows for updated details" in {

      val model =
        confirmationData(
          Seq(
            UnmatchedSubcontractorUpdate(
              detail = "Methods of contact",
              previous = Some("None selected"),
              updated = Some("Email address")
            ),
            UnmatchedSubcontractorUpdate(
              detail = "Email address",
              previous = Some("None provided"),
              updated = Some("martin.brody@virginmedia.com")
            )
          )
        )

      val result =
        UnmatchedSubcontractorDetailsUpdatedViewModel.rows(model)

      result.size mustEqual 2

      result.head.head.content mustEqual Text("Methods of contact")
      result.head.head.classes mustEqual "govuk-!-font-weight-bold"
      result.head(1).content mustEqual Text("None selected")
      result.head(2).content mustEqual Text("Email address")

      result(1).head.content mustEqual Text("Email address")
      result(1).head.classes mustEqual "govuk-!-font-weight-bold"
      result(1)(1).content mustEqual Text("None provided")
      result(1)(2).content mustEqual Text("martin.brody@virginmedia.com")
    }

    "must not create table rows when previous and updated values are the same" in {

      val model =
        confirmationData(
          Seq(
            UnmatchedSubcontractorUpdate(
              detail = "Email address",
              previous = Some("test@example.com"),
              updated = Some("test@example.com")
            )
          )
        )

      val result =
        UnmatchedSubcontractorDetailsUpdatedViewModel.rows(model)

      result mustBe empty
    }

    "must not create table rows when previous and updated values are both missing" in {

      val model =
        confirmationData(
          Seq(
            UnmatchedSubcontractorUpdate(
              detail = "UTR",
              previous = None,
              updated = None
            )
          )
        )

      val result =
        UnmatchedSubcontractorDetailsUpdatedViewModel.rows(model)

      result mustBe empty
    }

    "must not create table rows when previous and updated values only differ by surrounding spaces" in {

      val model =
        confirmationData(
          Seq(
            UnmatchedSubcontractorUpdate(
              detail = "Email address",
              previous = Some(" test@example.com "),
              updated = Some("test@example.com")
            )
          )
        )

      val result =
        UnmatchedSubcontractorDetailsUpdatedViewModel.rows(model)

      result mustBe empty
    }

    "must display None provided when previous value is empty" in {

      val model =
        confirmationData(
          Seq(
            UnmatchedSubcontractorUpdate(
              detail = "Email address",
              previous = Some(""),
              updated = Some("martin.brody@virginmedia.com")
            )
          )
        )

      val result =
        UnmatchedSubcontractorDetailsUpdatedViewModel.rows(model)

      result.size mustEqual 1
      result.head.head.content mustEqual Text("Email address")
      result.head(1).content mustEqual Text("None provided")
      result.head(2).content mustEqual Text("martin.brody@virginmedia.com")
    }

    "must display None provided when previous value contains only spaces" in {

      val model =
        confirmationData(
          Seq(
            UnmatchedSubcontractorUpdate(
              detail = "Email address",
              previous = Some("   "),
              updated = Some("martin.brody@virginmedia.com")
            )
          )
        )

      val result =
        UnmatchedSubcontractorDetailsUpdatedViewModel.rows(model)

      result.size mustEqual 1
      result.head.head.content mustEqual Text("Email address")
      result.head(1).content mustEqual Text("None provided")
      result.head(2).content mustEqual Text("martin.brody@virginmedia.com")
    }

    "must display None provided when updated value is missing" in {

      val model =
        confirmationData(
          Seq(
            UnmatchedSubcontractorUpdate(
              detail = "Email address",
              previous = Some("martin.brody@virginmedia.com"),
              updated = None
            )
          )
        )

      val result =
        UnmatchedSubcontractorDetailsUpdatedViewModel.rows(model)

      result.size mustEqual 1
      result.head.head.content mustEqual Text("Email address")
      result.head(1).content mustEqual Text("martin.brody@virginmedia.com")
      result.head(2).content mustEqual Text("None provided")
    }
  }
}

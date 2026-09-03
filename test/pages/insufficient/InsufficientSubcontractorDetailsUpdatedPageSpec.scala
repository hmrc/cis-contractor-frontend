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

package pages.insufficient

import models.insufficient.InsufficientSubcontractorDetailsUpdated
import models.insufficient.InsufficientSubcontractorDetailsUpdatedReturnTo
import models.insufficient.InsufficientSubcontractorName
import models.insufficient.InsufficientSubcontractorUpdate
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import pages.behaviours.PageBehaviours

class InsufficientSubcontractorDetailsUpdatedPageSpec extends PageBehaviours {

  private val confirmationData =
    InsufficientSubcontractorDetailsUpdated(
      subcontractorName = InsufficientSubcontractorName(
        firstName = Some("Martin"),
        lastName = Some("Brody")
      ),
      updates = Seq(
        InsufficientSubcontractorUpdate(
          detail = "Add UTR?",
          previous = Some("No"),
          updated = Some("Yes")
        ),
        InsufficientSubcontractorUpdate(
          detail = "UTR",
          previous = None,
          updated = Some("3992651526")
        )
      ),
      returnTo = InsufficientSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
    )

  implicit private val arbitraryInsufficientSubcontractorDetailsUpdated
    : Arbitrary[InsufficientSubcontractorDetailsUpdated] =
    Arbitrary(
      Gen.const(confirmationData)
    )

  "InsufficientSubcontractorDetailsUpdatedPage" - {

    beRetrievable[InsufficientSubcontractorDetailsUpdated](
      InsufficientSubcontractorDetailsUpdatedPage
    )

    beSettable[InsufficientSubcontractorDetailsUpdated](
      InsufficientSubcontractorDetailsUpdatedPage
    )

    beRemovable[InsufficientSubcontractorDetailsUpdated](
      InsufficientSubcontractorDetailsUpdatedPage
    )
  }
}

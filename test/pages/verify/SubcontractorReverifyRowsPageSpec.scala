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

package pages.verify

import org.scalacheck.{Arbitrary, Gen}
import pages.behaviours.PageBehaviours
import viewmodels.verify.SubcontractorReverifyRow

class SubcontractorReverifyRowsPageSpec extends PageBehaviours {

  private given Arbitrary[SubcontractorReverifyRow] =
    Arbitrary {
      for {
        id                 <- Gen.alphaNumStr.suchThat(_.nonEmpty)
        name               <- Gen.alphaStr.suchThat(_.nonEmpty)
        utr                <- Gen.alphaNumStr.suchThat(_.nonEmpty)
        verified           <- Gen.oneOf("Yes", "No")
        verificationNumber <- Gen.alphaNumStr.suchThat(_.nonEmpty)
        taxTreatment       <- Gen.oneOf("Standard rate", "Higher rate", "Gross", "Unknown")
        dateAdded          <- Gen.alphaNumStr.suchThat(_.nonEmpty)
      } yield SubcontractorReverifyRow(id, name, utr, verified, verificationNumber, taxTreatment, dateAdded)
    }

  "SubcontractorReverifyRowsPage" - {

    beRetrievable[Seq[SubcontractorReverifyRow]](SubcontractorReverifyRowsPage)

    beSettable[Seq[SubcontractorReverifyRow]](SubcontractorReverifyRowsPage)

    beRemovable[Seq[SubcontractorReverifyRow]](SubcontractorReverifyRowsPage)
  }
}

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

package pages.add

import models.add.SubcontractorName
import models.add.SubcontractorName.format
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.matchers.must.Matchers
import pages.SubcontractorNamePage
import pages.behaviours.PageBehaviours

class SubcontractorNamePageSpec extends PageBehaviours with Matchers {

  implicit val arbitrarySubcontractorName: Arbitrary[SubcontractorName] = Arbitrary {
    for {
      firstName  <- Gen.alphaStr.suchThat(_.nonEmpty).map(_.take(35))
      middleName <- Gen.option(Gen.alphaStr.map(_.take(35)))
      lastName   <- Gen.alphaStr.suchThat(_.nonEmpty).map(_.take(35))
    } yield SubcontractorName(firstName, middleName, lastName)
  }

  "SubcontractorNamePage" - {

    beRetrievable[SubcontractorName](SubcontractorNamePage)

    beSettable[SubcontractorName](SubcontractorNamePage)

    beRemovable[SubcontractorName](SubcontractorNamePage)
  }
}

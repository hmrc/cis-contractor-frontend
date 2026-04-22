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

import base.SpecBase
import models.verify.UnverifiedSubcontractor
import org.scalacheck.{Arbitrary, Gen}
import pages.behaviours.PageBehaviours

class UnverifiedSubcontractorsPageSpec extends SpecBase with PageBehaviours {

  implicit val arbitraryUnverifiedSubcontractor: Arbitrary[UnverifiedSubcontractor] =
    Arbitrary {
      for {
        id         <- Gen.posNum[Long]
        firstName  <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
        secondName <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
        surname    <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
      } yield UnverifiedSubcontractor(
        subcontractorId = id,
        firstName = firstName,
        secondName = secondName,
        surname = surname
      )
    }

  "UnverifiedSubcontractorsPage" - {

    beRetrievable[Seq[UnverifiedSubcontractor]](UnverifiedSubcontractorsPage)

    beSettable[Seq[UnverifiedSubcontractor]](UnverifiedSubcontractorsPage)

    beRemovable[Seq[UnverifiedSubcontractor]](UnverifiedSubcontractorsPage)
  }
}

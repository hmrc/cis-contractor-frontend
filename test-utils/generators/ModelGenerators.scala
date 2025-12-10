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

package generators

import models.*
import models.add.TypeOfSubcontractor
import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.Json

import java.time.Instant

trait ModelGenerators {

  implicit lazy val arbitrarySubcontractorTypes: Arbitrary[TypeOfSubcontractor] =
    Arbitrary {
      Gen.oneOf(TypeOfSubcontractor.values.toSeq)
    }

  implicit lazy val arbitraryUserAnswers: Arbitrary[UserAnswers] =
    Arbitrary {
      for {
        id <- Gen.uuid.map(_.toString)
      } yield UserAnswers(
        id = id,
        data = Json.obj(),
        lastUpdated = Instant.now()
      )
    }
}

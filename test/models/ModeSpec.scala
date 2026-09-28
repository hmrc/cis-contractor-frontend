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

package models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ModeSpec extends AnyWordSpec with Matchers {

  "Mode PathBindable" should {

    val modes = Seq(
      NormalMode          -> "NormalMode",
      CheckMode           -> "CheckMode",
      AmendMode           -> "AmendMode",
      FinalValidationMode -> "FinalValidationMode"
    )

    modes.foreach { case (mode, value) =>
      s"bind $value" in {
        Mode.pathBindable.bind("mode", value) mustBe Right(mode)
      }

      s"unbind $value" in {
        Mode.pathBindable.unbind("mode", mode) mustBe value
      }
    }

    "return an error for an invalid mode" in {
      Mode.pathBindable.bind("mode", "InvalidMode") mustBe
        Left("Invalid mode: InvalidMode")
    }
  }

  "Mode JavascriptLiteral" should {

    val modes = Seq(
      NormalMode          -> "NormalMode",
      CheckMode           -> "CheckMode",
      AmendMode           -> "AmendMode",
      FinalValidationMode -> "FinalValidationMode"
    )

    modes.foreach { case (mode, value) =>
      s"convert $value" in {
        Mode.jsLiteral.to(mode) mustBe value
      }
    }
  }
}

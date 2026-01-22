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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.QuestionPage
import play.api.libs.json.JsPath

class ValidationErrorSpec extends AnyFreeSpec with Matchers {

  case object TestPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ toString

    override def toString: String = "testPage"
  }

  "ValidationErrorSpec" - {
    ".MissingAnswer" - {
      "should return correct message" in {
        val error = MissingAnswer(TestPage)
        error.message mustBe "Missing answer for page: testPage"
      }
    }

    ".InvalidAnswer" - {
      "should return correct message" in {
        val error = InvalidAnswer(TestPage)
        error.message mustBe "Invalid answer for page: testPage"
      }
    }
  }
}

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

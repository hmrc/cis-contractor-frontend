package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class SubUseTradingNameSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "SubUseTradingName" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(SubUseTradingName.values.toSeq)

      forAll(gen) {
        subUseTradingName =>

          JsString(subUseTradingName.toString).validate[SubUseTradingName].asOpt.value mustEqual subUseTradingName
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!SubUseTradingName.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[SubUseTradingName] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(SubUseTradingName.values.toSeq)

      forAll(gen) {
        subUseTradingName =>

          Json.toJson(subUseTradingName) mustEqual JsString(subUseTradingName.toString)
      }
    }
  }
}

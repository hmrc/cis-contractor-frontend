package models

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class IndividualContactMethodOptionsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with ModelGenerators {

  "IndividualContactMethodOptions" - {

    "must deserialise valid values" in {

      val gen = arbitrary[IndividualContactMethodOptions]

      forAll(gen) {
        individualContactMethodOptions =>

          JsString(individualContactMethodOptions.toString).validate[IndividualContactMethodOptions].asOpt.value mustEqual individualContactMethodOptions
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!IndividualContactMethodOptions.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[IndividualContactMethodOptions] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = arbitrary[IndividualContactMethodOptions]

      forAll(gen) {
        individualContactMethodOptions =>

          Json.toJson(individualContactMethodOptions) mustEqual JsString(individualContactMethodOptions.toString)
      }
    }
  }
}

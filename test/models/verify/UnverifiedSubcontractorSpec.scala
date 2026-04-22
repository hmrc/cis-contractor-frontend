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

package models.verify

import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json._

class UnverifiedSubcontractorSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "UnverifiedSubcontractor" - {

    "must deserialise from valid JSON" in {

      val gen =
        for {
          id         <- Gen.posNum[Long]
          firstName  <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
          secondName <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
          surname    <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
        } yield (id, firstName, secondName, surname)

      forAll(gen) { case (id, firstName, secondName, surname) =>
        val json = Json.obj(
          "subcontractorId" -> id,
          "firstName"       -> firstName,
          "secondName"      -> secondName,
          "surname"         -> surname
        )

        json.validate[UnverifiedSubcontractor] match {
          case JsSuccess(value, _) =>
            value mustEqual UnverifiedSubcontractor(id, firstName, secondName, surname)
          case JsError(errors)     =>
            fail(s"Unexpected JsError: $errors")
        }
      }
    }

    "must fail to deserialise when subcontractorId is missing" in {

      val json = Json.obj(
        "firstName"  -> "Bob",
        "secondName" -> "A",
        "surname"    -> "Smith"
      )

      json.validate[UnverifiedSubcontractor] mustBe a[JsError]
    }

    "must fail to deserialise when subcontractorId is not a number" in {

      val json = Json.obj(
        "subcontractorId" -> "not-a-number",
        "firstName"       -> "Bob",
        "secondName"      -> "A",
        "surname"         -> "Smith"
      )

      json.validate[UnverifiedSubcontractor] mustBe a[JsError]
    }

    "must serialise and deserialise via JSON (round trip)" in {

      val gen =
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

      forAll(gen) { model =>
        Json.toJson(model).as[UnverifiedSubcontractor] mustEqual model
      }
    }
  }
}

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

package models.insufficient

import base.SpecBase
import play.api.libs.json.{JsNull, Json}

class InsufficientSubcontractorDetailsUpdatedSpec extends SpecBase {

  "InsufficientSubcontractorName" - {

    "must display first name and last name when both are present" in {

      val model =
        InsufficientSubcontractorName(
          firstName = Some("Martin"),
          lastName = Some("Brody")
        )

      model.displayName mustEqual Some("Martin Brody")
    }

    "must display first name when last name is missing" in {

      val model =
        InsufficientSubcontractorName(
          firstName = Some("Martin"),
          lastName = None
        )

      model.displayName mustEqual Some("Martin")
    }

    "must display last name when first name is missing" in {

      val model =
        InsufficientSubcontractorName(
          firstName = None,
          lastName = Some("Brody")
        )

      model.displayName mustEqual Some("Brody")
    }

    "must return None when first name and last name are missing" in {

      val model =
        InsufficientSubcontractorName(
          firstName = None,
          lastName = None
        )

      model.displayName mustEqual None
    }

    "must ignore blank first and last names" in {

      val model =
        InsufficientSubcontractorName(
          firstName = Some(" "),
          lastName = Some("  ")
        )

      model.displayName mustEqual None
    }

    "must trim first and last names" in {

      val model =
        InsufficientSubcontractorName(
          firstName = Some(" Martin "),
          lastName = Some(" Brody ")
        )

      model.displayName mustEqual Some("Martin Brody")
    }

    "must serialise and deserialise" in {

      val model =
        InsufficientSubcontractorName(
          firstName = Some("Martin"),
          lastName = Some("Brody")
        )

      Json.toJson(model).as[InsufficientSubcontractorName] mustEqual model
    }
  }

  "InsufficientSubcontractorDetailsUpdatedReturnTo" - {

    "must serialise and deserialise ReviewUnmatchedSubcontractors" in {

      val returnTo: InsufficientSubcontractorDetailsUpdatedReturnTo =
        InsufficientSubcontractorDetailsUpdatedReturnTo.ReviewUnmatchedSubcontractors

      Json
        .toJson[InsufficientSubcontractorDetailsUpdatedReturnTo](returnTo)
        .as[InsufficientSubcontractorDetailsUpdatedReturnTo] mustEqual returnTo
    }

    "must serialise and deserialise YourSubcontractors" in {

      val returnTo: InsufficientSubcontractorDetailsUpdatedReturnTo =
        InsufficientSubcontractorDetailsUpdatedReturnTo.YourSubcontractors

      Json
        .toJson[InsufficientSubcontractorDetailsUpdatedReturnTo](returnTo)
        .as[InsufficientSubcontractorDetailsUpdatedReturnTo] mustEqual returnTo
    }

    "must serialise and deserialise CannotVerifyAllSubcontractors" in {

      val returnTo: InsufficientSubcontractorDetailsUpdatedReturnTo =
        InsufficientSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors

      Json
        .toJson[InsufficientSubcontractorDetailsUpdatedReturnTo](returnTo)
        .as[InsufficientSubcontractorDetailsUpdatedReturnTo] mustEqual returnTo
    }

    "must reject an unknown returnTo value" in {

      Json
        .toJson("unknownReturnTo")
        .validate[InsufficientSubcontractorDetailsUpdatedReturnTo]
        .isError mustBe true
    }
  }

  "InsufficientSubcontractorUpdate" - {

    "must serialise and deserialise" in {

      val model =
        InsufficientSubcontractorUpdate(
          detail = "UTR",
          previous = None,
          updated = Some("3992651526")
        )

      Json.toJson(model).as[InsufficientSubcontractorUpdate] mustEqual model
    }
  }

  "InsufficientSubcontractorDetailsUpdated" - {

    "must serialise and deserialise with CannotVerifyAllSubcontractors returnTo" in {

      val model =
        InsufficientSubcontractorDetailsUpdated(
          subcontractorName = InsufficientSubcontractorName(
            firstName = Some("Martin"),
            lastName = Some("Brody")
          ),
          updates = Seq(
            InsufficientSubcontractorUpdate(
              detail = "Add UTR?",
              previous = Some("No"),
              updated = Some("Yes")
            ),
            InsufficientSubcontractorUpdate(
              detail = "UTR",
              previous = None,
              updated = Some("3992651526")
            )
          ),
          returnTo = InsufficientSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
        )

      Json.toJson(model).as[InsufficientSubcontractorDetailsUpdated] mustEqual model
    }

    "must serialise and deserialise with YourSubcontractors returnTo" in {

      val model =
        InsufficientSubcontractorDetailsUpdated(
          subcontractorName = InsufficientSubcontractorName(
            firstName = Some("Martin"),
            lastName = Some("Brody")
          ),
          updates = Seq.empty,
          returnTo = InsufficientSubcontractorDetailsUpdatedReturnTo.YourSubcontractors
        )

      Json.toJson(model).as[InsufficientSubcontractorDetailsUpdated] mustEqual model
    }

    "must serialise and deserialise with ReviewUnmatchedSubcontractors returnTo" in {

      val model =
        InsufficientSubcontractorDetailsUpdated(
          subcontractorName = InsufficientSubcontractorName(
            firstName = Some("Martin"),
            lastName = Some("Brody")
          ),
          updates = Seq.empty,
          returnTo = InsufficientSubcontractorDetailsUpdatedReturnTo.ReviewUnmatchedSubcontractors
        )

      Json.toJson(model).as[InsufficientSubcontractorDetailsUpdated] mustEqual model
    }

    "must default returnTo to CannotVerifyAllSubcontractors when returnTo is missing from JSON" in {

      val json =
        Json.obj(
          "subcontractorName" -> Json.obj(
            "firstName" -> "Martin",
            "lastName"  -> "Brody"
          ),
          "updates"           -> Json.arr(
            Json.obj(
              "detail"   -> "UTR",
              "previous" -> JsNull,
              "updated"  -> "3992651526"
            )
          )
        )

      val result =
        json.as[InsufficientSubcontractorDetailsUpdated]

      result.returnTo mustEqual
        InsufficientSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
    }

    "must use default returnTo when creating model without returnTo" in {

      val model =
        InsufficientSubcontractorDetailsUpdated(
          subcontractorName = InsufficientSubcontractorName(
            firstName = Some("Martin"),
            lastName = Some("Brody")
          ),
          updates = Seq.empty
        )

      model.returnTo mustEqual
        InsufficientSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
    }

    "must reject an unknown returnTo value when reading the complete model" in {

      val json =
        Json.obj(
          "subcontractorName" -> Json.obj(
            "firstName" -> "Martin",
            "lastName"  -> "Brody"
          ),
          "updates"           -> Json.arr(),
          "returnTo"          -> "unknownReturnTo"
        )

      json
        .validate[InsufficientSubcontractorDetailsUpdated]
        .isError mustBe true
    }
  }
}

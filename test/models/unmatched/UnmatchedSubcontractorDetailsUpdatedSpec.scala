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

package models.unmatched

import base.SpecBase
import play.api.libs.json.JsNull
import play.api.libs.json.Json

class UnmatchedSubcontractorDetailsUpdatedSpec extends SpecBase {

  "UnmatchedSubcontractorName" - {

    "must display first name and last name when both are present" in {

      val model =
        UnmatchedSubcontractorName(
          firstName = Some("Martin"),
          lastName = Some("Brody")
        )

      model.displayName mustEqual "Martin Brody"
    }

    "must display first name when last name is missing" in {

      val model =
        UnmatchedSubcontractorName(
          firstName = Some("Martin"),
          lastName = None
        )

      model.displayName mustEqual "Martin"
    }

    "must display last name when first name is missing" in {

      val model =
        UnmatchedSubcontractorName(
          firstName = None,
          lastName = Some("Brody")
        )

      model.displayName mustEqual "Brody"
    }

    "must display empty string when first name and last name are missing" in {

      val model =
        UnmatchedSubcontractorName(
          firstName = None,
          lastName = None
        )

      model.displayName mustEqual ""
    }

    "must serialise and deserialise" in {

      val model =
        UnmatchedSubcontractorName(
          firstName = Some("Martin"),
          lastName = Some("Brody")
        )

      Json.toJson(model).as[UnmatchedSubcontractorName] mustEqual model
    }
  }

  "InsufficientSubcontractorUpdate" - {

    "must serialise and deserialise" in {

      val model =
        UnmatchedSubcontractorUpdate(
          detail = "UTR",
          previous = None,
          updated = Some("3992651526")
        )

      Json.toJson(model).as[UnmatchedSubcontractorUpdate] mustEqual model
    }
  }

  "InsufficientSubcontractorDetailsUpdated" - {

    "must serialise and deserialise with CannotVerifyAllSubcontractors returnTo" in {

      val model =
        UnmatchedSubcontractorDetailsUpdated(
          subcontractorName = UnmatchedSubcontractorName(
            firstName = Some("Martin"),
            lastName = Some("Brody")
          ),
          updates = Seq(
            UnmatchedSubcontractorUpdate(
              detail = "Add UTR?",
              previous = Some("No"),
              updated = Some("Yes")
            ),
            UnmatchedSubcontractorUpdate(
              detail = "UTR",
              previous = None,
              updated = Some("3992651526")
            )
          ),
          returnTo = UnmatchedSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
        )

      Json.toJson(model).as[UnmatchedSubcontractorDetailsUpdated] mustEqual model
    }

    "must serialise and deserialise with YourSubcontractors returnTo" in {

      val model =
        UnmatchedSubcontractorDetailsUpdated(
          subcontractorName = UnmatchedSubcontractorName(
            firstName = Some("Martin"),
            lastName = Some("Brody")
          ),
          updates = Seq.empty,
          returnTo = UnmatchedSubcontractorDetailsUpdatedReturnTo.YourSubcontractors
        )

      Json.toJson(model).as[UnmatchedSubcontractorDetailsUpdated] mustEqual model
    }

    "must serialise and deserialise with ReviewUnmatchedSubcontractors returnTo" in {

      val model =
        UnmatchedSubcontractorDetailsUpdated(
          subcontractorName = UnmatchedSubcontractorName(
            firstName = Some("Martin"),
            lastName = Some("Brody")
          ),
          updates = Seq.empty,
          returnTo = UnmatchedSubcontractorDetailsUpdatedReturnTo.ReviewUnmatchedSubcontractors
        )

      Json.toJson(model).as[UnmatchedSubcontractorDetailsUpdated] mustEqual model
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
        json.as[UnmatchedSubcontractorDetailsUpdated]

      result.returnTo mustEqual
        UnmatchedSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
    }

    "must use default returnTo when creating model without returnTo" in {

      val model =
        UnmatchedSubcontractorDetailsUpdated(
          subcontractorName = UnmatchedSubcontractorName(
            firstName = Some("Martin"),
            lastName = Some("Brody")
          ),
          updates = Seq.empty
        )

      model.returnTo mustEqual
        UnmatchedSubcontractorDetailsUpdatedReturnTo.CannotVerifyAllSubcontractors
    }
  }
}


package models.response

import base.SpecBase
import play.api.libs.json.Json

class UpdateSubcontractorResponseSpec extends SpecBase {

  "UpdateSubcontractorResponse" - {

    "must serialise to JSON" in {
      val response =
        UpdateSubcontractorResponse(
          version = 3
        )

      Json.toJson(response) mustBe
        Json.obj(
          "version" -> 3
        )
    }

    "must deserialise from JSON" in {
      val json =
        Json.obj(
          "version" -> 3
        )

      json.as[UpdateSubcontractorResponse] mustBe
        UpdateSubcontractorResponse(
          version = 3
        )
    }

    "must round trip through JSON" in {
      val response =
        UpdateSubcontractorResponse(
          version = 3
        )

      val json =
        Json.toJson(response)

      Json.fromJson[UpdateSubcontractorResponse](json).get mustBe
        response
    }

    "must fail to deserialise when version is missing" in {
      val json =
        Json.obj()

      val result =
        Json.fromJson[UpdateSubcontractorResponse](json)

      result.isError mustBe true
    }

    "must fail to deserialise when version has the wrong type" in {
      val json =
        Json.obj(
          "version" -> "three"
        )

      val result =
        Json.fromJson[UpdateSubcontractorResponse](json)

      result.isError mustBe true
    }
  }
}
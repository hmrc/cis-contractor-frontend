package queries

import base.SpecBase
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsPath, Json}

class AmendCisIdQuerySpec extends AnyFreeSpec with Matchers with SpecBase {

  "AmendCisIdQuery" - {

    "must nest the path under amendSubcontractor" in {
      AmendCisIdQuery("uuid-1").path mustEqual (JsPath \ "amendSubcontractor" \ "uuid-1" \ "cisId")
    }

    "must set and get a CIS ID under the uuid" in {
      val ua = emptyUserAnswers
        .set(AmendCisIdQuery("uuid-1"), "CIS-12345")
        .success
        .value

      ua.data mustBe Json.obj(
        "amendSubcontractor" -> Json.obj(
          "uuid-1" -> Json.obj(
            "cisId" -> "CIS-12345"
          )
        )
      )
      ua.get(AmendCisIdQuery("uuid-1")) mustBe Some("CIS-12345")
      ua.get(CisIdQuery) mustBe None
    }
  }
}

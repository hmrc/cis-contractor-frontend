package models.add

import base.SpecBase
import play.api.libs.json.Json

class SubcontractorNameSpec extends SpecBase {

  "SubcontractorName" - {
    "serialise to JSON correctly" in {
      val subcontractorName = SubcontractorName("Alice", Some("Chloe"), "Smith")
      val json = Json.toJson(subcontractorName)

      (json \ "firstName").as[String] mustBe "Alice"
      (json \ "middleName").as[String] mustBe "Chloe"
      (json \ "lastName").as[String] mustBe "Smith"
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        """
          |{
          |  "firstName": "Alice",
          |  "middleName": "Chloe",
          |  "lastName": "Smith"
          |}
          |""".stripMargin
      )
      val result = json.as[SubcontractorName]
      result.firstName mustBe "Alice"
      result.middleName mustBe Some("Chloe")
      result.lastName mustBe "Smith"
    }

    "round-trip serialize and deserialize correctly" in {
      val subcontractorName = SubcontractorName("Alice", Some("Chloe"), "Smith")
      val json = Json.toJson(subcontractorName)
      val result = json.as[SubcontractorName]
      result mustBe subcontractorName
    }
  }
}

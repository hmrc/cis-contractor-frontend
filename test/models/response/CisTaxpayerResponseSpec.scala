package models.response

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class CisTaxpayerResponseSpec extends AnyWordSpec with Matchers {

  "CisTaxpayerResponse JSON format" should {

    "round-trip (writes -> reads) with all fields populated" in {
      val model = CisTaxpayerResponse(
        uniqueId = "CIS-123",
        taxOfficeNumber = "123",
        taxOfficeRef = "AB456",
        aoDistrict = Some("01"),
        aoPayType = Some("P"),
        aoCheckCode = Some("99"),
        aoReference = Some("123/AB456"),
        validBusinessAddr = Some("Y"),
        correlation = Some("corr-1"),
        ggAgentId = Some("AGENT-1"),
        employerName1 = Some("Test Ltd"),
        employerName2 = Some("Group"),
        agentOwnRef = Some("Ref-1"),
        schemeName = Some("Scheme-X"),
        utr = Some("1234567890"),
        enrolledSig = Some("Y")
      )

      val js = Json.toJson(model)
      js.as[CisTaxpayerResponse] mustBe model
    }

    "parse minimal JSON with only required fields" in {
      val json =
        Json.parse(
          """
            |{
            |  "uniqueId": "CIS-123",
            |  "taxOfficeNumber": "123",
            |  "taxOfficeRef": "AB456"
            |}
          """.stripMargin
        )

      val parsed = json.as[CisTaxpayerResponse]
      parsed.uniqueId mustBe "CIS-123"
      parsed.taxOfficeNumber mustBe "123"
      parsed.taxOfficeRef mustBe "AB456"
      parsed.aoDistrict mustBe None
      parsed.schemeName mustBe None
    }

    "fail to parse when a required field is missing" in {
      val jsonMissing =
        Json.parse(
          """
            |{
            |  "uniqueId": "CIS-123",
            |  "taxOfficeNumber": "123"
            |}
          """.stripMargin
        )

      jsonMissing.validate[CisTaxpayerResponse].isError mustBe true
    }
  }
}

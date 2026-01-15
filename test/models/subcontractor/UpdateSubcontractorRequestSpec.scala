package models.subcontractor

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class UpdateSubcontractorRequestSpec extends AnyWordSpec with Matchers {

  "CisTaxpayerResponse JSON format" should {

    val schemeId          = 10
    val subbieResourceRef = 20

    "round-trip (writes -> reads) with all fields populated" in {
      val model = UpdateSubcontractorRequest(
        schemeId = schemeId,
        subbieResourceRef = subbieResourceRef,
        firstName = Some("firstName"),
        secondName = Some("secondName"),
        surname = Some("surname"),
        tradingName = Some("trading name"),
        addressLine1 = Some("addressLine1"),
        addressLine2 = Some("addressLine2"),
        addressLine3 = Some("addressLine3"),
        addressLine4 = Some("addressLine4"),
        postcode = Some("post code"),
        nino = Some("national insurance number"),
        utr = Some("unique tax ref"),
        worksReferenceNumber = Some("work ref"),
        emailAddress = Some("hello@hmrc.co.uk"),
        phoneNumber = Some("0123456789")
      )

      val js = Json.toJson(model)
      js.as[UpdateSubcontractorRequest] mustBe model
    }

    "parse minimal JSON with only required fields" in {
      val json =
        Json.parse(
          """
            |{
            |  "schemeId": 10,
            |  "subbieResourceRef": 20
            |}
          """.stripMargin
        )

      val parsed = json.as[UpdateSubcontractorRequest]
      parsed.schemeId mustBe schemeId
      parsed.subbieResourceRef mustBe subbieResourceRef
    }

    "fail to parse when a required field is missing" in {
      val jsonMissing =
        Json.parse(
          """
            |{
            |  "schemeId": 10,
            |  "nino": "123"
            |}
          """.stripMargin
        )

      jsonMissing.validate[UpdateSubcontractorRequest].isError mustBe true
    }
  }
}

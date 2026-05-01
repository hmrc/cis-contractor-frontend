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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, post, stubFor, urlEqualTo, urlPathEqualTo}
import itutil.ApplicationWithWiremock
import models.add.TypeOfSubcontractor.Individualorsoletrader
import models.requests.CreateAndUpdateSubcontractorPayload
import models.requests.CreateVerificationBatchAndVerificationsRequest
import models.response.CreateVerificationBatchAndVerificationsResponse
import models.requests.CreateAndUpdateSubcontractorPayload.IndividualOrSoleTraderPayload
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{FORBIDDEN, INTERNAL_SERVER_ERROR, NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, UpstreamErrorResponse}

class ConstructionIndustrySchemeConnectorSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val connector: ConstructionIndustrySchemeConnector =
    app.injector.instanceOf[ConstructionIndustrySchemeConnector]

  "getCisTaxpayer" should {

    "return CisTaxpayer when BE returns 200 with valid JSON" in {
      stubFor(
        get(urlPathEqualTo("/cis/taxpayer"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                """{
                  |  "uniqueId": "123",
                  |  "taxOfficeNumber": "111",
                  |  "taxOfficeRef": "test111",
                  |  "employerName1": "TEST LTD"
                  |}""".stripMargin
              )
          )
      )

      val result = connector.getCisTaxpayer().futureValue
      result.uniqueId mustBe "123"
      result.taxOfficeNumber mustBe "111"
      result.taxOfficeRef mustBe "test111"
      result.employerName1 mustBe Some("TEST LTD")
    }

    "fail when BE returns 200 with invalid JSON" in {
      stubFor(
        get(urlPathEqualTo("/cis/taxpayer"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{ "unexpectedField": true }""")
          )
      )

      val ex = intercept[Exception] {
        connector.getCisTaxpayer().futureValue
      }
      ex.getMessage.toLowerCase must include("uniqueid")
    }

    "propagate an upstream error when BE returns 500" in {
      stubFor(
        get(urlPathEqualTo("/cis/taxpayer"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val ex = intercept[Exception] {
        connector.getCisTaxpayer().futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }

  "createAndUpdateSubcontractor" should {

    val cisId = "200"

    "successfully update subcontractor" in {
      stubFor(
        post(urlPathEqualTo("/cis/subcontractor/create-and-update"))
          .willReturn(aResponse().withStatus(NO_CONTENT))
      )

      val payload: CreateAndUpdateSubcontractorPayload =
        IndividualOrSoleTraderPayload(
          cisId = cisId,
          subcontractorType = Individualorsoletrader
        )

      val result: Unit =
        connector.createAndUpdateSubcontractor(payload).futureValue

      result shouldBe ()
    }

    "propagate upstream error on non-2xx" in {
      stubFor(
        post(urlPathEqualTo("/cis/subcontractor/create-and-update"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val payload: CreateAndUpdateSubcontractorPayload =
        IndividualOrSoleTraderPayload(
          cisId = cisId,
          subcontractorType = Individualorsoletrader
        )

      val ex = intercept[Exception] {
        connector.createAndUpdateSubcontractor(payload).futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }

  "getSubcontractorUTRs" should {

    val cisId = "cis-123"

    "successfully get a subcontractor utr list" in {
      val responseJson =
        """
          |{
          |  "subcontractorUTRs": ["1111111111", "2222222222"]
          |}
          |""".stripMargin

      stubFor(
        get(urlPathEqualTo(s"/cis/subcontractors/utr/$cisId"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(responseJson)
          )
      )

      val result = connector.getSubcontractorUTRs(cisId).futureValue
      result.subcontractorUTRs mustBe Seq("1111111111", "2222222222")
    }

    "propagate upstream error on non-2xx" in {
      stubFor(
        get(urlPathEqualTo(s"/cis/subcontractors/utr/$cisId"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val ex = intercept[Exception] {
        connector.getSubcontractorUTRs(cisId).futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }

  "getAgentClient" should {

    val userId = "some-user-id"
    val validJson: JsValue = Json.obj(
      "uniqueId" -> "1",
      "taxOfficeNumber" -> "123",
      "taxOfficeReference" -> "AB001"
    )

    "returns Some(Json) if OK" in {
      stubFor(
        get(urlEqualTo(s"/cis/user-cache/agent-client/$userId"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(Json.stringify(validJson))
          )
      )

      val result = connector.getAgentClient(userId).futureValue

      result mustBe Some(validJson)
    }

    "returns None if NOT_FOUND" in {
      stubFor(
        get(urlEqualTo(s"/cis/user-cache/agent-client/$userId"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      val result = connector.getAgentClient(userId).futureValue

      result mustBe None
    }

    "throws HttpException for other codes" in {
      stubFor(
        get(urlEqualTo(s"/cis/user-cache/agent-client/$userId"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("Something broke")
          )

      )

      val result = connector
        .getAgentClient(userId)
        .failed
        .futureValue

      result mustBe a[HttpException]
      result.getMessage must include("Something broke")
    }

  }
  
  "hasClient(taxOfficeNumber, taxOfficeReference)" should {

    "GET /cis/agent/has-client/:ton/:tor and return true when BE returns 200" in {
      stubFor(
        get(urlPathEqualTo("/cis/agent/has-client/163/AB0063"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody("""{ "hasClient": true }""")
          )
      )

      connector.hasClient("163", "AB0063").futureValue mustBe true
    }

    "fail the future when BE returns non-200 (e.g. 403)" in {
      stubFor(
        get(urlPathEqualTo("/cis/agent/has-client/163/AB0063"))
          .willReturn(aResponse().withStatus(FORBIDDEN).withBody("""{"error":"nope"}"""))
      )

      val ex = connector.hasClient("163", "AB0063").failed.futureValue
      ex mustBe a[UpstreamErrorResponse]
      ex.asInstanceOf[UpstreamErrorResponse].statusCode mustBe FORBIDDEN
    }
  }

  "getCurrentVerificationBatch" should {

    val instanceId = "cis-123"

    "successfully get a current verification batch" in {
      val responseJson =
        """
          |{
          |  "subcontractors": [
          |    {
          |      "subcontractorId": 1
          |    }
          |  ],
          |  "verificationBatch": [
          |    {
          |      "verificationBatchId": 99
          |    }
          |  ],
          |  "verifications": [
          |    {
          |      "verificationId": 1001
          |    }
          |  ]
          |}
          |""".stripMargin

      stubFor(
        get(urlPathEqualTo(s"/cis/verification-batch/current/$instanceId"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(responseJson)
          )
      )

      val result = connector.getCurrentVerificationBatch(instanceId).futureValue
      result.subcontractors.map(_.subcontractorId) mustBe Seq(1L)
      result.verificationBatch.map(_.verificationBatchId) mustBe Seq(99L)
      result.verifications.map(_.verificationId) mustBe Seq(1001L)
    }

    "propagate upstream error on non-2xx" in {
      stubFor(
        get(urlPathEqualTo(s"/cis/verification-batch/current/$instanceId"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val ex = intercept[Exception] {
        connector.getCurrentVerificationBatch(instanceId).futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }

  "createVerificationBatchAndVerifications" should {

    "successfully create verification batch and verifications when BE returns 200" in {
      val instanceId = "inst-123"

      val requestModel =
        CreateVerificationBatchAndVerificationsRequest(
          instanceId = instanceId,
          verificationResourceReferences = Seq(111L, 222L, 333L),
          actionIndicator = Some("A")
        )

      val responseJson =
        """
          |{
          |  "verificationBatchResourceReference": 666
          |}
          |""".stripMargin

      stubFor(
        post(urlPathEqualTo("/cis/verification-batch/create"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(responseJson)
          )
      )

      val result = connector.createVerificationBatchAndVerifications(requestModel).futureValue

      result mustBe CreateVerificationBatchAndVerificationsResponse(verificationBatchResourceReference = 666)
    }

    "propagate upstream error on non-2xx (e.g. 500)" in {
      val requestModel =
        CreateVerificationBatchAndVerificationsRequest(
          instanceId = "inst-123",
          verificationResourceReferences = Seq(111L),
          actionIndicator = None
        )

      stubFor(
        post(urlPathEqualTo("/cis/verification-batch/create"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val ex = connector.createVerificationBatchAndVerifications(requestModel).failed.futureValue
      ex.getMessage must include("returned 500")
    }
  }

}

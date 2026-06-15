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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, get, post, stubFor, urlEqualTo, urlPathEqualTo}
import itutil.ApplicationWithWiremock
import models.TypeOfSubcontractor.Individualorsoletrader
import models.requests.*
import models.requests.CreateAndUpdateSubcontractorPayload.IndividualOrSoleTraderPayload
import models.response.*
import models.verify.SubmissionStatus
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{FORBIDDEN, INTERNAL_SERVER_ERROR, NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, UpstreamErrorResponse}

import java.time.LocalDateTime

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
          |  "verificationBatch": {
          |      "verificationBatchId": 99
          |    },
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
      result.verificationBatch.map(_.verificationBatchId) mustBe Some(99L)
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

  "getScheme" should {

    "successfully get scheme when BE returns 200" in {
      val responseJson =
        """
          |{
          |  "schemeId": 1,
          |  "instanceId": "1",
          |  "accountsOfficeReference": "AO123",
          |  "taxOfficeNumber": "123",
          |  "taxOfficeReference": "AB456",
          |  "utr": "1234567890",
          |  "name": "Test Contractor",
          |  "emailAddress": "test@test.com"
          |}
          |""".stripMargin

      stubFor(
        get(urlPathEqualTo("/cis/scheme/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(responseJson)
          )
      )

      val result = connector.getScheme("1").futureValue

      result.instanceId mustBe "1"
      result.accountsOfficeReference mustBe "AO123"
      result.utr mustBe Some("1234567890")
    }

    "propagate upstream error on non-2xx" in {
      stubFor(
        get(urlPathEqualTo("/cis/scheme/1"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val ex = connector.getScheme("1").failed.futureValue
      ex.getMessage must include("returned 500")
    }
  }

  "submitVerificationToChris" should {

    "successfully submit verification to ChRIS when BE returns 200" in {
      val requestModel = ChrisVerificationRequest(
        instanceId = "1",
        isAgent = false,
        clientTaxOfficeNumber = "123",
        clientTaxOfficeRef = "AB456",
        contractorUTR = "1234567890",
        contractorAORef = "AO123",
        verificationBatchId = "1001",
        verificationBatchResourceRef = "2001",
        emailRecipient = Some("test@test.com"),
        subcontractors = Seq.empty,
        verifications = Seq(
          VerificationDetails(
            subcontractorName = "Test Subcontractor",
            verificationResourceRef = "4001",
            proceedVerification = true
          )
        )
      )

      val responseJson =
        """
          |{
          |  "submissionId": "13602",
          |  "status": "ACCEPTED",
          |  "hmrcMarkGenerated": "hmrc-mark",
          |  "correlationId": "corr-id",
          |  "responseEndPoint": {
          |    "url": "http://localhost/poll",
          |    "pollIntervalSeconds": 5
          |  },
          |  "gatewayTimestamp": "2026-06-15T03:30:52",
          |  "acceptedTime": "2026-06-15T03:30:53"
          |}
          |""".stripMargin

      stubFor(
        post(urlPathEqualTo("/cis/submissions/13602/submit-verification-to-chris"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(responseJson)
          )
      )

      val result =
        connector.submitVerificationToChris(13602L, requestModel).futureValue

      result.submissionId mustBe "13602"
      result.status mustBe "ACCEPTED"
      result.hmrcMarkGenerated mustBe "hmrc-mark"
      result.responseEndPoint.map(_.pollIntervalSeconds) mustBe Some(5)
    }

    "propagate upstream error on non-2xx" in {
      val requestModel = ChrisVerificationRequest(
        instanceId = "1",
        isAgent = false,
        clientTaxOfficeNumber = "123",
        clientTaxOfficeRef = "AB456",
        contractorUTR = "1234567890",
        contractorAORef = "AO123",
        verificationBatchId = "1001",
        verificationBatchResourceRef = "2001",
        emailRecipient = None,
        subcontractors = Seq.empty,
        verifications = Seq.empty
      )

      stubFor(
        post(urlPathEqualTo("/cis/submissions/13602/submit-verification-to-chris"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val ex =
        connector.submitVerificationToChris(13602L, requestModel).failed.futureValue

      ex.getMessage must include("returned 500")
    }
  }

  "updateVerificationSubmission" should {

    "successfully update verification submission when BE returns 200" in {
      val requestModel = UpdateVerificationSubmissionRequest(
        instanceId = "1",
        verificationBatchId = 1001L,
        verificationBatchResourceRef = 2001L,
        submittableStatus = "SUBMITTED",
        hmrcMarkGenerated = "hmrc-mark",
        hmrcMarkGgis = Some("ggis-mark"),
        emailRecipient = Some("test@test.com"),
        submissionRequestDate = Some(LocalDateTime.parse("2026-06-15T03:30:52")),
        acceptedTime = Some("2026-06-15T03:30:53")
      )

      stubFor(
        post(urlPathEqualTo("/cis/verification/submission/update"))
          .willReturn(aResponse().withStatus(OK).withBody(""))
      )

      val result =
        connector.updateVerificationSubmission("13602", requestModel).futureValue

      result mustBe()
    }

    "propagate upstream error on non-2xx" in {
      val requestModel = UpdateVerificationSubmissionRequest(
        instanceId = "1",
        verificationBatchId = 1001L,
        verificationBatchResourceRef = 2001L,
        submittableStatus = "SUBMITTED",
        hmrcMarkGenerated = "hmrc-mark"
      )

      stubFor(
        post(urlPathEqualTo("/cis/verification/submission/update"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val ex =
        connector.updateVerificationSubmission("13602", requestModel).failed.futureValue

      ex mustBe a[UpstreamErrorResponse]
      ex.getMessage mustBe "boom"
    }
  }

  "getSubmissionStatus" should {

    "successfully get verification submission status when BE returns 200" in {
      val responseJson =
        """
          |{
          |  "status": "SUBMITTED",
          |  "correlationId": "corr-id",
          |  "pollUrl": null,
          |  "pollInterval": null,
          |  "error": null,
          |  "irMarkReceived": "ggis-mark",
          |  "lastMessageDate": "2026-06-15T03:30:54",
          |  "acceptedTime": "2026-06-15T03:30:55",
          |  "govTalkErrorStatus": null
          |}
          |""".stripMargin

      stubFor(
        get(urlPathEqualTo("/cis/submissions/verification/poll"))
          .withQueryParam("submissionId", equalTo("13602"))
          .withQueryParam("pollUrl", equalTo("http://localhost/poll"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(responseJson)
          )
      )

      val result =
        connector.getSubmissionStatus("http://localhost/poll", "13602").futureValue

      result.status mustBe SubmissionStatus.SUBMITTED
      result.correlationId mustBe "corr-id"
      result.irMarkReceived mustBe Some("ggis-mark")
    }

    "propagate upstream error on non-2xx" in {
      stubFor(
        get(urlPathEqualTo("/cis/submissions/verification/poll"))
          .withQueryParam("submissionId", equalTo("13602"))
          .withQueryParam("pollUrl", equalTo("http://localhost/poll"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val ex =
        connector.getSubmissionStatus("http://localhost/poll", "13602").failed.futureValue

      ex.getMessage must include("returned 500")
    }
  }

}

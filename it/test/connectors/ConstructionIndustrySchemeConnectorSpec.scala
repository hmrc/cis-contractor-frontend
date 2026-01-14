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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, post, stubFor, urlPathEqualTo}
import itutil.ApplicationWithWiremock
import models.add.TypeOfSubcontractor
import models.subcontractor.{CreateSubcontractorRequest, UpdateSubcontractorRequest}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{CREATED, INTERNAL_SERVER_ERROR, OK}
import uk.gov.hmrc.http.HeaderCarrier

class ConstructionIndustrySchemeConnectorSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val connector: ConstructionIndustrySchemeConnector = app.injector.instanceOf[ConstructionIndustrySchemeConnector]

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

  "createSubcontractor" should {
    "successfully create a subcontractor" in {

      val responseJson =
        """
          |{
          |  "subbieResourceRef": 10
          |}
                """.stripMargin

      stubFor(
        post(urlPathEqualTo("/cis/subcontractor/create"))
          .willReturn(
            aResponse()
              .withStatus(CREATED)
              .withBody(responseJson)
          )
      )

      val result = connector
        .createSubcontractor(
          CreateSubcontractorRequest(
            schemeId = "10",
            subcontractorType = TypeOfSubcontractor.Trust.toString
          )
        )
        .futureValue

      result.subbieResourceRef mustBe 10
    }

    "propagate upstream error on non-2xx" in {
      stubFor(
        post(urlPathEqualTo("/cis/subcontractor/create"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val ex = intercept[Exception] {
        connector
          .createSubcontractor(
            CreateSubcontractorRequest(
              schemeId = "10",
              subcontractorType = TypeOfSubcontractor.Trust.toString
            )
          )
          .futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }

  "updateSubcontractor" should {

    val subbieResourceRef = 10

    "successfully update subcontractor" in {

      val responseJson =
        """
          |{
          |  "newVersion": 20
          |}
                  """.stripMargin

      stubFor(
        post(urlPathEqualTo("/cis/subcontractor/update"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(responseJson)
          )
      )

      val result = connector
        .updateSubcontractor(
          UpdateSubcontractorRequest(
            schemeId = "10",
            subbieResourceRef = subbieResourceRef,
            tradingName = Some("trader name")
          )
        )
        .futureValue

      result.newVersion mustBe 20
    }

    "propagate upstream error on non-2xx" in {
      stubFor(
        post(urlPathEqualTo("/cis/subcontractor/update"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("boom"))
      )

      val ex = intercept[Exception] {
        connector
          .updateSubcontractor(
            UpdateSubcontractorRequest(
              schemeId = "10",
              subbieResourceRef = subbieResourceRef,
              tradingName = Some("trader name")
            )
          )
          .futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }
}

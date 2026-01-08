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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlPathEqualTo}
import itutil.ApplicationWithWiremock
import models.add.TypeOfSubcontractor
import models.subcontractor.CreateSubcontractorRequest
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{CREATED, INTERNAL_SERVER_ERROR}
import uk.gov.hmrc.http.HeaderCarrier

class ConstructionIndustrySchemeConnectorSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val connector: ConstructionIndustrySchemeConnector = app.injector.instanceOf[ConstructionIndustrySchemeConnector]

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
            schemeId = 10,
            subcontractorType = TypeOfSubcontractor.Trust.toString,
            currentVersion = 0
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
              schemeId = 10,
              subcontractorType = TypeOfSubcontractor.Trust.toString,
              currentVersion = 0
            )
          )
          .futureValue
      }
      ex.getMessage must include("returned 500")
    }
  }
}

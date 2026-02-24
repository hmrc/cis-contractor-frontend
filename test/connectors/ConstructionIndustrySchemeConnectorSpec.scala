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

import models.add.TypeOfSubcontractor
import models.requests.CreateAndUpdateSubcontractorPayload
import models.requests.CreateAndUpdateSubcontractorPayload.{IndividualOrSoleTraderPayload, PartnershipPayload}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConstructionIndustrySchemeConnectorSpec
  extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "ConstructionIndustrySchemeConnector.createAndUpdateSubcontractor" should {

    "return Unit when CIS responds with NO_CONTENT (204) for IndividualOrSoleTraderPayload" in {
      val config = mock[ServicesConfig]
      val http   = mock[HttpClientV2]
      val rb     = mock[RequestBuilder]

      when(config.baseUrl("construction-industry-scheme")).thenReturn("http://cis-host")

      when(http.post(any())(any())).thenReturn(rb)
      when(rb.withBody(any[JsValue]())(any(), any(), any())).thenReturn(rb)
      when(rb.execute[HttpResponse](any(), any())).thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

      val connector = new ConstructionIndustrySchemeConnector(config, http)

      val payload: CreateAndUpdateSubcontractorPayload =
        IndividualOrSoleTraderPayload(
          cisId = "10",
          subcontractorType = TypeOfSubcontractor.Individualorsoletrader,
          firstName = Some("Jane"),
          surname = Some("Doe")
        )

      connector.createAndUpdateSubcontractor(payload).futureValue mustBe (())
      
      val bodyCaptor: ArgumentCaptor[JsValue] = ArgumentCaptor.forClass(classOf[JsValue])
      verify(rb).withBody(bodyCaptor.capture())(any(), any(), any())
      bodyCaptor.getValue mustBe Json.toJson(payload.asInstanceOf[IndividualOrSoleTraderPayload])
    }

    "return Unit when CIS responds with NO_CONTENT (204) for PartnershipPayload" in {
      val config = mock[ServicesConfig]
      val http   = mock[HttpClientV2]
      val rb     = mock[RequestBuilder]

      when(config.baseUrl("construction-industry-scheme")).thenReturn("http://cis-host")

      when(http.post(any())(any())).thenReturn(rb)
      when(rb.withBody(any[JsValue]())(any(), any(), any())).thenReturn(rb)
      when(rb.execute[HttpResponse](any(), any())).thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

      val connector = new ConstructionIndustrySchemeConnector(config, http)

      val payload: CreateAndUpdateSubcontractorPayload =
        PartnershipPayload(
          cisId = "11",
          subcontractorType = TypeOfSubcontractor.Partnership,
          utr = Some("1234567890"),
          tradingName = Some("Nominated Partner")
        )

      connector.createAndUpdateSubcontractor(payload).futureValue mustBe (())

      val bodyCaptor: ArgumentCaptor[JsValue] = ArgumentCaptor.forClass(classOf[JsValue])
      verify(rb).withBody(bodyCaptor.capture())(any(), any(), any())
      bodyCaptor.getValue mustBe Json.toJson(payload.asInstanceOf[PartnershipPayload])
    }

    "fail when CIS responds with a non-204 status" in {
      val config = mock[ServicesConfig]
      val http   = mock[HttpClientV2]
      val rb     = mock[RequestBuilder]

      when(config.baseUrl("construction-industry-scheme")).thenReturn("http://cis-host")

      when(http.post(any())(any())).thenReturn(rb)
      when(rb.withBody(any[JsValue]())(any(), any(), any())).thenReturn(rb)
      when(rb.execute[HttpResponse](any(), any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "boom")))

      val connector = new ConstructionIndustrySchemeConnector(config, http)

      val payload: CreateAndUpdateSubcontractorPayload =
        IndividualOrSoleTraderPayload(
          cisId = "10",
          subcontractorType = TypeOfSubcontractor.Individualorsoletrader
        )

      val ex = connector.createAndUpdateSubcontractor(payload).failed.futureValue
      ex.getMessage mustBe s"Update subcontractor failed, returned $INTERNAL_SERVER_ERROR"
    }
  }
}

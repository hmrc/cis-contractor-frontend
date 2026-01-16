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

import models.response.CisTaxpayerResponse
import models.subcontractor.{CreateSubcontractorRequest, CreateSubcontractorResponse, UpdateSubcontractorRequest}
import play.api.Logging
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConstructionIndustrySchemeConnector @Inject() (config: ServicesConfig, http: HttpClientV2)(implicit
  ec: ExecutionContext
) extends HttpReadsInstances
    with Logging {

  private val cisBaseUrl: String = config.baseUrl("construction-industry-scheme") + "/cis"

  def getCisTaxpayer()(implicit hc: HeaderCarrier): Future[CisTaxpayerResponse] =
    http
      .get(url"$cisBaseUrl/taxpayer")
      .execute[CisTaxpayerResponse]
      .map { response =>
        logger.info(s"[ConstructionIndustrySchemeConnector][getCisTaxpayer] Response: $response")
        response
      }

  def createSubcontractor(
    payload: CreateSubcontractorRequest
  )(implicit hc: HeaderCarrier): Future[CreateSubcontractorResponse] =
    logger.info(
      s"[ConstructionIndustrySchemeConnector][createSubcontractor] Payload: ${Json.toJson(payload)}"
    )
    http
      .post(url"$cisBaseUrl/subcontractor/create")
      .withBody(Json.toJson(payload))
      .execute[CreateSubcontractorResponse]
      .map { response =>
        logger.info(s"[ConstructionIndustrySchemeConnector][createSubcontractor] Response: $response")
        response
      }

  def updateSubcontractor(
    payload: UpdateSubcontractorRequest
  )(implicit hc: HeaderCarrier): Future[Unit] =
    logger.info(
      s"[ConstructionIndustrySchemeConnector][updateSubcontractor] Payload: ${Json.toJson(payload)}"
    )
    http
      .post(url"$cisBaseUrl/subcontractor/update")
      .withBody(Json.toJson(payload))
      .execute[HttpResponse]
      .flatMap { response =>
        if (response.status == NO_CONTENT) {
          Future.successful(())
        } else {
          Future.failed(
            new RuntimeException(
              s"Update subcontractor failed, returned ${response.status}"
            )
          )
        }
      }
}

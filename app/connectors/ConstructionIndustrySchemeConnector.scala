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

import models.requests.CreateAndUpdateSubcontractorPayload
import models.requests.CreateAndUpdateSubcontractorPayload.*
import models.response.CisTaxpayerResponse
import models.response.{GetNewestVerificationBatchResponse, GetSubcontractorUTRsResponse}
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, HttpReadsInstances, HttpResponse, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

final case class HasClientResponse(hasClient: Boolean)

object HasClientResponse {
  implicit val format: OFormat[HasClientResponse] = Json.format[HasClientResponse]
}

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

  def createAndUpdateSubcontractor(
    payload: CreateAndUpdateSubcontractorPayload
  )(implicit hc: HeaderCarrier): Future[Unit] = {

    val jsonPayload: JsValue = payload match {
      case p: IndividualOrSoleTraderPayload => Json.toJson(p)
      case p: PartnershipPayload            => Json.toJson(p)
      case p: CompanyPayload                => Json.toJson(p)
      case p: TrustPayload                  => Json.toJson(p)
    }

    logger.info(
      s"[ConstructionIndustrySchemeConnector][createAndUpdateSubcontractor] Payload: $jsonPayload"
    )

    http
      .post(url"$cisBaseUrl/subcontractor/create-and-update")
      .withBody(jsonPayload)
      .execute[HttpResponse]
      .flatMap { response =>
        if (response.status == NO_CONTENT) {
          Future.successful(())
        } else {
          Future.failed(new RuntimeException(s"Update subcontractor failed, returned ${response.status}"))
        }
      }
  }

  def getSubcontractorUTRs(cisId: String)(implicit hc: HeaderCarrier): Future[GetSubcontractorUTRsResponse] = {
    logger.info(s"[ConstructionIndustrySchemeConnector][getSubcontractorUTRs] cisId: $cisId")

    http
      .get(url"$cisBaseUrl/subcontractors/utr/$cisId")
      .execute[GetSubcontractorUTRsResponse]
      .map { response =>
        logger.info(s"[ConstructionIndustrySchemeConnector][getSubcontractorUTRs] Response: $response")
        response
      }
  }

  def hasClient(taxOfficeNumber: String, taxOfficeReference: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    http
      .get(url"$cisBaseUrl/agent/has-client/$taxOfficeNumber/$taxOfficeReference")
      .execute[HasClientResponse]
      .map(_.hasClient)

  def getAgentClient(userId: String)(implicit
    hc: HeaderCarrier
  ): Future[Option[JsValue]] =
    http
      .get(url"$cisBaseUrl/user-cache/agent-client/$userId")
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK        => Some(response.json)
          case NOT_FOUND => None
          case _         => throw new HttpException(response.body, response.status)
        }
      }

  def getNewestVerificationBatch(
    instanceId: String
  )(implicit hc: HeaderCarrier): Future[GetNewestVerificationBatchResponse] =
    http
      .get(url"$cisBaseUrl/verification-batch/newest/$instanceId")
      .execute[GetNewestVerificationBatchResponse]
      .map { response =>
        logger.info(
          s"[ConstructionIndustrySchemeConnector][getNewestVerificationBatch] instanceId=$instanceId - Response received"
        )
        response
      }

}

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

package services

import connectors.ConstructionIndustrySchemeConnector
import models.subContractor.{SubContractorCreateRequest, SubContractorCreateResponse}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class SubContractorService @Inject() (
  cisConnector: ConstructionIndustrySchemeConnector
)(implicit ec: ExecutionContext)
    extends Logging {
  def createSubContractor(
    schemeId: Int,
    subcontractorType: String,
    currentVersion: Int
  )(implicit hc: HeaderCarrier): Future[SubContractorCreateResponse] = {
    val payload = SubContractorCreateRequest(
      schemeId = schemeId,
      subcontractorType = subcontractorType,
      currentVersion = 0
    )
    logger.info(
      s"[SubContractorService] Calling BE  to create FormP sub contractor for $schemeId $subcontractorType $currentVersion"
    )
    cisConnector.createSubContractor(payload).andThen {
      case Success(response) =>
        logger.info(s"[SubContractorService] FormP sub contractor creation completed successfully")
      case Failure(error)    => logger.error("[SubContractorService] BE call failed", error)
    }
  }
}

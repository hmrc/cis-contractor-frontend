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
import models.UserAnswers
import models.subcontractor.{SubcontractorCreateRequest, SubcontractorCreateResponse}
import pages.add.TypeOfSubcontractorPage
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class SubcontractorService @Inject() (
  cisConnector: ConstructionIndustrySchemeConnector
)(implicit ec: ExecutionContext)
    extends Logging {
  def createSubcontractor(
    schemeId: Int,
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier): Future[SubcontractorCreateResponse] =
    for {
      subcontractorType <- getSubcontractorType(userAnswers)
      payload            = SubcontractorCreateRequest(
                             schemeId = schemeId,
                             subcontractorType = subcontractorType,
                             currentVersion = 0
                           )
      response          <- cisConnector.createSubcontractor(payload)
    } yield response

  private def getSubcontractorType(userAnswers: UserAnswers): Future[String] =
    userAnswers.get(TypeOfSubcontractorPage) match {
      case Some(subcontractorType) => Future.successful(subcontractorType.toString)
      case None                    => Future.failed(new RuntimeException("Subcontractor Type not found in session data"))
    }
}

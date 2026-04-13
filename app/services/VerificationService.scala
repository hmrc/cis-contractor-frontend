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
import pages.verification.NewestVerificationBatchResponsePage
import queries.CisIdQuery
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerificationBatchService @Inject() (
  cisConnector: ConstructionIndustrySchemeConnector,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext) {

  def refreshNewestVerificationBatch(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers] =
    for {
      instanceId <- userAnswers
                      .get(CisIdQuery)
                      .map(Future.successful)
                      .getOrElse(Future.failed(new RuntimeException("InstanceIdQuery not found in session data")))

      response <- cisConnector.getNewestVerificationBatch(instanceId)
      updated  <- Future.fromTry(userAnswers.set(NewestVerificationBatchResponsePage, response))
      _        <- sessionRepository.set(updated)
    } yield updated
}

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
import models.requests.IdentifierRequest
import models.response.CisTaxpayerResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CisTaxpayerService @Inject() (
  connector: ConstructionIndustrySchemeConnector
)(using ExecutionContext)
    extends FrontendHeaderCarrierProvider {

  def findForOrganisation(using req: IdentifierRequest[?]): Future[CisTaxpayerResponse] =
    if req.employerReference.isEmpty then
      Future.failed(new IllegalStateException("Organisation user has no employer reference"))
    else connector.getCisTaxpayer()

  def findForAgent(cisId: String)(using IdentifierRequest[?]): Future[Option[CisTaxpayerResponse]] =
    connector.getAllClients.map(_.find(_.uniqueId == cisId))
}

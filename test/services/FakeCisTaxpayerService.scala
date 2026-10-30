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
import org.scalatestplus.mockito.MockitoSugar.mock
import repositories.CisTaxpayerCache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FakeCisTaxpayerService
    extends CisTaxpayerService(
      mock[ConstructionIndustrySchemeConnector],
      mock[CisTaxpayerCache]
    ) {

  val taxpayer: CisTaxpayerResponse = CisTaxpayerResponse(
    uniqueId = "AB12",
    taxOfficeNumber = "123",
    taxOfficeRef = "AB456",
    aoDistrict = None,
    aoPayType = None,
    aoCheckCode = None,
    aoReference = None,
    validBusinessAddr = None,
    correlation = None,
    ggAgentId = None,
    employerName1 = None,
    employerName2 = None,
    agentOwnRef = None,
    schemeName = None,
    utr = None,
    enrolledSig = None
  )

  override def findForOrganisation(using req: IdentifierRequest[?]): Future[CisTaxpayerResponse] =
    if req.employerReference.isEmpty then
      Future.failed(new IllegalStateException("Organisation user has no employer reference"))
    else Future.successful(taxpayer)

  override def findForAgent(cisId: String)(using IdentifierRequest[?]): Future[Option[CisTaxpayerResponse]] =
    Future.successful(Option.when(cisId == taxpayer.uniqueId)(taxpayer))
}

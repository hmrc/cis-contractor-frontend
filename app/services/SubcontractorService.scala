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
import models.add.{SubcontractorName, TypeOfSubcontractor}
import models.subcontractor.CreateAndUpdateSubcontractorRequest
import pages.add.*
import play.api.Logging
import queries.CisIdQuery
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubcontractorService @Inject() (
  cisConnector: ConstructionIndustrySchemeConnector
)(implicit ec: ExecutionContext)
    extends Logging {

  def createAndUpdateSubcontractor(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier): Future[Unit] =
    for {
      cisId             <- getCisId(userAnswers)
      subcontractorType <- getSubcontractorType(userAnswers)

      payload = CreateAndUpdateSubcontractorRequest(
                  cisId = cisId,
                  subcontractorType = subcontractorType,
                  firstName = userAnswers.get(SubcontractorNamePage).map(_.firstName),
                  secondName = userAnswers.get(SubcontractorNamePage).flatMap(_.middleName),
                  surname = userAnswers.get(SubcontractorNamePage).map(_.lastName),
                  tradingName = userAnswers.get(TradingNameOfSubcontractorPage),
                  addressLine1 = userAnswers.get(AddressOfSubcontractorPage).map(_.addressLine1),
                  addressLine2 = userAnswers.get(AddressOfSubcontractorPage).flatMap(_.addressLine2),
                  addressLine3 = userAnswers.get(AddressOfSubcontractorPage).map(_.addressLine3),
                  addressLine4 = userAnswers.get(AddressOfSubcontractorPage).flatMap(_.addressLine4),
                  postcode = userAnswers.get(AddressOfSubcontractorPage).map(_.postCode),
                  nino = userAnswers.get(SubNationalInsuranceNumberPage),
                  utr = userAnswers.get(SubcontractorsUniqueTaxpayerReferencePage),
                  worksReferenceNumber = userAnswers.get(WorksReferenceNumberPage),
                  emailAddress = userAnswers.get(SubContactDetailsPage).map(_.email),
                  phoneNumber = userAnswers.get(SubContactDetailsPage).map(_.telephone)
                )
      _      <- cisConnector.createAndUpdateSubcontractor(payload)
    } yield ()
  
  def isDuplicateUTR(
    userAnswers: UserAnswers,
    utr: String
  )(implicit hc: HeaderCarrier): Future[Boolean] =
    for {
      cisId     <- getCisId(userAnswers)
      utrList   <- cisConnector.getSubcontractorUTRs(cisId.toString)
    } yield {
      utrList.subcontractorUTRs.contains(utr)
    }

  private def getCisId(userAnswers: UserAnswers): Future[String] =
    userAnswers.get(CisIdQuery) match {
      case Some(cisId) => Future.successful(cisId)
      case None        => Future.failed(new RuntimeException("CisIdQuery not found in session data"))
    }

  private def getSubcontractorType(userAnswers: UserAnswers): Future[TypeOfSubcontractor] =
    userAnswers.get(TypeOfSubcontractorPage) match {
      case Some(subcontractorType) => Future.successful(subcontractorType)
      case None                    => Future.failed(new RuntimeException("TypeOfSubcontractorPage not found in session data"))
    }
}

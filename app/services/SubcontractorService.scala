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
import models.add.SubcontractorName
import models.subcontractor.{CreateSubcontractorRequest, UpdateSubcontractorRequest, UpdateSubcontractorResponse}
import pages.add.{AddressOfSubcontractorPage, SubContactDetailsPage, SubNationalInsuranceNumberPage, SubcontractorNamePage, SubcontractorsUniqueTaxpayerReferencePage, TradingNameOfSubcontractorPage, TypeOfSubcontractorPage, WorksReferenceNumberPage}
import play.api.Logging
import queries.{CisIdQuery, SubbieResourceRefQuery}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubcontractorService @Inject() (
  cisConnector: ConstructionIndustrySchemeConnector
)(implicit ec: ExecutionContext)
    extends Logging {

  def initializeCisId(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers] =
    userAnswers.get(CisIdQuery) match {
      case Some(cisId) => Future.successful(userAnswers)
      case None        =>
        cisConnector.getCisTaxpayer().flatMap { tp =>
          val cisId = tp.uniqueId.trim
          if (cisId.isEmpty) {
            Future.failed(new RuntimeException("Empty cisId (uniqueId) returned from /cis/taxpayer"))
          } else {
            for {
              updatedUserAnswers <- Future.fromTry(userAnswers.set(CisIdQuery, cisId))
            } yield updatedUserAnswers
          }
        }
    }

  def ensureSubcontractorInUserAnswers(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier): Future[UserAnswers] =
    userAnswers.get(SubbieResourceRefQuery) match {
      // TODO: If the user changes the trading type, we need to create a new subcontractor.
      // This will be handled in a future change.
      case Some(value) => Future.successful(userAnswers)
      case None        =>
        for {
          cisId             <- getCisId(userAnswers)
          subcontractorType <- getSubcontractorType(userAnswers)
          payload            = CreateSubcontractorRequest(
                                 schemeId = cisId,
                                 subcontractorType = subcontractorType
                               )
          response          <- cisConnector.createSubcontractor(payload)
          updatedAnswers    <- Future.fromTry(userAnswers.set(SubbieResourceRefQuery, response.subbieResourceRef))
        } yield updatedAnswers
    }

  def updateSubcontractor(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier): Future[UpdateSubcontractorResponse] =
    for {
      cisId             <- getCisId(userAnswers)
      subbieResourceRef <- getSubbieResourceRef(userAnswers)
      payload            = UpdateSubcontractorRequest(
                             schemeId = cisId,
                             subbieResourceRef = subbieResourceRef,
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
      response          <- cisConnector.updateSubcontractor(payload)
    } yield response

  private def getCisId(userAnswers: UserAnswers): Future[String] =
    userAnswers.get(CisIdQuery) match {
      case Some(cisId) => Future.successful(cisId)
      case None        => Future.failed(new RuntimeException("CisIdQuery not found in session data"))
    }

  private def getSubbieResourceRef(userAnswers: UserAnswers): Future[Int] =
    userAnswers.get(SubbieResourceRefQuery) match {
      case Some(subbieResourceRef) => Future.successful(subbieResourceRef)
      case None                    => Future.failed(new RuntimeException("SubbieResourceRef not found in session data"))
    }

  private def getSubcontractorType(userAnswers: UserAnswers): Future[String] =
    userAnswers.get(TypeOfSubcontractorPage) match {
      case Some(subcontractorType) => Future.successful(subcontractorType.toString)
      case None                    => Future.failed(new RuntimeException("TypeOfSubcontractorPage not found in session data"))
    }
}

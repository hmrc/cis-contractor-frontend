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

  def createSubcontractor(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier): Future[UserAnswers] =
    userAnswers.get(SubbieResourceRefQuery) match {
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
      cisId                                                             <- getCisId(userAnswers)
      subbieResourceRef                                                 <- getSubbieResourceRef(userAnswers)
      (firstName, secondName, surname)                                   =
        getName(userAnswers)
      (addressLine1, addressLine2, addressLine3, addressLine4, postCode) =
        getAddress(userAnswers)
      (email, phone)                                                     = getContactDetails(userAnswers)
      payload                                                            = UpdateSubcontractorRequest(
                                                                             schemeId = cisId,
                                                                             subbieResourceRef = subbieResourceRef,
                                                                             firstName = firstName,
                                                                             secondName = secondName,
                                                                             surname = surname,
                                                                             tradingName = userAnswers.get(TradingNameOfSubcontractorPage),
                                                                             addressLine1 = addressLine1,
                                                                             addressLine2 = addressLine2,
                                                                             addressLine3 = addressLine3,
                                                                             addressLine4 = addressLine4,
                                                                             country = addressLine4,
                                                                             postcode = postCode,
                                                                             nino = userAnswers.get(SubNationalInsuranceNumberPage),
                                                                             utr = userAnswers.get(SubcontractorsUniqueTaxpayerReferencePage),
                                                                             worksReferenceNumber = userAnswers.get(WorksReferenceNumberPage),
                                                                             emailAddress = email,
                                                                             phoneNumber = phone
                                                                           )
      response                                                          <- cisConnector.updateSubcontractor(payload)
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

  private def getName(userAnswers: UserAnswers): (Option[String], Option[String], Option[String]) =
    userAnswers.get(SubcontractorNamePage) match {
      case Some(name) =>
        (Some(name.firstName), name.middleName, Some(name.lastName))
      case None       =>
        (None, None, None)
    }

  private def getAddress(
    userAnswers: UserAnswers
  ): (Option[String], Option[String], Option[String], Option[String], Option[String]) =
    userAnswers.get(AddressOfSubcontractorPage) match {
      case Some(address) =>
        (
          Some(address.addressLine1),
          address.addressLine2,
          Some(address.addressLine3),
          address.addressLine4,
          Some(address.postCode)
        )
      case None          =>
        (None, None, None, None, None)
    }

  private def getContactDetails(userAnswers: UserAnswers): (Option[String], Option[String]) =
    userAnswers.get(SubContactDetailsPage) match {
      case Some(contactDetails) =>
        (Some(contactDetails.email), Some(contactDetails.telephone))
      case None                 =>
        (None, None)
    }
}

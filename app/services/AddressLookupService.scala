/*
 * Copyright 2025 HM Revenue & Customs
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

import config.AddressLookupConfiguration
import connectors.AddressLookupConnector
import models.address.{Address, AddressLookupJourneyIdentifier, MandatoryFieldsConfigModel}
import models.requests.DataRequest
import play.api.mvc.{Call, Request}
import queries.{AddressLookupAmendReturnQuery, Settable}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupService @Inject() (
  addressLookupConnector: AddressLookupConnector,
  alfConfig: AddressLookupConfiguration,
  sessionRepository: SessionRepository
) extends LoggingUtil {

  def getAddressById(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Address] =
    addressLookupConnector.getAddress(id).map(_.normalise())

  def getJourneyUrl(
    journeyId: AddressLookupJourneyIdentifier.Value,
    continueUrl: Call,
    useUkMode: Boolean = false,
    optName: Option[String] = None,
    mandatoryFieldsConfigModel: MandatoryFieldsConfigModel,
    line1MaxLength: Option[Int] = None,
    line2MaxLength: Option[Int] = None,
    line3MaxLength: Option[Int] = None,
    townMaxLength: Option[Int] = None
  )(implicit hc: HeaderCarrier, request: Request[_], executionContext: ExecutionContext): Future[Call] =
    addressLookupConnector.getOnRampUrl(
      alfConfig(
        journeyId,
        continueUrl,
        useUkMode,
        optName,
        mandatoryFieldsConfigModel = mandatoryFieldsConfigModel,
        line1MaxLength = line1MaxLength,
        line2MaxLength = line2MaxLength,
        line3MaxLength = line3MaxLength,
        townMaxLength = townMaxLength
      )
    )

  def saveAddressDetails(address: Address, page: Settable[Address])(implicit
    request: DataRequest[_],
    ec: ExecutionContext
  ): Future[Boolean] = {

    val answers = request.userAnswers

    val updatedAnswersTry =
      if (answers.get(AddressLookupAmendReturnQuery).contains(true)) {
        answers.setAndAmend(page, address)
      } else {
        answers.set(page, address)
      }

    for {
      updatedAnswers <- Future.fromTry(updatedAnswersTry)
      cleanedAnswers <- Future.fromTry(updatedAnswers.remove(AddressLookupAmendReturnQuery))
      result         <- sessionRepository.set(cleanedAnswers)
    } yield result
  }

}

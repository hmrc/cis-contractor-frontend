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

package controllers.helpers

import models.add.SubcontractorName
import models.address.{Address, Country}
import models.contact.ContactMethodOptions
import models.response.SubcontractorResponse

object SubcontractorPopulatorUtils {

  def toAddress(
    subcontractor: SubcontractorResponse
  ): Option[Address] =
    subcontractor.addressLine1.map { line1 =>
      Address(
        addressLine1 = line1,
        addressLine2 = subcontractor.addressLine2,
        addressLine3 = subcontractor.addressLine3,
        addressLine4 = subcontractor.addressLine4,
        postcode = subcontractor.postcode,
        country = subcontractor.country.map(name => Country(None, Some(name)))
      )
    }

  def contactMethods(
    subcontractor: SubcontractorResponse
  ): Set[ContactMethodOptions] =
    Set(
      subcontractor.emailAddress.map(_ => ContactMethodOptions.Email),
      subcontractor.phoneNumber.map(_ => ContactMethodOptions.Phone),
      subcontractor.mobilePhoneNumber.map(_ => ContactMethodOptions.Mobile)
    ).flatten

  def individualName(
    subcontractor: SubcontractorResponse
  ): Option[SubcontractorName] =
    for {
      firstName <- subcontractor.firstName
      surname   <- subcontractor.surname
    } yield SubcontractorName(
      firstName = firstName,
      middleName = subcontractor.secondName,
      lastName = surname
    )

  def usesTradingName(
    subcontractor: SubcontractorResponse
  ): Boolean =
    subcontractor.tradingName.exists(_.trim.nonEmpty)
}

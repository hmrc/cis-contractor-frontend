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

package models.amend.company

sealed trait AmendCompanyRemoveDetail {
  def key: String
  def messageKey: String
}

object AmendCompanyRemoveDetail {

  case object Address extends AmendCompanyRemoveDetail {
    override val key: String =
      "address"

    override val messageKey: String =
      "amendCompanyRemoveDetailYesNo.detail.address"
  }

  case object ContactDetails extends AmendCompanyRemoveDetail {
    override val key: String =
      "contact-details"

    override val messageKey: String =
      "amendCompanyRemoveDetailYesNo.detail.contactDetails"

  }

  case object Utr extends AmendCompanyRemoveDetail {
    override val key: String =
      "utr"

    override val messageKey: String =
      "amendCompanyRemoveDetailYesNo.detail.utr"
  }

  case object CompanyRegistrationNumber extends AmendCompanyRemoveDetail {
    override val key: String =
      "company-registration-number"

    override val messageKey: String =
      "amendCompanyRemoveDetailYesNo.detail.companyRegistrationNumber"
  }

  case object WorksReferenceNumber extends AmendCompanyRemoveDetail {
    override val key: String =
      "works-reference-number"

    override val messageKey: String =
      "amendCompanyRemoveDetailYesNo.detail.worksReferenceNumber"
  }

  val values: Seq[AmendCompanyRemoveDetail] =
    Seq(
      Address,
      ContactDetails,
      Utr,
      CompanyRegistrationNumber,
      WorksReferenceNumber
    )

  def fromKey(
    key: String
  ): Option[AmendCompanyRemoveDetail] =
    values.find(_.key == key)
}

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

package models.amend

sealed trait AmendIndividualRemoveDetail {
  def key: String
  def messageKey: String
}

object AmendIndividualRemoveDetail {

  case object TradingName extends AmendIndividualRemoveDetail {
    override val key: String =
      "trading-name"

    override val messageKey: String =
      "amendIndividualRemoveDetailYesNo.detail.tradingName"
  }

  case object SubcontractorName extends AmendIndividualRemoveDetail {
    override val key: String =
      "subcontractor-name"

    override val messageKey: String =
      "amendIndividualRemoveDetailYesNo.detail.subcontractorName"
  }

  case object Address extends AmendIndividualRemoveDetail {
    override val key: String =
      "address"

    override val messageKey: String =
      "amendIndividualRemoveDetailYesNo.detail.address"
  }

  case object ContactDetails extends AmendIndividualRemoveDetail {
    override val key: String =
      "contact-details"

    override val messageKey: String =
      "amendIndividualRemoveDetailYesNo.detail.contactDetails"

  }

  case object Utr extends AmendIndividualRemoveDetail {
    override val key: String =
      "utr"

    override val messageKey: String =
      "amendIndividualRemoveDetailYesNo.detail.utr"
  }

  case object NationalInsuranceNumber extends AmendIndividualRemoveDetail {
    override val key: String =
      "national-insurance-number"

    override val messageKey: String =
      "amendIndividualRemoveDetailYesNo.detail.nationalInsuranceNumber"
  }

  case object WorksReferenceNumber extends AmendIndividualRemoveDetail {
    override val key: String =
      "works-reference-number"

    override val messageKey: String =
      "amendIndividualRemoveDetailYesNo.detail.worksReferenceNumber"
  }

  val values: Seq[AmendIndividualRemoveDetail] =
    Seq(
      TradingName,
      SubcontractorName,
      Address,
      ContactDetails,
      Utr,
      NationalInsuranceNumber,
      WorksReferenceNumber
    )

  def fromKey(
    key: String
  ): Option[AmendIndividualRemoveDetail] =
    values.find(_.key == key)
}

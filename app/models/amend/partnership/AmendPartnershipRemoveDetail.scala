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

package models.amend.partnership

sealed trait AmendPartnershipRemoveDetail {

  def key: String

  def messageKey: String

  def isNominatedPartnerDetail: Boolean
}

object AmendPartnershipRemoveDetail {

  case object Address extends AmendPartnershipRemoveDetail {
    override val key: String =
      "address"

    override val messageKey: String =
      "amendPartnershipRemoveDetailYesNo.detail.address"

    override val isNominatedPartnerDetail: Boolean =
      false
  }

  case object ContactDetails extends AmendPartnershipRemoveDetail {
    override val key: String =
      "contact-details"

    override val messageKey: String =
      "amendPartnershipRemoveDetailYesNo.detail.contactDetails"

    override val isNominatedPartnerDetail: Boolean =
      false
  }

  case object Utr extends AmendPartnershipRemoveDetail {
    override val key: String =
      "utr"

    override val messageKey: String =
      "amendPartnershipRemoveDetailYesNo.detail.utr"

    override val isNominatedPartnerDetail: Boolean =
      false
  }

  case object WorksReferenceNumber extends AmendPartnershipRemoveDetail {
    override val key: String =
      "works-reference-number"

    override val messageKey: String =
      "amendPartnershipRemoveDetailYesNo.detail.worksReferenceNumber"

    override val isNominatedPartnerDetail: Boolean =
      false
  }

  case object NominatedPartnerUtr extends AmendPartnershipRemoveDetail {
    override val key: String =
      "nominated-partner-utr"

    override val messageKey: String =
      "amendPartnershipRemoveDetailYesNo.detail.nominatedPartnerUtr"

    override val isNominatedPartnerDetail: Boolean =
      true
  }

  case object NominatedPartnerNino extends AmendPartnershipRemoveDetail {
    override val key: String =
      "nominated-partner-nino"

    override val messageKey: String =
      "amendPartnershipRemoveDetailYesNo.detail.nominatedPartnerNino"

    override val isNominatedPartnerDetail: Boolean =
      true
  }

  case object NominatedPartnerCompanyRegistrationNumber extends AmendPartnershipRemoveDetail {

    override val key: String =
      "nominated-partner-company-registration-number"

    override val messageKey: String =
      "amendPartnershipRemoveDetailYesNo.detail.nominatedPartnerCompanyRegistrationNumber"

    override val isNominatedPartnerDetail: Boolean =
      true
  }

  val values: Seq[AmendPartnershipRemoveDetail] =
    Seq(
      Address,
      ContactDetails,
      Utr,
      WorksReferenceNumber,
      NominatedPartnerUtr,
      NominatedPartnerNino,
      NominatedPartnerCompanyRegistrationNumber
    )

  def fromKey(
    key: String
  ): Option[AmendPartnershipRemoveDetail] =
    values.find(_.key == key)
}

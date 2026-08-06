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

package models.amend.trust

sealed trait AmendTrustRemoveDetail {
  def key: String
  def messageKey: String
}

object AmendTrustRemoveDetail {

  case object Address extends AmendTrustRemoveDetail {
    override val key: String =
      "address"

    override val messageKey: String =
      "amendTrustRemoveDetailYesNo.detail.address"
  }

  case object ContactDetails extends AmendTrustRemoveDetail {
    override val key: String =
      "contact-details"

    override val messageKey: String =
      "amendTrustRemoveDetailYesNo.detail.contactDetails"

  }

  case object Utr extends AmendTrustRemoveDetail {
    override val key: String =
      "utr"

    override val messageKey: String =
      "amendTrustRemoveDetailYesNo.detail.utr"
  }

  case object WorksReferenceNumber extends AmendTrustRemoveDetail {
    override val key: String =
      "works-reference-number"

    override val messageKey: String =
      "amendTrustRemoveDetailYesNo.detail.worksReferenceNumber"
  }

  val values: Seq[AmendTrustRemoveDetail] =
    Seq(
      Address,
      ContactDetails,
      Utr,
      WorksReferenceNumber
    )

  def fromKey(
    key: String
  ): Option[AmendTrustRemoveDetail] =
    values.find(_.key == key)
}

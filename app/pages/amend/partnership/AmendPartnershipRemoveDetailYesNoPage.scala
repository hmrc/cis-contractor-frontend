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

package pages.amend.partnership

import models.UserAnswers
import pages.QuestionPage
import pages.add.partnership.*
import play.api.libs.json.JsPath

case class AmendPartnershipRemoveDetailYesNoPage(
  detail: String
) extends QuestionPage[Boolean] {

  override def path: JsPath =
    JsPath \ toString

  override def toString: String =
    s"removePartnershipSubcontractorDetailYesNo-$detail"

  override def cleanup(
    value: Option[Boolean],
    userAnswers: UserAnswers
  ) =
    detail match {

      case "address" if value.contains(true) =>
        userAnswers
          .remove(PartnershipAddressPage)
          .flatMap(_.set(PartnershipAddressYesNoPage, false))

      case "contact-details" if value.contains(true) =>
        userAnswers
          .remove(PartnershipContactMethodOptionsPage)
          .flatMap(_.remove(PartnershipEmailAddressPage))
          .flatMap(_.remove(PartnershipPhoneNumberPage))
          .flatMap(_.remove(PartnershipMobileNumberPage))
          .flatMap(_.set(AddPartnershipContactMethodsYesNoPage, false))

      case "utr" if value.contains(true) =>
        userAnswers
          .remove(PartnershipUniqueTaxpayerReferencePage)
          .flatMap(_.set(PartnershipHasUtrYesNoPage, false))

      case "works-reference-number" if value.contains(true) =>
        userAnswers
          .remove(PartnershipWorksReferenceNumberPage)
          .flatMap(_.set(PartnershipWorksReferenceNumberYesNoPage, false))

      case "nominated-partner-utr" if value.contains(true) =>
        userAnswers
          .remove(PartnershipNominatedPartnerUtrPage)
          .flatMap(_.set(PartnershipNominatedPartnerUtrYesNoPage, false))

      case "nominated-partner-nino" if value.contains(true) =>
        userAnswers
          .remove(PartnershipNominatedPartnerNinoPage)
          .flatMap(_.set(PartnershipNominatedPartnerNinoYesNoPage, false))

      case "nominated-partner-company-registration-number" if value.contains(true) =>
        userAnswers
          .remove(PartnershipNominatedPartnerCrnPage)
          .flatMap(_.set(PartnershipNominatedPartnerCrnYesNoPage, false))

      case _ =>
        super.cleanup(value, userAnswers)
    }
}

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

package pages.amend

import models.UserAnswers
import pages.QuestionPage
import pages.add.*
import play.api.libs.json.JsPath

case class AmendIndividualRemoveDetailYesNoPage(subcontractorDetail: String) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "amendIndividualRemoveDetailYesNo"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers) =
    subcontractorDetail match {
      case "trading-name" if value.contains(true) =>
        userAnswers
          .remove(TradingNameOfSubcontractorPage)
          .flatMap(_.set(SubTradingNameYesNoPage, false))

      case "trading-name" if value.contains(false) =>
        userAnswers
          .set(SubTradingNameYesNoPage, true)

      case "subcontractor-name" if value.contains(true) =>
        userAnswers
          .remove(SubcontractorNamePage)
          .flatMap(_.set(SubTradingNameYesNoPage, true))

      case "subcontractor-name" if value.contains(false) =>
        userAnswers
          .set(SubTradingNameYesNoPage, false)

      case "address" if value.contains(true) =>
        userAnswers
          .remove(AddressOfSubcontractorPage)
          .flatMap(_.set(SubAddressYesNoPage, false))

      case "contact-details" if value.contains(true) =>
        userAnswers
          .remove(IndividualContactMethodOptionsPage)
          .flatMap(_.remove(IndividualEmailAddressPage))
          .flatMap(_.remove(IndividualPhoneNumberPage))
          .flatMap(_.remove(IndividualMobileNumberPage))
          .flatMap(_.set(AddIndividualContactMethodsYesNoPage, false))

      case "unique-taxpayer-reference" if value.contains(true) =>
        userAnswers
          .remove(SubcontractorsUniqueTaxpayerReferencePage)
          .flatMap(_.set(UniqueTaxpayerReferenceYesNoPage, false))

      case "national-insurance-number" if value.contains(true) =>
        userAnswers
          .remove(SubNationalInsuranceNumberPage)
          .flatMap(_.set(NationalInsuranceNumberYesNoPage, false))

      case "works-reference-number" if value.contains(true) =>
        userAnswers
          .remove(WorksReferenceNumberPage)
          .flatMap(_.set(WorksReferenceNumberYesNoPage, false))

      case _ =>
        super.cleanup(value, userAnswers)
    }
}

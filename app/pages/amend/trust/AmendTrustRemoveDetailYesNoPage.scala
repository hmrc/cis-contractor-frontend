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

package pages.amend.trust

import models.UserAnswers
import pages.QuestionPage
import pages.add.trust.*
import play.api.libs.json.JsPath

case class AmendTrustRemoveDetailYesNoPage(subcontractorDetail: String) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "amendTrustRemoveDetailYesNo"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers) =
    subcontractorDetail match {
      case "address" if value.contains(true) =>
        userAnswers
          .remove(TrustAddressPage)
          .flatMap(_.set(TrustAddressYesNoPage, false))

      case "contact-details" if value.contains(true) =>
        userAnswers
          .remove(TrustContactMethodOptionsPage)
          .flatMap(_.remove(TrustEmailAddressPage))
          .flatMap(_.remove(TrustPhoneNumberPage))
          .flatMap(_.remove(TrustMobileNumberPage))
          .flatMap(_.set(AddTrustContactMethodsYesNoPage, false))

      case "unique-taxpayer-reference" if value.contains(true) =>
        userAnswers
          .remove(TrustUtrPage)
          .flatMap(_.set(TrustUtrYesNoPage, false))

      case "works-reference-number" if value.contains(true) =>
        userAnswers
          .remove(TrustWorksReferencePage)
          .flatMap(_.set(TrustWorksReferenceYesNoPage, false))

      case _ =>
        super.cleanup(value, userAnswers)
    }
}

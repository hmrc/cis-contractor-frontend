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

package pages.amend.company

import models.UserAnswers
import models.amend.company.AmendCompanyRemoveDetail
import models.amend.company.AmendCompanyRemoveDetail.*
import pages.QuestionPage
import pages.add.company.*
import play.api.libs.json.JsPath

case class AmendCompanyRemoveDetailYesNoPage(
  subcontractorDetail: AmendCompanyRemoveDetail,
  subJourneyId: Option[String] = None
) extends QuestionPage[Boolean] {

  // All variants share one JSON key: this value is transient and removed immediately after submit, so concurrent detail types never clash.
  override def path: JsPath = subJourneyId match {
    case Some(id) => JsPath \ "amendSubcontractor" \ id \ toString
    case None     => JsPath \ toString
  }

  override def toString: String = "amendCompanyRemoveDetailYesNo"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers) =
    subJourneyId match {
      case Some(id) =>
        subcontractorDetail match {
          case Address if value.contains(true) =>
            userAnswers
              .remove(CompanyAddressPage(id))
              .flatMap(_.set(CompanyAddressYesNoPage(id), false))

          case ContactDetails if value.contains(true) =>
            userAnswers
              .remove(CompanyContactMethodOptionsPage(id))
              .flatMap(_.remove(CompanyEmailAddressPage(id)))
              .flatMap(_.remove(CompanyPhoneNumberPage(id)))
              .flatMap(_.remove(CompanyMobileNumberPage(id)))
              .flatMap(_.set(AddCompanyContactMethodsYesNoPage(id), false))

          case Utr if value.contains(true) =>
            userAnswers
              .remove(CompanyUtrPage(id))
              .flatMap(_.set(CompanyUtrYesNoPage(id), false))

          case CompanyRegistrationNumber if value.contains(true) =>
            userAnswers
              .remove(CompanyCrnPage(id))
              .flatMap(_.set(CompanyCrnYesNoPage(id), false))

          case WorksReferenceNumber if value.contains(true) =>
            userAnswers
              .remove(CompanyWorksReferencePage(id))
              .flatMap(_.set(CompanyWorksReferenceYesNoPage(id), false))

          case _ =>
            super.cleanup(value, userAnswers)
        }
      case None     =>
        subcontractorDetail match {
          case Address if value.contains(true) =>
            userAnswers
              .remove(CompanyAddressPage)
              .flatMap(_.set(CompanyAddressYesNoPage, false))

          case ContactDetails if value.contains(true) =>
            userAnswers
              .remove(CompanyContactMethodOptionsPage)
              .flatMap(_.remove(CompanyEmailAddressPage))
              .flatMap(_.remove(CompanyPhoneNumberPage))
              .flatMap(_.remove(CompanyMobileNumberPage))
              .flatMap(_.set(AddCompanyContactMethodsYesNoPage, false))

          case Utr if value.contains(true) =>
            userAnswers
              .remove(CompanyUtrPage)
              .flatMap(_.set(CompanyUtrYesNoPage, false))

          case CompanyRegistrationNumber if value.contains(true) =>
            userAnswers
              .remove(CompanyCrnPage)
              .flatMap(_.set(CompanyCrnYesNoPage, false))

          case WorksReferenceNumber if value.contains(true) =>
            userAnswers
              .remove(CompanyWorksReferencePage)
              .flatMap(_.set(CompanyWorksReferenceYesNoPage, false))

          case _ =>
            super.cleanup(value, userAnswers)
        }
    }
}

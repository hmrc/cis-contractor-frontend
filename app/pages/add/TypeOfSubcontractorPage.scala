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

package pages.add

import models.UserAnswers
import models.add.TypeOfSubcontractor
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case object TypeOfSubcontractorPage extends QuestionPage[TypeOfSubcontractor] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "typeOfSubcontractor"

  override def cleanup(value: Option[TypeOfSubcontractor], ua: UserAnswers): Try[UserAnswers] = {
    val previousValue = ua.get(TypeOfSubcontractorPage)

    (previousValue, value) match {
      case (Some(oldValue), Some(newValue)) if oldValue != newValue =>
        ua.remove(AddressOfSubcontractorPage)
          .flatMap(_.remove(NationalInsuranceNumberYesNoPage))
          .flatMap(_.remove(SubAddressYesNoPage))
          .flatMap(_.remove(SubContactDetailsPage))
          .flatMap(_.remove(SubcontractorContactDetailsYesNoPage))
          .flatMap(_.remove(SubcontractorNamePage))
          .flatMap(_.remove(SubcontractorsUniqueTaxpayerReferencePage))
          .flatMap(_.remove(SubNationalInsuranceNumberPage))
          .flatMap(_.remove(SubTradingNameYesNoPage))
          .flatMap(_.remove(TradingNameOfSubcontractorPage))
          .flatMap(_.remove(UniqueTaxpayerReferenceYesNoPage))
          .flatMap(_.remove(WorksReferenceNumberPage))
          .flatMap(_.remove(WorksReferenceNumberYesNoPage))
            // add any new page if added in future
      case _ =>
        super.cleanup(value, ua)
    }
  }
}

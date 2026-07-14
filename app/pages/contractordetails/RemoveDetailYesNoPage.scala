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

package pages.contractordetails

import models.UserAnswers
import pages.QuestionPage
import play.api.libs.json.JsPath

case class RemoveDetailYesNoPage(contractorDetail: String) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ "contractordetails" \ toString

  override def toString: String = s"removeDetailYesNo-$contractorDetail"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers) =
    contractorDetail match {
      case "email-address" if value.contains(true) =>
        userAnswers
          .remove(EnterContractorEmailAddressPage)
          .flatMap(_.set(AddEmailAddressYesNoPage, false))

      case "scheme-name" if value.contains(true) =>
        userAnswers
          .remove(SchemeNamePage)
          .flatMap(_.set(AddSchemeNameYesNoPage, false))

      case _ =>
        super.cleanup(value, userAnswers)
    }
}

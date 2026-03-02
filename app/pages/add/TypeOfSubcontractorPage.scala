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
import models.add.TypeOfSubcontractor.*
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case object TypeOfSubcontractorPage extends QuestionPage[TypeOfSubcontractor] with Cleanup {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "typeOfSubcontractor"

  override def cleanup(value: Option[TypeOfSubcontractor], ua: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(Individualorsoletrader) =>
        removePartnershipSubcontractor(ua)
          .flatMap(removeLimitedCompanySubcontractor)
//          .flatMap(removeTrustSubcontractor)

      case Some(Limitedcompany) =>
        removeIndividualSoleTraderSubcontractor(ua)
          .flatMap(removePartnershipSubcontractor)
//          .flatMap(removeTrustSubcontractor)

      case Some(Partnership) =>
        removeIndividualSoleTraderSubcontractor(ua)
          .flatMap(removeLimitedCompanySubcontractor)
//          .flatMap(removeTrustSubcontractor)

//      case Some(Trust) =>
//        removeIndividualSoleTraderSubcontractor(ua)
//          .flatMap(removeLimitedCompanySubcontractor)
//          .flatMap(removePartnershipSubcontractor)

      case _ =>
        super.cleanup(value, ua)
    }
}

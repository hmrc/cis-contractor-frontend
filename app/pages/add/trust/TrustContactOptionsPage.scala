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

package pages.add.trust

import models.UserAnswers
import models.add.trust.TrustContactOptions
import models.contact.ContactOptions.{Email, Mobile, NoDetails, Phone}
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case object TrustContactOptionsPage extends QuestionPage[TrustContactOptions] with TrustJourney {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "trustContactOptions"

  override def cleanup(value: Option[TrustContactOptions], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(Email)     =>
        for {
          ua <- userAnswers.remove(TrustPhoneNumberPage)
          ua <- ua.remove(TrustMobileNumberPage)
        } yield ua
      case Some(Phone)     =>
        for {
          ua <- userAnswers.remove(TrustEmailAddressPage)
          ua <- ua.remove(TrustMobileNumberPage)
        } yield ua
      case Some(Mobile)    =>
        for {
          ua <- userAnswers.remove(TrustEmailAddressPage)
          ua <- ua.remove(TrustPhoneNumberPage)
        } yield ua
      case Some(NoDetails) =>
        for {
          ua <- userAnswers.remove(TrustEmailAddressPage)
          ua <- ua.remove(TrustPhoneNumberPage)
          ua <- ua.remove(TrustMobileNumberPage)
        } yield ua
      case _               => super.cleanup(value, userAnswers)
    }

}

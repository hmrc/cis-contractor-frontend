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
import models.add.trust.TrustContactMethodOptions
import models.contact.ContactMethodOptions.{Email, Mobile, Phone}
import pages.QuestionPage
import pages.add.ContactMethodOptionsCleanup
import play.api.libs.json.JsPath

import scala.util.Try

case object TrustContactMethodOptionsPage
    extends QuestionPage[Set[TrustContactMethodOptions]]
    with TrustJourney
    with ContactMethodOptionsCleanup {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "trustContactMethodOptions"

  override def cleanup(value: Option[Set[TrustContactMethodOptions]], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {

      case Some(selectedAnswers) =>
        removeIfNotSelected(selectedAnswers, Email, TrustEmailAddressPage, userAnswers)
          .flatMap(removeIfNotSelected(selectedAnswers, Phone, TrustPhoneNumberPage, _))
          .flatMap(removeIfNotSelected(selectedAnswers, Mobile, TrustMobileNumberPage, _))

      case _ => super.cleanup(value, userAnswers)
    }
}

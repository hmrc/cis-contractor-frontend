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

package pages.add

import models.UserAnswers
import pages.QuestionPage
import play.api.libs.json.JsPath
import models.add.IndividualContactMethodOptions
import models.contact.ContactMethodOptions.{Email, Mobile, Phone}

import scala.util.{Success, Try}

case object IndividualContactMethodOptionsPage
    extends QuestionPage[Set[IndividualContactMethodOptions]]
    with IndividualJourney {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "individualContactMethodOptions"

  override def cleanup(value: Option[Set[IndividualContactMethodOptions]], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(selectedAnswers) =>
        removeIfNotSelected(selectedAnswers, Email, IndividualEmailAddressPage, userAnswers)
          .flatMap(removeIfNotSelected(selectedAnswers, Phone, IndividualPhoneNumberPage, _))
          .flatMap(removeIfNotSelected(selectedAnswers, Mobile, IndividualMobileNumberPage, _))
      case _                     =>
        super.cleanup(value, userAnswers)
    }

  private def removeIfNotSelected(
    selectedAnswers: Set[IndividualContactMethodOptions],
    answer: IndividualContactMethodOptions,
    page: QuestionPage[String],
    userAnswers: UserAnswers
  ): Try[UserAnswers] =
    if (selectedAnswers.contains(answer)) {
      Success(userAnswers)
    } else {
      userAnswers.remove(page)
    }

}

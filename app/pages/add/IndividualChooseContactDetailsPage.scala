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
import models.add.IndividualChooseContactDetails
import models.contact.ContactOptions
import pages.QuestionPage
import play.api.libs.json.JsPath
import scala.util.{Success, Try}

case object IndividualChooseContactDetailsPage
    extends QuestionPage[IndividualChooseContactDetails]
    with IndividualJourney {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "individualChooseContactDetails"

  override def cleanup(
    value: Option[IndividualChooseContactDetails],
    userAnswers: UserAnswers
  ): Try[UserAnswers] =
    value match {

      case Some(ContactOptions.Email) =>
        for {
          ua1 <- userAnswers.remove(IndividualPhoneNumberPage)
          ua2 <- ua1.remove(IndividualMobileNumberPage)
        } yield ua2

      case Some(ContactOptions.Phone) =>
        for {
          ua1 <- userAnswers.remove(IndividualEmailAddressPage)
          ua2 <- ua1.remove(IndividualMobileNumberPage)
        } yield ua2

      case Some(ContactOptions.Mobile) =>
        for {
          ua1 <- userAnswers.remove(IndividualEmailAddressPage)
          ua2 <- ua1.remove(IndividualPhoneNumberPage)
        } yield ua2

      case Some(ContactOptions.NoDetails) =>
        for {
          ua1 <- userAnswers.remove(IndividualEmailAddressPage)
          ua2 <- ua1.remove(IndividualPhoneNumberPage)
          ua3 <- ua2.remove(IndividualMobileNumberPage)
        } yield ua3

      case None =>
        Success(userAnswers)
    }
}

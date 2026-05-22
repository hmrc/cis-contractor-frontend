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

package utils

import models.UserAnswers
import models.verify.ContractorEmailConfirmationStored.{CurrentEmail, DifferentEmail, DoNotSend}
import pages.verify.{ContractorEmailConfirmationNotStoredPage, ContractorEmailConfirmationStoredPage, EmailAddressPage, NewestVerificationBatchResponsePage}

object VerifyEmailResolver {

  /** Returns the email address to use for the verification confirmation, based on the user's journey answers.
    *
    *   - Stored-email journey: CurrentEmail → scheme email; DifferentEmail → user-entered email; DoNotSend → None
    *   - No-email journey: Yes → user-entered email; No → None
    */
  def resolvedEmail(answers: UserAnswers): Option[String] =
    answers.get(ContractorEmailConfirmationStoredPage) match {
      case Some(CurrentEmail)   =>
        answers.get(NewestVerificationBatchResponsePage).flatMap(_.scheme).flatMap(_.emailAddress)
      case Some(DifferentEmail) =>
        answers.get(EmailAddressPage)
      case Some(DoNotSend)      =>
        None
      case None                 =>
        answers.get(ContractorEmailConfirmationNotStoredPage) match {
          case Some(true)  => answers.get(EmailAddressPage)
          case Some(false) => None
          case None        => None
        }
    }
}

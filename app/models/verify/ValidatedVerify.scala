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

package models.verify

import models.verify.ContractorEmailConfirmationStored.{CurrentEmail, DifferentEmail, DoNotSend}
import models.{InvalidAnswer, MissingAnswer, SubcontractorViewModel, UserAnswers, Validation, ValidationError}
import pages.verify.*

final case class ValidatedVerify(
  selectedSubcontractors: Set[SubcontractorViewModel],
  subcontractorsToReverify: Option[Set[SelectedSubcontractors]],
  emailToUse: Option[String]
)

object ValidatedVerify extends Validation {

  def build(answers: UserAnswers): Either[ValidationError, ValidatedVerify] = {
    val selectedSubcontractors: Set[SubcontractorViewModel] =
      answers.get(SelectSubcontractorPage).getOrElse(Set.empty)

    for {
      subcontractorsToReverify <- getOptionalPageAndQuestionValue(
                                    answers,
                                    SelectSubcontractorsToReverifyPage,
                                    ReverifyExistingSubcontractorsYesNoPage
                                  )

      _ <- Either.cond(
             selectedSubcontractors.nonEmpty || subcontractorsToReverify.exists(_.nonEmpty),
             (),
             InvalidAnswer(SelectSubcontractorPage)
           )

      emailToUse <- resolveEmail(answers)

      _ <- Either.cond(
             answers.get(VerificationBatchReadinessPage).contains(true),
             (),
             MissingAnswer(VerificationBatchReadinessPage)
           )
    } yield ValidatedVerify(selectedSubcontractors, subcontractorsToReverify, emailToUse)
  }

  private def resolveEmail(answers: UserAnswers): Either[ValidationError, Option[String]] =
    answers.get(ContractorEmailConfirmationStoredPage) match {
      case Some(CurrentEmail)   =>
        answers
          .get(NewestVerificationBatchResponsePage)
          .flatMap(_.scheme)
          .flatMap(_.emailAddress)
          .toRight(MissingAnswer(ContractorEmailConfirmationStoredPage))
          .map(Some(_))
      case Some(DifferentEmail) =>
        answers
          .get(EmailAddressPage)
          .toRight(MissingAnswer(EmailAddressPage))
          .map(Some(_))
      case Some(DoNotSend)      =>
        Right(None)
      case None                 =>
        answers.get(ContractorEmailConfirmationNotStoredPage) match {
          case Some(true)  => answers.get(EmailAddressPage).toRight(MissingAnswer(EmailAddressPage)).map(Some(_))
          case Some(false) => Right(None)
          case None        => Left(MissingAnswer(ContractorEmailConfirmationStoredPage))
        }
    }
}

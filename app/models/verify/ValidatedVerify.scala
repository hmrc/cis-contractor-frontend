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

import forms.mappings.Constants.MaxLength254
import models.response.GetNewestVerificationBatchResponse
import models.verify.ContractorEmailConfirmationStored.{CurrentEmail, DifferentEmail, DoNotSend}
import models.{InvalidAnswer, MissingAnswer, SubcontractorViewModel, UserAnswers, Validation, ValidationError}
import pages.QuestionPage
import pages.verify.*

final case class ValidatedVerify(
  selectedSubcontractors: Set[SubcontractorViewModel],
  subcontractorsToReverify: Option[Set[SelectedSubcontractors]],
  emailToUse: Option[String]
)

object ValidatedVerify extends Validation {

  def build(answers: UserAnswers): Either[ValidationError, ValidatedVerify] =
    build(
      answers,
      SelectSubcontractorPage,
      SelectSubcontractorsToReverifyPage,
      VerifyYourSubcontractorsYesNoPage,
      ReverifyExistingSubcontractorsYesNoPage,
      VerificationBatchReadinessPage,
      ContractorEmailConfirmationStoredPage,
      ContractorEmailConfirmationNotStoredPage,
      NewestVerificationBatchResponsePage,
      EmailAddressPage
    )

  def build(answers: UserAnswers, id: String): Either[ValidationError, ValidatedVerify] =
    build(
      answers,
      SelectSubcontractorPage(id),
      SelectSubcontractorsToReverifyPage(id),
      VerifyYourSubcontractorsYesNoPage(id),
      ReverifyExistingSubcontractorsYesNoPage(id),
      VerificationBatchReadinessPage(id),
      ContractorEmailConfirmationStoredPage(id),
      ContractorEmailConfirmationNotStoredPage(id),
      NewestVerificationBatchResponsePage(id),
      EmailAddressPage(id)
    )

  private def build(
    answers: UserAnswers,
    selectPage: QuestionPage[Set[SubcontractorViewModel]],
    reverifyPage: QuestionPage[Set[SelectedSubcontractors]],
    yesNoPage: QuestionPage[Boolean],
    reverifyYesNoPage: QuestionPage[Boolean],
    readinessPage: QuestionPage[Boolean],
    storedPage: QuestionPage[ContractorEmailConfirmationStored],
    notStoredPage: QuestionPage[Boolean],
    newestPage: QuestionPage[GetNewestVerificationBatchResponse],
    emailPage: QuestionPage[String]
  ): Either[ValidationError, ValidatedVerify] = {
    val selectedSubcontractors: Set[SubcontractorViewModel] =
      answers.get(selectPage).getOrElse(Set.empty)

    for {
      subcontractorsToReverify <- getOptionalPageAndQuestionValue(
                                    answers,
                                    reverifyPage,
                                    answers.get(yesNoPage).map(_ => yesNoPage).getOrElse(reverifyYesNoPage)
                                  )

      _ <- Either.cond(
             selectedSubcontractors.nonEmpty || subcontractorsToReverify.exists(_.nonEmpty),
             (),
             InvalidAnswer(selectPage)
           )

      emailToUse <- resolveEmail(answers, storedPage, notStoredPage, newestPage, emailPage)

      _ <- Either.cond(
             answers.get(readinessPage).contains(true),
             (),
             MissingAnswer(readinessPage)
           )
    } yield ValidatedVerify(selectedSubcontractors, subcontractorsToReverify, emailToUse)
  }

  private def resolveEmail(
    answers: UserAnswers,
    storedPage: QuestionPage[ContractorEmailConfirmationStored],
    notStoredPage: QuestionPage[Boolean],
    newestPage: QuestionPage[GetNewestVerificationBatchResponse],
    emailPage: QuestionPage[String]
  ): Either[ValidationError, Option[String]] =
    answers.get(storedPage) match {
      case Some(CurrentEmail)   =>
        answers
          .get(newestPage)
          .flatMap(_.scheme)
          .flatMap(_.emailAddress)
          .toRight(MissingAnswer(storedPage))
          .flatMap { email =>
            Either.cond(
              isValidEmail(email),
              Some(email),
              InvalidAnswer(storedPage)
            )
          }
      case Some(DifferentEmail) =>
        validateEnteredEmail(answers, emailPage)
      case Some(DoNotSend)      =>
        Right(None)
      case None                 =>
        answers.get(notStoredPage) match {
          case Some(true)  => validateEnteredEmail(answers, emailPage)
          case Some(false) => Right(None)
          case None        => Left(MissingAnswer(storedPage))
        }
    }

  private def validateEnteredEmail(
    answers: UserAnswers,
    emailPage: QuestionPage[String]
  ): Either[ValidationError, Option[String]] =
    answers
      .get(emailPage)
      .toRight(MissingAnswer(emailPage))
      .flatMap { email =>
        Either.cond(
          isValidEmail(email),
          Some(email),
          InvalidAnswer(emailPage)
        )
      }

  private def isValidEmail(email: String): Boolean =
    email.length <= MaxLength254 && email.matches(forms.Validation.emailRegex)
}

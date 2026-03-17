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

package models

import pages.QuestionPage
import play.api.libs.json.Reads

trait Validation {
  def getPageValue[A](
    answers: UserAnswers,
    questionPage: QuestionPage[A]
  )(implicit reads: Reads[A]): Either[ValidationError, A] =
    answers.get(questionPage) match {
      case Some(value) => Right(value)
      case None        => Left(MissingAnswer(questionPage))
    }

  def getOptionalPageValue[A](
    answers: UserAnswers,
    questionPage: QuestionPage[A],
    yesNoPage: QuestionPage[Boolean]
  )(implicit reads: Reads[A]): Either[ValidationError, Option[A]] =
    (answers.get(questionPage), answers.get(yesNoPage)) match {
      case (_, None)                 => Left(MissingAnswer(yesNoPage))
      case (Some(value), Some(true)) => Right(Some(value))
      case (None, Some(false))       => Right(None)
      case _                         => Left(InvalidAnswer(questionPage))
    }

}

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

import models.{AmendMode, Mode, UserAnswers}
import pages.add.company.CompanyNamePage
import pages.add.{SubcontractorNamePage, TradingNameOfSubcontractorPage}
import play.api.i18n.Messages

class SubcontractorNameExtractor {

  def getSubcontractorName(userAnswers: UserAnswers): Option[String] =
    userAnswers
      .get(SubcontractorNamePage)
      .flatMap { name =>
        val firstName = name.firstName.trim
        val lastName  = name.lastName.trim

        (firstName.nonEmpty, lastName.nonEmpty) match {
          case (true, true)  => Some(s"$firstName $lastName")
          case (false, true) => Some(lastName)
          case _             => None
        }
      }
      .orElse(userAnswers.get(TradingNameOfSubcontractorPage).map(_.trim).filter(_.nonEmpty))

  def getSubcontractorName(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages): Option[String] =
    getSubcontractorName(userAnswers)
      .orElse(Option.when(mode == AmendMode)(messages("verify.noName")))

  def displaySubcontractorName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    getSubcontractorName(userAnswers)
      .getOrElse(messages("verify.noName"))

  def getCompanyName(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages): Option[String] =
    userAnswers.get(CompanyNamePage).map(_.trim).filter(_.nonEmpty).orElse {
      if (mode == AmendMode) {
        Some(messages("verify.noName"))
      } else {
        None
      }
    }
}

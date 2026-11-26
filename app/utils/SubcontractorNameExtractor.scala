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

import models.add.SubcontractorName
import models.{AmendMode, FinalValidationMode, JourneyNamespace, Mode, UserAnswers}
import pages.QuestionPage
import pages.add.company.CompanyNamePage
import pages.add.{SubcontractorNamePage, TradingNameOfSubcontractorPage}
import play.api.i18n.Messages

class SubcontractorNameExtractor {

  def getSubcontractorName(userAnswers: UserAnswers): Option[String] =
    getSubcontractorName(
      userAnswers,
      SubcontractorNamePage,
      TradingNameOfSubcontractorPage
    )

  // Multi-tab
  def getSubcontractorName(
    userAnswers: UserAnswers,
    id: String,
    namespace: JourneyNamespace
  ): Option[String] =
    getSubcontractorName(
      userAnswers,
      SubcontractorNamePage(id, namespace),
      TradingNameOfSubcontractorPage(id, namespace)
    )

  private def getSubcontractorName(
    userAnswers: UserAnswers,
    namePage: QuestionPage[SubcontractorName],
    tradingNamePage: QuestionPage[String]
  ): Option[String] =
    userAnswers
      .get(namePage)
      .flatMap { name =>
        val firstName = name.firstName.trim
        val lastName  = name.lastName.trim

        (firstName.nonEmpty, lastName.nonEmpty) match {
          case (true, true)  => Some(s"$firstName $lastName")
          case (false, true) => Some(lastName)
          case _             => None
        }
      }
      .orElse(userAnswers.get(tradingNamePage).map(_.trim).filter(_.nonEmpty))

  def getSubcontractorName(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages): Option[String] =
    withMissingNameFallback(getSubcontractorName(userAnswers), mode)

  // Multi-tab
  def getSubcontractorName(
    userAnswers: UserAnswers,
    mode: Mode,
    id: String,
    namespace: JourneyNamespace
  )(implicit messages: Messages): Option[String] =
    withMissingNameFallback(getSubcontractorName(userAnswers, id, namespace), mode)

  def displaySubcontractorName(userAnswers: UserAnswers)(implicit messages: Messages): String =
    getSubcontractorName(userAnswers)
      .getOrElse(messages("verify.noName"))

  // Multi-tab
  def displaySubcontractorName(
    userAnswers: UserAnswers,
    id: String,
    namespace: JourneyNamespace
  )(implicit messages: Messages): String =
    getSubcontractorName(userAnswers, id, namespace)
      .getOrElse(messages("verify.noName"))

  def getCompanyName(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages): Option[String] =
    getCompanyName(userAnswers, mode, CompanyNamePage)

  def getCompanyName(
    userAnswers: UserAnswers,
    mode: Mode,
    id: String,
    namespace: JourneyNamespace
  )(implicit messages: Messages): Option[String] =
    getCompanyName(userAnswers, mode, CompanyNamePage(id, namespace))

  private def getCompanyName(
    userAnswers: UserAnswers,
    mode: Mode,
    page: QuestionPage[String]
  )(implicit messages: Messages): Option[String] =
    withMissingNameFallback(
      userAnswers.get(page).map(_.trim).filter(_.nonEmpty),
      mode
    )

  private def withMissingNameFallback(
    name: Option[String],
    mode: Mode
  )(implicit messages: Messages): Option[String] =
    name.orElse(
      Option.when(mode == AmendMode || mode == FinalValidationMode)(
        messages("verify.noName")
      )
    )
}

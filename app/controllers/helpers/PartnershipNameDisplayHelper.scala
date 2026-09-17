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

package controllers.helpers

import models.{AmendMode, Mode, UserAnswers}
import pages.add.partnership.{PartnershipNamePage, PartnershipNominatedPartnerNamePage}
import play.api.i18n.Messages

object PartnershipNameDisplayHelper {

  def getDisplayName(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages): Option[String] =
    userAnswers.get(PartnershipNamePage).map(_.trim).filter(_.nonEmpty).orElse {
      if (mode == AmendMode) {
        Some(messages("partnershipName.noNameProvided"))
      } else {
        None
      }
    }

  def getPartnerDisplayName(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages): Option[String] =
    userAnswers.get(PartnershipNominatedPartnerNamePage).map(_.trim).filter(_.nonEmpty).orElse {
      if (mode == AmendMode) {
        Some(messages("partnershipName.noNameProvided"))
      } else {
        None
      }
    }

  def displayName(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages): String =
    userAnswers
      .get(PartnershipNamePage)
      .map(_.trim)
      .filter(_.nonEmpty)
      .orElse(userAnswers.get(PartnershipNominatedPartnerNamePage).map(_.trim).filter(_.nonEmpty))
      .getOrElse {
        if (mode == AmendMode) messages("partnershipName.noNameProvided") else ""
      }
}

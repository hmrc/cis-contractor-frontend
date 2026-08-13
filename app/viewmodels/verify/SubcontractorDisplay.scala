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

package viewmodels.verify

import models.SubcontractorCurrentVerification
import play.api.i18n.Messages

object SubcontractorDisplay {

  def displayName(sub: SubcontractorCurrentVerification)(implicit messages: Messages): String =
    nameFor(sub).getOrElse(messages("verify.noName"))

  def nameFor(sub: SubcontractorCurrentVerification): Option[String] = {
    val first              = sub.firstName.map(_.trim).filter(_.nonEmpty)
    val surname            = sub.surname.map(_.trim).filter(_.nonEmpty)
    val trading            = sub.tradingName.map(_.trim).filter(_.nonEmpty)
    val partnershipTrading = sub.partnershipTradingName.map(_.trim).filter(_.nonEmpty)

    val individualName = surname.map { s =>
      first.map(f => s"$s, $f").getOrElse(s)
    }

    partnershipTrading.orElse(trading).orElse(individualName)
  }

  def utrDisplay(sub: SubcontractorCurrentVerification, noneProvidedKey: String)(implicit messages: Messages): String =
    sub.utr
      .map(_.trim)
      .filter(_.nonEmpty)
      .getOrElse(messages(noneProvidedKey))
}

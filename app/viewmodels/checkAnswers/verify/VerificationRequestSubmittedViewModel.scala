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

package viewmodels.checkAnswers.verify
import config.FrontendAppConfig
import models.UserAnswers
import pages.verify.*
import queries.CisIdQuery

import java.time.LocalDateTime

case class VerificationRequestSubmittedViewModel(
  manageSubcontractorsUrl: String,
  referenceNumber: String,
  submittedAt: LocalDateTime,
  subcontractorsToVerify: Seq[String] = Seq.empty,
  subcontractorsToReverify: Seq[String] = Seq.empty,
  confirmationEmail: Option[String] = None
) {
  val showEmail: Boolean    = confirmationEmail.isDefined
  val showVerify: Boolean   = subcontractorsToVerify.nonEmpty
  val showReverify: Boolean = subcontractorsToReverify.nonEmpty
}
object VerificationRequestSubmittedViewModel {

  private def namesFrom[A](maybe: Option[Iterable[A]])(name: A => String): Seq[String] =
    maybe.fold(Seq.empty)(_.map(name).toSeq)

  def fromUserAnswers(userAnswers: UserAnswers, appConfig: FrontendAppConfig): VerificationRequestSubmittedViewModel =
    val cisId = userAnswers.get(CisIdQuery) match {
      case Some(cisId) => cisId
      case _           =>
        throw new IllegalStateException("[VerificationRequestSubmittedViewModel] cisId missing from userAnswers")
    }
    VerificationRequestSubmittedViewModel(
      manageSubcontractorsUrl = s"${appConfig.manageSubcontractorsUrl}/$cisId",
      // TODO: Replace below with actuals - 1. referenceNumber 2. submittedAt
      referenceNumber = "Reference Number 12345",
      submittedAt = LocalDateTime.now(),
      subcontractorsToVerify = namesFrom(userAnswers.get(SelectSubcontractorPage))(_.name).sorted,
      subcontractorsToReverify = namesFrom(userAnswers.get(SelectSubcontractorsToReverifyPage))(_.name).sorted,
      confirmationEmail = userAnswers.get(EmailAddressPage)
    )
}

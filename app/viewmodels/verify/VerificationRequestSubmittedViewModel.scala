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

import config.FrontendAppConfig
import models.UserAnswers
import pages.verify.*
import queries.{CisIdQuery, VerifyCisIdQuery}
import utils.VerifyEmailResolver.resolvedEmail

import java.time.LocalDateTime

case class VerificationRequestSubmittedViewModel(
  manageSubcontractorsUrl: String,
  verificationHistoryUrl: String,
  referenceNumber: Option[String],
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
    maybe.fold(Seq.empty)(_.toSeq.map(name).sortBy(_.toLowerCase))

  def fromUserAnswers(
    userAnswers: UserAnswers,
    appConfig: FrontendAppConfig,
    subJourneyId: Option[String] = None
  ): VerificationRequestSubmittedViewModel = {

    val cisId = subJourneyId
      .map(id => userAnswers.get(VerifyCisIdQuery(id)))
      .getOrElse(userAnswers.get(CisIdQuery))
      .getOrElse(
        throw new IllegalStateException("[VerificationRequestSubmittedViewModel] cisId missing from userAnswers")
      )

    val newestPage   =
      subJourneyId.map(NewestVerificationBatchResponsePage.apply).getOrElse(NewestVerificationBatchResponsePage)
    val selectPage   = subJourneyId.map(SelectSubcontractorPage.apply).getOrElse(SelectSubcontractorPage)
    val reverifyPage =
      subJourneyId.map(SelectSubcontractorsToReverifyPage.apply).getOrElse(SelectSubcontractorsToReverifyPage)

    val backendResponse =
      userAnswers.get(newestPage)

    val newestBatch =
      backendResponse.getOrElse(
        throw new IllegalStateException(
          "[VerificationRequestSubmittedViewModel] NewestVerificationBatchResponsePage missing"
        )
      )

    val referenceNumber: Option[String] =
      newestBatch.verificationBatch
        .flatMap(_.verificationNumber)

    val submittedAt: LocalDateTime =
      newestBatch.submission
        .flatMap(_.submissionRequestDate)
        .getOrElse(
          throw new IllegalStateException(
            "[VerificationRequestSubmittedViewModel] submissionRequestDate missing"
          )
        )

    VerificationRequestSubmittedViewModel(
      manageSubcontractorsUrl = s"${appConfig.manageSubcontractorsUrl}/$cisId",
      verificationHistoryUrl = appConfig.verificationHistoryUrl,
      referenceNumber = referenceNumber,
      submittedAt = submittedAt,
      subcontractorsToVerify = namesFrom(userAnswers.get(selectPage))(_.name),
      subcontractorsToReverify = namesFrom(userAnswers.get(reverifyPage))(_.name),
      confirmationEmail = resolvedEmail(userAnswers, subJourneyId)
    )
  }
}

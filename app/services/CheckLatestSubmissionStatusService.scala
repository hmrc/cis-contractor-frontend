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

package services

import models.verify.VerificationBatchStatus
import models.verify.VerificationBatchStatus.*

sealed trait SubmissionStatusCheckResult

object SubmissionStatusCheckResult {

  case object Continue extends SubmissionStatusCheckResult

  case object ShowPendingVerificationWarning extends SubmissionStatusCheckResult
}

object CheckLatestSubmissionStatusService {

  def check(status: Option[VerificationBatchStatus]): SubmissionStatusCheckResult =
    status match {
      case None | Some(Started) | Some(Validated) =>
        SubmissionStatusCheckResult.Continue
      case Some(Pending) | Some(Accepted)         =>
        SubmissionStatusCheckResult.ShowPendingVerificationWarning
    }
}

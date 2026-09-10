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

import models.VerificationCurrentVerification
import models.response.GetCurrentVerificationBatchResponse

// F35 - UM - Check unmatched batch readiness.
object UnmatchedBatchReadiness {

  def isBatchReady(batch: GetCurrentVerificationBatchResponse): Boolean =
    batch.subcontractors.nonEmpty &&
      batch.subcontractors.forall { sub =>
        isVerificationReady(
          batch.verifications.find(_.subcontractorId.contains(sub.subcontractorId))
        )
      }

  // Ready when the verification has been edited (ACTION_INDICATOR = 'edit') or marked to proceed (PROCEED = 'Y').
  def isVerificationReady(verification: Option[VerificationCurrentVerification]): Boolean =
    verification.exists { v =>
      normalise(v.actionIndicator).contains("EDIT") ||
      normalise(v.proceed).contains("Y")
    }

  private def normalise(value: Option[String]): Option[String] =
    value.map(_.trim.toUpperCase).filter(_.nonEmpty)
}

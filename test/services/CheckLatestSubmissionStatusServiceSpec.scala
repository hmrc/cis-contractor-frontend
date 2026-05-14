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

import base.SpecBase
import services.SubmissionStatusCheckResult.*
import models.verify.VerificationBatchStatus.*

class CheckLatestSubmissionStatusServiceSpec extends SpecBase {

  private val service = new CheckLatestSubmissionStatusService()

  "CheckLatestSubmissionStatusService" - {

    "must return Continue when status is not present" in {

      service.check(None) mustEqual Continue
    }

    "must return Continue when status is STARTED" in {

      service.check(Some("STARTED")) mustEqual Continue
    }

    "must return Continue when status is VALIDATED" in {

      service.check(Some("VALIDATED")) mustEqual Continue
    }

    "must return ShowPendingVerificationWarning when status is PENDING" in {

      service.check(Some("PENDING")) mustEqual ShowPendingVerificationWarning
    }

    "must return ShowPendingVerificationWarning when status is ACCEPTED" in {

      service.check(Some("ACCEPTED")) mustEqual ShowPendingVerificationWarning
    }

    "must return Continue when status is unknown" in {

      service.check(Some("UNKNOWN")) mustEqual Continue
    }

    "must return Continue when status is empty" in {

      service.check(Some("")) mustEqual Continue
    }
  }
}

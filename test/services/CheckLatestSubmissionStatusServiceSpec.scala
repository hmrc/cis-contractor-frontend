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
import models.verify.VerificationBatchStatus.*
import services.SubmissionStatusCheckResult.*

class CheckLatestSubmissionStatusServiceSpec extends SpecBase {

  "CheckLatestSubmissionStatusService.check" - {

    "must return Continue when status is not present" in {

      CheckLatestSubmissionStatusService.check(None) mustEqual Continue
    }

    "must return Continue when status is Started" in {

      CheckLatestSubmissionStatusService.check(Some(Started)) mustEqual Continue
    }

    "must return Continue when status is Validated" in {

      CheckLatestSubmissionStatusService.check(Some(Validated)) mustEqual Continue
    }

    "must return ShowPendingVerificationWarning when status is Pending" in {

      CheckLatestSubmissionStatusService.check(Some(Pending)) mustEqual ShowPendingVerificationWarning
    }

    "must return ShowPendingVerificationWarning when status is Accepted" in {

      CheckLatestSubmissionStatusService.check(Some(Accepted)) mustEqual ShowPendingVerificationWarning
    }
  }
}

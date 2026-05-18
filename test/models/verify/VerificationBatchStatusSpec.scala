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

import base.SpecBase
import models.verify.VerificationBatchStatus.*

class VerificationBatchStatusSpec extends SpecBase {

  "VerificationBatchStatus.from" - {

    "must return Started when value is STARTED" in {

      VerificationBatchStatus.from("STARTED") mustEqual Some(Started)
    }

    "must return Validated when value is VALIDATED" in {

      VerificationBatchStatus.from("VALIDATED") mustEqual Some(Validated)
    }

    "must return Pending when value is PENDING" in {

      VerificationBatchStatus.from("PENDING") mustEqual Some(Pending)
    }

    "must return Accepted when value is ACCEPTED" in {

      VerificationBatchStatus.from("ACCEPTED") mustEqual Some(Accepted)
    }

    "must return None when value is unknown" in {

      VerificationBatchStatus.from("UNKNOWN") mustEqual None
    }

    "must return None when value is empty" in {

      VerificationBatchStatus.from("") mustEqual None
    }
  }
}

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

package controllers.amend

import base.SpecBase
import controllers.amend.AmendControllerUtils.*
import models.response.SubcontractorResponse
import pages.amend.ShowVerificationDetailsPage

class AmendControllerUtilsSpec extends SpecBase {

  private val subcontractor =
    SubcontractorResponse(
      subcontractorId = 1L,
      utr = None,
      pageVisited = None,
      partnerUtr = None,
      crn = None,
      firstName = None,
      nino = None,
      secondName = None,
      surname = None,
      partnershipTradingName = None,
      tradingName = None,
      subcontractorType = Some("company"),
      addressLine1 = None,
      addressLine2 = None,
      addressLine3 = None,
      addressLine4 = None,
      country = None,
      postcode = None,
      emailAddress = None,
      phoneNumber = None,
      mobilePhoneNumber = None,
      worksReferenceNumber = None,
      createDate = None,
      lastUpdate = None,
      subbieResourceRef = Some(1001L),
      matched = None,
      autoVerified = None,
      verified = None,
      verificationNumber = None,
      taxTreatment = None,
      verificationDate = None,
      version = None,
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = None
    )

  "AmendControllerUtils" - {

    "setOptional" - {

      "must set the page when a value is present" in {
        val result =
          setOptional(
            userAnswers = emptyUserAnswers,
            page = ShowVerificationDetailsPage,
            value = Some(true)
          ).success.value

        result
          .get(ShowVerificationDetailsPage)
          .value mustBe true
      }

      "must return the unchanged UserAnswers when the value is absent" in {
        val originalAnswers =
          emptyUserAnswers

        val result =
          setOptional(
            userAnswers = originalAnswers,
            page = ShowVerificationDetailsPage,
            value = None
          ).success.value

        result mustBe originalAnswers

        result
          .get(ShowVerificationDetailsPage) mustBe None
      }

      "must preserve an existing answer when the new optional value is absent" in {
        val existingAnswers =
          emptyUserAnswers
            .set(
              ShowVerificationDetailsPage,
              true
            )
            .success
            .value

        val result =
          setOptional(
            userAnswers = existingAnswers,
            page = ShowVerificationDetailsPage,
            value = None
          ).success.value

        result
          .get(ShowVerificationDetailsPage)
          .value mustBe true
      }
    }

    "isExpectedSubcontractorType" - {

      "must return true when the subcontractor type matches" in {
        isExpectedSubcontractorType(
          subcontractor = subcontractor.copy(
            subcontractorType = Some("company")
          ),
          expectedType = "company"
        ) mustBe true
      }

      "must ignore the subcontractor type case" in {
        isExpectedSubcontractorType(
          subcontractor = subcontractor.copy(
            subcontractorType = Some("COMPANY")
          ),
          expectedType = "company"
        ) mustBe true
      }

      "must ignore surrounding whitespace in the subcontractor type" in {
        isExpectedSubcontractorType(
          subcontractor = subcontractor.copy(
            subcontractorType = Some("  company  ")
          ),
          expectedType = "company"
        ) mustBe true
      }

      "must ignore case and whitespace in the expected type" in {
        isExpectedSubcontractorType(
          subcontractor = subcontractor.copy(
            subcontractorType = Some("company")
          ),
          expectedType = "  COMPANY  "
        ) mustBe false
      }

      "must return false when the subcontractor type does not match" in {
        isExpectedSubcontractorType(
          subcontractor = subcontractor.copy(
            subcontractorType = Some("trust")
          ),
          expectedType = "company"
        ) mustBe false
      }

      "must return false when the subcontractor type is missing" in {
        isExpectedSubcontractorType(
          subcontractor = subcontractor.copy(
            subcontractorType = None
          ),
          expectedType = "company"
        ) mustBe false
      }

      "must return false when the subcontractor type is empty" in {
        isExpectedSubcontractorType(
          subcontractor = subcontractor.copy(
            subcontractorType = Some("")
          ),
          expectedType = "company"
        ) mustBe false
      }

      "must return false when the subcontractor type contains only whitespace" in {
        isExpectedSubcontractorType(
          subcontractor = subcontractor.copy(
            subcontractorType = Some("   ")
          ),
          expectedType = "company"
        ) mustBe false
      }
    }

    "shouldShowVerificationDetails" - {

      "must return true when the subcontractor is verified" in {
        shouldShowVerificationDetails(
          subcontractor.copy(
            verified = Some("Y"),
            pendingVerifications = Some(0)
          )
        ) mustBe true
      }

      "must ignore case and whitespace in the verified value" in {
        shouldShowVerificationDetails(
          subcontractor.copy(
            verified = Some("  y  "),
            pendingVerifications = Some(0)
          )
        ) mustBe true
      }

      "must return true when the subcontractor is unverified but has one pending verification" in {
        shouldShowVerificationDetails(
          subcontractor.copy(
            verified = Some("N"),
            pendingVerifications = Some(1)
          )
        ) mustBe true
      }

      "must return true when the subcontractor has multiple pending verifications" in {
        shouldShowVerificationDetails(
          subcontractor.copy(
            verified = Some("N"),
            pendingVerifications = Some(3)
          )
        ) mustBe true
      }

      "must return true when verified is missing but a pending verification exists" in {
        shouldShowVerificationDetails(
          subcontractor.copy(
            verified = None,
            pendingVerifications = Some(1)
          )
        ) mustBe true
      }

      "must return false when the subcontractor is unverified and has no pending verification" in {
        shouldShowVerificationDetails(
          subcontractor.copy(
            verified = Some("N"),
            pendingVerifications = Some(0)
          )
        ) mustBe false
      }

      "must return false when the subcontractor is unverified and pending verification is missing" in {
        shouldShowVerificationDetails(
          subcontractor.copy(
            verified = Some("N"),
            pendingVerifications = None
          )
        ) mustBe false
      }

      "must return false when verification information is missing" in {
        shouldShowVerificationDetails(
          subcontractor.copy(
            verified = None,
            pendingVerifications = None
          )
        ) mustBe false
      }

      "must return false when pending verifications is negative" in {
        shouldShowVerificationDetails(
          subcontractor.copy(
            verified = Some("N"),
            pendingVerifications = Some(-1)
          )
        ) mustBe false
      }

      "must return false for unsupported verified values when there is no pending verification" in {
        shouldShowVerificationDetails(
          subcontractor.copy(
            verified = Some("YES"),
            pendingVerifications = Some(0)
          )
        ) mustBe false
      }
    }
  }
}

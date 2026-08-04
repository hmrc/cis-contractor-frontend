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

package utils

import base.SpecBase
import models.amend.OriginalIndividualAnswers
import models.amend.company.OriginalCompanyAnswers
import models.amend.partnership.OriginalPartnershipAnswers
import models.amend.trust.OriginalTrustAnswers
import org.scalatest.TryValues.*
import pages.add.CheckYourAnswersSubmittedPage
import queries.{OriginalCompanyAnswersQuery, OriginalIndividualAnswersQuery, OriginalPartnershipAnswersQuery, OriginalTrustAnswersQuery}
import pages.amend.{AmendCheckYourAnswersSubmittedPage, ShowVerificationDetailsPage}

class DefaultSubcontractorCleanupServiceSpec extends SpecBase {

  private val service = new DefaultSubcontractorCleanupService

  private val originalIndividual =
    OriginalIndividualAnswers(
      usesTradingName = None,
      tradingName = None,
      subcontractorName = None,
      address = None,
      individualContactMethod = None,
      email = None,
      phone = None,
      mobile = None,
      utr = None,
      nino = None,
      worksReference = None
    )

  private val originalCompany =
    OriginalCompanyAnswers(
      companyName = Some("Company Ltd"),
      addressYesNo = None,
      address = None,
      companyContactMethodsYesNo = None,
      companyContactMethod = Set.empty,
      email = None,
      phone = None,
      mobile = None,
      crnYesNo = None,
      crn = None,
      utrYesNo = None,
      utr = None,
      worksReferenceYesNo = None,
      worksReference = None,
      verificationNumber = None
    )

  private val originalPartnership =
    OriginalPartnershipAnswers(
      partnershipName = Some("Partnership Ltd"),
      addressYesNo = None,
      address = None,
      partnershipContactMethodsYesNo = None,
      partnershipContactMethodOptions = None,
      email = None,
      phone = None,
      mobile = None,
      hasUtrYesNo = None,
      utr = None,
      nominatedPartnerName = None,
      nominatedPartnerUtrYesNo = None,
      nominatedPartnerUtr = None,
      nominatedPartnerNinoYesNo = None,
      nominatedPartnerNino = None,
      nominatedPartnerCrnYesNo = None,
      nominatedPartnerCrn = None,
      nominatedPartnerWorksReferenceYesNo = None,
      nominatedPartnerWorksReference = None
    )

  private val originalTrust =
    OriginalTrustAnswers(
      trustName = Some("Test Trust"),
      addressYesNo = None,
      address = None,
      trustContactMethodsYesNo = None,
      trustContactMethod = Set.empty,
      email = None,
      phone = None,
      mobile = None,
      utrYesNo = None,
      utr = None,
      worksReferenceYesNo = None,
      worksReference = None,
      verificationNumber = None
    )

  "DefaultSubcontractorCleanupService" - {

    "clean" - {

      "must reset CheckYourAnswersSubmittedPage to false" in {

        val userAnswers =
          emptyUserAnswers
            .set(CheckYourAnswersSubmittedPage, true)
            .success
            .value

        val result = service.clean(userAnswers).success.value

        result.get(CheckYourAnswersSubmittedPage) mustBe Some(false)
      }
    }

    "cleanAmend" - {

      "must remove all original answers and reset AmendCheckYourAnswersSubmittedPage" in {

        val userAnswers =
          emptyUserAnswers
            .set(OriginalIndividualAnswersQuery, originalIndividual)
            .success
            .value
            .set(OriginalCompanyAnswersQuery, originalCompany)
            .success
            .value
            .set(OriginalPartnershipAnswersQuery, originalPartnership)
            .success
            .value
            .set(OriginalTrustAnswersQuery, originalTrust)
            .success
            .value
            .set(AmendCheckYourAnswersSubmittedPage, true)
            .success
            .value
            .set(CheckYourAnswersSubmittedPage, true)
            .success
            .value
            .set(ShowVerificationDetailsPage, true)
            .success
            .value

        val result = service.cleanAmend(userAnswers).success.value

        result.get(OriginalIndividualAnswersQuery) mustBe None
        result.get(OriginalCompanyAnswersQuery) mustBe None
        result.get(OriginalPartnershipAnswersQuery) mustBe None
        result.get(OriginalTrustAnswersQuery) mustBe None

        result.get(AmendCheckYourAnswersSubmittedPage) mustBe Some(false)

        result.get(CheckYourAnswersSubmittedPage) mustBe Some(true)
        result.get(ShowVerificationDetailsPage) mustBe None
      }

      "must succeed when no original answers exist" in {

        val userAnswers = emptyUserAnswers

        val result = service.cleanAmend(userAnswers).success.value

        result.get(OriginalIndividualAnswersQuery) mustBe None
        result.get(OriginalCompanyAnswersQuery) mustBe None
        result.get(OriginalPartnershipAnswersQuery) mustBe None
        result.get(OriginalTrustAnswersQuery) mustBe None
        result.get(ShowVerificationDetailsPage) mustBe None
        result.get(AmendCheckYourAnswersSubmittedPage) mustBe Some(false)
      }
    }
  }
}

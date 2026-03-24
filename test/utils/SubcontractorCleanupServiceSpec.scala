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
import pages.add.{CheckYourAnswersSubmittedPage, NationalInsuranceNumberYesNoPage}
import pages.add.company.CompanyAddressYesNoPage
import pages.add.partnership.PartnershipAddressYesNoPage
import pages.add.trust.TrustAddressYesNoPage

class SubcontractorCleanupServiceSpec extends SpecBase  {

  val defaultSubcontractorCleanupService = new DefaultSubcontractorCleanupService()

  "SubcontractorCleanupService" - {

    "should remove all subcontractor related journey answers and change CheckYourAnswersSubmittedPage to false" in {

      val userAnswers =
        emptyUserAnswers
          .set(NationalInsuranceNumberYesNoPage, true)
          .success
          .value
          .set(CompanyAddressYesNoPage, true)
          .success
          .value
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value
          .set(TrustAddressYesNoPage, true)
          .success
          .value
          .set(CheckYourAnswersSubmittedPage, true)
          .success
          .value


      val result = defaultSubcontractorCleanupService.clean(userAnswers).success.value

      result.get(NationalInsuranceNumberYesNoPage) mustBe None
      result.get(CompanyAddressYesNoPage) mustBe None
      result.get(PartnershipAddressYesNoPage) mustBe None
      result.get(TrustAddressYesNoPage) mustBe None
      result.get(CheckYourAnswersSubmittedPage) mustBe Some(false)
    }
  }

}

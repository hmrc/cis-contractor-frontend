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

package models.finalvalidation

import base.SpecBase
import models.TypeOfSubcontractor.*
import pages.add.*
import pages.add.company.CompanyCrnPage
import pages.add.partnership.PartnershipNominatedPartnerUtrPage
import pages.add.trust.TrustWorksReferencePage

class FinalValidationCorrectionBuilderSpec extends SpecBase {

  private val builder =
    new FinalValidationCorrectionBuilder()

  private def payload(
    target: FinalValidationChangeTarget
  ): FinalValidationHandoffPayload =
    FinalValidationHandoffPayload(
      draftId = "draft-123",
      instanceId = "CIS-123",
      subcontractorId = 101L,
      subbieResourceRef = 202L,
      field = FinalValidationField.Utr,
      changeTarget = target
    )

  "FinalValidationCorrectionBuilder" - {

    "must build an Individual trading name correction" in {

      val userAnswers =
        emptyUserAnswers
          .setOrException(
            TypeOfSubcontractorPage,
            Individualorsoletrader
          )
          .setOrException(
            TradingNameOfSubcontractorPage,
            "Smith Trading"
          )

      val result =
        builder
          .build(
            userAnswers,
            payload(FinalValidationChangeTarget.TradingName)
          )
          .success
          .value

      result mustBe FinalValidationCorrection(
        subcontractorId = 101L,
        changeTarget = FinalValidationChangeTarget.TradingName,
        patch = FinalValidationSubcontractorPatch(
          tradingName = Some("Smith Trading")
        )
      )
    }

    "must build a Company CRN correction" in {

      val userAnswers =
        emptyUserAnswers
          .setOrException(
            TypeOfSubcontractorPage,
            Limitedcompany
          )
          .setOrException(
            CompanyCrnPage,
            "12345678"
          )

      val result =
        builder
          .build(
            userAnswers,
            payload(FinalValidationChangeTarget.Crn)
          )
          .success
          .value

      result mustBe FinalValidationCorrection(
        subcontractorId = 101L,
        changeTarget = FinalValidationChangeTarget.Crn,
        patch = FinalValidationSubcontractorPatch(
          crn = Some("12345678")
        )
      )
    }

    "must build a Trust works reference number correction" in {

      val userAnswers =
        emptyUserAnswers
          .setOrException(
            TypeOfSubcontractorPage,
            Trust
          )
          .setOrException(
            TrustWorksReferencePage,
            "WRN123"
          )

      val result =
        builder
          .build(
            userAnswers,
            payload(FinalValidationChangeTarget.WorksReferenceNumber)
          )
          .success
          .value

      result mustBe FinalValidationCorrection(
        subcontractorId = 101L,
        changeTarget = FinalValidationChangeTarget.WorksReferenceNumber,
        patch = FinalValidationSubcontractorPatch(
          worksReferenceNumber = Some("WRN123")
        )
      )
    }

    "must build a Partnership partner UTR correction" in {

      val userAnswers =
        emptyUserAnswers
          .setOrException(
            TypeOfSubcontractorPage,
            Partnership
          )
          .setOrException(
            PartnershipNominatedPartnerUtrPage,
            "1234567890"
          )

      val result =
        builder
          .build(
            userAnswers,
            payload(FinalValidationChangeTarget.PartnerUtr)
          )
          .success
          .value

      result mustBe FinalValidationCorrection(
        subcontractorId = 101L,
        changeTarget = FinalValidationChangeTarget.PartnerUtr,
        patch = FinalValidationSubcontractorPatch(
          partnerUtr = Some("1234567890")
        )
      )
    }

    "must return an empty patch when a YesNo answer is No" in {

      val userAnswers =
        emptyUserAnswers
          .setOrException(
            TypeOfSubcontractorPage,
            Individualorsoletrader
          )
          .setOrException(
            UniqueTaxpayerReferenceYesNoPage,
            false
          )

      val result =
        builder
          .build(
            userAnswers,
            payload(FinalValidationChangeTarget.UtrYesNo)
          )
          .success
          .value

      result mustBe FinalValidationCorrection(
        subcontractorId = 101L,
        changeTarget = FinalValidationChangeTarget.UtrYesNo,
        patch = FinalValidationSubcontractorPatch()
      )
    }

    "must use the value page when a YesNo answer is Yes" in {

      val userAnswers =
        emptyUserAnswers
          .setOrException(
            TypeOfSubcontractorPage,
            Individualorsoletrader
          )
          .setOrException(
            UniqueTaxpayerReferenceYesNoPage,
            true
          )
          .setOrException(
            SubcontractorsUniqueTaxpayerReferencePage,
            "1234567890"
          )

      val result =
        builder
          .build(
            userAnswers,
            payload(FinalValidationChangeTarget.UtrYesNo)
          )
          .success
          .value

      result mustBe FinalValidationCorrection(
        subcontractorId = 101L,
        changeTarget = FinalValidationChangeTarget.UtrYesNo,
        patch = FinalValidationSubcontractorPatch(
          utr = Some("1234567890")
        )
      )
    }

    "must build contact details from the available contact pages" in {

      val userAnswers =
        emptyUserAnswers
          .setOrException(
            TypeOfSubcontractorPage,
            Individualorsoletrader
          )
          .setOrException(
            AddIndividualContactMethodsYesNoPage,
            true
          )
          .setOrException(
            IndividualEmailAddressPage,
            "test@example.com"
          )
          .setOrException(
            IndividualPhoneNumberPage,
            "01234567890"
          )
          .setOrException(
            IndividualMobileNumberPage,
            "07123456789"
          )

      val result =
        builder
          .build(
            userAnswers,
            payload(FinalValidationChangeTarget.ContactDetailsYesNo)
          )
          .success
          .value

      result mustBe FinalValidationCorrection(
        subcontractorId = 101L,
        changeTarget = FinalValidationChangeTarget.ContactDetailsYesNo,
        patch = FinalValidationSubcontractorPatch(
          emailAddress = Some("test@example.com"),
          phoneNumber = Some("01234567890"),
          mobilePhoneNumber = Some("07123456789")
        )
      )
    }

    "must return an empty contact patch when contact details is No" in {

      val userAnswers =
        emptyUserAnswers
          .setOrException(
            TypeOfSubcontractorPage,
            Individualorsoletrader
          )
          .setOrException(
            AddIndividualContactMethodsYesNoPage,
            false
          )

      val result =
        builder
          .build(
            userAnswers,
            payload(FinalValidationChangeTarget.ContactDetailsYesNo)
          )
          .success
          .value

      result.patch mustBe FinalValidationSubcontractorPatch()
    }

    "must fail when TypeOfSubcontractorPage is missing" in {

      val result =
        builder.build(
          emptyUserAnswers,
          payload(FinalValidationChangeTarget.TradingName)
        )

      result.failure.exception.getMessage mustBe
        "Type of subcontractor not found"
    }

    "must fail when the required value page is missing" in {

      val userAnswers =
        emptyUserAnswers
          .setOrException(
            TypeOfSubcontractorPage,
            Individualorsoletrader
          )

      val result =
        builder.build(
          userAnswers,
          payload(FinalValidationChangeTarget.TradingName)
        )

      result.failure.exception.getMessage mustBe
        s"${TradingNameOfSubcontractorPage.toString} not found"
    }

    "must fail when a YesNo page is missing" in {

      val userAnswers =
        emptyUserAnswers
          .setOrException(
            TypeOfSubcontractorPage,
            Individualorsoletrader
          )

      val result =
        builder.build(
          userAnswers,
          payload(FinalValidationChangeTarget.UtrYesNo)
        )

      result.failure.exception.getMessage mustBe
        s"${UniqueTaxpayerReferenceYesNoPage.toString} not found"
    }

    "must fail for an unsupported subcontractor type and change target combination" in {

      val userAnswers =
        emptyUserAnswers
          .setOrException(
            TypeOfSubcontractorPage,
            Limitedcompany
          )

      val result =
        builder.build(
          userAnswers,
          payload(FinalValidationChangeTarget.Nino)
        )

      result.failure.exception.getMessage mustBe
        s"Unsupported combination of subcontractor type: $Limitedcompany " +
        s"and change target: ${FinalValidationChangeTarget.Nino}"
    }
  }
}

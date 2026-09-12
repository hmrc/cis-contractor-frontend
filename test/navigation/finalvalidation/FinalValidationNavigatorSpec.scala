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

package navigation.finalvalidation

import base.SpecBase
import models.*
import models.TypeOfSubcontractor.*
import models.contact.ContactMethodOptions
import models.finalvalidation.FinalValidationChangeTarget
import pages.add.*
import pages.add.company.*
import pages.finalvalidation.FinalValidationChangeTargetPage

class FinalValidationNavigatorSpec extends SpecBase {

  "FinalValidationNavigator" - {

    "startPage" - {

      "must go to the sole trader trading name page" in {

        val navigator =
          new FinalValidationNavigator()

        val answers =
          emptyUserAnswers
            .setOrException(
              TypeOfSubcontractorPage,
              Individualorsoletrader
            )

        navigator.startPage(
          FinalValidationChangeTarget.TradingName,
          answers
        ) mustBe
          controllers.add.routes.TradingNameOfSubcontractorController
            .onPageLoad(FinalValidationMode)
      }

      "must go to the company address lookup for Address" in {

        val navigator =
          new FinalValidationNavigator()

        val answers =
          emptyUserAnswers
            .setOrException(
              TypeOfSubcontractorPage,
              Limitedcompany
            )

        navigator.startPage(
          FinalValidationChangeTarget.Address,
          answers
        ) mustBe
          controllers.add.company.routes.CompanyAddressController
            .redirectToAddressLookup(
              FinalValidationMode,
              None
            )
      }

      "must go to the partnership nominated partner UTR page" in {

        val navigator =
          new FinalValidationNavigator()

        val answers =
          emptyUserAnswers
            .setOrException(
              TypeOfSubcontractorPage,
              Partnership
            )

        navigator.startPage(
          FinalValidationChangeTarget.PartnerUtr,
          answers
        ) mustBe
          controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController
            .onPageLoad(FinalValidationMode)
      }

      "must go to the trust works reference number page" in {

        val navigator =
          new FinalValidationNavigator()

        val answers =
          emptyUserAnswers
            .setOrException(
              TypeOfSubcontractorPage,
              Trust
            )

        navigator.startPage(
          FinalValidationChangeTarget.WorksReferenceNumber,
          answers
        ) mustBe
          controllers.add.trust.routes.TrustWorksReferenceController
            .onPageLoad(FinalValidationMode)
      }

      "must go to Journey Recovery when the subcontractor type is missing" in {

        val navigator =
          new FinalValidationNavigator()

        navigator.startPage(
          FinalValidationChangeTarget.TradingName,
          emptyUserAnswers
        ) mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
      }
    }

    "nextPage" - {

      "must go to Journey Recovery when mode is not FinalValidationMode" in {

        val navigator =
          new FinalValidationNavigator()

        val answers =
          emptyUserAnswers
            .setOrException(
              FinalValidationChangeTargetPage,
              FinalValidationChangeTarget.TradingName
            )

        navigator.nextPage(
          CompanyNamePage,
          NormalMode,
          answers
        ) mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
      }

      "must go to Journey Recovery when FinalValidationChangeTargetPage is missing" in {

        val navigator =
          new FinalValidationNavigator()

        navigator.nextPage(
          CompanyNamePage,
          FinalValidationMode,
          emptyUserAnswers
        ) mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
      }

      "must go to FinalValidationComplete for a simple change target" in {

        val navigator =
          new FinalValidationNavigator()

        val answers =
          emptyUserAnswers
            .setOrException(
              FinalValidationChangeTargetPage,
              FinalValidationChangeTarget.TradingName
            )

        navigator.nextPage(
          CompanyNamePage,
          FinalValidationMode,
          answers
        ) mustBe
          controllers.finalvalidations.routes.FinalValidationCompleteController
            .onPageLoad()
      }

      "must go to the company address lookup when AddressYesNo is Yes" in {

        val navigator =
          new FinalValidationNavigator()

        val answers =
          emptyUserAnswers
            .setOrException(
              FinalValidationChangeTargetPage,
              FinalValidationChangeTarget.AddressYesNo
            )
            .setOrException(
              CompanyAddressYesNoPage,
              true
            )

        navigator.nextPage(
          CompanyAddressYesNoPage,
          FinalValidationMode,
          answers
        ) mustBe
          controllers.add.company.routes.CompanyAddressController
            .redirectToAddressLookup(
              FinalValidationMode,
              None
            )
      }

      "must complete when AddressYesNo is No" in {

        val navigator =
          new FinalValidationNavigator()

        val answers =
          emptyUserAnswers
            .setOrException(
              FinalValidationChangeTargetPage,
              FinalValidationChangeTarget.AddressYesNo
            )
            .setOrException(
              CompanyAddressYesNoPage,
              false
            )

        navigator.nextPage(
          CompanyAddressYesNoPage,
          FinalValidationMode,
          answers
        ) mustBe
          controllers.finalvalidations.routes.FinalValidationCompleteController
            .onPageLoad()
      }

      "must go to CompanyUtrPage when UtrYesNo is Yes" in {

        val navigator =
          new FinalValidationNavigator()

        val answers =
          emptyUserAnswers
            .setOrException(
              FinalValidationChangeTargetPage,
              FinalValidationChangeTarget.UtrYesNo
            )
            .setOrException(
              CompanyUtrYesNoPage,
              true
            )

        navigator.nextPage(
          CompanyUtrYesNoPage,
          FinalValidationMode,
          answers
        ) mustBe
          controllers.add.company.routes.CompanyUtrController
            .onPageLoad(FinalValidationMode)
      }

      "must complete when UtrYesNo is No" in {

        val navigator =
          new FinalValidationNavigator()

        val answers =
          emptyUserAnswers
            .setOrException(
              FinalValidationChangeTargetPage,
              FinalValidationChangeTarget.UtrYesNo
            )
            .setOrException(
              CompanyUtrYesNoPage,
              false
            )

        navigator.nextPage(
          CompanyUtrYesNoPage,
          FinalValidationMode,
          answers
        ) mustBe
          controllers.finalvalidations.routes.FinalValidationCompleteController
            .onPageLoad()
      }

      "must go to company contact method options when ContactDetailsYesNo is Yes" in {

        val navigator =
          new FinalValidationNavigator()

        val answers =
          emptyUserAnswers
            .setOrException(
              FinalValidationChangeTargetPage,
              FinalValidationChangeTarget.ContactDetailsYesNo
            )
            .setOrException(
              AddCompanyContactMethodsYesNoPage,
              true
            )

        navigator.nextPage(
          AddCompanyContactMethodsYesNoPage,
          FinalValidationMode,
          answers
        ) mustBe
          controllers.add.company.routes.CompanyContactMethodOptionsController
            .onPageLoad(FinalValidationMode)
      }

      "must go to the first selected company contact method" in {

        val navigator =
          new FinalValidationNavigator()

        val answers =
          emptyUserAnswers
            .setOrException(
              FinalValidationChangeTargetPage,
              FinalValidationChangeTarget.ContactDetailsYesNo
            )
            .setOrException(
              CompanyContactMethodOptionsPage,
              Set(
                ContactMethodOptions.Email,
                ContactMethodOptions.Phone
              )
            )

        navigator.nextPage(
          CompanyContactMethodOptionsPage,
          FinalValidationMode,
          answers
        ) mustBe
          controllers.add.company.routes.CompanyEmailAddressController
            .onPageLoad(FinalValidationMode)
      }

      "must go to the next selected company contact method" in {

        val navigator =
          new FinalValidationNavigator()

        val answers =
          emptyUserAnswers
            .setOrException(
              FinalValidationChangeTargetPage,
              FinalValidationChangeTarget.ContactDetailsYesNo
            )
            .setOrException(
              CompanyContactMethodOptionsPage,
              Set(
                ContactMethodOptions.Email,
                ContactMethodOptions.Phone
              )
            )

        navigator.nextPage(
          CompanyEmailAddressPage,
          FinalValidationMode,
          answers
        ) mustBe
          controllers.add.company.routes.CompanyPhoneNumberController
            .onPageLoad(FinalValidationMode)
      }

      "must complete after the last selected company contact method" in {

        val navigator =
          new FinalValidationNavigator()

        val answers =
          emptyUserAnswers
            .setOrException(
              FinalValidationChangeTargetPage,
              FinalValidationChangeTarget.ContactDetailsYesNo
            )
            .setOrException(
              CompanyContactMethodOptionsPage,
              Set(ContactMethodOptions.Email)
            )

        navigator.nextPage(
          CompanyEmailAddressPage,
          FinalValidationMode,
          answers
        ) mustBe
          controllers.finalvalidations.routes.FinalValidationCompleteController
            .onPageLoad()
      }

      "must go to Journey Recovery when the current contact method was not selected" in {

        val navigator =
          new FinalValidationNavigator()

        val answers =
          emptyUserAnswers
            .setOrException(
              FinalValidationChangeTargetPage,
              FinalValidationChangeTarget.ContactDetailsYesNo
            )
            .setOrException(
              CompanyContactMethodOptionsPage,
              Set(ContactMethodOptions.Email)
            )

        navigator.nextPage(
          CompanyPhoneNumberPage,
          FinalValidationMode,
          answers
        ) mustBe
          controllers.routes.JourneyRecoveryController
            .onPageLoad()
      }
    }
  }
}

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

package navigation.add

import base.SpecBase
import controllers.routes
import models.contact.ContactMethodOptions
import models.add.SubcontractorName
import models.{AmendMode, CheckMode, NormalMode, UserAnswers}
import pages.Page
import pages.QuestionPage
import pages.add.*
import play.api.libs.json.JsPath

class IndividualNavigatorSpec extends SpecBase {

  val navigator                    = new IndividualNavigator
  private lazy val journeyRecovery = routes.JourneyRecoveryController.onPageLoad()
  private lazy val CYA             = controllers.add.routes.CheckYourAnswersController.onPageLoad()
  private lazy val AmendCYA        =
    routes.JourneyRecoveryController
      .onPageLoad() // TODO when available controllers.add.routes.AmendCheckYourAnswersController.onPageLoad()

  "IndividualNavigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from a SubTradingNameYesNoPage to TradingNameOfSubcontractorPage when true" in {
        navigator.nextPage(
          SubTradingNameYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(SubTradingNameYesNoPage, true)
        ) mustBe controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(NormalMode)
      }

      "must go from a SubTradingNameYesNoPage to SubcontractorNamePage when false" in {
        navigator.nextPage(
          SubTradingNameYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(SubTradingNameYesNoPage, false)
        ) mustBe controllers.add.routes.SubcontractorNameController.onPageLoad(NormalMode)
      }

      "must go from a SubTradingNameYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          SubTradingNameYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a TradingNameOfSubcontractorPage to SubAddressYesNoPage" in {
        navigator.nextPage(
          TradingNameOfSubcontractorPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.SubAddressYesNoController.onPageLoad(NormalMode)
      }

      "must go from a SubcontractorNamePage to SubAddressYesNoPage" in {
        navigator.nextPage(
          SubcontractorNamePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.SubAddressYesNoController.onPageLoad(NormalMode)
      }

      "must go from a SubAddressYesNoPage to AddressOfSubcontractorPage when true" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(SubAddressYesNoPage, true)
        ) mustBe controllers.add.routes.AddressOfSubcontractorController.redirectToAddressLookup()
      }

      "must go from a SubAddressYesNoPage to AddIndividualContactMethodsYesNoController when false" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(SubAddressYesNoPage, false)
        ) mustBe controllers.add.routes.AddIndividualContactMethodsYesNoController.onPageLoad(NormalMode)
      }

      "must go from a SubAddressYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a NationalInsuranceNumberYesNoPage to SubNationalInsuranceNumberPage when true" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(NationalInsuranceNumberYesNoPage, true)
        ) mustBe controllers.add.routes.SubNationalInsuranceNumberController.onPageLoad(NormalMode)
      }

      "must go from a NationalInsuranceNumberYesNoPage to WorksReferenceNumberYesNoPage when false" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(NationalInsuranceNumberYesNoPage, false)
        ) mustBe controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(NormalMode)
      }

      "must go from a NationalInsuranceNumberYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a SubNationalInsuranceNumberPage to WorksReferenceNumberYesNoPage" in {
        navigator.nextPage(
          SubNationalInsuranceNumberPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(NormalMode)
      }

      "must go from a UniqueTaxpayerReferenceYesNoPage to SubcontractorsUniqueTaxpayerReferencePage when true" in {
        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(UniqueTaxpayerReferenceYesNoPage, true)
        ) mustBe controllers.add.routes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(NormalMode)
      }

      "must go from a UniqueTaxpayerReferenceYesNoPage to NationalInsuranceNumberYesNoPage when false" in {
        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(UniqueTaxpayerReferenceYesNoPage, false)
        ) mustBe controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode)
      }

      "must go from a UniqueTaxpayerReferenceYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a SubcontractorsUniqueTaxpayerReferencePage to NationalInsuranceNumberYesNoController" in {
        navigator.nextPage(
          SubcontractorsUniqueTaxpayerReferencePage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode)
      }

      "must go from a WorksReferenceNumberYesNoPage to WorksReferenceNumberPage when true" in {
        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers.setOrException(WorksReferenceNumberYesNoPage, true)
        ) mustBe controllers.add.routes.WorksReferenceNumberController.onPageLoad(NormalMode)
      }

      "must go from a WorksReferenceNumberYesNoPage to CYA when false" in {
        val ua =
          emptyUserAnswers
            .setOrException(WorksReferenceNumberYesNoPage, false)

        val navigator = new IndividualNavigator
        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          NormalMode,
          ua
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from a WorksReferenceNumberYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a WorksReferenceNumberPage to CYA" in {
        navigator.nextPage(
          WorksReferenceNumberPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from IndividualEmailAddressPage" - {

        "to IndividualPhoneNumberPage when Phone is selected in IndividualContactMethodOptions" in {
          navigator.nextPage(
            IndividualEmailAddressPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe controllers.add.routes.IndividualPhoneNumberController
            .onPageLoad(NormalMode)
        }

        "to IndividualMobileNumberPage when Mobile is selected in IndividualContactMethodOptions" in {
          navigator.nextPage(
            IndividualEmailAddressPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile)
              )
          ) mustBe controllers.add.routes.IndividualMobileNumberController
            .onPageLoad(NormalMode)
        }

        "to UniqueTaxpayerReferenceYesNoPage Page when only Email is selected in IndividualContactMethodOptions" in {
          navigator.nextPage(
            IndividualEmailAddressPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email)
              )
          ) mustBe controllers.add.routes.UniqueTaxpayerReferenceYesNoController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage Page when IndividualContactMethodOptions answer is not present" in {
          navigator.nextPage(
            IndividualEmailAddressPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecoveryPage when Email is not in the selected IndividualContactMethodOptions" in {
          navigator.nextPage(
            IndividualEmailAddressPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe journeyRecovery
        }

      }

      "must go from IndividualPhoneNumberPage" - {
        "to IndividualMobileNumberPage when Mobile is selected in IndividualContactMethodOptions" in {
          navigator.nextPage(
            IndividualPhoneNumberPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe controllers.add.routes.IndividualMobileNumberController
            .onPageLoad(NormalMode)
        }

        "to UniqueTaxpayerReferenceYesNoController Page when Mobile is not selected in IndividualContactMethodOptions" in {
          navigator.nextPage(
            IndividualPhoneNumberPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone)
              )
          ) mustBe controllers.add.routes.UniqueTaxpayerReferenceYesNoController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage Page when IndividualContactMethodOptions answer is not present" in {
          navigator.nextPage(
            IndividualPhoneNumberPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecoveryPage when Phone is not in the selected IndividualContactMethodOptions" in {
          navigator.nextPage(
            IndividualPhoneNumberPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from IndividualMobileNumberPage" - {
        "to IndividualHasUtrYesNo Page when PIndividualContactMethodOptions is present" in {
          navigator.nextPage(
            IndividualMobileNumberPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe controllers.add.routes.UniqueTaxpayerReferenceYesNoController
            .onPageLoad(NormalMode)
        }

        "to JourneyRecoveryPage Page when IndividualContactMethodOptions answer is not present" in {
          navigator.nextPage(
            IndividualMobileNumberPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecoveryPage when Mobile is not in the selected IndividualContactMethodOptions" in {
          navigator.nextPage(
            IndividualMobileNumberPage,
            NormalMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone)
              )
          ) mustBe journeyRecovery
        }
      }
    }

    "in Amend mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {
        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          AmendMode,
          UserAnswers("id")
        ) mustBe journeyRecovery // TODO: when CYA page available
      }

      "must go from SubTradingNameYesNoPage to SubcontractorNameController when answer is No and name is missing" in {
        val ua =
          emptyUserAnswers
            .set(SubTradingNameYesNoPage, false)
            .success
            .value

        navigator.nextPage(
          SubTradingNameYesNoPage,
          AmendMode,
          ua
        ) mustBe controllers.add.routes.SubcontractorNameController.onPageLoad(AmendMode)
      }

      "must go from SubTradingNameYesNoPage to Amend CYA when answer is No and subcontractor name already exists" in {
        val ua =
          emptyUserAnswers
            .set(SubTradingNameYesNoPage, false)
            .success
            .value
            .set(SubcontractorNamePage, SubcontractorName("Jane", None, "Doe"))
            .success
            .value

        navigator.nextPage(
          SubTradingNameYesNoPage,
          AmendMode,
          ua
        ) mustBe journeyRecovery // TODO: this needs to be redirected to amend cya page when it's implemented
      }

      "must go from SubTradingNameYesNoPage to TradingNameOfSubcontractorController when answer is Yes and trading name is missing" in {
        val ua =
          emptyUserAnswers
            .set(SubTradingNameYesNoPage, true)
            .success
            .value

        navigator.nextPage(
          SubTradingNameYesNoPage,
          AmendMode,
          ua
        ) mustBe controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(AmendMode)
      }

      "must go from SubTradingNameYesNoPage to Amend CYA when answer is Yes and trading name already exists" in {
        val ua =
          emptyUserAnswers
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ACME Construction")
            .success
            .value

        navigator.nextPage(
          SubTradingNameYesNoPage,
          AmendMode,
          ua
        ) mustBe journeyRecovery // TODO: this needs to be redirected to amend cya page when it's implemented
      }

      "must go from SubTradingNameYesNoPage to JourneyRecovery when SubTradingNameYesNoPage answer is missing" in {
        navigator.nextPage(
          SubTradingNameYesNoPage,
          AmendMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from SubcontractorNamePage to Amend CYA" in {
        navigator.nextPage(
          SubcontractorNamePage,
          AmendMode,
          emptyUserAnswers.setOrException(
            SubcontractorNamePage,
            SubcontractorName(firstName = "Jane", middleName = None, lastName = "Doe")
          )
        ) mustBe journeyRecovery // TODO: this needs to be redirected to amend cya page when it's implemented
      }

      "must go from WorksReferenceNumberYesNoPage to WorksReferenceNumberPage when true and no work reference number exists" in {
        val ua =
          emptyUserAnswers.setOrException(WorksReferenceNumberYesNoPage, true)

        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          AmendMode,
          ua
        ) mustBe controllers.add.routes.WorksReferenceNumberController.onPageLoad(AmendMode)
      }

      "must go from WorksReferenceNumberYesNoPage to amend CYA page when true and work reference number already exists" in {
        val ua =
          emptyUserAnswers
            .setOrException(WorksReferenceNumberYesNoPage, true)
            .setOrException(WorksReferenceNumberPage, "wrn-1")

        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          AmendMode,
          ua
        ) mustBe journeyRecovery // TODO: redirect to amend cya page when implemented
      }

      "must go from WorksReferenceNumberYesNoPage to amend CYA page when false" in {
        val ua =
          emptyUserAnswers.setOrException(WorksReferenceNumberYesNoPage, false)

        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          AmendMode,
          ua
        ) mustBe journeyRecovery // TODO: redirect to amend cya page when implemented
      }

      "must go from WorksReferenceNumberYesNoPage to JourneyRecovery when answer is missing" in {
        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          AmendMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from AddIndividualContactMethodsYesNoPage to amend CYA when answer is No" in {
        val answers = emptyUserAnswers.set(AddIndividualContactMethodsYesNoPage, false).success.value

        navigator.nextPage(
          AddIndividualContactMethodsYesNoPage,
          AmendMode,
          answers
        ) mustBe journeyRecovery
      }

      "to IndividualContactMethodOptions page when answer is Yes and IndividualContactMethodOptions not yet answered" in {
        val answers = emptyUserAnswers.setOrException(AddIndividualContactMethodsYesNoPage, true)

        navigator.nextPage(
          AddIndividualContactMethodsYesNoPage,
          AmendMode,
          answers
        ) mustBe controllers.add.routes.IndividualContactMethodOptionsController.onPageLoad(AmendMode)
      }

      "to amend CYA when answer is Yes and IndividualContactMethodOptions already answered" in {
        val answers = emptyUserAnswers
          .setOrException(AddIndividualContactMethodsYesNoPage, true)
          .setOrException(
            IndividualContactMethodOptionsPage,
            Set(ContactMethodOptions.Email, ContactMethodOptions.Phone)
          )

        navigator.nextPage(
          AddIndividualContactMethodsYesNoPage,
          AmendMode,
          answers
        ) mustBe AmendCYA
      }

      "must go from IndividualEmailAddressPage to CheckYourAnswersController" in {
        navigator.nextPage(
          IndividualEmailAddressPage,
          AmendMode,
          emptyUserAnswers.setOrException(
            IndividualEmailAddressPage,
            "test@test.com"
          )
        ) mustBe AmendCYA
      }

      "must go from IndividualEmailAddressPage to CheckYourAnswersController regardless of answers" in {
        navigator.nextPage(
          IndividualEmailAddressPage,
          AmendMode,
          emptyUserAnswers
        ) mustBe AmendCYA
      }

      "must go from IndividualMobileNumberPage to CheckYourAnswersController" in {
        navigator.nextPage(
          IndividualMobileNumberPage,
          AmendMode,
          emptyUserAnswers
        ) mustBe journeyRecovery // TODO: when CYA page available
      }

      "to CYA when answer is No" in {
        val answers = emptyUserAnswers.set(AddIndividualContactMethodsYesNoPage, false).success.value

        navigator.nextPage(
          AddIndividualContactMethodsYesNoPage,
          AmendMode,
          answers
        ) mustBe AmendCYA
      }

      "to JourneyRecovery when answer is not present" in {
        navigator.nextPage(
          AddIndividualContactMethodsYesNoPage,
          AmendMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from IndividualContactMethodOptions" - {
        "to IndividualEmailAddressPage when Email is selected and Email answer not exist" in {
          val answers = emptyUserAnswers
            .set(
              IndividualContactMethodOptionsPage,
              Set(ContactMethodOptions.Email)
            )
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            AmendMode,
            answers
          ) mustBe controllers.add.routes.IndividualEmailAddressController.onPageLoad(AmendMode)
        }

        "to IndividualEmailAddressPage when Email, Phone and Mobile are selected and no Email answer not exist" in {
          val answers = emptyUserAnswers
            .set(
              IndividualContactMethodOptionsPage,
              Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
            )
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            AmendMode,
            answers
          ) mustBe controllers.add.routes.IndividualEmailAddressController.onPageLoad(AmendMode)
        }

        "to CYA when only Email is selected and Email answer exists" in {
          val answers = emptyUserAnswers
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Email))
            .success
            .value
            .set(IndividualEmailAddressPage, "test@test.com")
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            AmendMode,
            answers
          ) mustBe AmendCYA
        }

        "to IndividualPhoneNumberPage when Phone is selected (Email is not selected) and Phone answer not exists" in {
          val answers = emptyUserAnswers
            .set(
              IndividualContactMethodOptionsPage,
              Set(ContactMethodOptions.Phone)
            )
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            AmendMode,
            answers
          ) mustBe controllers.add.routes.IndividualPhoneNumberController.onPageLoad(AmendMode)
        }

        "to CYA when only Phone is selected and Phone answer exists" in {
          val answers = emptyUserAnswers
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Phone))
            .success
            .value
            .set(IndividualPhoneNumberPage, "01234567890")
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            AmendMode,
            answers
          ) mustBe AmendCYA
        }

        "to CYA when only Mobile is selected and Mobile answer exists" in {
          val answers = emptyUserAnswers
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Mobile))
            .success
            .value
            .set(IndividualMobileNumberPage, "01234567890")
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            AmendMode,
            answers
          ) mustBe AmendCYA
        }

        "to IndividualPhoneNumberPage when Email and Phone are selected and Email answer exists, Phone answer not exists" in {
          val answers = emptyUserAnswers
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))
            .success
            .value
            .set(IndividualEmailAddressPage, "test@test.com")
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            AmendMode,
            answers
          ) mustBe controllers.add.routes.IndividualPhoneNumberController.onPageLoad(AmendMode)
        }

        "to IndividualMobileNumberPage when Email, Phone and Mobile are selected and Email and Phone answer exists, Mobile answer not exists" in {
          val answers = emptyUserAnswers
            .set(
              IndividualContactMethodOptionsPage,
              Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
            )
            .success
            .value
            .set(IndividualEmailAddressPage, "test@test.com")
            .success
            .value
            .set(IndividualPhoneNumberPage, "01234567890")
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            AmendMode,
            answers
          ) mustBe controllers.add.routes.IndividualMobileNumberController.onPageLoad(AmendMode)
        }

        "to JourneyRecovery when answer is not present" in {
          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            AmendMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from AddIndividualContactMethodsYesNo" - {
        "to IndividualContactMethodOptionsController page when answer is Yes" in {
          val answers = emptyUserAnswers.set(AddIndividualContactMethodsYesNoPage, true).success.value

          navigator.nextPage(
            AddIndividualContactMethodsYesNoPage,
            AmendMode,
            answers
          ) mustBe controllers.add.routes.IndividualContactMethodOptionsController
            .onPageLoad(AmendMode)
        }

        "to CYA when answer is No" in {
          val answers = emptyUserAnswers.set(AddIndividualContactMethodsYesNoPage, false).success.value

          navigator.nextPage(
            AddIndividualContactMethodsYesNoPage,
            AmendMode,
            answers
          ) mustBe AmendCYA
        }

        "to JourneyRecovery when answer is not present" in {
          navigator.nextPage(
            AddIndividualContactMethodsYesNoPage,
            AmendMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }
      "must go from IndividualEmailAddressPage" - {

        "to IndividualCheckYourAnswers when no missing ContactMethodOptions answer" in {
          navigator.nextPage(
            IndividualEmailAddressPage,
            AmendMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(IndividualEmailAddressPage, "test@test.com")
              .setOrException(IndividualPhoneNumberPage, "1234567")
              .setOrException(IndividualMobileNumberPage, "1234567")
          ) mustBe AmendCYA
        }

        "to IndividualPhoneNumberPage when IndividualPhoneNumber is missing from ContactMethodOptions answer" in {
          navigator.nextPage(
            IndividualEmailAddressPage,
            AmendMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(IndividualEmailAddressPage, "test@test.com")
              .setOrException(IndividualMobileNumberPage, "1234567")
          ) mustBe controllers.add.routes.IndividualPhoneNumberController.onPageLoad(AmendMode)
        }

        "to IndividualMobileNumberPage when IndividualMobileNumber is missing from ContactMethodOptions answer" in {
          navigator.nextPage(
            IndividualEmailAddressPage,
            AmendMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(IndividualEmailAddressPage, "test@test.com")
              .setOrException(IndividualPhoneNumberPage, "1234567")
          ) mustBe controllers.add.routes.IndividualMobileNumberController.onPageLoad(AmendMode)
        }

        "to JourneyRecovery when IndividualContactMethodOptions answer is not present" in {
          navigator.nextPage(
            IndividualEmailAddressPage,
            AmendMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecovery when Email is not in the selected IndividualContactMethodOptions" in {
          navigator.nextPage(
            IndividualEmailAddressPage,
            AmendMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from IndividualPhoneNumberPage" - {

        "to CheckYourAnswers when no missing ContactMethodOptions answer" in {
          navigator.nextPage(
            IndividualPhoneNumberPage,
            AmendMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(IndividualEmailAddressPage, "test@test.com")
              .setOrException(IndividualPhoneNumberPage, "1234567")
              .setOrException(IndividualMobileNumberPage, "1234567")
          ) mustBe AmendCYA
        }

        "to IndividualMobileNumberPage when IndividualMobileNumber is missing from ContactMethodOptions answer" in {
          navigator.nextPage(
            IndividualPhoneNumberPage,
            AmendMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(IndividualEmailAddressPage, "test@test.com")
              .setOrException(IndividualPhoneNumberPage, "1234567")
          ) mustBe controllers.add.routes.IndividualMobileNumberController.onPageLoad(AmendMode)
        }

        "to JourneyRecovery when IndividualContactMethodOptions answer is not present" in {
          navigator.nextPage(
            IndividualPhoneNumberPage,
            AmendMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecovery when Phone is not in the selected IndividualContactMethodOptions" in {
          navigator.nextPage(
            IndividualPhoneNumberPage,
            AmendMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from IndividualMobileNumberPage" - {

        "to CheckYourAnswers" in {
          navigator.nextPage(
            IndividualMobileNumberPage,
            AmendMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(IndividualEmailAddressPage, "test@test.com")
              .setOrException(IndividualPhoneNumberPage, "1234567")
              .setOrException(IndividualMobileNumberPage, "1234567")
          ) mustBe AmendCYA
        }

        "to JourneyRecovery when IndividualContactMethodOptions answer is not present" in {
          navigator.nextPage(
            IndividualMobileNumberPage,
            AmendMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecovery when Mobile is not in the selected IndividualContactMethodOptions" in {
          navigator.nextPage(
            IndividualMobileNumberPage,
            AmendMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from a SubAddressYesNoPage to CYA page when false" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          AmendMode,
          emptyUserAnswers.setOrException(SubAddressYesNoPage, false)
        ) mustBe journeyRecovery // TODO: change this to CYA when available
      }

      "must go from SubAddressYesNoPage to journeyRecovery when true and AddressOfSubcontractorPage is already answered" in {
        val addressSample = models.address.Address(
          addressLine1 = "10 Example Street",
          addressLine2 = Some("Suite 2"),
          addressLine3 = Some("Newcastle"),
          addressLine4 = Some("Tyne & Wear"),
          postcode = Some("NE1 1AA"),
          country = Some(models.address.Country(Some("GB"), Some("United Kingdom")))
        )

        val ua     =
          emptyUserAnswers
            .set(SubAddressYesNoPage, true)
            .success
            .value
            .set(AddressOfSubcontractorPage, addressSample)
            .success
            .value
        val result = navigator.nextPage(SubAddressYesNoPage, AmendMode, ua)
        result mustBe journeyRecovery // TODO: change this to CYA when available
      }

      "must go from a SubAddressYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          AmendMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }
      "must go from a NationalInsuranceNumberYesNoPage to CYA page when false" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          AmendMode,
          emptyUserAnswers.setOrException(NationalInsuranceNumberYesNoPage, false)
        ) mustBe journeyRecovery // TODO - change to CYA when wiring complete
      }

      "must go from a NationalInsuranceNumberYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          AmendMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a NationalInsuranceNumberYesNoPage to next page when true" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          AmendMode,
          emptyUserAnswers.setOrException(NationalInsuranceNumberYesNoPage, true)
        ) mustBe controllers.add.routes.SubNationalInsuranceNumberController.onPageLoad(AmendMode)
      }

      "must go from SubNationalInsuranceNumberPage to Amend CYA" in {
        navigator.nextPage(
          SubNationalInsuranceNumberPage,
          AmendMode,
          emptyUserAnswers.setOrException(
            SubNationalInsuranceNumberPage,
            "AB123456C"
          )
        ) mustBe journeyRecovery // TODO: this needs to be redirected to amend cya page when it's implemented
      }

      "must go from UniqueTaxpayerReferenceYesNoPage to SubcontractorsUniqueTaxpayerReferencePage when true and no utr exists" in {
        val ua =
          emptyUserAnswers.setOrException(UniqueTaxpayerReferenceYesNoPage, true)

        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          AmendMode,
          ua
        ) mustBe controllers.add.routes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(AmendMode)
      }

      "must go from UniqueTaxpayerReferenceYesNoPage to JourneyRecovery when true and utr already exists" in {
        val ua =
          emptyUserAnswers
            .setOrException(UniqueTaxpayerReferenceYesNoPage, true)
            .setOrException(SubcontractorsUniqueTaxpayerReferencePage, "utr-1")

        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          AmendMode,
          ua
        ) mustBe journeyRecovery // TODO: when CYA page available
      }

      "must go from UniqueTaxpayerReferenceYesNoPage to JourneyRecovery when false" in {
        val ua =
          emptyUserAnswers.setOrException(UniqueTaxpayerReferenceYesNoPage, false)

        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          AmendMode,
          ua
        ) mustBe journeyRecovery // TODO: when CYA page available
      }

      "must go from UniqueTaxpayerReferenceYesNoPage to JourneyRecovery when answer is missing" in {
        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          AmendMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a SubcontractorsUniqueTaxpayerReferencePage to journey recovery page" in {
        navigator.nextPage(
          SubcontractorsUniqueTaxpayerReferencePage,
          AmendMode,
          UserAnswers("id")
        ) mustBe journeyRecovery // TODO: this needs to be redirected to amend individual cya page, AmendIndividualCheckYourAnswersController when it's implemented
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.CheckYourAnswersController
          .onPageLoad()
      }

      "must go from SubTradingNameYesNoPage to TradingNameOfSubcontractorController when true" in {
        navigator.nextPage(
          SubTradingNameYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(SubTradingNameYesNoPage, true)
        ) mustBe controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(CheckMode)
      }

      "must go from SubTradingNameYesNoPage to SubcontractorNamePage when false" in {
        navigator.nextPage(
          SubTradingNameYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(SubTradingNameYesNoPage, false)
        ) mustBe controllers.add.routes.SubcontractorNameController.onPageLoad(CheckMode)
      }

      "must go from SubTradingNameYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          SubTradingNameYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a SubAddressYesNoPage to next page when true" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(SubAddressYesNoPage, true)
        ) mustBe controllers.add.routes.AddressOfSubcontractorController
          .redirectToAddressLookup(Some(CheckMode.toString))
      }

      "must go from a SubAddressYesNoPage to CYA page when false" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(SubAddressYesNoPage, false)
        ) mustBe CYA
      }

      "must go from a SubAddressYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          SubAddressYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a UniqueTaxpayerReferenceYesNoPage to next page when true" in {
        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(UniqueTaxpayerReferenceYesNoPage, true)
        ) mustBe controllers.add.routes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(CheckMode)
      }

      "must go from a UniqueTaxpayerReferenceYesNoPage to CYA page when false" in {
        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(UniqueTaxpayerReferenceYesNoPage, false)
        ) mustBe CYA
      }

      "must go from a AddressOfSubcontractorPage to CYA" in {
        navigator.nextPage(
          AddressOfSubcontractorPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe CYA
      }

      "must go from TradingNameOfSubcontractorPage to CYA in CheckMode" in {
        navigator.nextPage(
          TradingNameOfSubcontractorPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe CYA
      }

      "must go from SubcontractorNamePage to CYA in CheckMode" in {
        navigator.nextPage(
          SubcontractorNamePage,
          CheckMode,
          UserAnswers("id")
        ) mustBe CYA
      }

      "must go from SubNationalInsuranceNumberPage to CYA in CheckMode" in {
        navigator.nextPage(
          SubNationalInsuranceNumberPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe CYA
      }

      "must go from SubcontractorsUniqueTaxpayerReferencePage to CYA in CheckMode" in {
        navigator.nextPage(
          SubcontractorsUniqueTaxpayerReferencePage,
          CheckMode,
          UserAnswers("id")
        ) mustBe CYA
      }

      "must go from a UniqueTaxpayerReferenceYesNoPage to journey recovery when incomplete info provided" in {
        navigator.nextPage(
          UniqueTaxpayerReferenceYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from a NationalInsuranceNumberYesNoPage to next page when true" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(NationalInsuranceNumberYesNoPage, true)
        ) mustBe controllers.add.routes.SubNationalInsuranceNumberController.onPageLoad(CheckMode)
      }

      "must go from a NationalInsuranceNumberYesNoPage to CYA page when false" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(NationalInsuranceNumberYesNoPage, false)
        ) mustBe CYA
      }

      "must go from a NationalInsuranceNumberYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          NationalInsuranceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }
      "must go from a WorksReferenceNumberPage  to next page in check mode" in {
        navigator.nextPage(
          WorksReferenceNumberPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from a WorksReferenceNumberYesNoPage to next page when true" in {
        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(WorksReferenceNumberYesNoPage, true)
        ) mustBe controllers.add.routes.WorksReferenceNumberController.onPageLoad(CheckMode)
      }

      "must go from a WorksReferenceNumberYesNoPage to CYA page when false" in {
        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers.setOrException(WorksReferenceNumberYesNoPage, false)
        ) mustBe CYA
      }

      "must go from a WorksReferenceNumberYesNoPage to journey recovery page when incomplete info provided" in {
        navigator.nextPage(
          WorksReferenceNumberYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go to SubcontractorNameController when answer is No and name is missing" in {
        val ua =
          emptyUserAnswers
            .set(SubTradingNameYesNoPage, false)
            .success
            .value

        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, CheckMode, ua) mustBe
          controllers.add.routes.SubcontractorNameController.onPageLoad(CheckMode)
      }

      "must go to CYA when answer is No and subcontractor name already exists (Some(_))" in {
        val ua =
          emptyUserAnswers
            .set(SubTradingNameYesNoPage, false)
            .success
            .value
            .set(SubcontractorNamePage, SubcontractorName(firstName = "Jane", middleName = None, lastName = "Doe"))
            .success
            .value

        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, CheckMode, ua) mustBe
          controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to TradingNameOfSubcontractorController when answer is Yes and trading name is missing" in {
        val ua =
          emptyUserAnswers
            .set(SubTradingNameYesNoPage, true)
            .success
            .value

        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, CheckMode, ua) mustBe
          controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(CheckMode)
      }

      "must go to CYA when answer is Yes and trading name already exists (Some(_))" in {
        val ua =
          emptyUserAnswers
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ACME Construction")
            .success
            .value

        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, CheckMode, ua) mustBe
          controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "must route to JourneyRecovery when SubTradingNameYesNoPage answer is missing" in {
        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, CheckMode, emptyUserAnswers) mustBe
          routes.JourneyRecoveryController.onPageLoad()
      }

      "to CYA when answer is Yes and IndividualContactMethodOptions already answered" in {
        val answers = emptyUserAnswers
          .setOrException(AddIndividualContactMethodsYesNoPage, true)
          .setOrException(
            IndividualContactMethodOptionsPage,
            Set(ContactMethodOptions.Email, ContactMethodOptions.Phone)
          )

        navigator.nextPage(
          AddIndividualContactMethodsYesNoPage,
          CheckMode,
          answers
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "to IndividualContactMethodOptions page when answer is Yes and IndividualContactMethodOptions not yet answered" in {
        val answers = emptyUserAnswers.setOrException(AddIndividualContactMethodsYesNoPage, true)

        navigator.nextPage(
          AddIndividualContactMethodsYesNoPage,
          CheckMode,
          answers
        ) mustBe controllers.add.routes.IndividualContactMethodOptionsController.onPageLoad(CheckMode)
      }

      "to CYA when answer is No" in {
        val answers = emptyUserAnswers.set(AddIndividualContactMethodsYesNoPage, false).success.value

        navigator.nextPage(
          AddIndividualContactMethodsYesNoPage,
          CheckMode,
          answers
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }

      "to JourneyRecovery when answer is not present" in {
        navigator.nextPage(
          AddIndividualContactMethodsYesNoPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe journeyRecovery
      }

      "must go from IndividualContactMethodOptions" - {
        "to IndividualEmailAddressPage when Email is selected and Email answer not exist" in {
          val answers = emptyUserAnswers
            .set(
              IndividualContactMethodOptionsPage,
              Set(ContactMethodOptions.Email)
            )
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.routes.IndividualEmailAddressController.onPageLoad(CheckMode)
        }

        "to IndividualEmailAddressPage when Email, Phone and Mobile are selected and no Email answer not exist" in {
          val answers = emptyUserAnswers
            .set(
              IndividualContactMethodOptionsPage,
              Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
            )
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.routes.IndividualEmailAddressController.onPageLoad(CheckMode)
        }

        "to CYA when only Email is selected and Email answer exists" in {
          val answers = emptyUserAnswers
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Email))
            .success
            .value
            .set(IndividualEmailAddressPage, "test@test.com")
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
        }

        "to IndividualPhoneNumberPage when Phone is selected (Email is not selected) and Phone answer not exists" in {
          val answers = emptyUserAnswers
            .set(
              IndividualContactMethodOptionsPage,
              Set(ContactMethodOptions.Phone)
            )
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.routes.IndividualPhoneNumberController.onPageLoad(CheckMode)
        }

        "to CYA when only Phone is selected and Phone answer exists" in {
          val answers = emptyUserAnswers
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Phone))
            .success
            .value
            .set(IndividualPhoneNumberPage, "01234567890")
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
        }

        "to CYA when only Mobile is selected and Mobile answer exists" in {
          val answers = emptyUserAnswers
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Mobile))
            .success
            .value
            .set(IndividualMobileNumberPage, "01234567890")
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
        }

        "to IndividualPhoneNumberPage when Email and Phone are selected and Email answer exists, Phone answer not exists" in {
          val answers = emptyUserAnswers
            .set(IndividualContactMethodOptionsPage, Set(ContactMethodOptions.Email, ContactMethodOptions.Phone))
            .success
            .value
            .set(IndividualEmailAddressPage, "test@test.com")
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.routes.IndividualPhoneNumberController.onPageLoad(CheckMode)
        }

        "to IndividualMobileNumberPage when Email, Phone and Mobile are selected and Email and Phone answer exists, Mobile answer not exists" in {
          val answers = emptyUserAnswers
            .set(
              IndividualContactMethodOptionsPage,
              Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
            )
            .success
            .value
            .set(IndividualEmailAddressPage, "test@test.com")
            .success
            .value
            .set(IndividualPhoneNumberPage, "01234567890")
            .success
            .value

          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            CheckMode,
            answers
          ) mustBe controllers.add.routes.IndividualMobileNumberController.onPageLoad(CheckMode)
        }

        "to JourneyRecovery when answer is not present" in {
          navigator.nextPage(
            IndividualContactMethodOptionsPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }

      "must go from AddIndividualContactMethodsYesNo" - {
        "to IndividualContactMethodOptionsController page when answer is Yes" in {
          val answers = emptyUserAnswers.set(AddIndividualContactMethodsYesNoPage, true).success.value

          navigator.nextPage(
            AddIndividualContactMethodsYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.routes.IndividualContactMethodOptionsController
            .onPageLoad(CheckMode)
        }

        "to CYA when answer is No" in {
          val answers = emptyUserAnswers.set(AddIndividualContactMethodsYesNoPage, false).success.value

          navigator.nextPage(
            AddIndividualContactMethodsYesNoPage,
            CheckMode,
            answers
          ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
        }

        "to JourneyRecovery when answer is not present" in {
          navigator.nextPage(
            AddIndividualContactMethodsYesNoPage,
            CheckMode,
            emptyUserAnswers
          ) mustBe journeyRecovery
        }
      }
      "must go from IndividualEmailAddressPage" - {

        "to IndividualCheckYourAnswers when no missing ContactMethodOptions answer" in {
          navigator.nextPage(
            IndividualEmailAddressPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(IndividualEmailAddressPage, "test@test.com")
              .setOrException(IndividualPhoneNumberPage, "1234567")
              .setOrException(IndividualMobileNumberPage, "1234567")
          ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
        }

        "to IndividualPhoneNumberPage when IndividualPhoneNumber is missing from ContactMethodOptions answer" in {
          navigator.nextPage(
            IndividualEmailAddressPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(IndividualEmailAddressPage, "test@test.com")
              .setOrException(IndividualMobileNumberPage, "1234567")
          ) mustBe controllers.add.routes.IndividualPhoneNumberController.onPageLoad(CheckMode)
        }

        "to IndividualMobileNumberPage when IndividualMobileNumber is missing from ContactMethodOptions answer" in {
          navigator.nextPage(
            IndividualEmailAddressPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(IndividualEmailAddressPage, "test@test.com")
              .setOrException(IndividualPhoneNumberPage, "1234567")
          ) mustBe controllers.add.routes.IndividualMobileNumberController.onPageLoad(CheckMode)
        }

        "to JourneyRecovery when IndividualContactMethodOptions answer is not present" in {
          navigator.nextPage(
            IndividualEmailAddressPage,
            CheckMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecovery when Email is not in the selected IndividualContactMethodOptions" in {
          navigator.nextPage(
            IndividualEmailAddressPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from IndividualPhoneNumberPage" - {

        "to CheckYourAnswers when no missing ContactMethodOptions answer" in {
          navigator.nextPage(
            IndividualPhoneNumberPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(IndividualEmailAddressPage, "test@test.com")
              .setOrException(IndividualPhoneNumberPage, "1234567")
              .setOrException(IndividualMobileNumberPage, "1234567")
          ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
        }

        "to IndividualMobileNumberPage when IndividualMobileNumber is missing from ContactMethodOptions answer" in {
          navigator.nextPage(
            IndividualPhoneNumberPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(IndividualEmailAddressPage, "test@test.com")
              .setOrException(IndividualPhoneNumberPage, "1234567")
          ) mustBe controllers.add.routes.IndividualMobileNumberController.onPageLoad(CheckMode)
        }

        "to JourneyRecovery when IndividualContactMethodOptions answer is not present" in {
          navigator.nextPage(
            IndividualPhoneNumberPage,
            CheckMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecovery when Phone is not in the selected IndividualContactMethodOptions" in {
          navigator.nextPage(
            IndividualPhoneNumberPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Mobile)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go from IndividualMobileNumberPage" - {

        "to CheckYourAnswers" in {
          navigator.nextPage(
            IndividualMobileNumberPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
              )
              .setOrException(IndividualEmailAddressPage, "test@test.com")
              .setOrException(IndividualPhoneNumberPage, "1234567")
              .setOrException(IndividualMobileNumberPage, "1234567")
          ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
        }

        "to JourneyRecovery when IndividualContactMethodOptions answer is not present" in {
          navigator.nextPage(
            IndividualMobileNumberPage,
            CheckMode,
            UserAnswers("id")
          ) mustBe journeyRecovery
        }

        "to JourneyRecovery when Mobile is not in the selected IndividualContactMethodOptions" in {
          navigator.nextPage(
            IndividualMobileNumberPage,
            CheckMode,
            emptyUserAnswers
              .setOrException(
                IndividualContactMethodOptionsPage,
                Set(ContactMethodOptions.Email, ContactMethodOptions.Phone)
              )
          ) mustBe journeyRecovery
        }
      }

      "must go to CheckYourAnswersController for an unknown page in CheckMode (default case _)" in {
        case object UnknownPage extends QuestionPage[String] {
          override def path: JsPath = JsPath \ "unknown-page"
        }

        navigator.nextPage(
          UnknownPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe controllers.add.routes.CheckYourAnswersController.onPageLoad()
      }
    }

    "navigatorFromSubTradingNameYesNoPage in NormalMode" - {

      "must go to TradingNameOfSubcontractorController when answer is Yes" in {
        val ua        = emptyUserAnswers.set(SubTradingNameYesNoPage, true).success.value
        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, NormalMode, ua) mustBe
          controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(NormalMode)
      }

      "must go to SubcontractorNameController when answer is No" in {
        val ua        = emptyUserAnswers.set(SubTradingNameYesNoPage, false).success.value
        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, NormalMode, ua) mustBe
          controllers.add.routes.SubcontractorNameController.onPageLoad(NormalMode)
      }

      "must route to JourneyRecovery when SubTradingNameYesNoPage answer is missing" in {
        val navigator = new IndividualNavigator()
        navigator.nextPage(SubTradingNameYesNoPage, NormalMode, emptyUserAnswers) mustBe
          routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from SubAddressYesNoPage to CYA when true and AddressOfSubcontractorPage is already answered" in {
        val addressSample = models.address.Address(
          addressLine1 = "10 Example Street",
          addressLine2 = Some("Suite 2"),
          addressLine3 = Some("Newcastle"),
          addressLine4 = Some("Tyne & Wear"),
          postcode = Some("NE1 1AA"),
          country = Some(models.address.Country(Some("GB"), Some("United Kingdom")))
        )

        val ua     =
          emptyUserAnswers
            .set(SubAddressYesNoPage, true)
            .success
            .value
            .set(AddressOfSubcontractorPage, addressSample)
            .success
            .value
        val result = navigator.nextPage(SubAddressYesNoPage, CheckMode, ua)
        result mustBe CYA
      }

      "must go from NationalInsuranceNumberYesNoPage to CYA when true and SubNationalInsuranceNumberPage is already answered" in {
        val ua =
          emptyUserAnswers
            .set(NationalInsuranceNumberYesNoPage, true)
            .success
            .value
            .set(SubNationalInsuranceNumberPage, "AB123456C")
            .success
            .value // sample valid NINO

        val result = navigator.nextPage(NationalInsuranceNumberYesNoPage, CheckMode, ua)
        result mustBe CYA
      }

      "must go from UniqueTaxpayerReferenceYesNoPage to CYA when true and SubcontractorsUniqueTaxpayerReferencePage is already answered" in {
        val ua =
          emptyUserAnswers
            .set(UniqueTaxpayerReferenceYesNoPage, true)
            .success
            .value
            .set(SubcontractorsUniqueTaxpayerReferencePage, "1234567890")
            .success
            .value

        val result = navigator.nextPage(UniqueTaxpayerReferenceYesNoPage, CheckMode, ua)
        result mustBe CYA
      }

      "must go from WorksReferenceNumberYesNoPage to CYA when true and WorksReferenceNumberPage is already answered" in {
        val ua =
          emptyUserAnswers
            .set(WorksReferenceNumberYesNoPage, true)
            .success
            .value
            .set(WorksReferenceNumberPage, "WRN-001")
            .success
            .value // sample WRN

        val result = navigator.nextPage(WorksReferenceNumberYesNoPage, CheckMode, ua)
        result mustBe CYA
      }

    }

  }

}

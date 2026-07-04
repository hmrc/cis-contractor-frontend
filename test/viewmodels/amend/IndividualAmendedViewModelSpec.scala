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

package viewmodels.amend

import base.SpecBase
import models.add.SubcontractorName
import models.address.{Address, Country}
import models.amend.OriginalIndividualAnswers
import models.contact.ContactOptions
import pages.add.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class IndividualAmendedViewModelSpec extends SpecBase {
  implicit val msgs: Messages = messages(app)
  private val original =
    OriginalIndividualAnswers(
      usesTradingName = Some(false),
      tradingName = None,
      subcontractorName = Some(
        SubcontractorName(
          firstName = "John",
          middleName = Some("A"),
          lastName = "Smith"
        )
      ),
      address = Some(
        Address(
          addressLine1 = "1 High Street",
          addressLine3 = Some("Leeds"),
          postcode = Some("SA1 1AA"),
          country = Some(Country(code = None, name = Some("England")))
        )
      ),
      contactMethod = Some(ContactOptions.Email),
      contactValue = Some("john@test.com"),
      utr = Some("1234567890"),
      nino = Some("AB123456C"),
      worksReference = Some("WR123")
    )

  "rows" - {

    "must return no rows when nothing has changed" in {

      val answers =
        emptyUserAnswers
          .set(SubTradingNameYesNoPage, false).success.value
          .set(
            SubcontractorNamePage,
            SubcontractorName("John", Some("A"), "Smith")
          ).success.value
          .set(
            AddressOfSubcontractorPage,
            Address(
              addressLine1 = "1 High Street",
              addressLine3 = Some("Leeds"),
              postcode = Some("SA1 1AA"),
              country = Some(Country(code = None, name = Some("England")))
            )
          ).success.value
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email).success.value
          .set(IndividualEmailAddressPage, "john@test.com").success.value
          .set(SubcontractorsUniqueTaxpayerReferencePage, "1234567890").success.value
          .set(SubNationalInsuranceNumberPage, "AB123456C").success.value
          .set(WorksReferenceNumberPage, "WR123").success.value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result mustBe empty
    }

    "must return a name row when the name changes" in {

      val answers =
        emptyUserAnswers
          .set(SubTradingNameYesNoPage, false).success.value
          .set(
            SubcontractorNamePage,
            SubcontractorName("Jane", None, "Smith")
          ).success.value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result must have size 1

      result.head.head.content mustBe Text(msgs("subcontractorName.checkYourAnswersLabel"))
    }

    "must return an address row when the address changes" in {

      val answers =
        emptyUserAnswers
          .set(
            AddressOfSubcontractorPage,
            Address(
              addressLine1 = "2 High Street",
              addressLine3 = Some("Leeds"),
              postcode = Some("SA1 1AA"),
              country = Some(Country(code = None, name = Some("England")))
            )
          ).success.value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result.head.head.content mustBe Text(
        msgs("addressOfSubcontractor.checkYourAnswersLabel")
      )
    }

    "must return a contact method row when the contact method changes" in {

      val answers =
        emptyUserAnswers
          .set(IndividualChooseContactDetailsPage, ContactOptions.Phone).success.value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result.head.head.content mustBe Text(
        msgs("individualChooseContactDetails.checkYourAnswersLabel")
      )
    }

    "must return a contact value row when the contact value changes" in {

      val answers =
        emptyUserAnswers
          .set(IndividualChooseContactDetailsPage, ContactOptions.Email).success.value
          .set(IndividualEmailAddressPage, "new@test.com").success.value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result.head.head.content mustBe Text(
        msgs("individualEmailAddress.checkYourAnswersLabel")
      )
    }

    "must return a UTR row when the UTR changes" in {

      val answers =
        emptyUserAnswers
          .set(SubcontractorsUniqueTaxpayerReferencePage, "9999999999").success.value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result.head.head.content mustBe Text(
        msgs("subcontractorsUniqueTaxpayerReference.checkYourAnswersLabel")
      )
    }

    "must return a NINO row when the NINO changes" in {

      val answers =
        emptyUserAnswers
          .set(SubNationalInsuranceNumberPage, "CD123456E").success.value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result.head.head.content mustBe Text(
        msgs("subNationalInsuranceNumber.checkYourAnswersLabel")
      )
    }

    "must return a works reference row when the works reference changes" in {

      val answers =
        emptyUserAnswers
          .set(WorksReferenceNumberPage, "WR999").success.value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result.head.head.content mustBe Text(
        msgs("worksReferenceNumber.checkYourAnswersLabel")
      )
    }

    "must use the trading name label when the user uses a trading name" in {

      val answers =
        emptyUserAnswers
          .set(SubTradingNameYesNoPage, true).success.value
          .set(TradingNameOfSubcontractorPage, "ABC Contractors").success.value

      val result =
        IndividualAmendedViewModel.rows(original, answers)

      result.head.head.content mustBe Text(
        msgs("tradingNameOfSubcontractor.checkYourAnswersLabel")
      )
    }
  }
}

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

package models.add

import base.SpecBase
import models.contact.ContactOptions
import models.{InvalidAnswer, MissingAnswer}
import org.scalatest.Inside.inside
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.add.*
import play.api.libs.json._
import models.RichJsObject

class ValidatedSubcontractorSpec extends SpecBase with Matchers {

  private val address = InternationalAddress(
    addressLine1 = "10 Downing Street",
    addressLine2 = Some("Westminster"),
    addressLine3 = "London",
    addressLine4 = Some("Greater London"),
    postalCode = "SW1A 2AA",
    country = "United Kingdom"
  )

  private val subcontractorName = SubcontractorName("firstname", Some("middle name"), "lastname")

  private val minRequired =
    emptyUserAnswers
      .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
      .success
      .value
      .set(SubTradingNameYesNoPage, true)
      .success
      .value
      .set(TradingNameOfSubcontractorPage, "ABC Ltd")
      .success
      .value
      .set(SubAddressYesNoPage, false)
      .success
      .value
      .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
      .success
      .value
      .set(UniqueTaxpayerReferenceYesNoPage, false)
      .success
      .value
      .set(NationalInsuranceNumberYesNoPage, false)
      .success
      .value
      .set(WorksReferenceNumberYesNoPage, false)
      .success
      .value

  private def withStaleValue[A](
    ua: models.UserAnswers,
    page: pages.QuestionPage[A],
    value: A
  )(implicit w: Writes[A]): models.UserAnswers =
    ua.data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(updated, _) => ua.copy(data = updated)
      case JsError(_)            => ua
    }

  ".build" - {
    "must return a ValidatedSubcontractor when all mandatory questions are answered" - {

      "and all optional data is present with trading name" in {

        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value
            .set(SubAddressYesNoPage, true)
            .success
            .value
            .set(AddressOfSubcontractorPage, address)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, true)
            .success
            .value
            .set(SubNationalInsuranceNumberPage, "AB123456C")
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, true)
            .success
            .value
            .set(SubcontractorsUniqueTaxpayerReferencePage, "1234567890")
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, true)
            .success
            .value
            .set(WorksReferenceNumberPage, "WRN-001")
            .success
            .value
            .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
            .success
            .value
            .set(IndividualEmailAddressPage, "abc@test.com")
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        result mustBe Right(
          ValidatedSubcontractor(
            tradingName = Some("ABC Ltd"),
            subcontractorName = None,
            address = Some(address),
            individualContactDetails = ContactOptions.Email,
            individualEmail = Some("abc@test.com"),
            individualPhone = None,
            individualMobile = None,
            nino = Some("AB123456C"),
            utr = Some("1234567890"),
            workRefNumber = Some("WRN-001")
          )
        )
      }

      "and all optional data is present with subcontractor name" in {

        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, false)
            .success
            .value
            .set(SubcontractorNamePage, subcontractorName)
            .success
            .value
            .set(SubAddressYesNoPage, true)
            .success
            .value
            .set(AddressOfSubcontractorPage, address)
            .success
            .value
            .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
            .success
            .value
            .set(IndividualPhoneNumberPage, "098765433452")
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, true)
            .success
            .value
            .set(SubNationalInsuranceNumberPage, "AB123456C")
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, true)
            .success
            .value
            .set(SubcontractorsUniqueTaxpayerReferencePage, "1234567890")
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, true)
            .success
            .value
            .set(WorksReferenceNumberPage, "WRN-001")
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        result mustBe Right(
          ValidatedSubcontractor(
            tradingName = None,
            subcontractorName = Some(subcontractorName),
            address = Some(address),
            individualContactDetails = ContactOptions.Phone,
            individualEmail = None,
            individualPhone = Some("098765433452"),
            individualMobile = None,
            nino = Some("AB123456C"),
            utr = Some("1234567890"),
            workRefNumber = Some("WRN-001")
          )
        )
      }

      "and all optional data is missing but with trading name" in {

        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, false)
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, false)
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, false)
            .success
            .value
            .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        result mustBe Right(
          ValidatedSubcontractor(
            tradingName = Some("ABC Ltd"),
            subcontractorName = None,
            address = None,
            individualContactDetails = ContactOptions.NoDetails,
            individualEmail = None,
            individualPhone = None,
            individualMobile = None,
            nino = None,
            utr = None,
            workRefNumber = None
          )
        )
      }

      "and all optional data is missing but with subcontractor name" in {

        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, false)
            .success
            .value
            .set(SubcontractorNamePage, subcontractorName)
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value
            .set(IndividualChooseContactDetailsPage, ContactOptions.NoDetails)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, false)
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, false)
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, false)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        result mustBe Right(
          ValidatedSubcontractor(
            tradingName = None,
            subcontractorName = Some(subcontractorName),
            address = None,
            individualContactDetails = ContactOptions.NoDetails,
            individualEmail = None,
            individualPhone = None,
            individualMobile = None,
            nino = None,
            utr = None,
            workRefNumber = None
          )
        )
      }

      "build when contact option is Email and email address is present" in {
        val ua =
          minRequired
            .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
            .success
            .value
            .set(IndividualEmailAddressPage, "a@b.com")
            .success
            .value

        ValidatedSubcontractor.build(ua) mustBe a[Right[?, ?]]
      }

      "build when contact option is phone and phone is present" in {
        val ua =
          minRequired
            .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
            .success
            .value
            .set(IndividualPhoneNumberPage, "098765433452")
            .success
            .value

        ValidatedSubcontractor.build(ua) mustBe a[Right[?, ?]]
      }

      "build when contact option is mobile and mobile is present" in {
        val ua =
          minRequired
            .set(IndividualChooseContactDetailsPage, ContactOptions.Mobile)
            .success
            .value
            .set(IndividualMobileNumberPage, "098765433452")
            .success
            .value

        ValidatedSubcontractor.build(ua) mustBe a[Right[?, ?]]
      }

    }

    "must return error" - {

      "when TypeOfSubcontractorPage is missing" in {
        val result = ValidatedSubcontractor.build(emptyUserAnswers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(TypeOfSubcontractorPage)
        }
      }

      "when SubTradingNameYesNoPage is missing" in {
        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(SubTradingNameYesNoPage)
        }
      }

      "when SubAddressYesNoPage is missing" in {
        val answers =
          minRequired
            .remove(SubAddressYesNoPage)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(SubAddressYesNoPage)
        }
      }

      "when NationalInsuranceNumberYesNoPage is missing" in {
        val answers =
          minRequired
            .remove(NationalInsuranceNumberYesNoPage)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(NationalInsuranceNumberYesNoPage)
        }
      }

      "when UniqueTaxpayerReferenceYesNoPage is missing" in {
        val answers =
          minRequired
            .remove(UniqueTaxpayerReferenceYesNoPage)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(UniqueTaxpayerReferenceYesNoPage)
        }
      }

      "when WorksReferenceNumberYesNoPage is missing" in {
        val answers =
          minRequired
            .remove(WorksReferenceNumberYesNoPage)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(WorksReferenceNumberYesNoPage)
        }
      }

      "when user said yes to SubTradingNameYesNoPage but TradingNameOfSubcontractorPage is missing" in {
        val answers =
          minRequired
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .remove(TradingNameOfSubcontractorPage)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe InvalidAnswer(TradingNameOfSubcontractorPage)
        }
      }

      "when user said no to SubTradingNameYesNoPage but SubcontractorNamePage is missing" in {
        val answers =
          minRequired
            .set(SubTradingNameYesNoPage, false)
            .success
            .value
            .remove(SubcontractorNamePage)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe InvalidAnswer(SubcontractorNamePage)
        }
      }

      "when user said yes to SubAddressYesNoPage but SubcontractorNamePage is missing" in {
        val answers =
          minRequired
            .set(SubAddressYesNoPage, true)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe InvalidAnswer(AddressOfSubcontractorPage)
        }
      }

      "when user said yes to NationalInsuranceNumberYesNoPage but SubNationalInsuranceNumberPage is missing" in {
        val answers =
          minRequired
            .set(NationalInsuranceNumberYesNoPage, true)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe InvalidAnswer(SubNationalInsuranceNumberPage)
        }
      }

      "when user said yes to UniqueTaxpayerReferenceYesNoPage but SubcontractorsUniqueTaxpayerReferencePage is missing" in {
        val answers =
          minRequired
            .set(UniqueTaxpayerReferenceYesNoPage, true)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe InvalidAnswer(SubcontractorsUniqueTaxpayerReferencePage)
        }
      }

      "when user said yes to WorksReferenceNumberYesNoPage but WorksReferenceNumberPage is missing" in {
        val answers =
          minRequired
            .set(WorksReferenceNumberYesNoPage, true)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe InvalidAnswer(WorksReferenceNumberPage)
        }
      }

      "when user skipped SubTradingNameYesNoPage but answered TradingNameOfSubcontractorPage" in {
        val answers =
          minRequired
            .remove(SubTradingNameYesNoPage)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(SubTradingNameYesNoPage)
        }
      }

      "when user skipped SubTradingNameYesNoPage but answered SubcontractorNamePage" in {
        val answers =
          minRequired
            .remove(SubTradingNameYesNoPage)
            .success
            .value
            .set(SubcontractorNamePage, subcontractorName)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(SubTradingNameYesNoPage)
        }
      }

      "when user skipped SubAddressYesNoPage but answered AddressOfSubcontractorPage" in {
        val answers =
          minRequired
            .remove(SubAddressYesNoPage)
            .success
            .value
            .set(AddressOfSubcontractorPage, address)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(SubAddressYesNoPage)
        }
      }

      "when the user skipped NationalInsuranceNumberYesNoPage but answered SubNationalInsuranceNumberPage" in {
        val answers =
          minRequired
            .remove(NationalInsuranceNumberYesNoPage)
            .success
            .value
            .set(SubNationalInsuranceNumberPage, "AB012345C")
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(NationalInsuranceNumberYesNoPage)
        }
      }

      "when the user skipped UniqueTaxpayerReferenceYesNoPage but answered SubcontractorsUniqueTaxpayerReferencePage" in {
        val answers =
          minRequired
            .remove(UniqueTaxpayerReferenceYesNoPage)
            .success
            .value
            .set(SubcontractorsUniqueTaxpayerReferencePage, "012345678")
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(UniqueTaxpayerReferenceYesNoPage)
        }
      }

      "when the user skipped WorksReferenceNumberYesNoPage but answered WorksReferenceNumberPage" in {
        val answers =
          minRequired
            .remove(WorksReferenceNumberYesNoPage)
            .success
            .value
            .set(WorksReferenceNumberPage, "WRN-001")
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(WorksReferenceNumberYesNoPage)
        }
      }

      "when user said yes to SubTradingNameYesNoPage and answered SubcontractorNamePage" in {
        val answers =
          minRequired
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(SubcontractorNamePage, subcontractorName)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe InvalidAnswer(SubcontractorNamePage)
        }
      }

      "when user said no to SubTradingNameYesNoPage and answered TradingNameOfSubcontractorPage" in {
        val answers =
          minRequired
            .set(SubTradingNameYesNoPage, false)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe InvalidAnswer(TradingNameOfSubcontractorPage)
        }
      }

      "fail when SubTradingNameYesNo is false but TradingNameOfSubcontractor value is still present (stale session)" in {

        val userAnswers =
          minRequired
            .set(SubTradingNameYesNoPage, false)
            .success
            .value
            .set(SubcontractorNamePage, subcontractorName)
            .success
            .value

        val uaWithStale = withStaleValue(userAnswers, TradingNameOfSubcontractorPage, "ABC Ltd")

        ValidatedSubcontractor.build(uaWithStale) mustBe Left(InvalidAnswer(TradingNameOfSubcontractorPage))
      }

      "fail when SubTradingNameYesNo is true but SubcontractorName value is still present (stale session)" in {

        val userAnswers =
          minRequired
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value

        val uaWithStale = withStaleValue(userAnswers, SubcontractorNamePage, subcontractorName)

        ValidatedSubcontractor.build(uaWithStale) mustBe Left(InvalidAnswer(SubcontractorNamePage))
      }

      "fail when AddressYesNo is false but address value is still present (stale session)" in {
        val ua = withStaleValue(minRequired, AddressOfSubcontractorPage, address)

        ValidatedSubcontractor.build(ua) mustBe Left(InvalidAnswer(AddressOfSubcontractorPage))
      }

      "fail when UniqueTaxpayerReferenceYesNo is false but utr value is still present (stale session)" in {
        val ua = withStaleValue(minRequired, SubcontractorsUniqueTaxpayerReferencePage, "012345678")

        ValidatedSubcontractor.build(ua) mustBe Left(InvalidAnswer(SubcontractorsUniqueTaxpayerReferencePage))
      }

      "fail when NationalInsuranceNumberYesNo is false but nino value is still present (stale session)" in {
        val ua = withStaleValue(minRequired, SubNationalInsuranceNumberPage, "AB012345C")

        ValidatedSubcontractor.build(ua) mustBe Left(InvalidAnswer(SubNationalInsuranceNumberPage))
      }

      "fail when WorksReferenceNumberYesNo is false but wrn value is still present (stale session)" in {
        val ua = withStaleValue(minRequired, WorksReferenceNumberPage, "WRN-001")

        ValidatedSubcontractor.build(ua) mustBe Left(InvalidAnswer(WorksReferenceNumberPage))
      }

      "require email address when contact option is Email" in {
        val ua =
          minRequired
            .set(IndividualChooseContactDetailsPage, ContactOptions.Email)
            .success
            .value

        ValidatedSubcontractor.build(ua) mustBe Left(MissingAnswer(IndividualEmailAddressPage))
      }

      "fail when contact option is NoDetails but stale email exists" in {
        val ua = withStaleValue(minRequired, IndividualEmailAddressPage, "stale@x.com")

        ValidatedSubcontractor.build(ua) mustBe Left(InvalidAnswer(IndividualEmailAddressPage))
      }

      "require phone number when contact option is phone" in {
        val ua =
          minRequired
            .set(IndividualChooseContactDetailsPage, ContactOptions.Phone)
            .success
            .value

        ValidatedSubcontractor.build(ua) mustBe Left(MissingAnswer(IndividualPhoneNumberPage))
      }

      "fail when contact option is NoDetails but stale phone exists" in {
        val ua = withStaleValue(minRequired, IndividualPhoneNumberPage, "012345678")

        ValidatedSubcontractor.build(ua) mustBe Left(InvalidAnswer(IndividualPhoneNumberPage))
      }

      "require mobile number when contact option is mobile" in {
        val ua =
          minRequired
            .set(IndividualChooseContactDetailsPage, ContactOptions.Mobile)
            .success
            .value

        ValidatedSubcontractor.build(ua) mustBe Left(MissingAnswer(IndividualMobileNumberPage))
      }

      "fail when contact option is NoDetails but stale mobile exists" in {
        val ua = withStaleValue(minRequired, IndividualMobileNumberPage, "012345678")

        ValidatedSubcontractor.build(ua) mustBe Left(InvalidAnswer(IndividualMobileNumberPage))
      }
    }
  }
}

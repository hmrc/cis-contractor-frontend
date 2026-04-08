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

package models.add.trust

import base.SpecBase
import models.RichJsObject
import models.add.{InternationalAddress, TypeOfSubcontractor}
import models.contact.ContactOptions
import models.contact.ContactOptions.*
import models.{InvalidAnswer, MissingAnswer, UserAnswers}
import org.scalatest.matchers.must.Matchers
import pages.add.TypeOfSubcontractorPage
import pages.add.trust.*
import play.api.libs.json.{JsError, JsSuccess, Json, Writes}

class ValidatedTrustSpec extends SpecBase with Matchers {

  private val minRequired: UserAnswers =
    emptyUserAnswers
      .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
      .success
      .value
      .set(TrustNamePage, "Test Trust")
      .success
      .value
      .set(TrustAddressYesNoPage, false)
      .success
      .value
      .set(TrustContactOptionsPage, ContactOptions.NoDetails)
      .success
      .value
      .set(TrustUtrYesNoPage, false)
      .success
      .value
      .set(TrustWorksReferenceYesNoPage, false)
      .success
      .value

  private def withStaleValue[A](
    ua: UserAnswers,
    page: pages.QuestionPage[A],
    value: A
  )(implicit w: Writes[A]): UserAnswers =
    ua.data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(updated, _) => ua.copy(data = updated)
      case JsError(_)            => ua
    }

  "ValidatedTrust.build" - {

    "build successfully with minimum required answers (NoDetails + all optionals No)" in {
      ValidatedTrust.build(minRequired) mustBe a[Right[?, ?]]
    }

    "fail when TypeOfSubcontractor is not Trust" in {
      val ua =
        minRequired
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
          .success
          .value

      ValidatedTrust.build(ua) mustBe Left(InvalidAnswer(TypeOfSubcontractorPage))
    }

    "fail when a required page is missing (TrustNamePage)" in {
      val ua =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
          .success
          .value
          .set(TrustAddressYesNoPage, false)
          .success
          .value
          .set(TrustContactOptionsPage, ContactOptions.NoDetails)
          .success
          .value
          .set(TrustUtrYesNoPage, false)
          .success
          .value
          .set(TrustWorksReferenceYesNoPage, false)
          .success
          .value

      ValidatedTrust.build(ua) mustBe Left(MissingAnswer(TrustNamePage))
    }

    "contact option validation" - {

      "require email address when contact option is Email" in {
        val ua =
          minRequired
            .set(TrustContactOptionsPage, ContactOptions.Email)
            .success
            .value

        ValidatedTrust.build(ua) mustBe Left(MissingAnswer(TrustEmailAddressPage))
      }

      "build when contact option is Email and email address is present" in {
        val ua =
          minRequired
            .set(TrustContactOptionsPage, ContactOptions.Email)
            .success
            .value
            .set(TrustEmailAddressPage, "a@b.com")
            .success
            .value

        ValidatedTrust.build(ua) mustBe a[Right[?, ?]]
      }

      "require phone number when contact option is Phone" in {
        val ua =
          minRequired
            .set(TrustContactOptionsPage, ContactOptions.Phone)
            .success
            .value

        ValidatedTrust.build(ua) mustBe Left(MissingAnswer(TrustPhoneNumberPage))
      }

      "build when contact option is Phone and phone number is present" in {
        val ua =
          minRequired
            .set(TrustContactOptionsPage, ContactOptions.Phone)
            .success
            .value
            .set(TrustPhoneNumberPage, "01234567890")
            .success
            .value

        ValidatedTrust.build(ua) mustBe a[Right[?, ?]]
      }

      "require mobile number when contact option is Mobile" in {
        val ua =
          minRequired
            .set(TrustContactOptionsPage, ContactOptions.Mobile)
            .success
            .value

        ValidatedTrust.build(ua) mustBe Left(MissingAnswer(TrustMobileNumberPage))
      }

      "build when contact option is Mobile and mobile number is present" in {
        val ua =
          minRequired
            .set(TrustContactOptionsPage, ContactOptions.Mobile)
            .success
            .value
            .set(TrustMobileNumberPage, "07123456789")
            .success
            .value

        ValidatedTrust.build(ua) mustBe a[Right[?, ?]]
      }

      "fail when contact option is NoDetails but stale email exists" in {
        val ua = withStaleValue(minRequired, TrustEmailAddressPage, "stale@x.com")
        ValidatedTrust.build(ua) mustBe Left(InvalidAnswer(TrustEmailAddressPage))
      }

      "fail when contact option is NoDetails but stale phone exists" in {
        val ua = withStaleValue(minRequired, TrustPhoneNumberPage, "01234567890")
        ValidatedTrust.build(ua) mustBe Left(InvalidAnswer(TrustPhoneNumberPage))
      }

      "fail when contact option is NoDetails but stale mobile exists" in {
        val ua = withStaleValue(minRequired, TrustMobileNumberPage, "07123456789")
        ValidatedTrust.build(ua) mustBe Left(InvalidAnswer(TrustMobileNumberPage))
      }
    }

    "optional address validation" - {

      "require address when TrustAddressYesNo is true" in {
        val ua =
          minRequired
            .set(TrustAddressYesNoPage, true)
            .success
            .value

        ValidatedTrust.build(ua) mustBe Left(InvalidAnswer(TrustAddressPage))
      }

      "fail when TrustAddressYesNo is false but address value is still present (stale session)" in {
        val address = InternationalAddress("1", None, "City", None, "AA1 1AA", "GB")
        val ua      = withStaleValue(minRequired, TrustAddressPage, address)

        ValidatedTrust.build(ua) mustBe Left(InvalidAnswer(TrustAddressPage))
      }

      "build when TrustAddressYesNo is true and address is present" in {
        val address = InternationalAddress("1", None, "City", None, "AA1 1AA", "GB")

        val ua =
          minRequired
            .set(TrustAddressYesNoPage, true)
            .success
            .value
            .set(TrustAddressPage, address)
            .success
            .value

        ValidatedTrust.build(ua) mustBe a[Right[?, ?]]
      }
    }

    "optional UTR validation" - {

      "require UTR when TrustUtrYesNo is true" in {
        val ua =
          minRequired
            .set(TrustUtrYesNoPage, true)
            .success
            .value

        ValidatedTrust.build(ua) mustBe Left(InvalidAnswer(TrustUtrPage))
      }

      "fail when TrustUtrYesNo is false but UTR value is still present (stale session)" in {
        val ua = withStaleValue(minRequired, TrustUtrPage, "1234567890")
        ValidatedTrust.build(ua) mustBe Left(InvalidAnswer(TrustUtrPage))
      }

      "build when TrustUtrYesNo is true and UTR is present" in {
        val ua =
          minRequired
            .set(TrustUtrYesNoPage, true)
            .success
            .value
            .set(TrustUtrPage, "1234567890")
            .success
            .value

        ValidatedTrust.build(ua) mustBe a[Right[?, ?]]
      }
    }

    "optional works reference validation" - {

      "require works reference when TrustWorksReferenceYesNo is true" in {
        val ua =
          minRequired
            .set(TrustWorksReferenceYesNoPage, true)
            .success
            .value

        ValidatedTrust.build(ua) mustBe Left(InvalidAnswer(TrustWorksReferencePage))
      }

      "fail when TrustWorksReferenceYesNo is false but works reference remains (stale session)" in {
        val ua = withStaleValue(minRequired, TrustWorksReferencePage, "WRN-001")
        ValidatedTrust.build(ua) mustBe Left(InvalidAnswer(TrustWorksReferencePage))
      }

      "build when TrustWorksReferenceYesNo is true and works reference is present" in {
        val ua =
          minRequired
            .set(TrustWorksReferenceYesNoPage, true)
            .success
            .value
            .set(TrustWorksReferencePage, "WRN-001")
            .success
            .value

        ValidatedTrust.build(ua) mustBe a[Right[?, ?]]
      }
    }
  }
}

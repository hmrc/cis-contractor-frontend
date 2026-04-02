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
package models.add.company

import base.SpecBase
import models.add.{InternationalAddress, TypeOfSubcontractor}
import models.contact.ContactOptions
import models.contact.ContactOptions.*
import models.{InvalidAnswer, MissingAnswer, UserAnswers}
import models.RichJsObject
import pages.QuestionPage
import pages.add.TypeOfSubcontractorPage
import pages.add.company.*
import play.api.libs.json.{JsError, JsSuccess, Json, Writes}
import org.scalatest.matchers.must.Matchers

class ValidatedCompanySpec extends SpecBase with Matchers {

  private val minRequired: UserAnswers =
    emptyUserAnswers
      .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
      .success
      .value
      .set(CompanyNamePage, "Test Company Ltd")
      .success
      .value
      .set(CompanyAddressYesNoPage, false)
      .success
      .value
      .set(CompanyContactOptionsPage, ContactOptions.NoDetails)
      .success
      .value
      .set(CompanyUtrYesNoPage, false)
      .success
      .value
      .set(CompanyCrnYesNoPage, false)
      .success
      .value
      .set(CompanyWorksReferenceYesNoPage, false)
      .success
      .value

  private def withStaleValue[A](
    ua: UserAnswers,
    page: QuestionPage[A],
    value: A
  )(implicit w: Writes[A]): UserAnswers =
    ua.data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(updated, _) => ua.copy(data = updated)
      case JsError(_)            => ua
    }

  "ValidatedCompany.build" - {

    "build successfully with minimum required answers (NoDetails + all optionals No)" in {
      ValidatedCompany.build(minRequired) mustBe a[Right[?, ?]]
    }

    "fail when TypeOfSubcontractor is not Limitedcompany" in {
      val ua =
        minRequired
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
          .success
          .value

      ValidatedCompany.build(ua) mustBe Left(InvalidAnswer(TypeOfSubcontractorPage))
    }

    "fail when a required page is missing (CompanyNamePage)" in {
      val ua =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Limitedcompany)
          .success
          .value

      ValidatedCompany.build(ua) mustBe Left(MissingAnswer(CompanyNamePage))
    }

    "contact option validation" - {

      "require email address when contact option is Email" in {
        val ua =
          minRequired
            .set(CompanyContactOptionsPage, ContactOptions.Email)
            .success
            .value

        ValidatedCompany.build(ua) mustBe Left(MissingAnswer(CompanyEmailAddressPage))
      }

      "build when contact option is Email and email address is present" in {
        val ua =
          minRequired
            .set(CompanyContactOptionsPage, ContactOptions.Email)
            .success
            .value
            .set(CompanyEmailAddressPage, "a@b.com")
            .success
            .value

        ValidatedCompany.build(ua) mustBe a[Right[?, ?]]
      }

      "require phone number when contact option is Phone" in {
        val ua =
          minRequired
            .set(CompanyContactOptionsPage, ContactOptions.Phone)
            .success
            .value

        ValidatedCompany.build(ua) mustBe Left(MissingAnswer(CompanyPhoneNumberPage))
      }

      "build when contact option is Phone and phone number is present" in {
        val ua =
          minRequired
            .set(CompanyContactOptionsPage, ContactOptions.Phone)
            .success
            .value
            .set(CompanyPhoneNumberPage, "02071234567")
            .success
            .value

        ValidatedCompany.build(ua) mustBe a[Right[?, ?]]
      }

      "require mobile number when contact option is Mobile" in {
        val ua =
          minRequired
            .set(CompanyContactOptionsPage, ContactOptions.Mobile)
            .success
            .value

        ValidatedCompany.build(ua) mustBe Left(MissingAnswer(CompanyMobileNumberPage))
      }

      "build when contact option is Mobile and mobile number is present" in {
        val ua =
          minRequired
            .set(CompanyContactOptionsPage, ContactOptions.Mobile)
            .success
            .value
            .set(CompanyMobileNumberPage, "07123456789")
            .success
            .value

        ValidatedCompany.build(ua) mustBe a[Right[?, ?]]
      }

      "fail when contact option is NoDetails but stale email exists" in {
        val ua = withStaleValue(minRequired, CompanyEmailAddressPage, "stale@x.com")
        ValidatedCompany.build(ua) mustBe Left(InvalidAnswer(CompanyEmailAddressPage))
      }

      "fail when contact option is NoDetails but stale phone exists" in {
        val ua = withStaleValue(minRequired, CompanyPhoneNumberPage, "02070000000")
        ValidatedCompany.build(ua) mustBe Left(InvalidAnswer(CompanyPhoneNumberPage))
      }

      "fail when contact option is NoDetails but stale mobile exists" in {
        val ua = withStaleValue(minRequired, CompanyMobileNumberPage, "07111111111")
        ValidatedCompany.build(ua) mustBe Left(InvalidAnswer(CompanyMobileNumberPage))
      }
    }

    "address validation" - {

      "fail when AddressYesNo is true but CompanyAddressPage is missing" in {
        val ua =
          minRequired
            .set(CompanyAddressYesNoPage, true)
            .success
            .value

        ValidatedCompany.build(ua) mustBe Left(InvalidAnswer(CompanyAddressPage))
      }

      "fail when AddressYesNo is false but CompanyAddressPage is still present (stale session)" in {
        val address = InternationalAddress("1", None, "City", None, "AA1 1AA", "GB")
        val ua      = withStaleValue(minRequired, CompanyAddressPage, address)

        ValidatedCompany.build(ua) mustBe Left(InvalidAnswer(CompanyAddressPage))
      }
    }

    "UTR validation" - {

      "fail when UtrYesNo is true but CompanyUtrPage is missing" in {
        val ua =
          minRequired
            .set(CompanyUtrYesNoPage, true)
            .success
            .value

        ValidatedCompany.build(ua) mustBe Left(InvalidAnswer(CompanyUtrPage))
      }

      "fail when UtrYesNo is false but CompanyUtrPage is still present (stale session)" in {
        val ua = withStaleValue(minRequired, CompanyUtrPage, "1234567890")
        ValidatedCompany.build(ua) mustBe Left(InvalidAnswer(CompanyUtrPage))
      }
    }

    "CRN validation" - {

      "fail when CrnYesNo is true but CompanyCrnPage is missing" in {
        val ua =
          minRequired
            .set(CompanyCrnYesNoPage, true)
            .success
            .value

        ValidatedCompany.build(ua) mustBe Left(InvalidAnswer(CompanyCrnPage))
      }

      "fail when CrnYesNo is false but CompanyCrnPage is still present (stale session)" in {
        val ua = withStaleValue(minRequired, CompanyCrnPage, "AC012345")
        ValidatedCompany.build(ua) mustBe Left(InvalidAnswer(CompanyCrnPage))
      }
    }

    "Works reference validation" - {

      "fail when WorksReferenceYesNo is true but CompanyWorksReferencePage is missing" in {
        val ua =
          minRequired
            .set(CompanyWorksReferenceYesNoPage, true)
            .success
            .value

        ValidatedCompany.build(ua) mustBe Left(InvalidAnswer(CompanyWorksReferencePage))
      }

      "fail when WorksReferenceYesNo is false but CompanyWorksReferencePage is still present (stale session)" in {
        val ua = withStaleValue(minRequired, CompanyWorksReferencePage, "WRN-001")
        ValidatedCompany.build(ua) mustBe Left(InvalidAnswer(CompanyWorksReferencePage))
      }
    }
  }
}

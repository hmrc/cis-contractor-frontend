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

package models.add.partnership

import base.SpecBase
import models.add.{InternationalAddress, TypeOfSubcontractor}
import models.contact.ContactOptions
import models.contact.ContactOptions.*
import models.{InvalidAnswer, MissingAnswer}
import pages.add.TypeOfSubcontractorPage
import pages.add.partnership.*
import play.api.libs.json._
import org.scalatest.matchers.must.Matchers
import models.RichJsObject

class ValidatedPartnershipSpec extends SpecBase with Matchers {

  private val minRequired =
    emptyUserAnswers
      .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership).success.value
      .set(PartnershipNamePage, "Test Partnership").success.value
      .set(PartnershipAddressYesNoPage, false).success.value
      .set(PartnershipChooseContactDetailsPage, ContactOptions.NoDetails).success.value
      .set(PartnershipHasUtrYesNoPage, false).success.value
      .set(PartnershipNominatedPartnerNamePage, "Nominated Partner").success.value
      .set(PartnershipNominatedPartnerUtrYesNoPage, false).success.value
      .set(PartnershipNominatedPartnerNinoYesNoPage, false).success.value
      .set(PartnershipNominatedPartnerCrnYesNoPage, false).success.value
      .set(PartnershipWorksReferenceNumberYesNoPage, false).success.value

  private def withStaleValue[A](
                                 ua: models.UserAnswers,
                                 page: pages.QuestionPage[A],
                                 value: A
                               )(implicit w: Writes[A]): models.UserAnswers =
    ua.data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(updated, _) => ua.copy(data = updated)
      case JsError(_)            => ua
    }


  "ValidatedPartnership.build" - {

    "build successfully with minimum required answers (NoDetails + all optionals No)" in {
      ValidatedPartnership.build(minRequired) mustBe a[Right[?, ?]]
    }

    "fail when TypeOfSubcontractor is not Partnership" in {
      val ua =
        minRequired
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader).success.value

      ValidatedPartnership.build(ua) mustBe Left(InvalidAnswer(TypeOfSubcontractorPage))
    }

    "fail when a required page is missing (PartnershipNamePage)" in {
      val ua = emptyUserAnswers
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership).success.value

      ValidatedPartnership.build(ua) mustBe Left(MissingAnswer(PartnershipNamePage))
    }

    "require email address when contact option is Email" in {
      val ua =
        minRequired
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Email).success.value

   
      ValidatedPartnership.build(ua) mustBe Left(MissingAnswer(PartnershipEmailAddressPage))
    }

    "build when contact option is Email and email address is present" in {
      val ua =
        minRequired
          .set(PartnershipChooseContactDetailsPage, ContactOptions.Email).success.value
          .set(PartnershipEmailAddressPage, "a@b.com").success.value

      ValidatedPartnership.build(ua) mustBe a[Right[?, ?]]
    }

    "fail when contact option is NoDetails but stale email exists" in {
      val ua = withStaleValue(minRequired, PartnershipEmailAddressPage, "stale@x.com")

      ValidatedPartnership.build(ua) mustBe Left(InvalidAnswer(PartnershipEmailAddressPage))
    }

    "require address when AddressYesNo is true" in {
      val ua =
        minRequired
          .set(PartnershipAddressYesNoPage, true).success.value

      ValidatedPartnership.build(ua) mustBe Left(InvalidAnswer(PartnershipAddressPage))
    }

    "fail when AddressYesNo is false but address value is still present (stale session)" in {
      val address = InternationalAddress("1", None, "City", None, "AA1 1AA", "GB")
      val ua      = withStaleValue(minRequired, PartnershipAddressPage, address)

    
      ValidatedPartnership.build(ua) mustBe Left(InvalidAnswer(PartnershipAddressPage))
    }

    "require partnership UTR when HasUtrYesNo is true" in {
      val ua =
        minRequired
          .set(PartnershipHasUtrYesNoPage, true).success.value

      ValidatedPartnership.build(ua) mustBe Left(InvalidAnswer(PartnershipUniqueTaxpayerReferencePage))
    }

    "fail when HasUtrYesNo is false but UTR value is still present (stale session)" in {
      val ua = withStaleValue(minRequired, PartnershipUniqueTaxpayerReferencePage, "1234567890")
      ValidatedPartnership.build(ua) mustBe Left(InvalidAnswer(PartnershipUniqueTaxpayerReferencePage))
    }

    "require nominated partner NINO when NinoYesNo is true" in {
      val ua =
        minRequired
          .set(PartnershipNominatedPartnerNinoYesNoPage, true).success.value

      ValidatedPartnership.build(ua) mustBe Left(InvalidAnswer(PartnershipNominatedPartnerNinoPage))
    }

    "fail when NinoYesNo is false but NINO value is still present (stale session)" in {
      val ua = withStaleValue(minRequired, PartnershipNominatedPartnerNinoPage, "AB123456C")
      ValidatedPartnership.build(ua) mustBe Left(InvalidAnswer(PartnershipNominatedPartnerNinoPage))
    }

    "require nominated partner CRN when CrnYesNo is true" in {
      val ua =
        minRequired
          .set(PartnershipNominatedPartnerCrnYesNoPage, true).success.value

      ValidatedPartnership.build(ua) mustBe Left(InvalidAnswer(PartnershipNominatedPartnerCrnPage))
    }

    "fail when CrnYesNo is false but CRN value is still present (stale session)" in {
      val ua = withStaleValue(minRequired, PartnershipNominatedPartnerCrnPage, "12345678")
      ValidatedPartnership.build(ua) mustBe Left(InvalidAnswer(PartnershipNominatedPartnerCrnPage))
    }

    "require works reference number when WorksReferenceNumberYesNo is true" in {
      val ua =
        minRequired
          .set(PartnershipWorksReferenceNumberYesNoPage, true).success.value

      ValidatedPartnership.build(ua) mustBe Left(InvalidAnswer(PartnershipWorksReferenceNumberPage))
    }

    "fail when WorksReferenceNumberYesNo is false but WRN value is still present (stale session)" in {
      val ua = withStaleValue(minRequired, PartnershipWorksReferenceNumberPage, "WRN-001")
      ValidatedPartnership.build(ua) mustBe Left(InvalidAnswer(PartnershipWorksReferenceNumberPage))
    }
  }
}

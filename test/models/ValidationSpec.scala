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

package models

import base.SpecBase
import models.contact.ContactMethodOptions
import org.scalatest.matchers.must.Matchers
import pages.add.partnership.*
import play.api.libs.json.*

class ValidationSpec extends SpecBase with Matchers {

  private object TestValidation extends Validation

  "Validation.getPageValue" - {

    "return the value when the page is present" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, "Test Partnership")
          .success
          .value

      TestValidation.getPageValue(userAnswers, PartnershipNamePage) mustBe
        Right("Test Partnership")
    }

    "return MissingAnswer when the page is missing" in {
      TestValidation.getPageValue(
        emptyUserAnswers,
        PartnershipNamePage
      ) mustBe
        Left(MissingAnswer(PartnershipNamePage))
    }
  }

  "Validation.getOptionalPageValue" - {

    "return None when the yes/no answer is No and the question page is missing" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipHasUtrYesNoPage, false)
          .success
          .value

      TestValidation.getOptionalPageValue(
        userAnswers,
        PartnershipUniqueTaxpayerReferencePage,
        PartnershipHasUtrYesNoPage
      ) mustBe Right(None)
    }

    "return MissingAnswer when the yes/no page is missing" in {
      TestValidation.getOptionalPageValue(
        emptyUserAnswers,
        PartnershipUniqueTaxpayerReferencePage,
        PartnershipHasUtrYesNoPage
      ) mustBe
        Left(MissingAnswer(PartnershipHasUtrYesNoPage))
    }

    "return Some(value) when the yes/no answer is Yes and the question page is present" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipHasUtrYesNoPage, true)
          .success
          .value
          .set(PartnershipUniqueTaxpayerReferencePage, "1234567890")
          .success
          .value

      TestValidation.getOptionalPageValue(
        userAnswers,
        PartnershipUniqueTaxpayerReferencePage,
        PartnershipHasUtrYesNoPage
      ) mustBe
        Right(Some("1234567890"))
    }

    "return InvalidAnswer when the yes/no answer is Yes and the question page is missing" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipHasUtrYesNoPage, true)
          .success
          .value

      TestValidation.getOptionalPageValue(
        userAnswers,
        PartnershipUniqueTaxpayerReferencePage,
        PartnershipHasUtrYesNoPage
      ) mustBe
        Left(InvalidAnswer(PartnershipUniqueTaxpayerReferencePage))
    }

    "return InvalidAnswer when the yes/no answer is No and the question page is present" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipHasUtrYesNoPage, false)
          .success
          .value
          .set(PartnershipUniqueTaxpayerReferencePage, "1234567890")
          .success
          .value

      TestValidation.getOptionalPageValue(
        userAnswers,
        PartnershipUniqueTaxpayerReferencePage,
        PartnershipHasUtrYesNoPage
      ) mustBe
        Left(InvalidAnswer(PartnershipUniqueTaxpayerReferencePage))
    }
  }

  "Validation.getOptionalPageAndQuestionValue" - {

    "return None when both pages are missing" in {
      TestValidation.getOptionalPageAndQuestionValue(
        emptyUserAnswers,
        PartnershipUniqueTaxpayerReferencePage,
        PartnershipHasUtrYesNoPage
      ) mustBe Right(None)
    }

    "return None when the yes/no answer is No and the question page is missing" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipHasUtrYesNoPage, false)
          .success
          .value

      TestValidation.getOptionalPageAndQuestionValue(
        userAnswers,
        PartnershipUniqueTaxpayerReferencePage,
        PartnershipHasUtrYesNoPage
      ) mustBe Right(None)
    }

    "return Some(value) when the yes/no answer is Yes and the question page is present" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipHasUtrYesNoPage, true)
          .success
          .value
          .set(PartnershipUniqueTaxpayerReferencePage, "1234567890")
          .success
          .value

      TestValidation.getOptionalPageAndQuestionValue(
        userAnswers,
        PartnershipUniqueTaxpayerReferencePage,
        PartnershipHasUtrYesNoPage
      ) mustBe
        Right(Some("1234567890"))
    }

    "return MissingAnswer when the yes/no page is missing but the question page is present" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipUniqueTaxpayerReferencePage, "1234567890")
          .success
          .value

      TestValidation.getOptionalPageAndQuestionValue(
        userAnswers,
        PartnershipUniqueTaxpayerReferencePage,
        PartnershipHasUtrYesNoPage
      ) mustBe
        Left(MissingAnswer(PartnershipHasUtrYesNoPage))
    }

    "return InvalidAnswer when the yes/no answer is Yes but the question page is missing" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipHasUtrYesNoPage, true)
          .success
          .value

      TestValidation.getOptionalPageAndQuestionValue(
        userAnswers,
        PartnershipUniqueTaxpayerReferencePage,
        PartnershipHasUtrYesNoPage
      ) mustBe
        Left(InvalidAnswer(PartnershipUniqueTaxpayerReferencePage))
    }

    "return InvalidAnswer when the yes/no answer is No but the question page is present" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipHasUtrYesNoPage, false)
          .success
          .value
          .set(PartnershipUniqueTaxpayerReferencePage, "1234567890")
          .success
          .value

      TestValidation.getOptionalPageAndQuestionValue(
        userAnswers,
        PartnershipUniqueTaxpayerReferencePage,
        PartnershipHasUtrYesNoPage
      ) mustBe
        Left(InvalidAnswer(PartnershipUniqueTaxpayerReferencePage))
    }
  }

  "Validation.getContactPageValue" - {

    "return Some(value) when the expected contact method is selected and the page is present" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipEmailAddressPage, "test@example.com")
          .success
          .value

      TestValidation.getContactPageValue(
        userAnswers,
        Some(Set(ContactMethodOptions.Email)),
        PartnershipEmailAddressPage,
        ContactMethodOptions.Email
      ) mustBe
        Right(Some("test@example.com"))
    }

    "return MissingAnswer when the expected contact method is selected but the page is missing" in {
      TestValidation.getContactPageValue(
        emptyUserAnswers,
        Some(Set(ContactMethodOptions.Email)),
        PartnershipEmailAddressPage,
        ContactMethodOptions.Email
      ) mustBe
        Left(MissingAnswer(PartnershipEmailAddressPage))
    }

    "return None when the expected contact method is not selected and the page is missing" in {
      TestValidation.getContactPageValue(
        emptyUserAnswers,
        Some(Set(ContactMethodOptions.Phone)),
        PartnershipEmailAddressPage,
        ContactMethodOptions.Email
      ) mustBe Right(None)
    }

    "return InvalidAnswer when the expected contact method is not selected but the page is present" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipEmailAddressPage, "test@example.com")
          .success
          .value

      TestValidation.getContactPageValue(
        userAnswers,
        Some(Set(ContactMethodOptions.Phone)),
        PartnershipEmailAddressPage,
        ContactMethodOptions.Email
      ) mustBe
        Left(InvalidAnswer(PartnershipEmailAddressPage))
    }

    "return None when contact method options are missing and the page is missing" in {
      TestValidation.getContactPageValue(
        emptyUserAnswers,
        None,
        PartnershipEmailAddressPage,
        ContactMethodOptions.Email
      ) mustBe Right(None)
    }

    "return InvalidAnswer when contact method options are missing but the page is present" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipEmailAddressPage, "test@example.com")
          .success
          .value

      TestValidation.getContactPageValue(
        userAnswers,
        None,
        PartnershipEmailAddressPage,
        ContactMethodOptions.Email
      ) mustBe
        Left(InvalidAnswer(PartnershipEmailAddressPage))
    }
  }

  "Validation.getAmendPageValue" - {

    "return the value when the page is present" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, "Test Partnership")
          .success
          .value

      TestValidation.getAmendPageValue(
        userAnswers,
        PartnershipNamePage
      ) mustBe Right("Test Partnership")
    }

    "trim the value when the page is present" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, "  Test Partnership  ")
          .success
          .value

      TestValidation.getAmendPageValue(
        userAnswers,
        PartnershipNamePage
      ) mustBe Right("Test Partnership")
    }

    "return an empty string when the page is missing" in {
      TestValidation.getAmendPageValue(
        emptyUserAnswers,
        PartnershipNamePage
      ) mustBe Right("")
    }

    "return an empty string when the page contains only whitespace" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, "   ")
          .success
          .value

      TestValidation.getAmendPageValue(
        userAnswers,
        PartnershipNamePage
      ) mustBe Right("")
    }
  }
}

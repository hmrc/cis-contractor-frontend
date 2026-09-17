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

package controllers.helpers

import base.SpecBase
import models.{AmendMode, NormalMode}
import org.scalatest.matchers.must.Matchers
import pages.add.partnership.{
  PartnershipNamePage,
  PartnershipNominatedPartnerNamePage
}
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest

class PartnershipNameDisplayHelperSpec extends SpecBase with Matchers {

  implicit lazy val testMessages: Messages = messages(app)

  "PartnershipNameDisplayHelper.getDisplayName" - {

    "return the partnership name when it is provided" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, "Test Partnership")
          .success
          .value

      PartnershipNameDisplayHelper.getDisplayName(
        userAnswers,
        AmendMode
      ) mustBe Some("Test Partnership")
    }

    "return the trimmed partnership name when it contains whitespace" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, "  Test Partnership  ")
          .success
          .value

      PartnershipNameDisplayHelper.getDisplayName(
        userAnswers,
        AmendMode
      ) mustBe Some("Test Partnership")
    }

    "return no name provided when the partnership name is missing in amend mode" in {
      PartnershipNameDisplayHelper.getDisplayName(
        emptyUserAnswers,
        AmendMode
      ) mustBe Some(testMessages("partnershipName.noNameProvided"))
    }

    "return None when the partnership name is missing in normal mode" in {
      PartnershipNameDisplayHelper.getDisplayName(
        emptyUserAnswers,
        NormalMode
      ) mustBe None
    }

    "return no name provided when the partnership name is blank in amend mode" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, "   ")
          .success
          .value

      PartnershipNameDisplayHelper.getDisplayName(
        userAnswers,
        AmendMode
      ) mustBe Some(testMessages("partnershipName.noNameProvided"))
    }
  }

  "PartnershipNameDisplayHelper.getPartnerDisplayName" - {

    "return the nominated partner name when it is provided" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNominatedPartnerNamePage, "Test Partner")
          .success
          .value

      PartnershipNameDisplayHelper.getPartnerDisplayName(
        userAnswers,
        AmendMode
      ) mustBe Some("Test Partner")
    }

    "return the trimmed nominated partner name when it contains whitespace" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNominatedPartnerNamePage, "  Test Partner  ")
          .success
          .value

      PartnershipNameDisplayHelper.getPartnerDisplayName(
        userAnswers,
        AmendMode
      ) mustBe Some("Test Partner")
    }

    "return no name provided when the nominated partner name is missing in amend mode" in {
      PartnershipNameDisplayHelper.getPartnerDisplayName(
        emptyUserAnswers,
        AmendMode
      ) mustBe Some(testMessages("partnershipName.noNameProvided"))
    }

    "return None when the nominated partner name is missing in normal mode" in {
      PartnershipNameDisplayHelper.getPartnerDisplayName(
        emptyUserAnswers,
        NormalMode
      ) mustBe None
    }

    "return no name provided when the nominated partner name is blank in amend mode" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNominatedPartnerNamePage, "   ")
          .success
          .value

      PartnershipNameDisplayHelper.getPartnerDisplayName(
        userAnswers,
        AmendMode
      ) mustBe Some(testMessages("partnershipName.noNameProvided"))
    }
  }

  "PartnershipNameDisplayHelper.displayName" - {

    "return the partnership name when it is provided" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, "Test Partnership")
          .success
          .value

      PartnershipNameDisplayHelper.displayName(
        userAnswers,
        AmendMode
      ) mustBe "Test Partnership"
    }

    "return the trimmed partnership name when it contains whitespace" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, "  Test Partnership  ")
          .success
          .value

      PartnershipNameDisplayHelper.displayName(
        userAnswers,
        AmendMode
      ) mustBe "Test Partnership"
    }

    "return the nominated partner name when partnership name is missing" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNominatedPartnerNamePage, "Test Partner")
          .success
          .value

      PartnershipNameDisplayHelper.displayName(
        userAnswers,
        AmendMode
      ) mustBe "Test Partner"
    }

    "return the trimmed nominated partner name when partnership name is missing" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNominatedPartnerNamePage, "  Test Partner  ")
          .success
          .value

      PartnershipNameDisplayHelper.displayName(
        userAnswers,
        AmendMode
      ) mustBe "Test Partner"
    }

    "return the partnership name when both partnership and nominated partner names are provided" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, "Test Partnership")
          .success
          .value
          .set(PartnershipNominatedPartnerNamePage, "Test Partner")
          .success
          .value

      PartnershipNameDisplayHelper.displayName(
        userAnswers,
        AmendMode
      ) mustBe "Test Partnership"
    }

    "return no name provided when both names are missing in amend mode" in {
      PartnershipNameDisplayHelper.displayName(
        emptyUserAnswers,
        AmendMode
      ) mustBe testMessages("partnershipName.noNameProvided")
    }

    "return an empty string when both names are missing in normal mode" in {
      PartnershipNameDisplayHelper.displayName(
        emptyUserAnswers,
        NormalMode
      ) mustBe ""
    }

    "return the nominated partner name when partnership name is blank" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, "   ")
          .success
          .value
          .set(PartnershipNominatedPartnerNamePage, "Test Partner")
          .success
          .value

      PartnershipNameDisplayHelper.displayName(
        userAnswers,
        AmendMode
      ) mustBe "Test Partner"
    }

    "return no name provided when both names are blank in amend mode" in {
      val userAnswers =
        emptyUserAnswers
          .set(PartnershipNamePage, "   ")
          .success
          .value
          .set(PartnershipNominatedPartnerNamePage, "   ")
          .success
          .value

      PartnershipNameDisplayHelper.displayName(
        userAnswers,
        AmendMode
      ) mustBe testMessages("partnershipName.noNameProvided")
    }
  }
}

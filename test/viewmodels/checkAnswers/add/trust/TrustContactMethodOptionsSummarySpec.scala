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

package viewmodels.checkAnswers.add.trust

import base.SpecBase
import models.contact.ContactMethodOptions
import models.{CheckMode, UserAnswers}
import org.scalatest.matchers.must.Matchers
import pages.add.trust.TrustContactMethodOptionsPage
import play.api.i18n.{DefaultMessagesApi, Lang, Messages}

class TrustContactMethodOptionsSummarySpec extends SpecBase with Matchers {

  implicit val messages: Messages = new DefaultMessagesApi(
    Map(
      "en" -> Map(
        "trustContactMethodOptions.checkYourAnswersLabel" -> "Method of contact",
        "trustContactMethodOptions.email"                 -> "Email address",
        "trustContactMethodOptions.phone"                 -> "Phone number",
        "trustContactMethodOptions.mobile"                -> "Mobile number",
        "trustContactMethodOptions.noDetails"             -> "No details",
        "trustContactMethodOptions.change.hidden"         -> "method of contact",
        "site.change"                                     -> "Change"
      )
    )
  ).preferred(Seq(Lang("en")))

  "TrustContactMethodOptionsSummary.row" - {

    "must return a row with multiple selected options" in {

      val answers: UserAnswers =
        UserAnswers("test-id")
          .set(
            TrustContactMethodOptionsPage,
            Set(ContactMethodOptions.Email, ContactMethodOptions.Phone, ContactMethodOptions.Mobile)
          )
          .success
          .value

      val result = TrustContactMethodOptionsSummary.row(answers)

      result mustBe defined

      val row = result.value

      row.key.content.asHtml.toString must include(messages("trustContactMethodOptions.checkYourAnswersLabel"))

      val valueHtml = row.value.content.asHtml.toString

      valueHtml must include("Email address")
      valueHtml must include("Phone number")
      valueHtml must include("Mobile number")
      valueHtml must not include "<br>"
      valueHtml must include("govuk-list--bullet")

      row.actions mustBe defined

      val actions = row.actions.value.items
      actions must have size 1

      val action = actions.head

      action.href mustBe controllers.add.trust.routes.TrustContactMethodOptionsController
        .onPageLoad(CheckMode)
        .url

      action.content.asHtml.toString must include(messages("site.change"))

      action.visuallyHiddenText mustBe Some(
        messages("trustContactMethodOptions.change.hidden")
      )

      action.attributes must contain("id" -> "trust-contact-methods")
    }

    "must return a row with a single selected option" in {

      val answers: UserAnswers =
        emptyUserAnswers
          .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email))
          .success
          .value

      val result = TrustContactMethodOptionsSummary.row(answers)

      result mustBe defined

      val valueHtml = result.value.value.content.asHtml.toString

      valueHtml must include("Email address")
      valueHtml must not include "<br>"
      valueHtml must not include "govuk-list--bullet"
    }

//    "must return None when noDetails is selected" in {
//
//      val answers: UserAnswers =
//        emptyUserAnswers
//          .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.NoDetails))
//          .success
//          .value
//
//      val result = TrustContactMethodOptionsSummary.row(answers)
//
//      result mustBe defined
//
//      val valueHtml = result.value.value.content.asHtml.toString
//
//      valueHtml must include("None")
//      valueHtml must not include "<br>"
//      valueHtml must not include "govuk-list--bullet"
//    }
  }
}

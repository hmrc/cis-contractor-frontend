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

package viewmodels.checkAnswers.add.partnership

import base.SpecBase
import models.amend.partnership.AmendPartnershipRemoveDetail
import models.{AmendMode, CheckMode, UserAnswers}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.add.partnership.{PartnershipHasUtrYesNoPage, PartnershipNamePage}
import play.api.i18n.{Lang, Messages, MessagesImpl}
import play.api.test.Helpers.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import org.scalatest.matchers.must.Matchers.must
import models.TypeOfSubcontractor
import models.viewOnly.partnership.ViewOnlyPartnershipAnswers

class PartnershipHasUtrYesNoSummarySpec extends SpecBase with GuiceOneAppPerSuite {

  private val messagesApi                 = stubMessagesApi()
  private implicit val messages: Messages = MessagesImpl(Lang.defaultLang, messagesApi)

  private val partnershipName = "Atme Partners"

  "PartnershipHasUtrYesNoSummary.row" - {

    "return a row with key (including partnership name), value = yes, and change action when the answer is true" in {
      val ua: UserAnswers =
        emptyUserAnswers
          .set(PartnershipHasUtrYesNoPage, true)
          .success
          .value
          .set(PartnershipNamePage, partnershipName)
          .success
          .value

      val maybeRow = PartnershipHasUtrYesNoSummary.row(ua)
      maybeRow must not be empty

      val row: SummaryListRow = maybeRow.value

      row.key mustBe Key(content = Text(messages("partnershipHasUtrYesNo.checkYourAnswersLabel", partnershipName)))
      row.value mustBe Value(content = Text(messages("site.yes")))

      row.actions must not be empty
      val actions: Actions = row.actions.value
      actions.items must have size 1

      val action: ActionItem = actions.items.head
      action.href mustBe controllers.add.partnership.routes.PartnershipHasUtrYesNoController
        .onPageLoad(CheckMode)
        .url
      action.content mustBe Text(messages("site.change"))
      action.visuallyHiddenText mustBe Some(messages("partnershipHasUtrYesNo.change.hidden"))
      action.attributes must contain("id" -> "add-partnership-utr")
    }

    "return a row with key (including partnership name), value = yes, and change action when the answer is true in Amend journey" in {
      val ua: UserAnswers =
        emptyUserAnswers
          .set(PartnershipHasUtrYesNoPage, true)
          .success
          .value
          .set(PartnershipNamePage, partnershipName)
          .success
          .value

      val maybeRow = PartnershipHasUtrYesNoSummary.row(ua, AmendMode)
      maybeRow must not be empty

      val row: SummaryListRow = maybeRow.value

      row.key mustBe Key(content = Text(messages("partnershipHasUtrYesNo.checkYourAnswersLabel", partnershipName)))
      row.value mustBe Value(content = Text(messages("site.yes")))

      row.actions must not be empty
      val actions: Actions = row.actions.value
      actions.items must have size 1

      val action: ActionItem = actions.items.head
      action.href mustBe controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
        .onPageLoad(AmendPartnershipRemoveDetail.Utr.key)
        .url
      action.content mustBe Text(messages("site.change"))
      action.visuallyHiddenText mustBe Some(messages("partnershipHasUtrYesNo.change.hidden"))
      action.attributes must contain("id" -> "add-partnership-utr")
    }

    "return a row with key (including partnership name), value = no, and change action when the answer is false" in {
      val ua: UserAnswers =
        emptyUserAnswers
          .set(PartnershipHasUtrYesNoPage, false)
          .success
          .value
          .set(PartnershipNamePage, partnershipName)
          .success
          .value

      val maybeRow = PartnershipHasUtrYesNoSummary.row(ua, AmendMode)
      maybeRow must not be empty

      val row: SummaryListRow = maybeRow.value

      row.key mustBe Key(content = Text(messages("partnershipHasUtrYesNo.checkYourAnswersLabel", partnershipName)))
      row.value mustBe Value(content = Text(messages("site.no")))

      row.actions must not be empty
      val actions: Actions = row.actions.value
      actions.items must have size 1

      val action: ActionItem = actions.items.head
      action.href mustBe controllers.add.partnership.routes.PartnershipHasUtrYesNoController
        .onPageLoad(AmendMode)
        .url
      action.content mustBe Text(messages("site.change"))
      action.visuallyHiddenText mustBe Some(messages("partnershipHasUtrYesNo.change.hidden"))
      action.attributes must contain("id" -> "add-partnership-utr")
    }

    "return a row with key (including partnership name), value = no, and change action pointing to add flow when the answer is false in AmendMode" in {
      val ua: UserAnswers =
        emptyUserAnswers
          .set(PartnershipHasUtrYesNoPage, false)
          .success
          .value
          .set(PartnershipNamePage, partnershipName)
          .success
          .value

      val maybeRow = PartnershipHasUtrYesNoSummary.row(ua, AmendMode)
      maybeRow must not be empty

      val row: SummaryListRow = maybeRow.value

      row.key mustBe Key(content = Text(messages("partnershipHasUtrYesNo.checkYourAnswersLabel", partnershipName)))
      row.value mustBe Value(content = Text(messages("site.no")))

      row.actions must not be empty
      val actions: Actions = row.actions.value
      actions.items must have size 1

      val action: ActionItem = actions.items.head
      action.href mustBe controllers.add.partnership.routes.PartnershipHasUtrYesNoController
        .onPageLoad(AmendMode)
        .url
      action.content mustBe Text(messages("site.change"))
      action.visuallyHiddenText mustBe Some(messages("partnershipHasUtrYesNo.change.hidden"))
      action.attributes must contain("id" -> "add-partnership-utr")
    }

    "throw MissingRequiredAnswer when the partnership name is missing but the answer exists" in {
      val ua: UserAnswers =
        emptyUserAnswers
          .set(PartnershipHasUtrYesNoPage, true)
          .success
          .value

      an[MissingRequiredAnswer] must be thrownBy {
        PartnershipHasUtrYesNoSummary.row(ua)
      }
    }

    "return None when the answer is missing (even if the partnership name is absent)" in {
      val ua: UserAnswers = emptyUserAnswers
      PartnershipHasUtrYesNoSummary.row(ua) mustBe None
    }
  }

  "PartnershipHasUtrYesNoSummary.row(ViewOnlyPartnershipAnswers)" - {

    "return a row with partnership name, value = yes, and no change action when the answer is true" in {

      val answers = ViewOnlyPartnershipAnswers(
        subcontractorType = TypeOfSubcontractor.Partnership,
        showVerificationDetails = false,
        partnershipName = Some(partnershipName),
        addressYesNo = None,
        address = None,
        partnershipContactMethodsYesNo = None,
        partnershipContactMethodOptions = Set.empty,
        email = None,
        phone = None,
        mobile = None,
        hasUtrYesNo = Some(true),
        utr = None,
        nominatedPartnerName = None,
        nominatedPartnerUtrYesNo = None,
        nominatedPartnerUtr = None,
        nominatedPartnerNinoYesNo = None,
        nominatedPartnerNino = None,
        nominatedPartnerCrnYesNo = None,
        nominatedPartnerCrn = None,
        nominatedPartnerWorksReferenceYesNo = None,
        nominatedPartnerWorksReference = None,
        verificationNumber = None
      )

      val maybeRow = PartnershipHasUtrYesNoSummary.row(answers)

      maybeRow must not be empty

      val row: SummaryListRow = maybeRow.value

      row.key mustBe Key(
        content = Text(
          messages(
            "partnershipHasUtrYesNo.checkYourAnswersLabel",
            partnershipName
          )
        )
      )

      row.value mustBe Value(
        content = Text(messages("site.yes"))
      )

      row.actions mustBe defined
      row.actions.value.items mustBe empty
    }

    "return a row with partnership name, value = no, and no change action when the answer is false" in {

      val answers = ViewOnlyPartnershipAnswers(
        subcontractorType = TypeOfSubcontractor.Partnership,
        showVerificationDetails = false,
        partnershipName = Some(partnershipName),
        addressYesNo = None,
        address = None,
        partnershipContactMethodsYesNo = None,
        partnershipContactMethodOptions = Set.empty,
        email = None,
        phone = None,
        mobile = None,
        hasUtrYesNo = Some(false),
        utr = None,
        nominatedPartnerName = None,
        nominatedPartnerUtrYesNo = None,
        nominatedPartnerUtr = None,
        nominatedPartnerNinoYesNo = None,
        nominatedPartnerNino = None,
        nominatedPartnerCrnYesNo = None,
        nominatedPartnerCrn = None,
        nominatedPartnerWorksReferenceYesNo = None,
        nominatedPartnerWorksReference = None,
        verificationNumber = None
      )

      val maybeRow = PartnershipHasUtrYesNoSummary.row(answers)

      maybeRow must not be empty

      val row: SummaryListRow = maybeRow.value

      row.key mustBe Key(
        content = Text(
          messages(
            "partnershipHasUtrYesNo.checkYourAnswersLabel",
            partnershipName
          )
        )
      )

      row.value mustBe Value(
        content = Text(messages("site.no"))
      )

      row.actions mustBe defined
      row.actions.value.items mustBe empty
    }

    "return a row with an empty partnership name when partnership name is missing" in {

      val answers = ViewOnlyPartnershipAnswers(
        subcontractorType = TypeOfSubcontractor.Partnership,
        showVerificationDetails = false,
        partnershipName = None,
        addressYesNo = None,
        address = None,
        partnershipContactMethodsYesNo = None,
        partnershipContactMethodOptions = Set.empty,
        email = None,
        phone = None,
        mobile = None,
        hasUtrYesNo = Some(true),
        utr = None,
        nominatedPartnerName = None,
        nominatedPartnerUtrYesNo = None,
        nominatedPartnerUtr = None,
        nominatedPartnerNinoYesNo = None,
        nominatedPartnerNino = None,
        nominatedPartnerCrnYesNo = None,
        nominatedPartnerCrn = None,
        nominatedPartnerWorksReferenceYesNo = None,
        nominatedPartnerWorksReference = None,
        verificationNumber = None
      )

      val maybeRow = PartnershipHasUtrYesNoSummary.row(answers)

      maybeRow must not be empty

      val row: SummaryListRow = maybeRow.value

      row.key mustBe Key(
        content = Text(
          messages(
            "partnershipHasUtrYesNo.checkYourAnswersLabel",
            ""
          )
        )
      )

      row.value mustBe Value(
        content = Text(messages("site.yes"))
      )

      row.actions mustBe defined
      row.actions.value.items mustBe empty
    }

    "return None when hasUtrYesNo is missing" in {

      val answers = ViewOnlyPartnershipAnswers(
        subcontractorType = TypeOfSubcontractor.Partnership,
        showVerificationDetails = false,
        partnershipName = Some(partnershipName),
        addressYesNo = None,
        address = None,
        partnershipContactMethodsYesNo = None,
        partnershipContactMethodOptions = Set.empty,
        email = None,
        phone = None,
        mobile = None,
        hasUtrYesNo = None,
        utr = None,
        nominatedPartnerName = None,
        nominatedPartnerUtrYesNo = None,
        nominatedPartnerUtr = None,
        nominatedPartnerNinoYesNo = None,
        nominatedPartnerNino = None,
        nominatedPartnerCrnYesNo = None,
        nominatedPartnerCrn = None,
        nominatedPartnerWorksReferenceYesNo = None,
        nominatedPartnerWorksReference = None,
        verificationNumber = None
      )

      PartnershipHasUtrYesNoSummary.row(answers) mustBe None
    }
  }
}

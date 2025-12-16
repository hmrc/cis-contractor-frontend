package viewmodels.checkAnswers.add

import controllers.add.routes
import models.{CheckMode, UserAnswers}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pages.add.WorksReferenceNumberYesNoPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*


class WorksReferenceNumberYesNoSummarySpec extends AnyFreeSpec with Matchers {
  implicit val messages: Messages = stubMessages()

  "WorksReferenceNumberYesNoSummary.row" - {

    "must return a SummaryListRow with 'Yes' when the answer is true" in {
      val answers = UserAnswers("test-id")
        .set(WorksReferenceNumberYesNoPage, true)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = WorksReferenceNumberYesNoSummary.row(answers)
      maybeRow shouldBe defined

      val row =
        maybeRow.value

      val expectedKeyText = messages("worksReferenceNumberYesNo.checkYourAnswersLabel")
      row.key.content.asHtml.toString should include(expectedKeyText)

      val expectedValue = messages("site.yes")
      row.value.content.asHtml.toString should include(expectedValue)

      row.actions shouldBe defined
      val actions = row.actions.value.items
      actions should have size 1

      val changeAction = actions.head
      val expectedChangeText = messages("site.change")
      val expectedHref = routes.WorksReferenceNumberYesNoController.onPageLoad(CheckMode).url
      val expectedHiddenText = messages("worksReferenceNumberYesNo.change.hidden")

      changeAction.content.asHtml.toString should include(expectedChangeText)
      changeAction.href shouldBe expectedHref
      changeAction.visuallyHiddenText.value shouldBe expectedHiddenText
    }

    "must return a SummaryListRow with 'No' when the answer is false" in {
      val answers = UserAnswers("test-id")
        .set(WorksReferenceNumberYesNoPage, false)
        .success
        .value

      val maybeRow: Option[SummaryListRow] = WorksReferenceNumberYesNoSummary.row(answers)
      maybeRow shouldBe defined

      val row = maybeRow.value
      val expectedValue = messages("site.no")
      row.value.content.asHtml.toString should include(expectedValue)
    }

    "must return None when the answer does not exist" in {
      val answers = UserAnswers("test-id")
      WorksReferenceNumberYesNoSummary.row(answers) shouldBe None
    }
  }
}

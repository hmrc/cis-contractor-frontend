package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.RemoveSchemeNameYesNoPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object RemoveSchemeNameYesNoSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(RemoveSchemeNameYesNoPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "removeSchemeNameYesNo.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.RemoveSchemeNameYesNoController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("removeSchemeNameYesNo.change.hidden"))
          )
        )
    }
}

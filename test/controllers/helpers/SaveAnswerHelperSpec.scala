package controllers.helpers

import base.SpecBase
import models.{AmendMode, NormalMode}
import pages.add.trust.TrustNamePage

class SaveAnswerHelperSpec extends SpecBase {

  "saveAnswer" - {

    "must set and amend the answer when in AmendMode" in {

      val result =
        SaveAnswerHelper
          .saveAnswer(
            emptyUserAnswers,
            TrustNamePage,
            "ABC Trust",
            AmendMode
          )
          .success
          .value

      result.get(TrustNamePage) mustBe Some("ABC Trust")

      result
        .get(pages.amend.AmendedPagesPage)
        .value must contain(TrustNamePage.toString)
    }

    "must set the answer without amending when in NormalMode" in {

      val result =
        SaveAnswerHelper
          .saveAnswer(
            emptyUserAnswers,
            TrustNamePage,
            "ABC Trust",
            NormalMode
          )
          .success
          .value

      result.get(TrustNamePage) mustBe Some("ABC Trust")

      result.get(pages.amend.AmendedPagesPage) mustBe None
    }
  }
}

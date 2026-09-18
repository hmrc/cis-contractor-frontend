package pages.finalvalidation

import base.SpecBase
import play.api.libs.json.JsPath

class VerifyFinalValidationContinuationPageSpec extends SpecBase {

  "VerifyFinalValidationContinuationPage" - {

    "must have the correct path" in {
      VerifyFinalValidationContinuationPage.path mustBe
        JsPath \ "finalvalidation" \ "verifyFinalValidationContinuation"
    }

    "must have the correct toString" in {
      VerifyFinalValidationContinuationPage.toString mustBe
        "verifyFinalValidationContinuation"
    }
  }
}

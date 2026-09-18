package models.finalvalidation

import base.SpecBase

class VerifyFinalValidationContinuationSpec extends SpecBase {

  "VerifyFinalValidationContinuation" - {

    "must define ContractorEmailConfirmationStored" in {
      VerifyFinalValidationContinuation.ContractorEmailConfirmationStored mustBe
        "contractorEmailConfirmationStored"
    }

    "must define ContractorEmailConfirmationNotStored" in {
      VerifyFinalValidationContinuation.ContractorEmailConfirmationNotStored mustBe
        "contractorEmailConfirmationNotStored"
    }
  }
}
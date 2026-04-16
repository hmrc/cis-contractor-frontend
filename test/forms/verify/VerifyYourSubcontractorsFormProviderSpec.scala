package forms.verify

import forms.behaviours.BooleanFieldBehaviours
import forms.verify.VerifyYourSubcontractorsFormProvider
import play.api.data.FormError

class VerifyYourSubcontractorsFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "verifyYourSubcontractors.error.required"
  val invalidKey = "error.boolean"

  val form = new VerifyYourSubcontractorsFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}

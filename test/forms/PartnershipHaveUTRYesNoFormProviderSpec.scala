package forms

import forms.add.PartnershipHaveUTRYesNoFormProvider
import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class PartnershipHaveUTRYesNoFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "partnershipHaveUTRYesNo.error.required"
  val invalidKey = "error.boolean"

  val form = new PartnershipHaveUTRYesNoFormProvider()()

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

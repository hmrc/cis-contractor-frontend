package forms.add

import forms.add.SubcontractorContactDetailsYesNoFormProvider
import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class SubcontractorContactDetailsYesNoFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "subcontractorContactDetailsYesNo.error.required"
  val invalidKey = "error.boolean"

  val form = new SubcontractorContactDetailsYesNoFormProvider()()

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

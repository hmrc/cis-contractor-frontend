package forms.add

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class WorksReferenceNumberYesNoFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "worksReferenceNumberYesNo.error.required"
  val invalidKey = "error.boolean"

  val form = new WorksReferenceNumberYesNoFormProvider()()

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

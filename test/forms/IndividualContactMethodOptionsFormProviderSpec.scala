package forms

import forms.behaviours.CheckboxFieldBehaviours
import models.IndividualContactMethodOptions
import play.api.data.FormError

class IndividualContactMethodOptionsFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new IndividualContactMethodOptionsFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "individualContactMethodOptions.error.required"

    behave like checkboxField[IndividualContactMethodOptions](
      form,
      fieldName,
      validValues = IndividualContactMethodOptions.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}

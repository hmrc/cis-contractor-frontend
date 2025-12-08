package forms

import forms.behaviours.OptionFieldBehaviours
import models.SubUseTradingName
import play.api.data.FormError

class SubUseTradingNameFormProviderSpec extends OptionFieldBehaviours {

  val form = new SubUseTradingNameFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "subUseTradingName.error.required"

    behave like optionsField[SubUseTradingName](
      form,
      fieldName,
      validValues  = SubUseTradingName.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}

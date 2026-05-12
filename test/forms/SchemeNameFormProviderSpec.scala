package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class SchemeNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "schemeName.error.required"
  val lengthKey = "schemeName.error.length"
  val maxLength = 56

  val form = new SchemeNameFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}

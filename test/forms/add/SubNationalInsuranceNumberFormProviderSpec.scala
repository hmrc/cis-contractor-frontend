package forms.add

import forms.add.SubNationalInsuranceNumberFormProvider
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class SubNationalInsuranceNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "subNationalInsuranceNumber.error.required"
  val lengthKey = "subNationalInsuranceNumber.error.length"
  val maxLength = 9

  val form = new SubNationalInsuranceNumberFormProvider()()

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

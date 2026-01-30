package forms.add.partnership

import forms.add.partnership.PartnershipContactDetailsYesNoFormProvider
import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class PartnershipContactDetailsYesNoFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "partnershipContactDetailsYesNo.error.required"
  val invalidKey = "error.boolean"

  val form = new PartnershipContactDetailsYesNoFormProvider()()

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

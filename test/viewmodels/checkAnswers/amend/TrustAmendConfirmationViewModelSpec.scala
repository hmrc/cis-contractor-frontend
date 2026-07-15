package viewmodels.checkAnswers.amend

import base.SpecBase
import models.address.{Address, Country}
import models.amend.trust.OriginalTrustAnswers
import models.contact.ContactMethodOptions
import pages.add.trust.*
import pages.amend.AmendedPagesPage
import play.api.i18n.Messages
import viewmodels.amend.TrustAmendConfirmationViewModel
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class TrustAmendConfirmationViewModelSpec extends SpecBase {

  implicit val msgs: Messages = messages(app)

  private val original =
    OriginalTrustAnswers(
      trustName = Some("ABC Trust"),
      addressYesNo = Some(true),
      address = Some(
        Address(
          addressLine1 = "1 test Street",
          addressLine3 = Some("Newcastle"),
          postcode = Some("SA1 1AA"),
          country = Some(Country(code = None, name = Some("England")))
        )
      ),
      trustContactMethodsYesNo = Some(true),
      trustContactMethod = Set(ContactMethodOptions.Email),
      email = Some("trust@test.com"),
      phone = None,
      mobile = None,
      utrYesNo = Some(true),
      utr = Some("1123456789"),
      worksReferenceYesNo = Some(true),
      worksReference = Some("WR123")
    )

  private val answersMatchingOriginal =
    emptyUserAnswers
      .set(TrustNamePage, "ABC Trust")
      .success
      .value
      .set(TrustAddressYesNoPage, true)
      .success
      .value
      .set(
        TrustAddressPage,
        Address(
          addressLine1 = "1 Test Street",
          addressLine3 = Some("Newcastle"),
          postcode = Some("SA1 1AA"),
          country = Some(Country(code = None, name = Some("England")))
        )
      )
      .success
      .value
      .set(AddTrustContactMethodsYesNoPage, true)
      .success
      .value
      .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Email))
      .success
      .value
      .set(TrustEmailAddressPage, "trust@test.com")
      .success
      .value
      .set(TrustUtrYesNoPage, true)
      .success
      .value
      .set(TrustUtrPage, "1123456789")
      .success
      .value
      .set(TrustWorksReferenceYesNoPage, true)
      .success
      .value
      .set(TrustWorksReferencePage, "WR123")
      .success
      .value

  "rows" - {

    "must return no rows when nothing has changed" in {

      val result =
        TrustAmendConfirmationViewModel.rows(original, answersMatchingOriginal)

      result mustBe empty
    }

    "must return a trust name row when the trust name changes" in {

      val answers =
        answersMatchingOriginal
          .set(TrustNamePage, "XYZ Trust")
          .success
          .value
          .set(AmendedPagesPage, Set(TrustNamePage.toString))
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("trustName.checkYourAnswersLabel"))
      row(1).content mustBe Text("ABC Trust")
      row(2).content mustBe Text("XYZ Trust")
    }

    "must return address yes/no and address rows when the address is removed" in {

      val answers =
        answersMatchingOriginal
          .set(TrustAddressYesNoPage, false)
          .success
          .value
          .remove(TrustAddressPage)
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow   = result.head
      val addressRow = result(1)

      yesNoRow.head.content mustBe Text(msgs("trustAddressYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      addressRow.head.content mustBe Text(msgs("trustAddress.checkYourAnswersLabel"))
      addressRow(1).content mustBe Text("1 Test Street, Newcastle, SA1 1AA, England")
      addressRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }

    "must return an address row when the address changes" in {

      val answers =
        answersMatchingOriginal
          .set(
            TrustAddressPage,
            Address(
              addressLine1 = "10 test Street",
              addressLine3 = Some("Newcastle"),
              postcode = Some("SA1 1AA"),
              country = Some(Country(code = None, name = Some("England")))
            )
          )
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("trustAddress.checkYourAnswersLabel"))
      row(1).content mustBe Text("1 test Street, Newcastle, SA1 1AA, England")
      row(2).content mustBe Text("10 test Street, Newcastle, SA1 1AA, England")
    }

    "must return contact rows when contact methods are removed" in {

      val answers =
        answersMatchingOriginal
          .set(AddTrustContactMethodsYesNoPage, false)
          .success
          .value
          .remove(TrustContactMethodOptionsPage)
          .success
          .value
          .remove(TrustEmailAddressPage)
          .success
          .value
          .set(
            AmendedPagesPage,
            Set(
              AddTrustContactMethodsYesNoPage.toString,
              TrustContactMethodOptionsPage.toString,
              TrustEmailAddressPage.toString
            )
          )
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(original, answers)

      result must have size 3

      val yesNoRow  = result(0)
      val methodRow = result(1)
      val emailRow  = result(2)

      yesNoRow.head.content mustBe Text(msgs("addTrustContactMethodsYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      methodRow.head.content mustBe Text(msgs("trustContactMethodOptions.checkYourAnswersLabel"))
      methodRow(1).content mustBe Text(msgs("trustContactMethodOptions.email"))
      methodRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))

      emailRow.head.content mustBe Text(msgs("trustEmailAddress.checkYourAnswersLabel"))
      emailRow(1).content mustBe Text("trust@test.com")
      emailRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }

    "must return contact method, email and phone rows when changing from email to phone" in {

      val answers =
        answersMatchingOriginal
          .set(
            TrustContactMethodOptionsPage,
            Set(ContactMethodOptions.Phone)
          )
          .success
          .value
          .remove(TrustEmailAddressPage)
          .success
          .value
          .set(TrustPhoneNumberPage, "01131234567")
          .success
          .value
          .set(
            AmendedPagesPage,
            Set(
              TrustContactMethodOptionsPage.toString,
              TrustEmailAddressPage.toString,
              TrustPhoneNumberPage.toString
            )
          )
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(original, answers)

      result must have size 3

      val methodRow = result(0)
      val emailRow  = result(1)
      val phoneRow  = result(2)

      methodRow.head.content mustBe Text(msgs("trustContactMethodOptions.checkYourAnswersLabel"))
      methodRow(1).content mustBe Text(msgs("trustContactMethodOptions.email"))
      methodRow(2).content mustBe Text(msgs("trustContactMethodOptions.phone"))

      emailRow.head.content mustBe Text(msgs("trustEmailAddress.checkYourAnswersLabel"))
      emailRow(1).content mustBe Text("trust@test.com")
      emailRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))

      phoneRow.head.content mustBe Text(msgs("trustPhoneNumber.checkYourAnswersLabel"))
      phoneRow(1).content mustBe Text(msgs("amendConfirmation.table.content.none"))
      phoneRow(2).content mustBe Text("01131234567")
    }

    "must return an email row when the email changes" in {

      val answers =
        answersMatchingOriginal
          .set(TrustEmailAddressPage, "new@test.com")
          .success
          .value
          .set(AmendedPagesPage, Set(TrustEmailAddressPage.toString))
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("trustEmailAddress.checkYourAnswersLabel"))
      row(1).content mustBe Text("trust@test.com")
      row(2).content mustBe Text("new@test.com")
    }

    "must return a phone row when a phone number is added" in {

      val answers =
        answersMatchingOriginal
          .set(TrustPhoneNumberPage, "01131234567")
          .success
          .value
          .set(AmendedPagesPage, Set(TrustPhoneNumberPage.toString))
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("trustPhoneNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text(msgs("amendConfirmation.table.content.none"))
      row(2).content mustBe Text("01131234567")
    }

    "must return a phone row when the phone number changes" in {

      val originalPhone =
        original.copy(
          trustContactMethod = Set(ContactMethodOptions.Phone),
          email = None,
          phone = Some("01131234567")
        )

      val answers =
        answersMatchingOriginal
          .remove(TrustEmailAddressPage)
          .success
          .value
          .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Phone))
          .success
          .value
          .set(TrustPhoneNumberPage, "07700900123")
          .success
          .value
          .set(AmendedPagesPage, Set(TrustPhoneNumberPage.toString))
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(originalPhone, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("trustPhoneNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text("01131234567")
      row(2).content mustBe Text("07700900123")
    }

    "must return a mobile row when a mobile number is added" in {

      val answers =
        answersMatchingOriginal
          .set(TrustMobileNumberPage, "07700900123")
          .success
          .value
          .set(AmendedPagesPage, Set(TrustMobileNumberPage.toString))
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("trustMobileNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text(msgs("amendConfirmation.table.content.none"))
      row(2).content mustBe Text("07700900123")
    }

    "must return a mobile row when the mobile number changes" in {

      val originalMobile =
        original.copy(
          trustContactMethod = Set(ContactMethodOptions.Mobile),
          email = None,
          mobile = Some("07700900123")
        )

      val answers =
        answersMatchingOriginal
          .remove(TrustEmailAddressPage)
          .success
          .value
          .set(TrustContactMethodOptionsPage, Set(ContactMethodOptions.Mobile))
          .success
          .value
          .set(TrustMobileNumberPage, "07700900456")
          .success
          .value
          .set(AmendedPagesPage, Set(TrustMobileNumberPage.toString))
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(originalMobile, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("trustMobileNumber.checkYourAnswersLabel"))
      row(1).content mustBe Text("07700900123")
      row(2).content mustBe Text("07700900456")
    }

    "must return a contact methods row when multiple contact methods are selected" in {

      val answers =
        answersMatchingOriginal
          .set(
            TrustContactMethodOptionsPage,
            Set(
              ContactMethodOptions.Email,
              ContactMethodOptions.Phone
            )
          )
          .success
          .value
          .set(TrustPhoneNumberPage, "01131234567")
          .success
          .value
          .set(
            AmendedPagesPage,
            Set(
              TrustContactMethodOptionsPage.toString,
              TrustPhoneNumberPage.toString
            )
          )
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(original, answers)

      result must have size 2

      val methodRow = result(0)
      val phoneRow  = result(1)

      methodRow.head.content mustBe Text(msgs("trustContactMethodOptions.checkYourAnswersLabel"))
      methodRow(1).content mustBe Text(msgs("trustContactMethodOptions.email"))
      methodRow(2).content mustBe Text(
        s"${msgs("trustContactMethodOptions.email")}, ${msgs("trustContactMethodOptions.phone")}"
      )

      phoneRow.head.content mustBe Text(msgs("trustPhoneNumber.checkYourAnswersLabel"))
      phoneRow(1).content mustBe Text(msgs("amendConfirmation.table.content.none"))
      phoneRow(2).content mustBe Text("01131234567")
    }

    "must return a UTR row when the UTR changes" in {

      val answers =
        answersMatchingOriginal
          .set(TrustUtrPage, "2000000000")
          .success
          .value
          .set(AmendedPagesPage, Set(TrustUtrPage.toString))
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("trustUtr.checkYourAnswersLabel"))
      row(1).content mustBe Text("1123456789")
      row(2).content mustBe Text("2000000000")
    }

    "must display none when the UTR is removed" in {

      val answers =
        answersMatchingOriginal
          .remove(TrustUtrPage)
          .success
          .value
          .set(AmendedPagesPage, Set(TrustUtrPage.toString))
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("trustUtr.checkYourAnswersLabel"))
      row(1).content mustBe Text("1123456789")
      row(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }

    "must return a works reference yes/no row when the answer changes" in {

      val answers =
        answersMatchingOriginal
          .set(TrustWorksReferenceYesNoPage, false)
          .success
          .value
          .set(AmendedPagesPage, Set(TrustWorksReferenceYesNoPage.toString))
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow = result.head
      val worksRow = result(1)

      yesNoRow.head.content mustBe Text(msgs("trustWorksReferenceYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      worksRow.head.content mustBe Text(msgs("trustWorksReference.checkYourAnswersLabel"))
      worksRow(1).content mustBe Text("WR123")
      worksRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }

    "must return a works reference row when the works reference changes" in {

      val answers =
        answersMatchingOriginal
          .set(TrustWorksReferencePage, "WR999")
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(original, answers)

      val row = result.head

      row.head.content mustBe Text(msgs("trustWorksReference.checkYourAnswersLabel"))
      row(1).content mustBe Text("WR123")
      row(2).content mustBe Text("WR999")
    }

    "must return a UTR yes/no row when the answer changes" in {

      val answers =
        answersMatchingOriginal
          .set(TrustUtrYesNoPage, false)
          .success
          .value
          .set(AmendedPagesPage, Set(TrustUtrYesNoPage.toString))
          .success
          .value

      val result =
        TrustAmendConfirmationViewModel.rows(original, answers)

      result must have size 2

      val yesNoRow = result.head
      val utrRow   = result(1)

      yesNoRow.head.content mustBe Text(msgs("trustUtrYesNo.checkYourAnswersLabel"))
      yesNoRow(1).content mustBe Text(msgs("site.yes"))
      yesNoRow(2).content mustBe Text(msgs("site.no"))

      utrRow.head.content mustBe Text(msgs("trustUtr.checkYourAnswersLabel"))
      utrRow(1).content mustBe Text("1123456789")
      utrRow(2).content mustBe Text(msgs("amendConfirmation.table.content.none"))
    }
  }
}

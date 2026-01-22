package models.add

import base.SpecBase
import models.{InvalidAnswer, MissingAnswer}
import org.scalatest.Inside.inside
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.add.*

class ValidatedSubcontractorSpec extends SpecBase with Matchers {

  val address = UKAddress(
    addressLine1 = "10 Downing Street",
    addressLine2 = Some("Westminster"),
    addressLine3 = "London",
    addressLine4 = Some("UK"),
    postCode = "SW1A 2AA"
  )

  val subcontractorName = SubcontractorName("firstname", Some("middle name"), "lastname")

  val contactDetails = SubContactDetails("test@example.com", "0123456789")

  ".build" - {

    "must return a ValidatedSubcontractor when all mandatory questions are answered" - {

      "and all optional data is present with trading name" in {

        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value
            .set(SubAddressYesNoPage, true)
            .success
            .value
            .set(AddressOfSubcontractorPage, address)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, true)
            .success
            .value
            .set(SubNationalInsuranceNumberPage, "AB123456C")
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, true)
            .success
            .value
            .set(SubcontractorsUniqueTaxpayerReferencePage, "1234567890")
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, true)
            .success
            .value
            .set(WorksReferenceNumberPage, "WRN-001")
            .success
            .value
            .set(SubcontractorContactDetailsYesNoPage, true)
            .success
            .value
            .set(SubContactDetailsPage, contactDetails)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        result mustBe Right(
          ValidatedSubcontractor(
            typeOfSubcontractor = TypeOfSubcontractor.Individualorsoletrader,
            tradingName = Some("ABC Ltd"),
            subcontractorName = None,
            address = Some(address),
            nino = Some("AB123456C"),
            utr = Some("1234567890"),
            workRefNumber = Some("WRN-001"),
            contactDetails = Some(contactDetails)
          )
        )
      }

      "and all optional data is present with subcontractor name" in {

        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, false)
            .success
            .value
            .set(SubcontractorNamePage, subcontractorName)
            .success
            .value
            .set(SubAddressYesNoPage, true)
            .success
            .value
            .set(AddressOfSubcontractorPage, address)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, true)
            .success
            .value
            .set(SubNationalInsuranceNumberPage, "AB123456C")
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, true)
            .success
            .value
            .set(SubcontractorsUniqueTaxpayerReferencePage, "1234567890")
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, true)
            .success
            .value
            .set(WorksReferenceNumberPage, "WRN-001")
            .success
            .value
            .set(SubcontractorContactDetailsYesNoPage, true)
            .success
            .value
            .set(SubContactDetailsPage, contactDetails)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        result mustBe Right(
          ValidatedSubcontractor(
            typeOfSubcontractor = TypeOfSubcontractor.Individualorsoletrader,
            tradingName = None,
            subcontractorName = Some(subcontractorName),
            address = Some(address),
            nino = Some("AB123456C"),
            utr = Some("1234567890"),
            workRefNumber = Some("WRN-001"),
            contactDetails = Some(contactDetails)
          )
        )
      }

      "and all optional data is missing but with trading name" in {

        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, false)
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, false)
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, false)
            .success
            .value
            .set(SubcontractorContactDetailsYesNoPage, false)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        result mustBe Right(
          ValidatedSubcontractor(
            typeOfSubcontractor = TypeOfSubcontractor.Individualorsoletrader,
            tradingName = Some("ABC Ltd"),
            subcontractorName = None,
            address = None,
            nino = None,
            utr = None,
            workRefNumber = None,
            contactDetails = None
          )
        )
      }

      "and all optional data is missing but with subcontractor name" in {

        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, false)
            .success
            .value
            .set(SubcontractorNamePage, subcontractorName)
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, false)
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, false)
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, false)
            .success
            .value
            .set(SubcontractorContactDetailsYesNoPage, false)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        result mustBe Right(
          ValidatedSubcontractor(
            typeOfSubcontractor = TypeOfSubcontractor.Individualorsoletrader,
            tradingName = None,
            subcontractorName = Some(subcontractorName),
            address = None,
            nino = None,
            utr = None,
            workRefNumber = None,
            contactDetails = None
          )
        )
      }
    }

    "must return error" - {

      "when TypeOfSubcontractorPage is missing" in {
        val result = ValidatedSubcontractor.build(emptyUserAnswers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(TypeOfSubcontractorPage)
        }
      }

      "when SubTradingNameYesNoPage is missing" in {
        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(SubTradingNameYesNoPage)
        }
      }

      "when SubAddressYesNoPage is missing" in {
        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(SubAddressYesNoPage)
        }
      }

      "when NationalInsuranceNumberYesNoPage is missing" in {
        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(NationalInsuranceNumberYesNoPage)
        }
      }

      "when UniqueTaxpayerReferenceYesNoPage is missing" in {
        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, false)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(UniqueTaxpayerReferenceYesNoPage)
        }
      }

      "when WorksReferenceNumberYesNoPage is missing" in {
        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, false)
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, false)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(WorksReferenceNumberYesNoPage)
        }
      }

      "when SubcontractorContactDetailsYesNoPage is missing" in {
        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, false)
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, false)
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, false)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe MissingAnswer(SubcontractorContactDetailsYesNoPage)
        }
      }

      "when the user said yes to SubTradingNameYesNoPage but TradingNameOfSubcontractorPage is missing" in {
        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, false)
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, false)
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, false)
            .success
            .value
            .set(SubcontractorContactDetailsYesNoPage, false)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe InvalidAnswer(TradingNameOfSubcontractorPage)
        }
      }

      "when the user said no to SubTradingNameYesNoPage but SubcontractorNamePage is missing" in {
        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, false)
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, false)
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, false)
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, false)
            .success
            .value
            .set(SubcontractorContactDetailsYesNoPage, false)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe InvalidAnswer(SubcontractorNamePage)
        }
      }

      "when the user said yes to SubAddressYesNoPage but SubcontractorNamePage is missing" in {
        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value
            .set(SubAddressYesNoPage, true)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, false)
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, false)
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, false)
            .success
            .value
            .set(SubcontractorContactDetailsYesNoPage, false)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe InvalidAnswer(AddressOfSubcontractorPage)
        }
      }

      "when the user said yes to NationalInsuranceNumberYesNoPage but SubNationalInsuranceNumberPage is missing" in {
        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, true)
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, false)
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, false)
            .success
            .value
            .set(SubcontractorContactDetailsYesNoPage, false)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe InvalidAnswer(SubNationalInsuranceNumberPage)
        }
      }

      "when the user said yes to UniqueTaxpayerReferenceYesNoPage but SubcontractorsUniqueTaxpayerReferencePage is missing" in {
        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, false)
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, true)
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, false)
            .success
            .value
            .set(SubcontractorContactDetailsYesNoPage, false)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe InvalidAnswer(SubcontractorsUniqueTaxpayerReferencePage)
        }
      }

      "when the user said yes to WorksReferenceNumberYesNoPage but WorksReferenceNumberPage is missing" in {
        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, false)
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, false)
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, true)
            .success
            .value
            .set(SubcontractorContactDetailsYesNoPage, false)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe InvalidAnswer(WorksReferenceNumberPage)
        }
      }

      "when the user said yes to SubcontractorContactDetailsYesNoPage but SubContactDetailsPage is missing" in {
        val answers =
          emptyUserAnswers
            .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Individualorsoletrader)
            .success
            .value
            .set(SubTradingNameYesNoPage, true)
            .success
            .value
            .set(TradingNameOfSubcontractorPage, "ABC Ltd")
            .success
            .value
            .set(SubAddressYesNoPage, false)
            .success
            .value
            .set(NationalInsuranceNumberYesNoPage, false)
            .success
            .value
            .set(UniqueTaxpayerReferenceYesNoPage, false)
            .success
            .value
            .set(WorksReferenceNumberYesNoPage, false)
            .success
            .value
            .set(SubcontractorContactDetailsYesNoPage, true)
            .success
            .value

        val result = ValidatedSubcontractor.build(answers)

        inside(result) { case Left(error) =>
          error mustBe InvalidAnswer(SubContactDetailsPage)
        }
      }
    }
  }
}

package models.address

import base.SpecBase
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.{Lang, MessagesApi}

class MaxLengthErrorMessagesModelSpec extends SpecBase with Matchers {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "MaxLengthErrorMessage.forLang" - {

    "return a populated model when all max lengths are provided" in {

      val result =
        MaxLengthErrorMessage.forLang(
          Lang("en"),
          Some(35),
          Some(35),
          Some(35),
          Some(35)
        )

      result mustBe defined

      result.value.addressLine1 must include("35")
      result.value.addressLine2 must include("35")
      result.value.addressLine3 must include("35")
      result.value.town         must include("35")
    }

    "return None when addressLine1 max length is missing" in {

      MaxLengthErrorMessage.forLang(
        Lang("en"),
        None,
        Some(35),
        Some(35),
        Some(35)
      ) mustBe None
    }
  }

  "MaxLengthErrorMessagesModel.forConfig" - {

    "build English messages when all max lengths are provided" in {

      val result =
        MaxLengthErrorMessagesModel.forConfig(
          Some(35),
          Some(35),
          Some(35),
          Some(35)
        )

      result.en mustBe defined

      result.en.value.addressLine1 must include("35")
      result.en.value.addressLine2 must include("35")
      result.en.value.addressLine3 must include("35")
      result.en.value.town         must include("35")
    }

    "return None for English when a required max length is missing" in {

      val result =
        MaxLengthErrorMessagesModel.forConfig(
          None,
          Some(35),
          Some(35),
          Some(35)
        )

      result.en mustBe None
    }
  }
}

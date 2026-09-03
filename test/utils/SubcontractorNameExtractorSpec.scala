/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import base.SpecBase
import models.add.SubcontractorName
import org.scalatestplus.mockito.MockitoSugar
import pages.add.{SubcontractorNamePage, TradingNameOfSubcontractorPage}
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class SubcontractorNameExtractorSpec extends SpecBase with MockitoSugar {

  "SubcontractorNameExtractor.getSubcontractorName" - {

    "should return the subcontractor firstName and lastName when SubcontractorNamePage is in userAnswers" in {

      val subcontractorNameExtractor = new SubcontractorNameExtractor()

      val subContractorName =
        SubcontractorName("John", Some("Paul"), "Smith")

      val userAnswers =
        emptyUserAnswers
          .set(SubcontractorNamePage, subContractorName)
          .success
          .value

      val result =
        subcontractorNameExtractor.getSubcontractorName(userAnswers)

      result mustBe Some("John Smith")
    }

    "should return trading name when TradingNameOfSubcontractorPage exists" in {

      val subcontractorNameExtractor = new SubcontractorNameExtractor()

      val tradingNameOfSubcontractor = "ABC Contractors"

      val userAnswers =
        emptyUserAnswers
          .set(TradingNameOfSubcontractorPage, tradingNameOfSubcontractor)
          .success
          .value

      val result =
        subcontractorNameExtractor.getSubcontractorName(userAnswers)

      result mustBe Some(tradingNameOfSubcontractor)
    }

    "should return None when no subcontractor name exists" in {

      val subcontractorNameExtractor = new SubcontractorNameExtractor()

      val result =
        subcontractorNameExtractor.getSubcontractorName(emptyUserAnswers)

      result mustBe None
    }
  }

  "SubcontractorNameExtractor.displaySubcontractorName" - {

    implicit val messagesApi: MessagesApi =
      stubMessagesApi()

    implicit val messages: Messages =
      messagesApi.preferred(FakeRequest())

    val subcontractorNameExtractor = new SubcontractorNameExtractor()

    val tradingNameOfSubcontractor = "ABC Contractors"

    val subContractorName =
      SubcontractorName("John", Some("Paul"), "Smith")

    "should return the subcontractor firstName and lastName when SubcontractorNamePage is in userAnswers" in {

      val userAnswers =
        emptyUserAnswers
          .set(SubcontractorNamePage, subContractorName)
          .success
          .value

      val result =
        subcontractorNameExtractor.displaySubcontractorName(userAnswers)

      result mustBe "John Smith"
    }

    "should return the subcontractor firstName and lastName when SubcontractorNamePage and TradingNameOfSubcontractorPage in userAnswers" in {

      val userAnswers =
        emptyUserAnswers
          .set(SubcontractorNamePage, subContractorName)
          .success
          .value
          .set(TradingNameOfSubcontractorPage, tradingNameOfSubcontractor)
          .success
          .value

      val result =
        subcontractorNameExtractor.displaySubcontractorName(userAnswers)

      result mustBe "John Smith"
    }

    "should return lastName when only last name in SubcontractorNamePage and TradingNameOfSubcontractorPage is in userAnswers" in {

      val subContractorName =
        SubcontractorName("", None, "Smith")

      val userAnswers =
        emptyUserAnswers
          .set(SubcontractorNamePage, subContractorName)
          .success
          .value
          .set(TradingNameOfSubcontractorPage, tradingNameOfSubcontractor)
          .success
          .value

      val result =
        subcontractorNameExtractor.displaySubcontractorName(userAnswers)

      result mustBe "Smith"
    }

    "should return TradingName when first and last name in SubcontractorNamePage is empty and TradingNameOfSubcontractorPage is in userAnswers" in {

      val subContractorName =
        SubcontractorName("  ", None, "  ")

      val userAnswers =
        emptyUserAnswers
          .set(SubcontractorNamePage, subContractorName)
          .success
          .value
          .set(TradingNameOfSubcontractorPage, tradingNameOfSubcontractor)
          .success
          .value

      val result =
        subcontractorNameExtractor.displaySubcontractorName(userAnswers)

      result mustBe tradingNameOfSubcontractor
    }

    "should return trading name when only TradingNameOfSubcontractorPage in userAnswers" in {

      val userAnswers =
        emptyUserAnswers
          .set(TradingNameOfSubcontractorPage, tradingNameOfSubcontractor)
          .success
          .value

      val result =
        subcontractorNameExtractor.displaySubcontractorName(userAnswers)

      result mustBe tradingNameOfSubcontractor
    }

    "should return trading name when only first name in SubcontractorNamePage and TradingNameOfSubcontractorPage is in userAnswers" in {

      val subContractorName =
        SubcontractorName("John", None, "")

      val userAnswers =
        emptyUserAnswers
          .set(SubcontractorNamePage, subContractorName)
          .success
          .value
          .set(TradingNameOfSubcontractorPage, tradingNameOfSubcontractor)
          .success
          .value

      val result =
        subcontractorNameExtractor.displaySubcontractorName(userAnswers)

      result mustBe tradingNameOfSubcontractor
    }

    "should return No name provided when no subcontractor name exists" in {

      val result =
        subcontractorNameExtractor.displaySubcontractorName(emptyUserAnswers)

      result mustBe messages("verify.noName")
    }

    "should return No name provided when only first name in SubcontractorNamePage" in {

      val subContractorName =
        SubcontractorName("John", None, "")

      val userAnswers =
        emptyUserAnswers
          .set(SubcontractorNamePage, subContractorName)
          .success
          .value

      val result =
        subcontractorNameExtractor.displaySubcontractorName(userAnswers)

      result mustBe messages("verify.noName")
    }

    "should return No name provided when only middle name in SubcontractorNamePage" in {

      val subContractorName =
        SubcontractorName("", Some("Paul"), "")

      val userAnswers =
        emptyUserAnswers
          .set(SubcontractorNamePage, subContractorName)
          .success
          .value

      val result =
        subcontractorNameExtractor.displaySubcontractorName(userAnswers)

      result mustBe messages("verify.noName")
    }
  }
}

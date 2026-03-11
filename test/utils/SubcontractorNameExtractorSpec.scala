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

class SubcontractorNameExtractorSpec extends SpecBase with MockitoSugar {

  "SubcontractorNameExtractor" - {

    "should return the subcontractor firstName and lastName when SubcontractorNamePage is in userAnswers" in {

      val subcontractorNameExtractor = new SubcontractorNameExtractor()

      val subcontractorName = SubcontractorName("John", Some("Paul"), "Smith")

      val userAnswers = emptyUserAnswers
        .set(SubcontractorNamePage, subcontractorName)
        .success
        .value

      val result = subcontractorNameExtractor.getSubcontractorName(userAnswers)
      result mustBe Some("John Smith")
    }

    "should return TradingNameOfSubcontractor when TradingNameOfSubcontractorPage is in userAnswers" in {

      val subcontractorNameExtractor = new SubcontractorNameExtractor()

      val tradingNameOfSubcontractor = "ABC Construction LTD"

      val userAnswers = emptyUserAnswers
        .set(TradingNameOfSubcontractorPage, tradingNameOfSubcontractor)
        .success
        .value

      val result = subcontractorNameExtractor.getSubcontractorName(userAnswers)

      result mustBe Some(tradingNameOfSubcontractor)
    }

    "should return None when SubcontractorNamePage and TradingNameOfSubcontractorPage are not in userAnswers" in {

      val subcontractorNameExtractor = new SubcontractorNameExtractor()

      val result = subcontractorNameExtractor.getSubcontractorName(emptyUserAnswers)

      result mustBe None
    }
  }
}

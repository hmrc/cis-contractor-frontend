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

package pages.add

import models.add.{IndividualNamesOptions, SubcontractorName}
import pages.behaviours.PageBehaviours

class IndividualNamesOptionsPageSpec extends PageBehaviours {
  "IndividualNamesOptionsPage" - {

    beRetrievable[Set[IndividualNamesOptions]](IndividualNamesOptionsPage)

    beSettable[Set[IndividualNamesOptions]](IndividualNamesOptionsPage)

    beRemovable[Set[IndividualNamesOptions]](IndividualNamesOptionsPage)

    "cleanup" - {

      val subcontractorName = SubcontractorName(
        firstName = "John",
        middleName = Some("Paul"),
        lastName = "Smith"
      )

      val tradingName = "Test Trading"

      val userAnswers = emptyUserAnswers
        .set(SubcontractorNamePage, subcontractorName)
        .success
        .value
        .set(TradingNameOfSubcontractorPage, tradingName)
        .success
        .value

      "must remove TradingNameOfSubcontractorPage when SubcontractorName is selected" in {
        val updatedUserAnswers =
          userAnswers.set(IndividualNamesOptionsPage, Set(IndividualNamesOptions.SubcontractorName)).success.value

        updatedUserAnswers.get(SubcontractorNamePage) mustBe Some(subcontractorName)
        updatedUserAnswers.get(TradingNameOfSubcontractorPage) mustBe None
      }

      "must remove SubcontractorNamePage when TradingName is selected" in {
        val updatedUserAnswers =
          userAnswers.set(IndividualNamesOptionsPage, Set(IndividualNamesOptions.TradingName)).success.value

        updatedUserAnswers.get(TradingNameOfSubcontractorPage) mustBe Some(tradingName)
        updatedUserAnswers.get(SubcontractorNamePage) mustBe None
      }

      "must not remove SubcontractorNamePage and TradingNameOfSubcontractorPage when SubcontractorName and TradingName are selected" in {
        val updatedUserAnswers =
          userAnswers
            .set(
              IndividualNamesOptionsPage,
              Set(IndividualNamesOptions.SubcontractorName, IndividualNamesOptions.TradingName)
            )
            .success
            .value

        updatedUserAnswers.get(SubcontractorNamePage) mustBe Some(subcontractorName)
        updatedUserAnswers.get(TradingNameOfSubcontractorPage) mustBe Some(tradingName)
      }
    }
  }
}

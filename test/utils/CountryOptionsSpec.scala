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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import config.FrontendAppConfig
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._

class CountryOptionsSpec extends AnyWordSpec with Matchers with MockitoSugar {

  "CountryOptions" should {

    "return a list of InputOptions from config" in {
      val mockConfig = mock[FrontendAppConfig]
      when(mockConfig.locationCanonicalList).thenReturn(
        Seq("United Kingdom" -> "GB", "France" -> "FR")
      )

      val countryOptions = new CountryOptions(mockConfig)

      val result = countryOptions.options()

      result should contain theSameElementsAs Seq(
        InputOption(value = "United Kingdom", label = "United Kingdom"),
        InputOption(value = "France", label = "France")
      )
    }

    "return country name for a given code" in {
      val mockConfig = mock[FrontendAppConfig]
      when(mockConfig.locationCanonicalList).thenReturn(
        Seq("United Kingdom" -> "GB", "France" -> "FR")
      )

      val countryOptions = new CountryOptions(mockConfig)

      countryOptions.getCountryNameFromCode("United Kingdom") shouldBe "United Kingdom"
      countryOptions.getCountryNameFromCode("France")         shouldBe "France"
    }

    "return input itself if country code/name is not found" in {
      val mockConfig = mock[FrontendAppConfig]
      when(mockConfig.locationCanonicalList).thenReturn(
        Seq("United Kingdom" -> "GB")
      )

      val countryOptions = new CountryOptions(mockConfig)

      countryOptions.getCountryNameFromCode("US") shouldBe "US"
    }
  }
}

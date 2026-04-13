/*
 * Copyright 2025 HM Revenue & Customs
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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.{DefaultMessagesApi, Lang, Messages}

class UtilsSpec extends AnyFreeSpec with Matchers {

  ".findFirstMessageValue" - {

    "must return the translated value when the key exists in messages" in {
      implicit val messages: Messages =
        new DefaultMessagesApi(Map("en" -> Map("existingKey" -> "Translated Value")))
          .preferred(Seq(Lang("en")))

      Utils.findFirstMessagesValue(Seq("existingKey")) mustEqual "Translated Value"
    }

    "must skip a missing multi-character key and return the first resolved value" in {
      implicit val messages: Messages =
        new DefaultMessagesApi(Map("en" -> Map("secondKey" -> "Second Value")))
          .preferred(Seq(Lang("en")))

      Utils.findFirstMessagesValue(Seq("missingKey", "secondKey")) mustEqual "Second Value"
    }

    "must return the key as a fallback when it is the only key and not found in messages" in {
      implicit val messages: Messages =
        new DefaultMessagesApi(Map("en" -> Map.empty))
          .preferred(Seq(Lang("en")))

      Utils.findFirstMessagesValue(Seq("missingKey")) mustEqual "missingKey"
    }

    "must return the first resolved value when multiple existing keys are provided" in {
      implicit val messages: Messages =
        new DefaultMessagesApi(Map("en" -> Map("firstKey" -> "First Value", "secondKey" -> "Second Value")))
          .preferred(Seq(Lang("en")))

      Utils.findFirstMessagesValue(Seq("firstKey", "secondKey")) mustEqual "First Value"
    }

    "must throw IllegalArgumentException when given an empty sequence of keys" in {
      implicit val messages: Messages =
        new DefaultMessagesApi(Map("en" -> Map.empty))
          .preferred(Seq(Lang("en")))

      an[IllegalArgumentException] must be thrownBy Utils.findFirstMessagesValue(Seq.empty)
    }

    "must return the first key as a fallback when all keys are missing from messages" in {
      implicit val messages: Messages =
        new DefaultMessagesApi(Map("en" -> Map.empty))
          .preferred(Seq(Lang("en")))

      Utils.findFirstMessagesValue(Seq("missingKey", "anotherMissingKey")) mustEqual "missingKey"
    }
  }
}

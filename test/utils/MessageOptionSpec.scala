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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{Lang, MessagesApi}
import play.api.test.Helpers.stubMessagesApi

class MessageOptionSpec extends AnyWordSpec with Matchers {

  private val english = Lang("en")
  private val welsh   = Lang("cy")

  implicit val messagesApi: MessagesApi = stubMessagesApi(
    messages = Map(
      "en" -> Map(
        "present.key" -> "Hello",
        "param.key"   -> "Hello {0} and {1}",
        "empty.key"   -> ""
      ),
      "cy" -> Map(
        "present.key" -> "Helo"
      )
    )
  )

  "MessageOption.apply" must {

    "return Some message when the key is defined and non-empty" in {
      MessageOption("present.key", english) mustBe Some("Hello")
    }

    "resolve the message for the requested language" in {
      MessageOption("present.key", welsh) mustBe Some("Helo")
    }

    "substitute parameters into the message" in {
      MessageOption("param.key", english, "world", "everyone") mustBe Some("Hello world and everyone")
    }

    "return None when the key is not defined" in {
      MessageOption("missing.key", english) mustBe None
    }

    "return None when the key resolves to an empty string" in {
      MessageOption("empty.key", english) mustBe None
    }
  }
}

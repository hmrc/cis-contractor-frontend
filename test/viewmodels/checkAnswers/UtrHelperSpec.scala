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

package viewmodels.checkAnswers

import base.SpecBase
import org.scalatest.matchers.should.Matchers.should

class UtrHelperSpec extends SpecBase {

  "UtrHelperSpec" - {

    "UtrContent" - {
      "must prevent Safari from detecting the UTR as a telephone number" in {
        val content = UtrContent("123456789")

        content.asHtml.toString should include("""x-apple-data-detectors="false"""")
        content.asHtml.toString should include("123456789")
      }
    }

    "UtrViewModel" - {
      "must prevent Safari from detecting the UTR as a telephone number" in {
        val value = UtrViewModel("123456789")

        value.content.asHtml.toString should include("""x-apple-data-detectors="false"""")
        value.content.asHtml.toString should include("123456789")
      }
    }
  }
}

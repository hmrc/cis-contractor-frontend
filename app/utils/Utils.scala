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

import play.api.i18n.Messages

object Utils {
  val emptyString: String = ""

  /** Finds the first messages value for a given set of keys
    * @param keys
    * @param messages
    * @return
    *   message value or the key if not found
    */
  def findFirstMessagesValue(keys: Seq[String])(implicit messages: Messages): String = {
    require(keys.nonEmpty, "keys must not be empty")
    keys
      .collectFirst { case key if messages(key) != key => messages(key) }
      .getOrElse(keys.head)
  }

}

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

object FirstMiddleName {

  private val validNameFormat          = "[A-Za-z\\'\\-]+"
  private val validNameFirstCharFormat = "^[a-zA-Z]{1}.*"
  private val length                   = 35

  def isValid(name: String): Boolean =
    name != null && name.matches(validNameFormat) && name.matches(validNameFirstCharFormat)

  def isLengthInRange(name: String): Boolean = name != null && (name.length <= length)
}

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

package forms

import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.domain.Nino

object Validation {

  final val worksRefRegex = """^[A-Za-z0-9 ~!@#$%&'()*+,-./:;=?_{}£€]+$"""
  final val emailRegex    = """^[A-Za-z0-9!#$%&*+\-/=?^_`{|}~.]+@[A-Za-z0-9!#$%&*+\-/=?^_`{|}~.]+$"""

  final val companyRegNumberRegex =
    """(?i)^(?:[A-Z]{2}\d{1,6}|\d{1,8})$"""

  final val nameRegex =
    """^[A-Za-z0-9"~!@#\$%*+:\;=\?\s,\.\[\]_\{\}\(\)/&'\-\^\\£€]+$"""

  final val firstCharLetterRegex =
    """^[A-Za-z].*"""

  final val firstCharLetterOrDigitRegex = """^[A-Za-z0-9].*"""

  final val ukPostcodeRegex =
    """^(GIR\s?0AA|(?:(?:[A-Z]{1,2}\d[A-Z\d]?|\d[A-Z]{2})\s?\d[A-Z]{2}))$"""

  def isNinoValid(value: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if Nino.isValid(str.replaceAll("\\s", "").toUpperCase) =>
        Valid
      case _                                                          =>
        Invalid(errorKey, value)
    }

}

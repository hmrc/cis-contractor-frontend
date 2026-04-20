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

import java.net.IDN
import play.api.data.validation.{Constraint, Invalid, Valid}
import scala.util.Try
import uk.gov.hmrc.domain.Nino

object Validation {

  final val companyRegNumberRegex = """(?i)^(?:[A-Z]{2}\d{6}|\d{8})$"""
  final val emailRegex            =
    """^[A-Za-z0-9!#$%&'*+\-/=?^_`{|}~.]+@[A-Za-z0-9]([A-Za-z0-9\-]*[A-Za-z0-9])?(\.[A-Za-z0-9]([A-Za-z0-9\-]*[A-Za-z0-9])?)*\.[A-Za-z]{2,}$"""
  final val nameRegex             = """^[A-Za-z0-9"~!@#\$%*+:\;=\?\s,\.\[\]_\\\{\}\(\)/&'\-\^\u00A3\u20AC]+$"""
  final val worksRefRegex         = """^[A-Za-z0-9 ~!@#$%&'()*+,-./:;=?_{}£€]+$"""
  final val mobileRegex           = """^(?=(?:.*\d){6,})[0-9()+\- ]*$"""
  final val phoneRegex            = """^(?=(?:.*\d){6,})[0-9()+\- ]*$"""
  final val addressRegex          = """^[A-Za-z0-9"~!@#\$%*+:\;=\?\s,\.\[\]_\\\{\}\(\)/&'\-\^\u00A3\u20AC]+$"""

  final val firstCharLetterRegex =
    """^[A-Za-z].*"""

  final val firstCharLetterOrDigitRegex = """^[A-Za-z0-9].*"""

  final val ukPostcodeRegex =
    """^[A-Za-z0-9 ~!\"@#$%\&\'\(\)\*\+,\-\./:;\<=\>\?\[\\\]^_\{\}\£\€]*$"""

  def noPunycodeDomain(errorKey: String): Constraint[String] =
    Constraint { email =>
      val domain           = email.split("@", 2).lift(1).getOrElse("")
      val hasPunycodeLabel = domain.split('.').exists(_.toLowerCase.startsWith("xn--"))
      if (hasPunycodeLabel) Invalid(errorKey) else Valid
    }

  def noInvalidDomainCharacters(errorKey: String): Constraint[String] =
    Constraint { email =>
      val domain          = email.split("@", 2).lift(1).getOrElse("")
      val hasInvalidLabel = domain.split('.').exists { label =>
        val decoded = Try(IDN.toUnicode(label)).getOrElse(label)
        decoded.exists(c => !Character.isLetterOrDigit(c) && c != '-')
      }
      if (hasInvalidLabel) Invalid(errorKey) else Valid
    }

  def isNinoValid(value: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if Nino.isValid(str.replaceAll("\\s", "").toUpperCase) =>
        Valid
      case _                                                          =>
        Invalid(errorKey, value)
    }

}

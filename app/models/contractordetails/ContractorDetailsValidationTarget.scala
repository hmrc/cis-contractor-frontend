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

package models.contractordetails

import play.api.libs.json.{JsError, JsString, JsSuccess, Reads, Writes}

sealed trait ContractorDetailsValidationTarget {
  def key: String
}

object ContractorDetailsValidationTarget {
  case object FileMonthlyReturn extends ContractorDetailsValidationTarget {
    override val key: String = "file-monthly-return"
  }

  case object FileNilReturn extends ContractorDetailsValidationTarget {
    override val key: String = "file-nil-return"
  }

  case object VerifySubcontractors extends ContractorDetailsValidationTarget {
    override val key: String = "verify-subcontractors"
  }

  case object ReviewUnmatchedSubcontractors extends ContractorDetailsValidationTarget {
    override val key: String = "review-unmatched-subcontractors"
  }

  val values: Seq[ContractorDetailsValidationTarget] =
    Seq(FileMonthlyReturn, FileNilReturn, VerifySubcontractors, ReviewUnmatchedSubcontractors)

  def fromKey(key: String): Option[ContractorDetailsValidationTarget] =
    values.find(_.key == key)

  implicit val reads: Reads[ContractorDetailsValidationTarget] =
    Reads {
      case JsString(value) => fromKey(value).map(JsSuccess(_)).getOrElse(JsError("error.invalid"))
      case _               => JsError("error.invalid")
    }

  implicit val writes: Writes[ContractorDetailsValidationTarget] =
    Writes(target => JsString(target.key))
}

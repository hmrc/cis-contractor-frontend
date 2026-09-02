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

package models.finalvalidation

import play.api.libs.json.*
import models.finalvalidation.VerifyFinalValidationSource.*

sealed trait VerifyFinalValidationSource

object VerifyFinalValidationSource {
  case object SelectSubcontractor extends VerifyFinalValidationSource

  case object SelectSubcontractorsToReverify extends VerifyFinalValidationSource

  case object ReviewUnmatchedSubcontractors extends VerifyFinalValidationSource

  case object ReviewInsufficientInfoSubcontractors extends VerifyFinalValidationSource

  given Format[VerifyFinalValidationSource] = new Format[VerifyFinalValidationSource] {
    override def writes(source: VerifyFinalValidationSource): JsValue =
      JsString(
        source match {
          case SelectSubcontractor => "SelectSubcontractor"
          case SelectSubcontractorsToReverify => "SelectSubcontractorsToReverify"
          case ReviewUnmatchedSubcontractors => "ReviewUnmatchedSubcontractors"
          case ReviewInsufficientInfoSubcontractors => "ReviewInsufficientInfoSubcontractors"
        }
      )

    override def reads(json: JsValue): JsResult[VerifyFinalValidationSource] =
      json.validate[String].flatMap {
        case "SelectSubcontractor" => JsSuccess(SelectSubcontractor)
        case "SelectSubcontractorsToReverify" => JsSuccess(SelectSubcontractorsToReverify)
        case "ReviewUnmatchedSubcontractors" => JsSuccess(ReviewUnmatchedSubcontractors)
        case "ReviewInsufficientInfoSubcontractors" => JsSuccess(ReviewInsufficientInfoSubcontractors)
        case other => JsError(s"Unknown VerifyFinalValidationSource: $other")
      }
  }
}

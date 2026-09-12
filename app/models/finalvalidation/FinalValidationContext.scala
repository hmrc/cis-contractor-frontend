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

sealed trait FinalValidationContext

object FinalValidationContext {
  case object MonthlyReturn extends FinalValidationContext
  case object VerifySubcontractor extends FinalValidationContext

  given Format[FinalValidationContext] = new Format[FinalValidationContext] {

    override def writes(context: FinalValidationContext): JsValue =
      JsString(
        context match {
          case MonthlyReturn       => "MonthlyReturn"
          case VerifySubcontractor => "VerifySubcontractor"
        }
      )

    override def reads(json: JsValue): JsResult[FinalValidationContext] =
      json.validate[String].flatMap {
        case "MonthlyReturn"       => JsSuccess(MonthlyReturn)
        case "VerifySubcontractor" => JsSuccess(VerifySubcontractor)
        case other                 => JsError(s"Unknown FinalValidationContext: $other")
      }
  }
}

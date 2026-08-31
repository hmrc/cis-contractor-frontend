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

package models.amend

import play.api.libs.json.*

sealed trait AmendJourneyType {
  def routeValue: String
}

object AmendJourneyType {

  case object Standard extends AmendJourneyType {
    override def routeValue: String = "standard"
  }
  case object InsufficientInfo extends AmendJourneyType {
    override def routeValue: String = "insufficient"
  }
  case object UnmatchedInfo extends AmendJourneyType {
    override def routeValue: String = "unmatched"
  }

  def fromString(value: String): Option[AmendJourneyType] =
    value match {
      case "standard"     => Some(Standard)
      case "insufficient" => Some(InsufficientInfo)
      case "unmatched"    => Some(UnmatchedInfo)
      case _              => None
    }

  implicit val format: Format[AmendJourneyType] =
    new Format[AmendJourneyType] {

      override def writes(o: AmendJourneyType): JsValue =
        JsString(
          o match {
            case Standard         => "Standard"
            case InsufficientInfo => "InsufficientInfo"
            case UnmatchedInfo    => "UnmatchedInfo"
          }
        )

      override def reads(json: JsValue): JsResult[AmendJourneyType] =
        json match {
          case JsString("Standard")         => JsSuccess(Standard)
          case JsString("InsufficientInfo") => JsSuccess(InsufficientInfo)
          case JsString("UnmatchedInfo")    => JsSuccess(UnmatchedInfo)
          case _                            => JsError("Invalid AmendJourneyType")
        }
    }
}

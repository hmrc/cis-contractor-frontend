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

package models.requests

import play.api.mvc.PathBindable

import java.util.UUID

final case class CisPath(id: UUID) {
  def toUrl: String = id.toString
}

object CisPath {

  given PathBindable[CisPath] = new PathBindable[CisPath] {
    def bind(key: String, value: String): Either[String, CisPath] =
      try Right(CisPath(UUID.fromString(value)))
      catch case _: IllegalArgumentException => Left(s"Could not resolve CIS path from: $value")

    def unbind(key: String, value: CisPath): String = value.toUrl
  }
}

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

package controllers.helpers

import models.{AmendMode, Mode, UserAnswers}
import play.api.libs.json.Writes
import queries.Settable

import scala.util.Try

object SaveAnswerHelper {

  def saveAnswer[A](
                     userAnswers: UserAnswers,
                     page: Settable[A],
                     value: A,
                     mode: Mode
                   )(implicit writes: Writes[A]): Try[UserAnswers] =
    if (mode == AmendMode) {
      userAnswers.setAndAmend(page, value)
    } else {
      userAnswers.set(page, value)
    }
}

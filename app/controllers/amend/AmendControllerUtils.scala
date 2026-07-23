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

package controllers.amend

import models.UserAnswers
import models.response.SubcontractorResponse
import pages.QuestionPage
import play.api.libs.json.Writes

import scala.util.Try

object AmendControllerUtils {

  def setOptional[A: Writes](userAnswers: UserAnswers, page: QuestionPage[A], value: Option[A]): Try[UserAnswers] =
    value.fold(Try(userAnswers)) { answer =>
      userAnswers.set(page, answer)
    }

  def isExpectedSubcontractorType(
    subcontractor: SubcontractorResponse,
    expectedType: String
  ): Boolean =
    subcontractor.subcontractorType.exists(
      _.trim.equalsIgnoreCase(expectedType)
    )
}

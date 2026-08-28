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

package pages.add

import models.UserAnswers
import models.add.SubcontractorName
import pages.QuestionPage
import play.api.libs.json.JsPath
import queries.AmendIndividualSubcontractorNameRemovedQuery

import scala.util.Try

case object SubcontractorNamePage extends QuestionPage[SubcontractorName] with IndividualJourney {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "subcontractorName"

  override def cleanup(value: Option[SubcontractorName], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(_) => userAnswers.remove(AmendIndividualSubcontractorNameRemovedQuery)
      case None    => super.cleanup(value, userAnswers)
    }
}

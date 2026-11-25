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

package controllers.actions

import models.{JourneyNamespace, Mode}
import models.requests.DataRequest
import play.api.mvc.{Action, AnyContent, Result, Results}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

// Multi-tab
// Resolves the journey namespace from the mode and applies the required
// action chain for controller methods shared across subcontractor journeys.
@Singleton
class SharedSubcontractorJourneyAction @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction
) {

  def apply(flowId: UUID, mode: Mode)(
    block: JourneyNamespace => DataRequest[AnyContent] => Result
  ): Action[AnyContent] =
    async(flowId, mode)(
      namespace => request =>
        Future.successful(block(namespace)(request))
    )

  def async(flowId: UUID, mode: Mode)(
    block: JourneyNamespace => DataRequest[AnyContent] => Future[Result]
  ): Action[AnyContent] =
    JourneyNamespace.resolveNamespace(mode) match {
      case Some(namespace) =>
        (identify andThen getData andThen requireData andThen
          multiTabAction.forJourney(flowId, namespace)).async {
          request => block(namespace)(request)
        }
      case None =>
        identify { _ =>
          Results.Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }
}

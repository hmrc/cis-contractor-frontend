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

import models.requests.{CisIdDataRequest, DataRequest}
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.Redirect
import queries.CisIdQuery
import play.api.Logging

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait CisIdRequiredAction extends ActionRefiner[DataRequest, CisIdDataRequest]

class CisIdRequiredActionImpl @Inject() (implicit
  val executionContext: ExecutionContext
) extends CisIdRequiredAction
    with Logging {

  override protected def refine[A](
    request: DataRequest[A]
  ): Future[Either[Result, CisIdDataRequest[A]]] =
    request.userAnswers.get(CisIdQuery) match {

      case Some(cisId) =>
        Future.successful(
          Right(
            CisIdDataRequest(
              request = request.request,
              userId = request.userId,
              userAnswers = request.userAnswers,
              cisId = cisId,
              employerReference = request.employerReference,
              agentReference = request.agentReference,
              isAgent = request.isAgent
            )
          )
        )

      case None =>
        logger.error(
          "[CisIdRequiredAction] Missing CIS ID, redirecting to journey recovery"
        )

        Future.successful(
          Left(
            Redirect(
              controllers.routes.JourneyRecoveryController.onPageLoad()
            )
          )
        )
    }
}

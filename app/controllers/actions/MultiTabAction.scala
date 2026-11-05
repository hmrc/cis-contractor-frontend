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

import models.requests.{DataRequest, IdentifierRequest}
import play.api.Logging
import play.api.mvc.{ActionFilter, Result, Results}
import queries.CisIdQuery
import services.CisTaxpayerService

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class MultiTabAction @Inject() (cisTaxpayerService: CisTaxpayerService)(using ec: ExecutionContext) extends Logging {

  def forStart(cisId: String): ActionFilter[IdentifierRequest] =
    new ActionFilter[IdentifierRequest] {
      override protected def executionContext: ExecutionContext = ec

      override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] =
        isAuthorised(cisId)(using request).map {
          case true  => None
          case false => Some(Results.Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }

  def forJourney(flowId: UUID): ActionFilter[DataRequest] =
    new ActionFilter[DataRequest] {
      override protected def executionContext: ExecutionContext = ec

      override protected def filter[A](request: DataRequest[A]): Future[Option[Result]] =
        request.userAnswers.get(CisIdQuery(flowId.toString)) match {
          case None        =>
            Future.successful(Some(Results.Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
          case Some(cisId) =>
            given IdentifierRequest[A] = IdentifierRequest(
              request.request,
              request.userId,
              request.employerReference,
              request.agentReference,
              request.isAgent,
              request.agentCode
            )
            isAuthorised(cisId).map {
              case true  => None
              case false => Some(Results.Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            }
        }
    }

  private def isAuthorised(cisId: String)(using request: IdentifierRequest[?]): Future[Boolean] =
    if cisId.isBlank then Future.successful(false)
    else
      val result =
        if request.isAgent then cisTaxpayerService.findForAgent(cisId).map(_.isDefined)
        else cisTaxpayerService.findForOrganisation.map(_.uniqueId == cisId)
      result.recover { case NonFatal(e) =>
        logger.error("[MultiTabAction] Failed to authorise CIS id", e)
        false
      }
}

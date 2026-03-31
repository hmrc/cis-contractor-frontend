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

package controllers

import controllers.actions.DataRetrievalAction
import controllers.actions.IdentifierAction
import models.agent.AgentClientIdentifiers
import models.requests.OptionalDataRequest
import models.{NormalMode, UserAnswers}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.CisIdQuery
import repositories.SessionRepository
import services.CisManageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import controllers.add.routes as addSubcontractorRoutes

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class IndexController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  sessionRepository: SessionRepository,
  cisManagerService: CisManageService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))

    getAgentClient(request)
      .flatMap(clientInfoOpt => handleRequest(clientInfoOpt, userAnswers)(request))
      .recover { case NonFatal(e) =>
        logger.error("[IndexController] onPageLoad failed", e)
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  private def handleRequest(
                             agentClient: Option[AgentClientIdentifiers],
                             userAnswers: UserAnswers
                           )(implicit request: OptionalDataRequest[AnyContent]): Future[Result] =
    if (!request.isAgent) {
      for {
        updated <- cisManagerService.ensureCisIdInUserAnswers(userAnswers)
        _ <- sessionRepository.set(updated)
      } yield Redirect(addSubcontractorRoutes.TypeOfSubcontractorController.onPageLoad(NormalMode))
    } else {
      agentClient match {
        case None =>
          logger.warn("[IndexController] Missing agent client data")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

        case Some(AgentClientIdentifiers(uniqueId, ton, tor)) =>
          cisManagerService
            .hasClient(ton.trim, tor.trim)
            .flatMap {
              case true => storeInstanceId(uniqueId.trim, userAnswers)
                .map(_ => Redirect(addSubcontractorRoutes.TypeOfSubcontractorController.onPageLoad(NormalMode)))
              case false =>
                logger.warn(s"[IndexController] hasClient=false for taxOfficeNumber=$ton taxOfficeReference=$tor")
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            }
            .recover { case NonFatal(e) =>
              logger.error(s"[IndexController] hasClient check failed: ${e.getMessage}", e)
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
      }
    }

  private def getAgentClient(implicit request: OptionalDataRequest[_], hc: HeaderCarrier): Future[Option[AgentClientIdentifiers]] =
    if (request.isAgent) {
      cisManagerService.getAgentClient(request.userId).map(_.map { data =>
        AgentClientIdentifiers(data.uniqueId, data.taxOfficeNumber, data.taxOfficeReference)
      })
    } else {
      Future.successful(None)
    }

  private def storeInstanceId(instanceId: String, userAnswers: UserAnswers): Future[Unit] =
    for {
      updated <- Future.fromTry(userAnswers.set(CisIdQuery, instanceId))
      _ <- sessionRepository.set(updated)
    } yield ()

}

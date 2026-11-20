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

package controllers.contractordetails

import controllers.actions.*
import models.{NormalMode, UserAnswers}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.contractordetails.ContractorDetailsView
import queries.CisIdQuery

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContractorDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  multiTabAction: MultiTabAction,
  val controllerComponents: MessagesControllerComponents,
  view: ContractorDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = identify { implicit request =>
    Ok(
      view(
        routes.ContractorDetailsController.onSubmit()
      )
    )
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData).async { implicit request =>
    val answers = request.userAnswers.getOrElse(UserAnswers(request.userId))

    sessionRepository.set(answers).map { _ =>
      Redirect(routes.ContractorUtrController.onPageLoad(NormalMode))
    }
  }

  def onPageLoadStart(cisId: String): Action[AnyContent] =
    (identify andThen multiTabAction.forStart(cisId) andThen getData) { implicit request =>
      Ok(
        view(
          routes.ContractorDetailsController.onSubmitStart(cisId)
        )
      )
    }

  def onSubmitStart(cisId: String): Action[AnyContent] =
    (identify andThen multiTabAction.forStart(cisId) andThen getData).async { implicit request =>
      val answers = request.userAnswers.getOrElse(UserAnswers(request.userId))

      val flowId = UUID.randomUUID()
      val id     = flowId.toString

      Future
        .fromTry(answers.set(CisIdQuery(id), cisId))
        .flatMap { updatedAnswers =>
          sessionRepository.set(updatedAnswers).map { _ =>
            Redirect(
              routes.ContractorUtrController.onPageLoadWithId(flowId, NormalMode)
            )
          }
        }
    }

  def onPageLoadWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      Ok(
        view(
          routes.ContractorDetailsController.onSubmitWithId(flowId)
        )
      )
    }

  def onSubmitWithId(flowId: UUID): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val answers = request.userAnswers

      sessionRepository.set(answers).map { _ =>
        Redirect(routes.ContractorUtrController.onPageLoadWithId(flowId, NormalMode))
      }
    }
}

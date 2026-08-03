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

package controllers.amend.company

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import pages.add.company.CompanyNamePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{CisIdQuery, OriginalCompanyAnswersQuery}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DefaultSubcontractorCleanupService
import viewmodels.amend.company.CompanyAmendConfirmationViewModel
import views.html.amend.AmendConfirmationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AmendCompanyConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  cleanupService: DefaultSubcontractorCleanupService,
  sessionRepository: SessionRepository,
  view: AmendConfirmationView,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val recoveryRedirect =
        Redirect(routes.JourneyRecoveryController.onPageLoad())

      val ua = request.userAnswers

      ua.get(OriginalCompanyAnswersQuery) match {

        case None =>
          logger.error("[AmendCompanyConfirmationController] Missing OriginalCompanyAnswersQuery")
          Future.successful(recoveryRedirect)

        case Some(originalCompanyAnswers) =>
          ua.get(CisIdQuery) match {

            case None =>
              logger.error("[AmendCompanyConfirmationController] Missing CisIdQuery")
              Future.successful(recoveryRedirect)

            case Some(_) =>
              val tableRows   = CompanyAmendConfirmationViewModel.rows(originalCompanyAnswers, ua)
              val companyName = ua.get(CompanyNamePage).getOrElse("")
              cleanupService.cleanAmend(ua) match {

                case Success(cleanedUa) =>
                  sessionRepository.set(cleanedUa).map { _ =>
                    Ok(
                      view(
                        tableRows,
                        companyName,
                        appConfig.retrieveSubcontractorListUrl
                      )
                    )
                  }
                case Failure(exception) =>
                  logger.warn(
                    "[AmendCompanyConfirmationController] Failed to clean user answers",
                    exception
                  )
                  Future.successful(recoveryRedirect)
              }
          }
      }
    }

}

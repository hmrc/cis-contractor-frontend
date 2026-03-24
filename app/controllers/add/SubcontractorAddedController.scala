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

package controllers.add

import controllers.actions.*
import models.add.TypeOfSubcontractor
import models.UserAnswers
import models.add.TypeOfSubcontractor.*
import models.requests.DataRequest
import pages.add.company.CompanyNamePage
import pages.add.CheckYourAnswersSubmittedPage
import pages.add.partnership.PartnershipNamePage
import pages.add.trust.TrustNamePage
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{DefaultSubcontractorCleanupService, SubcontractorNameExtractor}
import views.html.add.SubcontractorAddedView

import scala.util.{Failure, Success, Try}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubcontractorAddedController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subcontractorNameExtractor: SubcontractorNameExtractor,
  sessionRepository: SessionRepository,
  cleanupService: DefaultSubcontractorCleanupService,
  val controllerComponents: MessagesControllerComponents,
  view: SubcontractorAddedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def individualSubcontractorAdded: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      subcontractorAdded(TypeOfSubcontractor.Individualorsoletrader)
  }

  def companySubcontractorAdded: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      subcontractorAdded(TypeOfSubcontractor.Limitedcompany)
  }

  def partnershipSubcontractorAdded: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      subcontractorAdded(TypeOfSubcontractor.Partnership)
  }

  def trustSubcontractorAdded: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      subcontractorAdded(TypeOfSubcontractor.Trust)
  }

  private def subcontractorAdded(
    subcontractorType: TypeOfSubcontractor
  )(implicit request: DataRequest[AnyContent]): Future[Result] = {

    def recoveryRedirect =
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

    if (request.userAnswers.get(CheckYourAnswersSubmittedPage).contains(true)) {

      val (name: Option[String], subcontractorTypeTitle: String) = subcontractorType match {
        case Individualorsoletrader =>
          (
            subcontractorNameExtractor.getSubcontractorName(request.userAnswers),
            Messages("subcontractorAdded.individual")
          )
        case Limitedcompany         => (request.userAnswers.get(CompanyNamePage), Messages("subcontractorAdded.company"))
        case Partnership            => (request.userAnswers.get(PartnershipNamePage), Messages("subcontractorAdded.partnership"))
        case Trust                  => (request.userAnswers.get(TrustNamePage), Messages("subcontractorAdded.trust"))
      }

      name.fold(Future.successful(recoveryRedirect)) { name =>

        val cleanedUaTry: Try[UserAnswers] = cleanupService.clean(request.userAnswers)

        cleanedUaTry match {
          case Success(cleanedUa) =>
            sessionRepository.set(cleanedUa).map { _ =>
              Ok(view(name, subcontractorTypeTitle))
            }
          case Failure(exception) =>
            logger.warn(s"Failed to clean user answers: $exception")
            Future.successful(recoveryRedirect)
        }
      }
    } else {
      Future.successful(recoveryRedirect)
    }
  }
}

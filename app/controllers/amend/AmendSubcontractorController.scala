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

import controllers.actions.*
import controllers.helpers.AmendSubcontractorPopulator
import models.TypeOfSubcontractor.{Individualorsoletrader, Limitedcompany, Partnership, Trust}
import models.{TypeOfSubcontractor, UserAnswers}
import models.response.SubcontractorResponse
import play.api.Logging
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AmendSubcontractorController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subcontractorService: SubcontractorService,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with Logging {

  def onPageLoad(
    cisId: String,
    subbieResourceRef: Long
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      subcontractorService
        .getSubcontractor(cisId, subbieResourceRef)
        .flatMap { response =>
          response.subcontractor match {

            case None =>
              logger.error(
                s"[AmendSubcontractorController] No subcontractor returned " +
                  s"for cisId=$cisId, subbieResourceRef=$subbieResourceRef"
              )
              Future.successful(recovery)

            case Some(subcontractor) =>
              subcontractor.subcontractorType
                .flatMap(TypeOfSubcontractor.fromString)
                .fold[Future[Result]] {

                  logger.error(
                    s"[AmendSubcontractorController] Unsupported subcontractor type. " +
                      s"type=${subcontractor.subcontractorType.getOrElse("missing")}, " +
                      s"cisId=$cisId, subbieResourceRef=$subbieResourceRef"
                  )

                  Future.successful(recovery)

                } { subcontractorType =>
                  handleSubcontractor(
                    subcontractorType = subcontractorType,
                    userAnswers = request.userAnswers,
                    cisId = cisId,
                    subbieResourceRef = subbieResourceRef,
                    subcontractor = subcontractor
                  )
                }
          }
        }
        .recover { case error =>
          logger.error(
            s"[AmendSubcontractorController] Failed to retrieve subcontractor. " +
              s"cisId=$cisId, subbieResourceRef=$subbieResourceRef",
            error
          )

          recovery
        }
    }

  private def handleSubcontractor(
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers,
    cisId: String,
    subbieResourceRef: Long,
    subcontractor: SubcontractorResponse
  ): Future[Result] =
    populateUserAnswers(
      subcontractorType,
      userAnswers,
      cisId,
      subcontractor
    ).fold(
      error => {
        logger.error(
          s"[AmendSubcontractorController] Failed to populate UserAnswers " +
            s"for type=$subcontractorType, " +
            s"cisId=$cisId, subbieResourceRef=$subbieResourceRef",
          error
        )

        Future.successful(recovery)
      },
      updatedAnswers =>
        sessionRepository
          .set(updatedAnswers)
          .map(_ => Redirect(onwardRoute(subcontractorType)))
    )
  private def populateUserAnswers(
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers,
    cisId: String,
    subcontractor: SubcontractorResponse
  ): Try[UserAnswers] =
    subcontractorType match {
      case Individualorsoletrader =>
        AmendSubcontractorPopulator.IndividualPopulator
          .populate(userAnswers, cisId, subcontractor)

      case Limitedcompany =>
        AmendSubcontractorPopulator.CompanyPopulator
          .populate(userAnswers, cisId, subcontractor)

      case Partnership =>
        AmendSubcontractorPopulator.PartnershipPopulator
          .populate(userAnswers, cisId, subcontractor)

      case Trust =>
        AmendSubcontractorPopulator.TrustPopulator
          .populate(userAnswers, cisId, subcontractor)
    }

  private def onwardRoute(subcontractorType: TypeOfSubcontractor): Call =
    subcontractorType match {
      case Individualorsoletrader =>
        controllers.amend.routes.AmendIndividualCheckYourAnswersController
          .onPageLoad()

      case Limitedcompany =>
        controllers.amend.company.routes.AmendCompanyCheckYourAnswersController
          .onPageLoad()

      case Partnership =>
        controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController
          .onPageLoad()

      case Trust =>
        controllers.amend.trust.routes.AmendTrustCheckYourAnswersController
          .onPageLoad()
    }
  private def recovery: Result                                          =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
}

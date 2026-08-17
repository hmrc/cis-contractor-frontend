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

import controllers.AgentClientChecks
import controllers.actions.*
import controllers.helpers.AmendSubcontractorPopulator
import models.TypeOfSubcontractor.{Individualorsoletrader, Limitedcompany, Partnership, Trust}
import models.{TypeOfSubcontractor, UserAnswers}
import models.response.{GetSubcontractorResponse, SubcontractorResponse}
import play.api.Logging
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import queries.CisIdQuery
import repositories.SessionRepository
import services.{CisManageService, SubcontractorService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AmendSubcontractorController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  subcontractorService: SubcontractorService,
  val controllerComponents: MessagesControllerComponents,
  override protected val cisManageService: CisManageService,
  override protected val sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with AgentClientChecks
    with Logging {

  def onPageLoad(
    subbieResourceRef: Long
  ): Action[AnyContent] =
    (identify andThen getData).async { implicit request =>
      val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))

      withAgentClientChecks(request.userId, request.isAgent, userAnswers)
        .flatMap {
          case Left(redirect)        => Future.successful(redirect)
          case Right(checkedAnswers) =>
            checkedAnswers.get(CisIdQuery) match {
              case None                 =>
                logger.error("[AmendSubcontractorController] CIS ID missing from checked answers")
                Future.successful(recovery)
              case Some(validatedCisId) =>
                subcontractorService
                  .getSubcontractor(validatedCisId, subbieResourceRef)
                  .flatMap(
                    resolveSubcontractor(
                      _,
                      validatedCisId,
                      subbieResourceRef,
                      userAnswers
                    )
                  )
                  .recover { case error =>
                    logger.error(
                      s"[AmendSubcontractorController] Failed to resolve subcontractor. " +
                        s"cisId=$validatedCisId, subbieResourceRef=$subbieResourceRef",
                      error
                    )

                    recovery
                  }
            }
        }
        .recover { case error =>
          logger.error(
            s"[AmendSubcontractorController] Failed to retrieve subcontractor. " +
              s"cisId=${userAnswers.get(CisIdQuery)}, subbieResourceRef=$subbieResourceRef",
            error
          )

          recovery
        }
    }

  private def resolveSubcontractor(
    response: GetSubcontractorResponse,
    cisId: String,
    subbieResourceRef: Long,
    userAnswers: UserAnswers
  ): Future[Result] =
    response.subcontractor match {
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
              subcontractorType,
              userAnswers,
              cisId,
              subbieResourceRef,
              subcontractor
            )
          }

      case None =>
        logger.error(
          s"[AmendSubcontractorController] No subcontractor returned " +
            s"for cisId=$cisId, subbieResourceRef=$subbieResourceRef"
        )
        Future.successful(recovery)
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

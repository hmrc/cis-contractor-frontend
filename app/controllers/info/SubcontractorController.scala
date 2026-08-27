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

package controllers.info

import controllers.actions.*
import controllers.helpers.info.SubcontractorPopulator
import models.TypeOfSubcontractor.{Individualorsoletrader, Limitedcompany, Partnership, Trust}
import models.response.SubcontractorResponse
import models.{TypeOfSubcontractor, UserAnswers}
import play.api.Logging
import play.api.mvc.*
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class SubcontractorController @Inject() (
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
                s"[ViewOnlySubcontractorController] No subcontractor returned " +
                  s"for cisId=$cisId, subbieResourceRef=$subbieResourceRef"
              )

              Future.successful(recovery)

            case Some(subcontractor) =>
              subcontractor.subcontractorType
                .flatMap(TypeOfSubcontractor.fromString)
                .fold[Future[Result]] {

                  logger.error(
                    s"[ViewOnlySubcontractorController] Unsupported subcontractor type. " +
                      s"type=${subcontractor.subcontractorType.getOrElse("missing")}, " +
                      s"cisId=$cisId, subbieResourceRef=$subbieResourceRef"
                  )

                  Future.successful(recovery)

                } { subcontractorType =>
                  handleSubcontractor(
                    subcontractorType = subcontractorType,
                    userAnswers = request.userAnswers,
                    subcontractor = subcontractor
                  )
                }
          }
        }
        .recover { case error =>
          logger.error(
            s"[ViewOnlySubcontractorController] Failed to retrieve subcontractor. " +
              s"cisId=$cisId, subbieResourceRef=$subbieResourceRef",
            error
          )

          recovery
        }
    }

  private def handleSubcontractor(
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers,
    subcontractor: SubcontractorResponse
  ): Future[Result] =
    populateUserAnswers(
      subcontractorType,
      userAnswers,
      subcontractor
    ).fold(
      error => {
        logger.error(
          s"[ViewOnlySubcontractorController] Failed to populate ViewOnly UserAnswers " +
            s"for type=$subcontractorType",
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
    subcontractor: SubcontractorResponse
  ): Try[UserAnswers] =
    SubcontractorPopulator.populate(
      userAnswers,
      subcontractorType,
      subcontractor
    )

  private def onwardRoute(
    subcontractorType: TypeOfSubcontractor
  ): Call =
    subcontractorType match {

      // TODO- update logic so view shows dynamic content+URL for back to link at the bottom of page to VF-07-03 or INSF-07-03
      case Individualorsoletrader =>
        controllers.info.routes.IndividualCheckYourAnswersController
          .onPageLoad()

      case Limitedcompany =>
        controllers.info.company.routes.CompanyCheckYourAnswersController
          .onPageLoad()

      case Partnership =>
        controllers.info.partnership.routes.PartnershipCheckYourAnswersController
          .onPageLoad()

      case Trust =>
        controllers.info.trust.routes.TrustCheckYourAnswersController
          .onPageLoad()
    }

  private def recovery: Result =
    Redirect(
      controllers.routes.JourneyRecoveryController.onPageLoad()
    )
}

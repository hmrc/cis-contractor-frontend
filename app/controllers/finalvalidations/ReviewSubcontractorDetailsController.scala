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

package controllers.finalvalidations

import controllers.actions.*
import models.{NormalMode, UserAnswers}
import models.finalvalidation.VerifyFinalValidationSource.*
import models.finalvalidation.{ReviewSubcontractorDetailsPageModel, ReviewSubcontractorDetailsRow, VerifyFinalValidationSource}
import pages.finalvalidation.{FinalValidationErrorPage, VerifyFinalValidationSourcePage}
import pages.verify.{SelectSubcontractorPage, SelectSubcontractorsToReverifyPage}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.finalvalidations.ReviewSubcontractorDetailsView

@Singleton
class ReviewSubcontractorDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ReviewSubcontractorDetailsView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    
    request.userAnswers.get(VerifyFinalValidationSourcePage) match {
      case Some(source) =>
        selectedSubcontractors(request.userAnswers, source) match {
          case Some(selected) =>
            val failures = request.userAnswers.get(FinalValidationErrorPage).getOrElse(Set.empty)
            val erroneousIds = failures.filter(_.issues.nonEmpty).map(_.subcontractorId).toSet
            val rows = selected.map { case (subcontractorId, name) =>
              ReviewSubcontractorDetailsRow(
                subcontractorId = subcontractorId,
                name = name,
                hasErrors = erroneousIds.contains(subcontractorId)
              )
            }
            Ok(
              view(
                ReviewSubcontractorDetailsPageModel(
                  subcontractors = rows,
                  canContinue = failures.isEmpty,
                  backUrl = backUrl(source)
                )
              )
            )

          case None =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }

      case None =>
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
  
  private def selectedSubcontractors(userAnswers: UserAnswers, source: VerifyFinalValidationSource): Option[Seq[(Long, String)]] = {
    source match {
      case SelectSubcontractor =>
        userAnswers.get(SelectSubcontractorPage).map {
          _.toSeq.flatMap { selected =>
            selected.id.toLongOption.map { id =>
              id -> selected.name
            }
          }
        }

      case SelectSubcontractorsToReverify =>
        userAnswers.get(SelectSubcontractorsToReverifyPage).map {
          _.toSeq.flatMap { selected =>
            selected.id.toLongOption.map { id =>
              id -> selected.name
            }
          }
        }

      case ReviewUnmatchedSubcontractors =>
        None

      case ReviewInsufficientInfoSubcontractors =>
        None
    }
  }

  private def backUrl(source: VerifyFinalValidationSource): String =
    source match {
      case SelectSubcontractor =>
        controllers.verify.routes.SelectSubcontractorController.onPageLoad(NormalMode).url

      case SelectSubcontractorsToReverify =>
        controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(NormalMode).url

//      case ReviewUnmatchedSubcontractors =>
//        controllers.verify.routes.ReviewUnmatchedSubcontractorsController.onPageLoad().url

      case ReviewInsufficientInfoSubcontractors =>
        controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController.onPageLoad().url
    }

//  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
//    val failures = request.userAnswers.get(FinalValidationErrorPage).getOrElse(Set.empty)
//    if (failures.exists(_.issues.nonEmpty)) {
//      Redirect(controllers.finalvalidations.routes.ReviewSubcontractorDetailsController.onPageLoad())
//    } else {
//      verifyFinalValidationService.continueFromFinalValidation(request.userAnswers)
//    }
//  }
  
}

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
import models.finalvalidation.{UpdateSubcontractorDetailsPageModel, UpdateSubcontractorDetailsPageModelBuilder}
import pages.finalvalidation.FinalValidationErrorPage
import services.SubcontractorService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.finalvalidations.UpdateSubcontractorDetailsView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateSubcontractorDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  requireCisId: CisIdRequiredAction,
  subcontractorService: SubcontractorService,
  pageModelBuilder: UpdateSubcontractorDetailsPageModelBuilder,
  val controllerComponents: MessagesControllerComponents,
  view: UpdateSubcontractorDetailsView
)(using ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(subcontractorId: Long): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen requireCisId).async { implicit request =>

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      
      val failure =
        request.userAnswers.get(FinalValidationErrorPage)
          .flatMap(_.find(_.subcontractorId == subcontractorId))
      
      failure match {

        case Some(failure) =>
          failure.subbieResourceRef match {

            case Some(subbieResourceRef) =>
              subcontractorService.getSubcontractor(request.cisId, subbieResourceRef).map { response =>
                response.subcontractor match {

                  case Some(subcontractor) =>
                    val rows = pageModelBuilder
                                 .build(subcontractor,
                                        failure,
                                        (field, target) => controllers.finalvalidations.routes
                                                             .FinalValidationChangeController
                                                             .onPageLoad(subcontractorId, field.key, target.key)
                                                             .url
                                 )
                    Ok(
                      view(
                        UpdateSubcontractorDetailsPageModel(
                          subcontractorId,
                          subcontractor.displayName,
                          rows
                        )
                      )
                    )

                  case None =>  
                    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                }
              }

            case None =>  
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
          
        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
    
//  def onSubmit(subcontractorId: Long): Action[AnyContent] =
//    (identify andThen getData andThen requireData).async { implicit request =>
//      // F1/F1b rerun belongs here when that behabiour is implemented for verify-subcontractor
//      Future.successful(Redirect(routes.ReviewSubcontractorDetailsController.onPageLoad()))
//    }

}

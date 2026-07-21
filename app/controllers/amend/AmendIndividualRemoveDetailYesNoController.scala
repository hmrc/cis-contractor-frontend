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
import forms.amend.AmendIndividualRemoveDetailYesNoFormProvider
import pages.amend.AmendIndividualRemoveDetailYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.SubcontractorNameExtractor
import views.html.amend.AmendIndividualRemoveDetailYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendIndividualRemoveDetailYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AmendIndividualRemoveDetailYesNoFormProvider,
  subcontractorNameExtractor: SubcontractorNameExtractor,
  val controllerComponents: MessagesControllerComponents,
  view: AmendIndividualRemoveDetailYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  private val validDetails = Set(
    "trading-name",
    "address",
    "contact-details",
    "unique-taxpayer-reference",
    "national-insurance-number",
    "works-reference-number"
  )

  private def withValidDetail(subcontractorDetail: String)(action: => Future[Result]): Future[Result] =
    if (!validDetails.contains(subcontractorDetail)) {
      Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    } else {
      action
    }

  def onPageLoad(subcontractorDetail: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers)
        .map { subcontractorName =>
          withValidDetail(subcontractorDetail) {
            val form = formProvider()
            Future.successful(Ok(view(subcontractorName, subcontractorDetail, form)))
          }
        }
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
    }

  def onSubmit(subcontractorDetail: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers)
        .map { subcontractorName =>
          withValidDetail(subcontractorDetail) {
            formProvider()
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(BadRequest(view(subcontractorName, subcontractorDetail, formWithErrors))),
                value =>
                  if (value) {
                    for {
                      updatedAnswers <-
                        Future.fromTry(
                          request.userAnswers
                            .set(AmendIndividualRemoveDetailYesNoPage(subcontractorDetail), value)
                            .flatMap(_.remove(AmendIndividualRemoveDetailYesNoPage(subcontractorDetail)))
                        )
                      _              <- sessionRepository.set(updatedAnswers)
                    } yield Redirect(
                      controllers.add.routes.CheckYourAnswersController.onPageLoad().url
                    )
                  } else {
                    Future.successful(
                      Redirect(
                        controllers.add.routes.CheckYourAnswersController.onPageLoad().url
                      )
                    )
                  }
              )
          }
        }
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }
}

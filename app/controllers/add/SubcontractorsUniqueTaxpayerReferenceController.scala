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

package controllers.add

import controllers.actions.*
import forms.add.UtrFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.add.SubcontractorsUniqueTaxpayerReferencePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.SubcontractorNameExtractor
import views.html.add.SubcontractorsUniqueTaxpayerReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubcontractorsUniqueTaxpayerReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: UtrFormProvider,
  subcontractorService: SubcontractorService,
  subcontractorNameExtractor: SubcontractorNameExtractor,
  val controllerComponents: MessagesControllerComponents,
  view: SubcontractorsUniqueTaxpayerReferenceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  private def recoveryRedirect =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  private def preparedForm(implicit request: DataRequest[?]) =
    request.userAnswers.get(SubcontractorsUniqueTaxpayerReferencePage).fold(form)(form.fill)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers)
        .fold(recoveryRedirect) { subcontractorName =>
          Ok(view(preparedForm, mode, subcontractorName))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers)
        .fold(Future.successful(recoveryRedirect)) { subcontractorName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, subcontractorName))),
              value =>
                subcontractorService.isDuplicateUTR(request.userAnswers, value).flatMap {
                  case true =>
                    val errorForm = form
                      .fill(value)
                      .withError(
                        key = "value",
                        message = "subcontractorsUniqueTaxpayerReference.error.duplicate"
                      )
                    Future.successful(
                      BadRequest(view(errorForm, mode, subcontractorName))
                    )

                  case false =>
                    for {
                      updatedAnswers <-
                        Future.fromTry(request.userAnswers.set(SubcontractorsUniqueTaxpayerReferencePage, value))
                      _              <- sessionRepository.set(updatedAnswers)
                    } yield Redirect(
                      navigator.nextPage(SubcontractorsUniqueTaxpayerReferencePage, mode, updatedAnswers)
                    )
                }
            )
        }
    }
}

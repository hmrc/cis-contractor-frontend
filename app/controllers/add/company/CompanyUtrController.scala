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

package controllers.add.company

import controllers.actions.*
import forms.add.company.CompanyUtrFormProvider
import models.requests.DataRequest
import models.{AmendMode, FinalValidationMode, Mode}
import navigation.Navigator
import pages.add.company.{CompanyUtrPage, CompanyUtrYesNoPage}
import pages.finalvalidation.FinalValidationBaseUtrPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{SubcontractorService, YesOrNoPageGuardService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import controllers.helpers.SubcontractorNameDisplayHelper
import views.html.add.company.CompanyUtrView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyUtrController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  formProvider: CompanyUtrFormProvider,
  subcontractorService: SubcontractorService,
  yesOrNoPageGuardService: YesOrNoPageGuardService,
  redirectVerifiedSubcontractor: RedirectVerifiedSubcontractorAction,
  val controllerComponents: MessagesControllerComponents,
  view: CompanyUtrView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  private def saveAndContinue(
    mode: Mode,
    value: String
  )(implicit request: DataRequest[?]) =
    for {
      updatedAnswers <-
        Future.fromTry(
          request.userAnswers.set(
            CompanyUtrPage,
            value
          )
        )
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(
      navigator.nextPage(
        CompanyUtrPage,
        mode,
        updatedAnswers
      )
    )

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen redirectVerifiedSubcontractor) { implicit request =>

      val yesOrNoPage       = CompanyUtrYesNoPage
      val yesOrNoPageOption = request.userAnswers.get(CompanyUtrYesNoPage)
      val submitAction      = routes.CompanyUtrController.onSubmit(mode)

      SubcontractorNameDisplayHelper
        .getCompanyDisplayName(request.userAnswers, mode)
        .map { companyName =>
          val preparedForm = request.userAnswers.get(CompanyUtrPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          val result =
            Ok(
              view(
                preparedForm,
                mode,
                companyName,
                submitAction
              )
            )

          yesOrNoPageGuardService.yesOrNoPageRoute(
            result,
            yesOrNoPageOption,
            yesOrNoPage,
            mode
          )
        }
        .getOrElse {
          logger.error(
            "[CompanyUtrController][onPageLoad] - CompanyNamePage missing from userAnswers"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen redirectVerifiedSubcontractor).async { implicit request =>

      val submitAction =
        routes.CompanyUtrController.onSubmit(mode)

      SubcontractorNameDisplayHelper
        .getCompanyDisplayName(request.userAnswers, mode)
        .map { companyName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      mode,
                      companyName,
                      submitAction
                    )
                  )
                ),
              value =>
                val prevValue =
                  request.userAnswers.get(CompanyUtrPage)

                val baseValue =
                  request.userAnswers.get(FinalValidationBaseUtrPage)

                mode match {
                  case AmendMode if prevValue.contains(value) =>
                    saveAndContinue(mode, value)

                  case FinalValidationMode if prevValue.contains(value) || baseValue.contains(value) =>
                    saveAndContinue(mode, value)

                  case _ =>
                    subcontractorService
                      .isDuplicateUTR(request.userAnswers, value)
                      .flatMap {
                        case true =>
                          val errorForm =
                            form
                              .fill(value)
                              .withError(
                                key = "value",
                                message = "companyUtr.error.duplicate"
                              )

                          Future.successful(
                            BadRequest(
                              view(
                                errorForm,
                                mode,
                                companyName,
                                submitAction
                              )
                            )
                          )

                        case false =>
                          saveAndContinue(mode, value)
                      }
                }
            )
        }
        .getOrElse {
          logger.error(
            "[CompanyUtrController][onSubmit] - CompanyNamePage missing from userAnswers"
          )
          Future.successful(
            Redirect(
              controllers.routes.JourneyRecoveryController.onPageLoad()
            )
          )
        }
    }

  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val id = flowId.toString
      if (!request.userAnswers.get(CompanyUtrYesNoPage(id)).contains(true)) {
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      } else {
        SubcontractorNameDisplayHelper
          .getCompanyDisplayName(request.userAnswers, mode, id)
          .map { companyName =>
            val page         = CompanyUtrPage(id)
            val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)
            Ok(
              view(
                preparedForm,
                mode,
                companyName,
                routes.CompanyUtrController.onSubmitWithId(flowId, mode)
              )
            )
          }
          .getOrElse {
            logger.error(
              "[CompanyUtrController][onPageLoadWithId] - CompanyNamePage missing from userAnswers"
            )
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
      }
    }

  def onSubmitWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val id           = flowId.toString
      val submitAction = routes.CompanyUtrController.onSubmitWithId(flowId, mode)
      if (!request.userAnswers.get(CompanyUtrYesNoPage(id)).contains(true)) {
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      } else {
        SubcontractorNameDisplayHelper
          .getCompanyDisplayName(request.userAnswers, mode, id)
          .map { companyName =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, companyName, submitAction))),
                value =>
                  subcontractorService.isDuplicateUTR(request.userAnswers, value, Some(id)).flatMap {
                    case true  =>
                      val errorForm = form
                        .fill(value)
                        .withError(
                          key = "value",
                          message = "companyUtr.error.duplicate"
                        )
                      Future.successful(BadRequest(view(errorForm, mode, companyName, submitAction)))
                    case false =>
                      for {
                        updatedAnswers <-
                          Future.fromTry(
                            request.userAnswers
                              .set(CompanyUtrPage(id), value)
                          )
                        _              <- sessionRepository.set(updatedAnswers)
                      } yield Redirect(
                        navigator.nextPage(CompanyUtrPage(id), mode, updatedAnswers)
                      )
                  }
              )
          }
          .getOrElse {
            logger.error(
              "[CompanyUtrController][onSubmitWithId] - CompanyNamePage missing from userAnswers"
            )
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
      }
    }
}

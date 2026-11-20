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

package controllers.add.partnership

import controllers.actions.*
import controllers.helpers.SubcontractorNameDisplayHelper
import forms.add.partnership.PartnershipUtrFormProvider
import models.{AmendMode, FinalValidationMode, Mode}
import models.requests.DataRequest
import navigation.Navigator
import pages.add.partnership.*
import pages.finalvalidation.FinalValidationBaseUtrPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{SubcontractorService, YesOrNoPageGuardService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.partnership.PartnershipUniqueTaxpayerReferenceView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipUniqueTaxpayerReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  formProvider: PartnershipUtrFormProvider,
  subcontractorService: SubcontractorService,
  yesOrNoPageGuardService: YesOrNoPageGuardService,
  redirectVerifiedSubcontractor: RedirectVerifiedSubcontractorAction,
  val controllerComponents: MessagesControllerComponents,
  view: PartnershipUniqueTaxpayerReferenceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  private def saveAndContinue(mode: Mode, value: String)(implicit request: DataRequest[?]) =
    for {
      updatedAnswers <-
        Future.fromTry(request.userAnswers.set(PartnershipUniqueTaxpayerReferencePage, value))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(
      navigator.nextPage(PartnershipUniqueTaxpayerReferencePage, mode, updatedAnswers)
    )

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen redirectVerifiedSubcontractor) { implicit request =>
      val yesOrNoPage       = PartnershipHasUtrYesNoPage
      val yesOrNoPageOption = request.userAnswers.get(PartnershipHasUtrYesNoPage)
      val submitAction      = routes.PartnershipUniqueTaxpayerReferenceController.onSubmit(mode)

      SubcontractorNameDisplayHelper
        .getPartnershipDisplayName(request.userAnswers, mode)
        .map { partnershipName =>
          val preparedForm = request.userAnswers.get(PartnershipUniqueTaxpayerReferencePage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          val result       = Ok(view(preparedForm, mode, partnershipName, submitAction))
          yesOrNoPageGuardService.yesOrNoPageRoute(result, yesOrNoPageOption, yesOrNoPage, mode)
        }
        .getOrElse {
          logger.error(
            s"[PartnershipUniqueTaxpayerReferenceController][onPageLoad] - PartnershipNamePage missing from userAnswers"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen redirectVerifiedSubcontractor).async { implicit request =>
      val submitAction = routes.PartnershipUniqueTaxpayerReferenceController.onSubmit(mode)
      SubcontractorNameDisplayHelper
        .getPartnershipDisplayName(request.userAnswers, mode)
        .map { name =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, name, submitAction))),
              value =>
                val prevValue = request.userAnswers.get(PartnershipUniqueTaxpayerReferencePage)
                val baseValue = request.userAnswers.get(FinalValidationBaseUtrPage)

                mode match {
                  case AmendMode if prevValue.contains(value) => saveAndContinue(mode, value)

                  case FinalValidationMode if prevValue.contains(value) || baseValue.contains(value) =>
                    saveAndContinue(mode, value)

                  case _ =>
                    subcontractorService.isDuplicateUTR(request.userAnswers, value).flatMap {
                      case true  =>
                        val errorForm = form
                          .fill(value)
                          .withError(
                            key = "value",
                            message = "partnershipUniqueTaxpayerReference.error.duplicate"
                          )
                        Future.successful(
                          BadRequest(view(errorForm, mode, name, submitAction))
                        )
                      case false =>
                        saveAndContinue(mode, value)
                    }
                }
            )
        }
        .getOrElse {
          logger.error(
            s"[PartnershipUniqueTaxpayerReferenceController][onSubmit] - PartnershipNamePage missing from userAnswers"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }

  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>
      val id = flowId.toString
      if (!request.userAnswers.get(PartnershipHasUtrYesNoPage(id)).contains(true)) {
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      } else {
        SubcontractorNameDisplayHelper
          .getPartnershipDisplayName(request.userAnswers, mode, id)
          .map { partnershipName =>
            val page         = PartnershipUniqueTaxpayerReferencePage(id)
            val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)
            Ok(
              view(
                preparedForm,
                mode,
                partnershipName,
                routes.PartnershipUniqueTaxpayerReferenceController.onSubmitWithId(flowId, mode)
              )
            )
          }
          .getOrElse {
            logger.error(
              s"[PartnershipUniqueTaxpayerReferenceController][onPageLoadWithId] - PartnershipNamePage missing from userAnswers"
            )
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
      }
    }

  def onSubmitWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val id           = flowId.toString
      val submitAction = routes.PartnershipUniqueTaxpayerReferenceController.onSubmitWithId(flowId, mode)
      if (!request.userAnswers.get(PartnershipHasUtrYesNoPage(id)).contains(true)) {
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      } else {
        SubcontractorNameDisplayHelper
          .getPartnershipDisplayName(request.userAnswers, mode, id)
          .map { name =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, name, submitAction))),
                value =>
                  subcontractorService.isDuplicateUTR(request.userAnswers, value, Some(id)).flatMap {
                    case true  =>
                      val errorForm = form
                        .fill(value)
                        .withError(
                          key = "value",
                          message = "partnershipUniqueTaxpayerReference.error.duplicate"
                        )
                      Future.successful(BadRequest(view(errorForm, mode, name, submitAction)))
                    case false =>
                      for {
                        updatedAnswers <-
                          Future.fromTry(
                            request.userAnswers
                              .set(PartnershipUniqueTaxpayerReferencePage(id), value)
                          )
                        _              <- sessionRepository.set(updatedAnswers)
                      } yield Redirect(
                        navigator
                          .nextPage(PartnershipUniqueTaxpayerReferencePage(id), mode, updatedAnswers)
                      )
                  }
              )
          }
          .getOrElse {
            logger.error(
              s"[PartnershipUniqueTaxpayerReferenceController][onSubmitWithId] - PartnershipNamePage missing from userAnswers"
            )
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
      }
    }
}

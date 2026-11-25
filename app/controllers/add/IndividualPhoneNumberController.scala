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
import forms.add.IndividualPhoneNumberFormProvider
import models.{FinalValidationMode, Mode}
import models.contact.ContactMethodOptions
import navigation.Navigator
import pages.add.{IndividualContactMethodOptionsPage, IndividualPhoneNumberPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.IndividualPhoneNumberView
import utils.SubcontractorNameExtractor

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndividualPhoneNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sharedSubcontractorJourneyAction: SharedSubcontractorJourneyAction,
  formProvider: IndividualPhoneNumberFormProvider,
  subcontractorNameExtractor: SubcontractorNameExtractor,
  val controllerComponents: MessagesControllerComponents,
  view: IndividualPhoneNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>

      val contactOption     = request.userAnswers.get(IndividualContactMethodOptionsPage)
      val subcontractorName = subcontractorNameExtractor.getSubcontractorName(request.userAnswers, mode)

      val phoneIsAvailable =
        mode == FinalValidationMode ||
          contactOption.exists(_.contains(ContactMethodOptions.Phone))

      (subcontractorName, phoneIsAvailable) match {
        case (Some(subcontractorName), true) =>
          val preparedForm = request.userAnswers.get(IndividualPhoneNumberPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, mode, subcontractorName))

        case (Some(_), false) =>
          Redirect(controllers.add.routes.IndividualContactMethodOptionsController.onPageLoad(mode))
        case _                =>
          logger.error("[IndividualPhoneNumberController][onPageLoad] - subcontractor name missing from userAnswers")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val contactOption     = request.userAnswers.get(IndividualContactMethodOptionsPage)
      val subcontractorName = subcontractorNameExtractor.getSubcontractorName(request.userAnswers, mode)

      val phoneIsAvailable =
        mode == FinalValidationMode ||
          contactOption.exists(_.contains(ContactMethodOptions.Phone))

      (for {
        subcontractorName <- subcontractorName
        if phoneIsAvailable
      } yield form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, subcontractorName))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(IndividualPhoneNumberPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(IndividualPhoneNumberPage, mode, updatedAnswers))
        ))
        .getOrElse {
          logger.error(
            "[IndividualPhoneNumberController][onSubmit] - subcontractor name missing or phone not selected"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
  }

  // Multi-tab
  def onPageLoadWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    sharedSubcontractorJourneyAction(flowId, mode) { namespace =>
      implicit request =>

        val id = flowId.toString

        val contactOption =
          request.userAnswers.get(IndividualContactMethodOptionsPage(id, namespace))

        val subcontractorName =
          subcontractorNameExtractor.getSubcontractorName(request.userAnswers, mode, id, namespace)

        val phoneIsAvailable =
          mode == FinalValidationMode ||
            contactOption.exists(_.contains(ContactMethodOptions.Phone))

        (subcontractorName, phoneIsAvailable) match {
          case (Some(subcontractorName), true) =>
            val preparedForm = request.userAnswers.get(IndividualPhoneNumberPage(id, namespace)) match {
              case None => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, mode, subcontractorName, Some(flowId)))

          case (Some(_), false) =>
            // TODO: Restore scoped Add Subcontractor navigation when Add routes are integrated.
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

          case _ =>
            logger.error(
              "[IndividualPhoneNumberController][onPageLoadWithId] - subcontractor name missing from userAnswers"
            )
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmitWithId(flowId: UUID, mode: Mode): Action[AnyContent] =
    sharedSubcontractorJourneyAction.async(flowId, mode) { namespace =>
      implicit request =>

        val id = flowId.toString
        val page = IndividualPhoneNumberPage(id, namespace)

        val contactOption =
          request.userAnswers.get(IndividualContactMethodOptionsPage(id, namespace))

        val subcontractorName =
          subcontractorNameExtractor.getSubcontractorName(request.userAnswers, mode, id, namespace)

        val phoneIsAvailable =
          mode == FinalValidationMode ||
            contactOption.exists(_.contains(ContactMethodOptions.Phone))

        (for {
          subcontractorName <- subcontractorName
          if phoneIsAvailable
        } yield form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, subcontractorName, Some(flowId)))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(page, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(page, mode, updatedAnswers))
          ))
          .getOrElse {
            logger.error(
              "[IndividualPhoneNumberController][onSubmitWithId] - subcontractor name missing or phone not selected"
            )
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
    }
}

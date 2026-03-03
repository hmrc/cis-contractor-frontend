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
import forms.add.TypeOfSubcontractorFormProvider
import models.{CheckMode, Mode, NormalMode}
import navigation.Navigator
import pages.add.{ChangingTypeFromCyaPage, HasSwitchedTypeFromCyaPage, TypeOfSubcontractorPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.add.TypeOfSubcontractorView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TypeOfSubcontractorController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: TypeOfSubcontractorFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: TypeOfSubcontractorView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(TypeOfSubcontractorPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value => {
          val oldValue = request.userAnswers.get(TypeOfSubcontractorPage)
          val newValue = value

          val changingFromCya = request.userAnswers.get(ChangingTypeFromCyaPage).contains(true)
          val hasSwitchedType = request.userAnswers.get(HasSwitchedTypeFromCyaPage).contains(true)

          val isDifferentType = oldValue.exists(_ != newValue)

          // Existing logic (keep)
          val shouldResetToNormalModeBecauseDifferentType = mode == CheckMode && isDifferentType

          // New: if they've already switched type, never allow check-mode shortcut back to CYA
          val shouldForceNormalModeBecauseAlreadySwitched = mode == CheckMode && hasSwitchedType

          val resolvedMode =
            if (shouldResetToNormalModeBecauseDifferentType || shouldForceNormalModeBecauseAlreadySwitched) NormalMode else mode

          val shouldClearFlagsBecauseNoChange =
            mode == CheckMode && changingFromCya && !isDifferentType

          val shouldMarkHasSwitched =
            mode == CheckMode && changingFromCya && isDifferentType

          for {
            uaWithType <- Future.fromTry(request.userAnswers.set(TypeOfSubcontractorPage, value))

            uaWithFlags <-
              if (shouldClearFlagsBecauseNoChange) {
                for {
                  ua1 <- Future.fromTry(uaWithType.set(ChangingTypeFromCyaPage, false))
                  ua2 <- Future.fromTry(ua1.set(HasSwitchedTypeFromCyaPage, false))
                } yield ua2
              } else if (shouldMarkHasSwitched) {
                Future.fromTry(uaWithType.set(HasSwitchedTypeFromCyaPage, true))
                // keep ChangingTypeFromCyaPage = true until CYA is reached again (optional)
              } else {
                Future.successful(uaWithType)
              }

            // IMPORTANT: keep your existing "clear data when type changes" behaviour here
            // e.g. uaFinal = clearTypeSpecificData(oldValue, newValue, uaWithFlags)
            // If cleanup already happens inside set(TypeOfSubcontractorPage, ...) via Page.cleanup, you don't need extra code.

            _ <- sessionRepository.set(uaWithFlags)
          } yield Redirect(navigator.nextPage(TypeOfSubcontractorPage, resolvedMode, uaWithFlags))
        }
      )
  }
}

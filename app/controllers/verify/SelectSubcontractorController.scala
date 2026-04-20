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

package controllers.verify

import controllers.actions.*
import forms.verify.SelectSubcontractorFormProvider
import models.Mode
import models.UserAnswers
import navigation.Navigator
import pages.verify.SelectSubcontractorPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.SelectSubcontractorView
import services.PaginationService
import models.SelectSubcontractor

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectSubcontractorController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  formProvider: SelectSubcontractorFormProvider,
  paginationService: PaginationService,
  val controllerComponents: MessagesControllerComponents,
  view: SelectSubcontractorView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode, page: Int = 1): Action[AnyContent] =
    (identify andThen getData) { implicit request =>

      implicit val messages: Messages = messagesApi.preferred(request)

      request.userAnswers match {

        case None =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

        case Some(ua) =>
          val preparedForm =
            ua.get(SelectSubcontractorPage)
              .map(form.fill)
              .getOrElse(form)

          val result =
            paginationService.paginateCheckboxItems(
              SelectSubcontractor.checkboxItems,
              page,
              routes.SelectSubcontractorController.onPageLoad(mode, page).url
            )

          Ok(
            view(
              preparedForm,
              mode,
              result.paginatedData,
              result.paginationViewModel,
              page
            )
          )
      }
    }

  def onSubmit(mode: Mode, page: Int = 1): Action[AnyContent] =
    (identify andThen getData).async { implicit request =>

      implicit val messages: Messages = messagesApi.preferred(request)

      request.userAnswers match {

        case None =>
          Future.successful(
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          )

        case Some(ua) =>
          val allItems =
            SelectSubcontractor.checkboxItems

          val result =
            paginationService.paginateCheckboxItems(
              allItems,
              page,
              routes.SelectSubcontractorController.onPageLoad(mode, 1).url
            )

          val currentPageValues: Set[SelectSubcontractor] =
            result.paginatedData
              .flatMap(item => SelectSubcontractor.values.find(_.toString == item.value))
              .toSet

          val otherPageValues: Set[SelectSubcontractor] =
            ua.get(SelectSubcontractorPage).getOrElse(Set.empty).diff(currentPageValues)

          val gotoPage: Option[Int] =
            request.body.asFormUrlEncoded
              .flatMap(_.get("gotoPage"))
              .flatMap(_.headOption)
              .flatMap(_.toIntOption)

          gotoPage match {
            case Some(targetPage) =>
              val currentSelectedValues: Set[SelectSubcontractor] =
                request.body.asFormUrlEncoded
                  .getOrElse(Map.empty)
                  .filter { case (k, _) => k == "value" || k.matches("""value\[\d+]""") }
                  .values
                  .flatten
                  .flatMap(v => SelectSubcontractor.values.find(_.toString == v))
                  .toSet

              val mergedValues = otherPageValues ++ currentSelectedValues

              val saveAction: Future[UserAnswers] =
                if (mergedValues.nonEmpty) {
                  Future.fromTry(ua.set(SelectSubcontractorPage, mergedValues))
                } else {
                  Future.successful(ua)
                }

              for {
                updatedAnswers <- saveAction
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(routes.SelectSubcontractorController.onPageLoad(mode, targetPage))

            case None =>
              form
                .bindFromRequest()
                .fold(
                  formWithErrors =>
                    Future.successful(
                      BadRequest(
                        view(
                          formWithErrors,
                          mode,
                          result.paginatedData,
                          result.paginationViewModel,
                          page
                        )
                      )
                    ),
                  value =>
                    for {
                      updatedAnswers <- Future.fromTry(ua.set(SelectSubcontractorPage, value ++ otherPageValues))
                      _              <- sessionRepository.set(updatedAnswers)
                    } yield Redirect(
                      navigator.nextPage(SelectSubcontractorPage, mode, updatedAnswers)
                    )
                )
          }
      }
    }
}

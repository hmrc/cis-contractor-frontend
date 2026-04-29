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
import forms.verify.SelectSubcontractorsToReverifyFormProvider
import models.Mode
import navigation.Navigator
import pages.verify.SelectSubcontractorsToReverifyPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.SelectSubcontractorsToReverifyView
import viewmodels.verify.SubcontractorReverifyData
import models.verify.SelectedSubcontractors
import services.PaginationToReverifyService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectSubcontractorsToReverifyController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SelectSubcontractorsToReverifyFormProvider,
  paginationToReverifyService: PaginationToReverifyService,
  val controllerComponents: MessagesControllerComponents,
  view: SelectSubcontractorsToReverifyView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  private val allRows = SubcontractorReverifyData.rows

  def onPageLoad(mode: Mode, page: Int = 1): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>

      val result =
        paginationToReverifyService.paginate(
          allItems = allRows,
          currentPage = page,
          recordsPerPage = 6,
          baseUrl = controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(mode).url
        )

      val preparedForm =
        request.userAnswers
          .get(SelectSubcontractorsToReverifyPage)
          .map(subs => form.fill(subs.map(_.id)))
          .getOrElse(form)

      Ok(
        view(
          preparedForm,
          mode,
          result.items,
          result.pagination,
          page,
          result.startIndex,
          result.totalCount
        )
      )
    }

  def onSubmit(mode: Mode, page: Int = 1): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val result =
        paginationToReverifyService.paginate(
          allItems = allRows,
          currentPage = page,
          recordsPerPage = 6,
          baseUrl = controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(mode).url
        )

      val boundForm = form.bindFromRequest()

      val selectedIds: Set[String] =
        boundForm.value.getOrElse(Set.empty)

      val allSubs = allRows

      val currentSelections: Set[SelectedSubcontractors] =
        selectedIds.flatMap(id => allSubs.find(_.id == id).map(row => SelectedSubcontractors(row.id, row.name)))

      val currentPageIds: Set[String] =
        result.items.map(_.id).toSet

      val previousSelections: Set[SelectedSubcontractors] =
        request.userAnswers
          .get(SelectSubcontractorsToReverifyPage)
          .getOrElse(Set.empty)
          .filterNot(sub => currentPageIds.contains(sub.id))

      val mergedSelections: Set[SelectedSubcontractors] =
        previousSelections ++ currentSelections

      val gotoPage: Option[Int] =
        request.body.asFormUrlEncoded
          .flatMap(_.get("gotoPage"))
          .flatMap(_.headOption)
          .flatMap(_.toIntOption)

      gotoPage match {

        case Some(targetPage) =>
          for {
            updatedAnswers <- Future.fromTry(
                                request.userAnswers.set(SelectSubcontractorsToReverifyPage, mergedSelections)
                              )
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(
            routes.SelectSubcontractorsToReverifyController.onPageLoad(mode, targetPage)
          )

        case None =>
          boundForm.fold(
            formWithErrors =>
              Future.successful(
                BadRequest(
                  view(
                    formWithErrors,
                    mode,
                    result.items,
                    result.pagination,
                    page,
                    result.startIndex,
                    result.totalCount
                  )
                )
              ),
            _ =>
              for {
                updatedAnswers <- Future.fromTry(
                                    request.userAnswers.set(SelectSubcontractorsToReverifyPage, mergedSelections)
                                  )
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(
                navigator.nextPage(SelectSubcontractorsToReverifyPage, mode, updatedAnswers)
              )
          )
      }
    }
}

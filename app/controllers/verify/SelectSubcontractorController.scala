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
import models.{Mode, SubcontractorViewModel}
import navigation.Navigator
import pages.verify.SelectSubcontractorPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{PaginationService, SubcontractorSource}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.SelectSubcontractorView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

object SelectSubcontractorController {
  val subcontractors: Seq[SubcontractorViewModel] = SubcontractorSource.subcontractors
}

class SelectSubcontractorController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  formProvider: SelectSubcontractorFormProvider,
  paginationService: PaginationService,
  subcontractorSource: SubcontractorSource,
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

        case Some(_) if subcontractorSource.get().isEmpty =>
          Redirect(routes.NoSubcontractorsAddedController.onPageLoad())

        case Some(ua) =>
          val preparedForm =
            ua.get(SelectSubcontractorPage)
              .map(subs => form.fill(subs.map(_.id)))
              .getOrElse(form)

          val result =
            paginationService.paginateCheckboxItems(
              SubcontractorViewModel.checkboxItems(subcontractorSource.get()),
              page
            )

          Ok(
            view(
              preparedForm,
              mode,
              result.paginatedData,
              result.paginationViewModel,
              page,
              result.startIndex,
              result.totalCount
            )
          )
      }
    }

  def onSubmit(mode: Mode, page: Int = 1): Action[AnyContent] =
    (identify andThen getData).async { implicit request =>

      implicit val messages: Messages = messagesApi.preferred(request)

      request.userAnswers match {

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

        case Some(ua) =>
          val allSubs  = subcontractorSource.get()
          val allItems = SubcontractorViewModel.checkboxItems(allSubs)

          val result =
            paginationService.paginateCheckboxItems(allItems, page)

          val currentPageIds: Set[String] =
            result.paginatedData.map(_.value).toSet

          val otherPageValues: Set[SubcontractorViewModel] =
            ua.get(SelectSubcontractorPage)
              .getOrElse(Set.empty)
              .filterNot(sub => currentPageIds.contains(sub.id))

          val boundForm = form.bindFromRequest()

          val currentSelectedValues: Set[SubcontractorViewModel] =
            boundForm.value
              .getOrElse(Set.empty)
              .flatMap(id => allSubs.find(_.id == id))

          val mergedValues: Set[SubcontractorViewModel] = otherPageValues ++ currentSelectedValues

          val gotoPage: Option[Int] =
            request.body.asFormUrlEncoded
              .flatMap(_.get("gotoPage"))
              .flatMap(_.headOption)
              .flatMap(_.toIntOption)

          gotoPage match {
            case Some(targetPage) =>
              for {
                updatedAnswers <- Future.fromTry(ua.set(SelectSubcontractorPage, mergedValues))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(routes.SelectSubcontractorController.onPageLoad(mode, targetPage))

            case None =>
              if (mergedValues.nonEmpty) {
                for {
                  updatedAnswers <- Future.fromTry(ua.set(SelectSubcontractorPage, mergedValues))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(SelectSubcontractorPage, mode, updatedAnswers))
              } else {
                boundForm.fold(
                  formWithErrors =>
                    Future.successful(
                      BadRequest(
                        view(
                          formWithErrors,
                          mode,
                          result.paginatedData,
                          result.paginationViewModel,
                          page,
                          result.startIndex,
                          result.totalCount
                        )
                      )
                    ),
                  ids =>
                    val selected = ids.flatMap(id => allSubs.find(_.id == id)) ++ otherPageValues
                    for {
                      updatedAnswers <- Future.fromTry(ua.set(SelectSubcontractorPage, selected))
                      _              <- sessionRepository.set(updatedAnswers)
                    } yield Redirect(navigator.nextPage(SelectSubcontractorPage, mode, updatedAnswers))
                )
              }
          }
      }
    }
}

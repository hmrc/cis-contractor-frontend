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
import models.{Mode, Subcontractor, SubcontractorViewModel, UserAnswers}
import navigation.Navigator
import pages.verify.{NewestVerificationBatchResponsePage, SelectSubcontractorPage, UnverifiedSubcontractorsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.PaginationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.SelectSubcontractorView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectSubcontractorController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SelectSubcontractorFormProvider,
  paginationService: PaginationService,
  val controllerComponents: MessagesControllerComponents,
  view: SelectSubcontractorView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form = formProvider()

  def onPageLoad(mode: Mode, page: Int = 1): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>

      val userAnswers = request.userAnswers

      getUnverifiedSubcontractorsOrRedirect(userAnswers) match {
        case Right(unverifiedSubcontractors) =>
          val (errors, subcontractorsVm) =
            SubcontractorViewModel.fromSubcontractors(unverifiedSubcontractors)

          errors.foreach(logger.error(_))

          if (subcontractorsVm.isEmpty) {
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          } else {
            val preparedForm =
              userAnswers
                .get(SelectSubcontractorPage)
                .map(subs => form.fill(subs.map(_.id)))
                .getOrElse(form)

            val result =
              paginationService.paginateCheckboxItems(
                SubcontractorViewModel.checkboxItems(subcontractorsVm),
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

        case Left(redirectResult) =>
          redirectResult
      }
    }

  def onSubmit(mode: Mode, page: Int = 1): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val ua = request.userAnswers

      ua.get(UnverifiedSubcontractorsPage) match {

        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

        case Some(unverifiedSubcontractors) if unverifiedSubcontractors.isEmpty =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

        case Some(unverifiedSubcontractors) =>
          val (errors, subcontractorsVm) =
            SubcontractorViewModel.fromSubcontractors(unverifiedSubcontractors)

          errors.foreach(logger.error(_))

          if (subcontractorsVm.isEmpty) {
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          } else {
            val allItems = SubcontractorViewModel.checkboxItems(subcontractorsVm)

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
                .flatMap(id => subcontractorsVm.find(_.id == id))

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
                      val selected = ids.flatMap(id => subcontractorsVm.find(_.id == id)) ++ otherPageValues
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

  private def getUnverifiedSubcontractorsOrRedirect(userAnswers: UserAnswers): Either[Result, Seq[Subcontractor]] =
    userAnswers.get(NewestVerificationBatchResponsePage) match {
      case None =>
        Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

      case Some(newestVerificationBatchResponse) if newestVerificationBatchResponse.subcontractors.isEmpty =>
        Left(Redirect(routes.NoSubcontractorsAddedController.onPageLoad()))

      case Some(_) =>
        userAnswers.get(UnverifiedSubcontractorsPage) match {
          case Some(unverifiedSubcontractors) if unverifiedSubcontractors.nonEmpty =>
            Right(unverifiedSubcontractors)

          case Some(_) =>
            Left(Redirect(controllers.verify.routes.VerifyYourSubcontractorsYesNoController.onPageLoad))

          case None =>
            Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }

}

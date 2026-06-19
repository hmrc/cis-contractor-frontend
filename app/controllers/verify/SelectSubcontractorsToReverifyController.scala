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
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.SelectSubcontractorsToReverifyView
import viewmodels.verify.SubcontractorReverifyRow
import models.verify.SelectedSubcontractors
import pages.verify.UnverifiedSubcontractorsPage
import pages.verify.SelectSubcontractorPage
import services.PaginationToReverifyService
import models.Subcontractor
import models.requests.DataRequest
import models.verify.*
import pages.verify.*
import rules.verify.ReverificationRules

import java.time.{Clock, LocalDate}
import java.time.format.DateTimeFormatter
import java.util.Locale
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
  clock: Clock,
  val controllerComponents: MessagesControllerComponents,
  view: SelectSubcontractorsToReverifyView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val dateFmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.UK)

  private def recovery =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  private def toRow(sub: Subcontractor, currentDate: LocalDate)(implicit
    messages: Messages
  ): Option[SubcontractorReverifyRow] =
    if (!sub.verified.contains("Y")) {
      None
    } else {
      val reverify = ReverificationRules.reverifyRequired(sub, currentDate)

      val name = nameFor(sub).getOrElse(messages("verify.noName"))
      val utr  = sub.utr.filter(_.nonEmpty).getOrElse("")

      val verifiedCol =
        if (reverify) messages("site.no")
        else messages("site.yes")

      val verificationNumber =
        if (!reverify) {
          sub.verificationNumber
            .filter(_.nonEmpty)
            .getOrElse(messages("site.unknown"))
        } else {
          messages("site.unknown")
        }

      val taxTreatment =
        if (reverify) {
          messages("site.unknown")
        } else {
          sub.taxTreatment match {
            case Some("net")       => messages("verify.selectSubcontractorsToReverify.taxTreatment.net")
            case Some("unmatched") => messages("verify.selectSubcontractorsToReverify.taxTreatment.unmatched")
            case Some("gross")     => messages("verify.selectSubcontractorsToReverify.taxTreatment.gross")
            case _                 => messages("site.unknown")
          }
        }

      val dateAdded =
        sub.createDate
          .map(_.toLocalDate.format(dateFmt))
          .getOrElse(messages("site.unknown"))

      Some(
        SubcontractorReverifyRow(
          id = sub.subcontractorId.toString,
          name = name,
          utr = utr,
          verified = verifiedCol,
          verificationNumber = verificationNumber,
          taxTreatment = taxTreatment,
          dateAdded = dateAdded
        )
      )
    }

  private def nameFor(sub: Subcontractor)(implicit messages: Messages): Option[String] = {
    val first              = sub.firstName.map(_.trim).filter(_.nonEmpty)
    val sur                = sub.surname.map(_.trim).filter(_.nonEmpty)
    val trading            = sub.tradingName.map(_.trim).filter(_.nonEmpty)
    val partnershipTrading = sub.partnershipTradingName.map(_.trim).filter(_.nonEmpty)

    val individualName: Option[String] =
      sur.map { sur =>
        first match {
          case Some(first) => s"$sur, $first"
          case None        => sur
        }
      }

    sub.subcontractorType match {
      case Some(t) if t.equalsIgnoreCase("partnership")                            =>
        partnershipTrading.orElse(trading)
      case Some(t) if t.equalsIgnoreCase("company") || t.equalsIgnoreCase("trust") =>
        trading
      case Some(_)                                                                 =>
        individualName.orElse(trading)
      case None                                                                    =>
        Some(messages("verify.noName"))
    }
  }

  private def buildRowsFromSession(implicit request: DataRequest[?]): Either[Result, Seq[SubcontractorReverifyRow]] = {
    val currentDate = LocalDate.now(clock)

    request.userAnswers.get(NewestVerificationBatchResponsePage) match {
      case None       =>
        Left(recovery)
      case Some(resp) =>
        Right(resp.subcontractors.flatMap(s => toRow(s, currentDate)))
    }
  }

  def onPageLoad(mode: Mode, page: Int = 1): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      buildRowsFromSession match {
        case Left(result) => Future.successful(result)

        case Right(rows) =>
          val sortedRows = rows.sortBy(_.name.toLowerCase(Locale.UK))

          val result =
            paginationToReverifyService.paginate(
              allItems = sortedRows,
              currentPage = page,
              recordsPerPage = 6,
              baseUrl = controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(mode).url
            )

          val preparedForm =
            request.userAnswers
              .get(SelectSubcontractorsToReverifyPage)
              .map(subs => formProvider(requireSelection = false).fill(subs.map(_.id)))
              .getOrElse(formProvider(requireSelection = false))

          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(SubcontractorReverifyRowsPage, sortedRows))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Ok(
            view(preparedForm, mode, result.items, result.pagination, page, result.startIndex, result.totalCount)
          )
      }
    }

  def onSubmit(mode: Mode, page: Int = 1): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val allRows: Seq[SubcontractorReverifyRow] =
        request.userAnswers
          .get(SubcontractorReverifyRowsPage)
          .getOrElse(Seq.empty)
          .sortBy(_.name.toLowerCase(Locale.UK))

      val result =
        paginationToReverifyService.paginate(
          allItems = allRows,
          currentPage = page,
          recordsPerPage = 6,
          baseUrl = routes.SelectSubcontractorsToReverifyController.onPageLoad(mode).url
        )

      val hasUnverified: Boolean =
        request.userAnswers.get(UnverifiedSubcontractorsPage).exists(_.nonEmpty)

      val hasSelectedUnverifiedEarlier: Boolean =
        request.userAnswers.get(SelectSubcontractorPage).exists(_.nonEmpty)

      val requireSelection: Boolean =
        !hasUnverified && !hasSelectedUnverifiedEarlier

      val boundForm = formProvider(requireSelection).bindFromRequest()

      val selectedIdsThisPage: Set[String] =
        boundForm.value.getOrElse(Set.empty[String])

      val currentPageIds: Set[String] =
        result.items.map(_.id).toSet

      val existingSelections: Set[SelectedSubcontractors] =
        request.userAnswers
          .get(SelectSubcontractorsToReverifyPage)
          .getOrElse(Set.empty)

      val previousSelections: Set[SelectedSubcontractors] =
        existingSelections.filterNot(sub => currentPageIds.contains(sub.id))

      val currentSelections: Set[SelectedSubcontractors] =
        selectedIdsThisPage.flatMap(id => allRows.find(_.id == id).map(r => SelectedSubcontractors(r.id, r.name)))

      val mergedSelections: Set[SelectedSubcontractors] =
        previousSelections ++ currentSelections

      val hasAnyReverifySelection: Boolean =
        mergedSelections.nonEmpty

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
          if (hasUnverified && !hasSelectedUnverifiedEarlier && !hasAnyReverifySelection) {
            Future.successful(
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            )

          } else if (hasUnverified && hasSelectedUnverifiedEarlier && !hasAnyReverifySelection) {
            for {
              updatedAnswers <- Future.fromTry(
                                  request.userAnswers.set(SelectSubcontractorsToReverifyPage, mergedSelections)
                                )
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(SelectSubcontractorsToReverifyPage, mode, updatedAnswers)
            )

          } else {
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
}

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
import models.finalvalidation.{FinalValidationContext, VerifyFinalValidationSource}
import models.{Mode, Subcontractor, TypeOfSubcontractor, UserAnswers}
import navigation.Navigator
import pages.verify.SelectSubcontractorsToReverifyPage
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import utils.DateTimeFormats
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.verify.SelectSubcontractorsToReverifyView
import viewmodels.verify.SubcontractorReverifyRow
import models.verify.SelectedSubcontractors
import pages.verify.UnverifiedSubcontractorsPage
import pages.verify.SelectSubcontractorPage
import services.{PaginationToReverifyService, VerificationPreSelectionService}
import models.requests.DataRequest
import models.verify.*
import pages.verify.*
import pages.finalvalidation.*
import play.api.data.Form
import rules.verify.ReverificationRules

import java.time.{Clock, LocalDate}
import java.util.{Locale, UUID}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectSubcontractorsToReverifyController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  formProvider: SelectSubcontractorsToReverifyFormProvider,
  paginationToReverifyService: PaginationToReverifyService,
  verificationPreSelectionService: VerificationPreSelectionService,
  clock: Clock,
  val controllerComponents: MessagesControllerComponents,
  view: SelectSubcontractorsToReverifyView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with PendingVerificationRequestGuard
    with Logging {

  private def dateFmt(implicit messages: Messages) =
    DateTimeFormats.shortDateFormat()(messages.lang)

  private def recovery =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  private def toRow(
    sub: Subcontractor,
    currentDate: LocalDate
  )(implicit messages: Messages): Option[SubcontractorReverifyRow] =
    if (!sub.isVerified) {
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
            case Some("net")       =>
              messages("verify.selectSubcontractorsToReverify.taxTreatment.net")
            case Some("unmatched") =>
              messages("verify.selectSubcontractorsToReverify.taxTreatment.unmatched")
            case Some("gross")     =>
              messages("verify.selectSubcontractorsToReverify.taxTreatment.gross")
            case _                 =>
              messages("site.unknown")
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

  private def nameFor(
    sub: Subcontractor
  )(implicit messages: Messages): Option[String] = {

    val first =
      sub.firstName.map(_.trim).filter(_.nonEmpty)

    val sur =
      sub.surname.map(_.trim).filter(_.nonEmpty)

    val trading =
      sub.tradingName.map(_.trim).filter(_.nonEmpty)

    val partnershipTrading =
      sub.partnershipTradingName.map(_.trim).filter(_.nonEmpty)

    val individualName: Option[String] =
      sur.map { surname =>
        first match {
          case Some(firstName) =>
            s"$surname, $firstName"
          case None            =>
            surname
        }
      }

    sub.subcontractorType
      .flatMap(TypeOfSubcontractor.fromString)
      .map {
        case TypeOfSubcontractor.Partnership =>
          partnershipTrading.orElse(trading)

        case TypeOfSubcontractor.Limitedcompany | TypeOfSubcontractor.Trust =>
          trading

        case TypeOfSubcontractor.Individualorsoletrader =>
          individualName.orElse(trading)
      }
      .getOrElse(Some(messages("verify.noName")))
  }

  private def reverifySubmitAction(mode: Mode, page: Int, flowId: Option[UUID]): Call =
    flowId
      .map(id => routes.SelectSubcontractorsToReverifyController.onSubmitWithId(id, mode, page))
      .getOrElse(routes.SelectSubcontractorsToReverifyController.onSubmit(mode, page))

  private def reverifyPageUrl(mode: Mode, flowId: Option[UUID]): String =
    flowId
      .map(id => routes.SelectSubcontractorsToReverifyController.onPageLoadWithId(id, mode).url)
      .getOrElse(routes.SelectSubcontractorsToReverifyController.onPageLoad(mode).url)

  private def buildRowsFromSession(
    subJourneyId: Option[String]
  )(implicit request: DataRequest[?]): Either[Result, Seq[(Subcontractor, SubcontractorReverifyRow)]] = {

    val currentDate = LocalDate.now(clock)
    val newestPage  =
      subJourneyId.map(NewestVerificationBatchResponsePage.apply).getOrElse(NewestVerificationBatchResponsePage)

    request.userAnswers.get(newestPage) match {
      case None =>
        logger.error(
          "[SelectSubcontractorsToReverifyController][buildRowsFromSession] - " +
            "NewestVerificationBatchResponsePage missing from userAnswers"
        )
        Left(recovery)

      case Some(resp) =>
        Right(
          resp.subcontractors.flatMap { subcontractor =>
            toRow(subcontractor, currentDate)
              .map(row => subcontractor -> row)
          }
        )
    }
  }

  def onPageLoad(
    mode: Mode,
    page: Int = 1
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      loadReverifyPage(mode, page, None)
    }

  def onPageLoadWithId(flowId: UUID, mode: Mode, page: Int = 1): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)).async {
      implicit request =>
        loadReverifyPage(mode, page, Some(flowId))
    }

  private def loadReverifyPage(mode: Mode, page: Int, flowId: Option[UUID])(implicit
    request: DataRequest[?]
  ): Future[Result] = {
    val id = flowId.map(_.toString)
    redirectIfVerificationRequestInProgress(request.userAnswers, id) match {
      case Some(redirectResult) =>
        Future.successful(redirectResult)

      case None =>
        buildRowsFromSession(id) match {

          case Left(result) =>
            Future.successful(result)

          case Right(rowsWithSubcontractors) =>
            val sortedRowsWithSubcontractors =
              rowsWithSubcontractors.sortBy { case (_, row) =>
                row.name.toLowerCase(Locale.UK)
              }

            val sortedRows =
              sortedRowsWithSubcontractors.map(_._2)

            val result =
              paginationToReverifyService.paginate(
                allItems = sortedRows,
                currentPage = page,
                baseUrl = reverifyPageUrl(mode, flowId)
              )

            val displayedSubcontractors =
              sortedRowsWithSubcontractors.map(_._1)

            val reverifyPage =
              id.map(SelectSubcontractorsToReverifyPage.apply).getOrElse(SelectSubcontractorsToReverifyPage)
            val rowsPage     = id.map(SubcontractorReverifyRowsPage.apply).getOrElse(SubcontractorReverifyRowsPage)

            val selectedSubcontractors =
              request.userAnswers.get(reverifyPage).getOrElse {
                val selectedIds =
                  verificationPreSelectionService.preSelectedSubcontractorIds(
                    displayedSubcontractors,
                    request.userAnswers
                  )

                sortedRows
                  .filter(row => selectedIds.contains(row.id))
                  .map(row => SelectedSubcontractors(row.id, row.name))
                  .toSet
              }

            val preparedForm =
              formProvider(requireSelection = false)
                .fill(selectedSubcontractors.map(_.id))

            for {
              updatedAnswers <- Future.fromTry(
                                  request.userAnswers
                                    .set(
                                      rowsPage,
                                      sortedRows
                                    )
                                    .flatMap(
                                      _.set(
                                        reverifyPage,
                                        selectedSubcontractors
                                      )
                                    )
                                )

              _ <- sessionRepository.set(updatedAnswers)

            } yield Ok(
              view(
                preparedForm,
                mode,
                result.items,
                result.pagination,
                result.currentPage,
                result.startIndex,
                result.totalCount,
                result.totalPages,
                reverifySubmitAction(mode, page, flowId)
              )
            )
        }
    }
  }

  def onSubmit(
    mode: Mode,
    page: Int = 1
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      submitReverify(mode, page, None)
    }

  def onSubmitWithId(flowId: UUID, mode: Mode, page: Int = 1): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forVerifyJourney(flowId)).async {
      implicit request =>
        submitReverify(mode, page, Some(flowId))
    }

  private def submitReverify(mode: Mode, page: Int, flowId: Option[UUID])(implicit
    request: DataRequest[AnyContent]
  ): Future[Result] = {
    val id = flowId.map(_.toString)
    redirectIfVerificationRequestInProgress(request.userAnswers, id) match {
      case Some(redirectResult) =>
        Future.successful(redirectResult)

      case None =>
        val rowsPage       = id.map(SubcontractorReverifyRowsPage.apply).getOrElse(SubcontractorReverifyRowsPage)
        val reverifyPage   =
          id.map(SelectSubcontractorsToReverifyPage.apply).getOrElse(SelectSubcontractorsToReverifyPage)
        val unverifiedPage = id.map(UnverifiedSubcontractorsPage.apply).getOrElse(UnverifiedSubcontractorsPage)
        val selectPage     = id.map(SelectSubcontractorPage.apply).getOrElse(SelectSubcontractorPage)
        val contextPage    = id.map(FinalValidationContextPage.apply).getOrElse(FinalValidationContextPage)
        val sourcePage     = id.map(VerifyFinalValidationSourcePage.apply).getOrElse(VerifyFinalValidationSourcePage)

        val allRows: Seq[SubcontractorReverifyRow] =
          request.userAnswers
            .get(rowsPage)
            .getOrElse(Seq.empty)
            .sortBy(_.name.toLowerCase(Locale.UK))

        val result =
          paginationToReverifyService.paginate(
            allItems = allRows,
            currentPage = page,
            baseUrl = reverifyPageUrl(mode, flowId)
          )

        val hasUnverified: Boolean =
          request.userAnswers
            .get(unverifiedPage)
            .exists(_.nonEmpty)

        val hasSelectedUnverifiedEarlier: Boolean =
          request.userAnswers
            .get(selectPage)
            .exists(_.nonEmpty)

        val existingSelections: Set[SelectedSubcontractors] =
          request.userAnswers
            .get(reverifyPage)
            .getOrElse(Set.empty)

        val currentPageIds: Set[String] =
          result.items.map(_.id).toSet

        val requireSelection: Boolean =
          !hasSelectedUnverifiedEarlier &&
            !hasUnverified &&
            existingSelections.filterNot(s => currentPageIds.contains(s.id)).isEmpty

        val boundForm =
          formProvider(requireSelection).bindFromRequest()

        val selectedIdsThisPage: Set[String] =
          boundForm.value.getOrElse(Set.empty[String])

        val selectionsFromOtherPages: Set[SelectedSubcontractors] =
          existingSelections.filterNot { selection =>
            currentPageIds.contains(selection.id)
          }

        val selectionsFromCurrentPage: Set[SelectedSubcontractors] =
          result.items
            .filter(row => selectedIdsThisPage.contains(row.id))
            .map { row =>
              SelectedSubcontractors(
                row.id,
                row.name
              )
            }
            .toSet

        val updatedSelections: Set[SelectedSubcontractors] =
          selectionsFromOtherPages ++ selectionsFromCurrentPage

        val gotoPage: Option[Int] =
          request.body.asFormUrlEncoded
            .flatMap(_.get("gotoPage"))
            .flatMap(_.headOption)
            .flatMap(_.toIntOption)

        def renderForm(
          formWithErrors: Form[Set[String]]
        ): Result =
          BadRequest(
            view(
              formWithErrors,
              mode,
              result.items,
              result.pagination,
              result.currentPage,
              result.startIndex,
              result.totalCount,
              result.totalPages,
              reverifySubmitAction(mode, page, flowId)
            )
          )

        def saveSelectionsAndRedirect(
          redirectTo: UserAnswers => Call
        ): Future[Result] =
          for {
            updatedAnswers <- Future.fromTry(
                                request.userAnswers.set(
                                  reverifyPage,
                                  updatedSelections
                                )
                              )
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(redirectTo(updatedAnswers))

        val previouslySelectedUnverifiedCount: Int =
          request.userAnswers
            .get(selectPage)
            .map(_.size)
            .getOrElse(0)

        gotoPage match {
          case Some(targetPage) =>
            saveSelectionsAndRedirect { _ =>
              flowId
                .map(fid => routes.SelectSubcontractorsToReverifyController.onPageLoadWithId(fid, mode, targetPage))
                .getOrElse(routes.SelectSubcontractorsToReverifyController.onPageLoad(mode, targetPage))
            }

          case None =>
            if (updatedSelections.size + previouslySelectedUnverifiedCount > 100) {
              val formWithErrors =
                boundForm.withError(
                  "value",
                  "verify.selectSubcontractorsToReverify.error.maxSelected"
                )

              Future.successful(renderForm(formWithErrors))
            } else {
              boundForm.fold(
                formWithErrors => Future.successful(renderForm(formWithErrors)),
                _ =>
                  for {
                    withSelections <- Future.fromTry(
                                        request.userAnswers.set(
                                          reverifyPage,
                                          updatedSelections
                                        )
                                      )

                    withContext <- Future.fromTry(
                                     withSelections.set(
                                       contextPage,
                                       FinalValidationContext.VerifySubcontractor
                                     )
                                   )

                    withSource <- Future.fromTry(
                                    withContext.set(
                                      sourcePage,
                                      VerifyFinalValidationSource.SelectSubcontractorsToReverify
                                    )
                                  )

                    _ <- sessionRepository.set(withSource)

                  } yield Redirect(
                    navigator.nextPage(
                      reverifyPage,
                      mode,
                      withSource
                    )
                  )
              )
            }
        }
    }
  }
}

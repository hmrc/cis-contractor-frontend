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

package controllers.amend

import controllers.actions.*
import models.add.ValidatedSubcontractor
import models.amend.OriginalIndividualAnswers
import models.{AmendMode, UserAnswers}
import pages.add.*
import pages.amend.{AmendCheckYourAnswersSubmittedPage, ShowVerificationDetailsPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.OriginalIndividualAnswersQuery
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.add.*
import viewmodels.govuk.summarylist.*
import views.html.amend.AmendCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendIndividualCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  subcontractorService: SubcontractorService,
  sessionRepository: SessionRepository,
  view: AmendCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val ua = request.userAnswers

    ValidatedSubcontractor.build(ua) match {
      case Right(_) =>
        val originalAnswers = ua.get(OriginalIndividualAnswersQuery)

        val isVerified = ua.get(ShowVerificationDetailsPage)

        val subcontractorInformationList =
          SummaryListViewModel(rows = subcontractorInformationRows(ua, originalAnswers).flatten)

        val detailsList =
          SummaryListViewModel(rows = detailsRows(ua, isVerified).flatten)

        Ok(
          view(
            subcontractorInformationList,
            detailsList,
            displayName(ua),
            routes.AmendIndividualCheckYourAnswersController.onSubmit(),
            routes.AmendIndividualCheckYourAnswersController.onCancel()
          )
        )

      case Left(error) =>
        logger.error(s"[AmendIndividualCheckYourAnswersController.onPageLoad] Failed to load the page: $error")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  private def subcontractorInformationRows(
    ua: UserAnswers,
    originalAnswers: Option[OriginalIndividualAnswers]
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val verificationRows =
      Option
        .when(ua.get(ShowVerificationDetailsPage).contains(true)) {

          val verificationNumber =
            originalAnswers.flatMap(_.verificationNumber).getOrElse("")

          Seq(
            SubcontractorsUniqueTaxpayerReferenceSummary.row(
              ua,
              AmendMode,
              showActions = false
            ),
            Some(
              SummaryListRowViewModel(
                key = Key(Text(messages("amendCheckYourAnswers.verificationNumber.label"))),
                value = Value(Text(verificationNumber))
              )
            )
          )
        }
        .getOrElse(Nil)

    Seq(
      TypeOfSubcontractorSummary.row(
        ua,
        showActions = false
      )
    ) ++ verificationRows
  }

  private def detailsRows(
    ua: UserAnswers,
    isVerified: Option[Boolean]
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val nameRows =
      Option
        .when(!isVerified.contains(true)) {
          Seq(
            SubTradingNameYesNoSummary.row(ua, AmendMode),
            SubcontractorNameSummary.row(ua, AmendMode),
            TradingNameOfSubcontractorSummary.row(ua, AmendMode)
          )
        }
        .getOrElse(Nil)

    val addressRows    =
      Seq(
        SubAddressYesNoSummary.row(ua, AmendMode),
        AddressOfSubcontractorSummary.row(ua, AmendMode)
      )
    val utrRows        =
      Option
        .when(!isVerified.contains(true)) {
          Seq(
            UniqueTaxpayerReferenceYesNoSummary.row(ua, AmendMode),
            SubcontractorsUniqueTaxpayerReferenceSummary.row(ua, AmendMode)
          )
        }
        .getOrElse(Nil)
    val contactRows    =
      Seq(
        AddIndividualContactMethodsYesNoSummary.row(ua, AmendMode),
        IndividualContactMethodOptionsSummary.row(ua, AmendMode),
        IndividualEmailAddressSummary.row(ua, AmendMode),
        IndividualPhoneNumberSummary.row(ua, AmendMode),
        IndividualMobileNumberSummary.row(ua, AmendMode)
      )
    val additionalRows =
      Seq(
        NationalInsuranceNumberYesNoSummary.row(ua, AmendMode),
        SubNationalInsuranceNumberSummary.row(ua, AmendMode),
        WorksReferenceNumberYesNoSummary.row(ua, AmendMode),
        WorksReferenceNumberSummary.row(ua, AmendMode)
      )
    nameRows ++
      addressRows ++
      contactRows ++
      utrRows ++
      additionalRows
  }

  private def displayName(ua: UserAnswers): String =
    ua.get(SubcontractorNamePage)
      .map(n => s"${n.firstName} ${n.lastName}")
      .orElse(ua.get(TradingNameOfSubcontractorPage))
      .getOrElse("")

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      if (request.userAnswers.get(AmendCheckYourAnswersSubmittedPage).contains(true)) {
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      } else {
        ValidatedSubcontractor.build(request.userAnswers) match {
          case Right(_)    =>
            subcontractorService
              .createAndUpdateSubcontractor(request.userAnswers)
              .flatMap { _ =>
                Future
                  .fromTry(request.userAnswers.set(AmendCheckYourAnswersSubmittedPage, true))
                  .flatMap(updated => sessionRepository.set(updated).map(_ => ()))
                  .map(_ =>
                    Redirect(
                      controllers.amend.routes.AmendIndividualCheckYourAnswersController.onPageLoad()
                    ) // TODO: Redirect to confirmation page
                  )
              }
              .recover { case t =>
                logger.error(
                  "[AmendIndividualCheckYourAnswersController.onSubmit] Failed to update subcontractor",
                  t
                )
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }
          case Left(error) =>
            logger.error(s"[AmendIndividualCheckYourAnswersController.onSubmit] Validation failed: $error")
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      }
    }

  def onCancel(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionRepository
      .set(UserAnswers(request.userAnswers.id))
      .map(_ => Redirect(controllers.routes.IndexController.onPageLoad()))
  }
}

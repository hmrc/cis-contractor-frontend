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

package controllers.amend.partnership

import controllers.actions.*
import models.add.partnership.ValidatedPartnership
import models.{AmendMode, UserAnswers}
import pages.add.*
import pages.add.partnership.PartnershipNamePage
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{SubContractorVerificationNumberQuery, SubContractorVerifiedQuery}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.add.*
import viewmodels.checkAnswers.add.trust.*
import viewmodels.govuk.summarylist.*
import views.html.amend.AmendCheckYourAnswersView
import controllers.routes
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendPartnershipCheckYourAnswersController @Inject() (
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

    ValidatedPartnership.build(ua) match {
      case Right(_) =>
        val isVerified = ua.get(SubContractorVerifiedQuery).contains(true)
        val trustName  = ua.get(PartnershipNamePage).getOrElse("")

        val subcontractorInformationList =
          SummaryListViewModel(rows = subcontractorInformationRows(ua, isVerified).flatten)

        val detailsList =
          SummaryListViewModel(rows = detailsRows(ua, isVerified).flatten)

        Ok(view(subcontractorInformationList, detailsList, trustName))

      case Left(error) =>
        logger.error(s"[AmendPartnershipCheckYourAnswersController.onPageLoad] Failed to load the page: $error")
        Redirect(routes.JourneyRecoveryController.onPageLoad())
    }
  }

  private def subcontractorInformationRows(
                                            ua: UserAnswers,
                                            isVerified: Boolean
                                          )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val verificationRows =
      Option
        .when(isVerified) {
          val verificationNumber =
            ua.get(SubContractorVerificationNumberQuery).getOrElse("")

          Seq(
            TrustUtrSummary.row(ua, AmendMode, showActions = false),
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
      TypeOfSubcontractorSummary.row(ua, showActions = false)
    ) ++ verificationRows
  }

  private def detailsRows(
                           ua: UserAnswers,
                           isVerified: Boolean
                         )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val nameRows =
      if (isVerified) {
        Nil
      } else {
        Seq(TrustNameSummary.row(ua, AmendMode))
      }

    val utrRows =
      if (isVerified) {
        Nil
      } else {
        Seq(
          TrustUtrYesNoSummary.row(ua, AmendMode),
          TrustUtrSummary.row(ua, AmendMode)
        )
      }

    nameRows ++
      Seq(
        TrustAddressYesNoSummary.row(ua, AmendMode),
        TrustAddressSummary.row(ua, AmendMode),
        AddTrustContactMethodsYesNoSummary.row(ua, AmendMode),
        TrustContactMethodOptionsSummary.row(ua, AmendMode),
        TrustEmailAddressSummary.row(ua, AmendMode),
        TrustPhoneNumberSummary.row(ua, AmendMode),
        TrustMobileNumberSummary.row(ua, AmendMode)
      ) ++
      utrRows ++
      Seq(
        TrustWorksReferenceYesNoSummary.row(ua, AmendMode),
        TrustWorksReferenceSummary.row(ua, AmendMode)
      )
  }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      ValidatedPartnership.build(request.userAnswers) match {
        case Right(_) =>
          subcontractorService
            .createAndUpdateSubcontractor(request.userAnswers)
            .map(_ => Redirect(controllers.amend.trust.routes.AmendPartnershipCheckYourAnswersController.onPageLoad()))
            .recover { case t =>
              logger.error(
                "[AmendTrustCheckYourAnswersController.onSubmit] Failed to update subcontractor",
                t
              )
              Redirect(routes.JourneyRecoveryController.onPageLoad())
            }

        case Left(error) =>
          logger.error(s"[AmendPartnershipCheckYourAnswersController.onSubmit] Validation failed: $error")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def onCancel(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionRepository
      .set(UserAnswers(request.userAnswers.id))
      .map(_ => Redirect(routes.IndexController.onPageLoad()))
  }
}

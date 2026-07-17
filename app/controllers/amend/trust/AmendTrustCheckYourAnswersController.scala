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
import models.add.trust.ValidatedTrust
import models.contact.ContactOptions.*
import models.{AmendMode, UserAnswers}
import pages.add.*
import pages.add.trust.{TrustNamePage, TrustUtrYesNoPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.SubContractorVerifiedQuery
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.add.*
import viewmodels.govuk.summarylist.*
import views.html.amend.AmendCheckYourAnswersView
import queries.SubContractorVerifiedQuery
import viewmodels.checkAnswers.add.trust.{TrustAddressSummary, TrustContactMethodOptionsSummary, TrustMobileNumberSummary, TrustNameSummary, TrustPhoneNumberSummary, TrustUtrYesNoSummary, TrustWorksReferenceYesNoSummary}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendTrustCheckYourAnswersController @Inject() (
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

    ValidatedTrust.build(ua) match {
      case Right(_) =>
        val isVerified = ua.get(SubContractorVerifiedQuery).contains(true)
        val trustName = ua.get(TrustNamePage).getOrElse("")

        val subcontractorInformationList =
          SummaryListViewModel(rows = subcontractorInformationRows(ua, isVerified).flatten)

        val detailsList =
          SummaryListViewModel(rows = detailsRows(ua, isVerified).flatten)

        Ok(view(subcontractorInformationList, detailsList, trustName))

      case Left(error) =>
        logger.error(s"[AmendIndividualCheckYourAnswersController.onPageLoad] Failed to load the page: $error")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  private def subcontractorInformationRows(
                                            ua: UserAnswers,
                                            isVerified: Boolean
                                          )(implicit messages: Messages): Seq[Option[SummaryListRow]] =
    Seq(TypeOfSubcontractorSummary.row(ua, showActions = false)) ++
      (if (isVerified) {
        Seq(
          TrustUtrSummary.row(
            ua,
            AmendMode,
            showActions = false
          )
        )
      } else {
        Seq.empty
      })

  private def detailsRows(
                           ua: UserAnswers,
                           isVerified: Boolean
                         )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    val nameRows =
      if (isVerified) { Seq.empty }
      else {
        Seq(
          TrustNameSummary.row(ua, AmendMode)
        )
      }

    val utrRows =
      if (isVerified) { Seq.empty }
      else {
        Seq(
          TrustUtrYesNoSummary.row(ua, AmendMode),
          TrustUtrSummary.row(
            ua,
            AmendMode
          )
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
      ValidatedSubcontractor.build(request.userAnswers) match {
        case Right(_) =>
          subcontractorService
            .createAndUpdateSubcontractor(request.userAnswers)
            .map(_ => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
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

  def onCancel(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionRepository
      .set(UserAnswers(request.userAnswers.id))
      .map(_ => Redirect(controllers.routes.IndexController.onPageLoad()))
  }
}

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

package controllers.amend.company

import controllers.actions.*
import models.{AmendMode, UserAnswers}
import pages.add.*
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
import viewmodels.checkAnswers.add.company.*
import viewmodels.govuk.summarylist.*
import views.html.amend.AmendCheckYourAnswersView
import controllers.routes
import models.add.company.ValidatedCompany
import pages.add.company.CompanyNamePage
import viewmodels.checkAnswers.add.company.*
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendCompanyCheckYourAnswersController @Inject() (
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

    ValidatedCompany.build(ua) match {
      case Right(_) =>
        val isVerified = ua.get(SubContractorVerifiedQuery).contains(true)
        val companyName  = ua.get(CompanyNamePage).getOrElse("")

        val subcontractorInformationList =
          SummaryListViewModel(rows = subcontractorInformationRows(ua, isVerified).flatten)

        val detailsList =
          SummaryListViewModel(rows = detailsRows(ua, isVerified).flatten)

        val submitUrl = controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onSubmit()
        val cancelUrl = controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onCancel()

        Ok(view(subcontractorInformationList, detailsList, companyName, submitUrl, cancelUrl))

      case Left(error) =>
        logger.error(s"[AmendCompanyCheckYourAnswersController.onPageLoad] Failed to load the page: $error")
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
            CompanyUtrSummary.row(ua, AmendMode, showActions = false),
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
        Seq(CompanyNameSummary.row(ua, AmendMode))
      }

    val utrRows =
      if (isVerified) {
        Nil
      } else {
        Seq(
          CompanyUtrYesNoSummary.row(ua, AmendMode),
          CompanyUtrSummary.row(ua, AmendMode)
        )
      }

    nameRows ++
      Seq(
        CompanyAddressYesNoSummary.row(ua, AmendMode),
        CompanyAddressSummary.row(ua, AmendMode),
        AddCompanyContactMethodsYesNoSummary.row(ua, AmendMode),
        CompanyContactMethodOptionsSummary.row(ua, AmendMode),
        CompanyEmailAddressSummary.row(ua, AmendMode),
        CompanyPhoneNumberSummary.row(ua, AmendMode),
        CompanyMobileNumberSummary.row(ua, AmendMode)
      ) ++
      utrRows ++
      Seq(
        CompanyCrnYesNoSummary.row(ua, AmendMode),
        CompanyCrnSummary.row(ua, AmendMode),
        CompanyWorksReferenceYesNoSummary.row(ua, AmendMode),
        CompanyWorksReferenceSummary.row(ua, AmendMode)
      )
  }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      ValidatedCompany.build(request.userAnswers) match {
        case Right(_) =>
          subcontractorService
            .createAndUpdateSubcontractor(request.userAnswers)
            .map(_ => Redirect(controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onPageLoad()))
            .recover { case t =>
              logger.error(
                "[AmendCompanyCheckYourAnswersController.onSubmit] Failed to update subcontractor",
                t
              )
              Redirect(routes.JourneyRecoveryController.onPageLoad())
            }

        case Left(error) =>
          logger.error(s"[AmendCompanyCheckYourAnswersController.onSubmit] Validation failed: $error")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def onCancel(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionRepository
      .set(UserAnswers(request.userAnswers.id))
      .map(_ => Redirect(routes.IndexController.onPageLoad()))
  }
}

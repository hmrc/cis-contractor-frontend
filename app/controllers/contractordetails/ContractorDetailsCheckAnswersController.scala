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

package controllers.contractordetails

import config.FrontendAppConfig
import controllers.actions.*
import models.Scheme
import models.requests.{DataRequest, UpdateContractorSchemeParams}
import pages.contractordetails.{AddEmailAddressYesNoPage, AddSchemeNameYesNoPage, ContractorSchemePage, ContractorUtrPage, EnterContractorEmailAddressPage, SchemeNamePage}
import pages.CisIdPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.ContractorDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.contractordetails.*
import views.html.contractordetails.ContractorDetailsCheckAnswersView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContractorDetailsCheckAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  service: ContractorDetailsService,
  val controllerComponents: MessagesControllerComponents,
  view: ContractorDetailsCheckAnswersView
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(target: Option[String] = None): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>

      val cisAccountUrl =
        if (!request.isAgent) {
          appConfig.constructionIndustryOrgAccountUrl
        } else {
          request.userAnswers
            .get(CisIdPage)
            .fold(appConfig.constructionIndustryAgentAccountUrl)(cisId =>
              s"${appConfig.constructionIndustryAgentAccountUrl}$cisId"
            )
        }

      request.userAnswers.get(ContractorSchemePage) match {
        case Some(scheme) =>
          if (hasRequiredDetails(request)) {
            val summaryRows = Seq(
              ContractorUtrSummary.row(request.userAnswers),
              AddSchemeNameYesNoSummary.row(request.userAnswers),
              SchemeNameSummary.row(request.userAnswers),
              AddEmailAddressYesNoSummary.row(request.userAnswers),
              EnterContractorEmailAddressSummary.row(request.userAnswers)
            ).flatten

            Ok(
              view(
                scheme.accountsOfficeReference,
                summaryRows,
                cisAccountUrl,
                validTarget(target),
                routes.ContractorDetailsCheckAnswersController.onSubmit(target)
              )
            )
          } else {
            logger.error("[ContractorDetailsCheckAnswersController][onPageLoad] - required details invalid")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }

        case None =>
          logger.error("[ContractorDetailsCheckAnswersController][onPageLoad] - scheme missing from userAnswers")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmit(target: Option[String] = None): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.get(ContractorSchemePage) match {
        case Some(scheme) if hasRequiredDetails(request) =>
          validTarget(target)
            .map(target => Future.successful(Redirect(targetUrl(target, scheme.instanceId))))
            .getOrElse(updateContractorDetails(scheme))

        case _ =>
          logger.error("[ContractorDetailsCheckAnswersController][onSubmit] - scheme or required details invalid")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def onPageLoadWithId(flowId: UUID, target: Option[String] = None): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)) { implicit request =>

      val id = flowId.toString
      val ua = request.userAnswers

      val cisAccountUrl =
        if (!request.isAgent) {
          appConfig.constructionIndustryOrgAccountUrl
        } else {
          ua.get(CisIdPage)
            .fold(appConfig.constructionIndustryAgentAccountUrl)(cisId =>
              s"${appConfig.constructionIndustryAgentAccountUrl}$cisId"
            )
        }

      ua.get(ContractorSchemePage(id)) match {
        case Some(scheme) =>
          if (hasRequiredDetails(request, id)) {

            val summaryRows = Seq(
              ContractorUtrSummary.row(ua, subJourneyId = Some(id)),
              AddSchemeNameYesNoSummary.row(ua, subJourneyId = Some(id)),
              SchemeNameSummary.row(ua, subJourneyId = Some(id)),
              AddEmailAddressYesNoSummary.row(ua, id = Some(id)),
              EnterContractorEmailAddressSummary.row(ua, subJourneyId = Some(id))
            ).flatten

            Ok(
              view(
                scheme.accountsOfficeReference,
                summaryRows,
                cisAccountUrl,
                validTarget(target),
                routes.ContractorDetailsCheckAnswersController.onSubmitWithId(flowId, target)
              )
            )
          } else {
            logger.error(
              "[ContractorDetailsCheckAnswersController][onPageLoadWithId] - required details invalid"
            )
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }

        case None =>
          logger.error(
            "[ContractorDetailsCheckAnswersController][onPageLoadWithId] - scheme missing from userAnswers"
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmitWithId(flowId: UUID, target: Option[String] = None): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val id = flowId.toString

      request.userAnswers.get(ContractorSchemePage(id)) match {
        case Some(scheme) if hasRequiredDetails(request, id) =>
          validTarget(target)
            .map(target => Future.successful(Redirect(targetUrl(target, scheme.instanceId))))
            .getOrElse(updateContractorDetailsWithId(scheme, id, flowId))

        case _ =>
          logger.error(
            "[ContractorDetailsCheckAnswersController][onSubmitWithId] - scheme or required details invalid"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  private def updateContractorDetails(
    scheme: Scheme
  )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val updateRequest = UpdateContractorSchemeParams(
      schemeId = scheme.schemeId,
      instanceId = scheme.instanceId,
      accountsOfficeReference = scheme.accountsOfficeReference,
      taxOfficeNumber = scheme.taxOfficeNumber,
      taxOfficeReference = scheme.taxOfficeReference,
      utr = request.userAnswers.get(ContractorUtrPage),
      name = request.userAnswers.get(SchemeNamePage),
      emailAddress = request.userAnswers.get(EnterContractorEmailAddressPage),
      version = scheme.version,
      displayWelcomePage = scheme.displayWelcomePage,
      prePopCount = scheme.prePopCount,
      prePopSuccessful = scheme.prePopSuccessful
    )

    service
      .updateContractorDetails(updateRequest)
      .map(_ => Redirect(routes.ContractorDetailsUpdatedController.onPageLoad()))
      .recover { case t =>
        logger.error(
          "[ContractorDetailsCheckAnswersController.onSubmit] Failed to update contractor details",
          t
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  private def updateContractorDetailsWithId(
    scheme: Scheme,
    id: String,
    flowId: UUID
  )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val updateRequest = UpdateContractorSchemeParams(
      schemeId = scheme.schemeId,
      instanceId = scheme.instanceId,
      accountsOfficeReference = scheme.accountsOfficeReference,
      taxOfficeNumber = scheme.taxOfficeNumber,
      taxOfficeReference = scheme.taxOfficeReference,
      utr = request.userAnswers.get(ContractorUtrPage(id)),
      name = request.userAnswers.get(SchemeNamePage(id)),
      emailAddress = request.userAnswers.get(EnterContractorEmailAddressPage(id)),
      version = scheme.version,
      displayWelcomePage = scheme.displayWelcomePage,
      prePopCount = scheme.prePopCount,
      prePopSuccessful = scheme.prePopSuccessful
    )

    service
      .updateContractorDetails(updateRequest)
      .map(_ => Redirect(routes.ContractorDetailsUpdatedController.onPageLoadWithId(flowId)))
      .recover { case t =>
        logger.error(
          "[ContractorDetailsCheckAnswersController.onSubmitWithId] Failed to update contractor details",
          t
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  private def hasRequiredDetails(request: DataRequest[AnyContent]): Boolean = {
    val schemeNameValid = request.userAnswers.get(AddSchemeNameYesNoPage) match {
      case Some(true)  => request.userAnswers.get(SchemeNamePage).isDefined
      case Some(false) => true
      case None        => false
    }

    val emailAddressValid = request.userAnswers.get(AddEmailAddressYesNoPage) match {
      case Some(true)  => request.userAnswers.get(EnterContractorEmailAddressPage).isDefined
      case Some(false) => request.userAnswers.get(EnterContractorEmailAddressPage).isEmpty
      case None        => false
    }

    schemeNameValid && emailAddressValid
  }

  private def hasRequiredDetails(request: DataRequest[AnyContent], id: String): Boolean = {
    val schemeNameValid = request.userAnswers.get(AddSchemeNameYesNoPage(id)) match {
      case Some(true)  => request.userAnswers.get(SchemeNamePage(id)).isDefined
      case Some(false) => true
      case None        => false
    }

    val emailAddressValid = request.userAnswers.get(AddEmailAddressYesNoPage(id)) match {
      case Some(true)  => request.userAnswers.get(EnterContractorEmailAddressPage(id)).isDefined
      case Some(false) => request.userAnswers.get(EnterContractorEmailAddressPage(id)).isEmpty
      case None        => false
    }

    schemeNameValid && emailAddressValid
  }

  private val ManageYourCisReturnTarget = "manageYourCisReturn"
  private val SubcontractorsTarget      = "subcontractors"

  private def validTarget(target: Option[String]): Option[String] =
    target.filter {
      case ManageYourCisReturnTarget | SubcontractorsTarget => true
      case _                                                => false
    }

  private def targetUrl(target: String, instanceId: String): String =
    target match {
      case ManageYourCisReturnTarget =>
        appConfig.manageCisReturnUrl(instanceId)

      case SubcontractorsTarget =>
        appConfig.manageSubcontractorsLandingUrl(instanceId)
    }
}

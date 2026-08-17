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
import connectors.ConstructionIndustrySchemeConnector
import controllers.Execution.trampoline
import controllers.actions.*
import models.requests.UpdateContractorSchemeParams
import pages.contractordetails.{ContractorSchemePage, ContractorUtrPage, EnterContractorEmailAddressPage, SchemeNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.contractordetails.*
import views.html.contractordetails.ContractorDetailsCheckAnswersView

import javax.inject.Inject
import scala.concurrent.Future

class ContractorDetailsCheckAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  connector: ConstructionIndustrySchemeConnector,
  val controllerComponents: MessagesControllerComponents,
  view: ContractorDetailsCheckAnswersView
)(implicit appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      request.userAnswers.get(ContractorSchemePage) match {

        case Some(scheme) =>
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
              summaryRows
            )
          )

        case None =>
          Redirect(
            controllers.routes.JourneyRecoveryController.onPageLoad()
          )
      }
    }

  def onSubmit: Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.get(ContractorSchemePage) match {

        case Some(scheme) =>
          val updateRequest =
            UpdateContractorSchemeParams(
              schemeId = scheme.schemeId,
              instanceId = scheme.instanceId,
              accountsOfficeReference = scheme.accountsOfficeReference,
              taxOfficeNumber = scheme.taxOfficeNumber,
              taxOfficeReference = scheme.taxOfficeReference,
              utr = request.userAnswers.get(ContractorUtrPage),
              name = request.userAnswers.get(SchemeNamePage),
              emailAddress = request.userAnswers.get(EnterContractorEmailAddressPage),
              version = scheme.version
            )

          connector
            .submitContractorDetails(updateRequest)
            .map { _ =>
              Redirect(
                routes.ContractorDetailsUpdatedController.onPageLoad()
              )
            }

        case None =>
          Future.successful(
            Redirect(
              controllers.routes.JourneyRecoveryController.onPageLoad()
            )
          )
      }
    }
}

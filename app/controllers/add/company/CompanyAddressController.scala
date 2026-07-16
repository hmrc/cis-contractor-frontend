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

package controllers.add.company

import controllers.actions.*
import controllers.add.AddressLookupJourneyController
import models.{Mode, UserAnswers}
import models.address.Address
import models.address.AddressLookupJourneyIdentifier.companyQuestionsAddress
import pages.add.company.{CompanyAddressPage, CompanyNamePage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import queries.{AddressLookupAmendReturnQuery, Settable}
import repositories.SessionRepository
import services.AddressLookupService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyAddressController @Inject() (
  override val messagesApi: MessagesApi,
  override protected val sessionRepository: SessionRepository,
  override protected val identify: IdentifierAction,
  override protected val getData: DataRetrievalAction,
  override protected val requireData: DataRequiredAction,
  override protected val addressLookupService: AddressLookupService,
  val controllerComponents: MessagesControllerComponents
)(implicit override protected val executionContext: ExecutionContext)
    extends AddressLookupJourneyController {

  override protected def journeyId = companyQuestionsAddress

  override protected def addressPage: Settable[Address] = CompanyAddressPage

  override protected def subcontractorName(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(CompanyNamePage)

  override protected def standardCallback: Call =
    routes.CompanyAddressController.addressLookupCallback()

  override protected def changeCallback: Call =
    routes.CompanyAddressController.addressLookupCallbackChange()

  override protected def onCompletion(mode: Mode): Call =
    routes.AddCompanyContactMethodsYesNoController.onPageLoad(mode)

  override protected def onChangeCompletion(isAmend: Boolean): Call =
    if (isAmend) {
      controllers.routes.JourneyRecoveryController.onPageLoad()
    } // TODO: redirect to amend cya page
    else {
      routes.CompanyCheckYourAnswersController.onPageLoad()
    }

  def redirectToAmendAddressLookup(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      (for {
        ua <- Future.fromTry(request.userAnswers.set(AddressLookupAmendReturnQuery, true))
        _  <- sessionRepository.set(ua)
      } yield Redirect(routes.CompanyAddressController.redirectToAddressLookup(Some("change"))))
        .recover { case _ => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()) }
    }

}

/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.add

import controllers.actions.*
import models.{Mode, UserAnswers}
import models.address.Address
import models.address.AddressLookupJourneyIdentifier.individualQuestionsAddress
import pages.add.AddressOfSubcontractorPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Call, MessagesControllerComponents}
import queries.Settable
import repositories.SessionRepository
import services.AddressLookupService
import utils.SubcontractorNameExtractor

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AddressOfSubcontractorController @Inject() (
  override val messagesApi: MessagesApi,
  override protected val sessionRepository: SessionRepository,
  override protected val identify: IdentifierAction,
  override protected val getData: DataRetrievalAction,
  override protected val requireData: DataRequiredAction,
  override protected val addressLookupService: AddressLookupService,
  subcontractorNameExtractor: SubcontractorNameExtractor,
  val controllerComponents: MessagesControllerComponents
)(implicit override protected val executionContext: ExecutionContext)
    extends AddressLookupJourneyController {

  override protected def journeyId = individualQuestionsAddress

  override protected def addressPage: Settable[Address] = AddressOfSubcontractorPage

  override protected def subcontractorName(userAnswers: UserAnswers): Option[String] =
    subcontractorNameExtractor.getSubcontractorName(userAnswers)

  override protected def standardCallback: Call =
    routes.AddressOfSubcontractorController.addressLookupCallback()

  override protected def changeCallback: Call =
    routes.AddressOfSubcontractorController.addressLookupCallbackChange()

  override protected def onCompletion(mode: Mode): Call =
    routes.IndividualChooseContactDetailsController.onPageLoad(mode)

  override protected def onChangeCompletion(isAmend: Boolean): Call =
    routes.CheckYourAnswersController.onPageLoad()

}

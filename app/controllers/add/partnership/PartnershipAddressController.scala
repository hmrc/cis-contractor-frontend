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

package controllers.add.partnership

import controllers.actions.*
import models.Mode
import models.address.AddressLookupJourneyIdentifier.partnershipQuestionsAddress
import models.address.MandatoryFieldsConfigModel
import pages.add.partnership.{PartnershipAddressPage, PartnershipNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AddressLookupService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  addressLookupService: AddressLookupService,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def redirectToAddressLookup(mode: Mode, changeRoute: Option[String] = None): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val journeyId     = partnershipQuestionsAddress
      val addressConfig = MandatoryFieldsConfigModel(
        addressLine1 = Some(true),
        town = Some(true),
        postcode = Some(true)
      )

      sessionRepository.get(request.userAnswers.id).flatMap {
        case Some(_) =>
          val callback = if (changeRoute.isDefined) {
            controllers.add.partnership.routes.PartnershipAddressController.addressLookupCallbackChange()
          } else {
            controllers.add.partnership.routes.PartnershipAddressController.addressLookupCallback()
          }
          request.userAnswers.get(PartnershipNamePage) match {
            case Some(partnershipName) =>
              addressLookupService
                .getJourneyUrl(
                  journeyId,
                  callback,
                  optName = Some(partnershipName),
                  mandatoryFieldsConfigModel = addressConfig
                )
                .map(Redirect)
            case None                  => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
        case None    =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def addressLookupCallback(id: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      for {
        address <- addressLookupService.getAddressById(id)
        updated <- addressLookupService.saveAddressDetails(address, PartnershipAddressPage)
      } yield
        if (updated) {
          Redirect(controllers.add.partnership.routes.PartnershipChooseContactDetailsController.onPageLoad(mode))
        } else {
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def addressLookupCallbackChange(id: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      for {
        address <- addressLookupService.getAddressById(id)
        updated <- addressLookupService.saveAddressDetails(address, PartnershipAddressPage)
      } yield
        if (updated) {
          Redirect(controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad())
        } else {
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

}

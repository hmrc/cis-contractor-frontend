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

package controllers.add.trust

import controllers.actions.*
import models.Mode
import models.address.AddressLookupJourneyIdentifier.trustQuestionsAddress
import models.address.MandatoryFieldsConfigModel
import pages.add.trust.{TrustAddressPage, TrustNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AddressLookupService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrustAddressController @Inject() (
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
      val journeyId     = trustQuestionsAddress
      val addressConfig = MandatoryFieldsConfigModel(
        addressLine1 = Some(true),
        town = Some(true),
        postcode = Some(true)
      )

      sessionRepository.get(request.userAnswers.id).flatMap {
        case Some(_) =>
          val callback = if (changeRoute.isDefined) {
            controllers.add.trust.routes.TrustAddressController.addressLookupCallbackChange()
          } else {
            controllers.add.trust.routes.TrustAddressController.addressLookupCallback()
          }
          request.userAnswers.get(TrustNamePage) match {
            case Some(trustName) =>
              addressLookupService
                .getJourneyUrl(
                  journeyId,
                  callback,
                  optName = Some(trustName),
                  mandatoryFieldsConfigModel = addressConfig
                )
                .map(Redirect)
            case None            => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
        case None    =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def addressLookupCallback(id: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      for {
        address <- addressLookupService.getAddressById(id)
        updated <- addressLookupService.saveAddressDetails(address, TrustAddressPage)
      } yield
        if (updated) {
          Redirect(controllers.add.trust.routes.TrustContactOptionsController.onPageLoad(mode))
        } else {
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def addressLookupCallbackChange(id: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      for {
        address <- addressLookupService.getAddressById(id)
        updated <- addressLookupService.saveAddressDetails(address, TrustAddressPage)
      } yield
        if (updated) {
          Redirect(controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad())
        } else {
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

}

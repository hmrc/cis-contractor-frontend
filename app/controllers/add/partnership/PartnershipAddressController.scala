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
import controllers.add.AddressLookupJourneyController
import forms.mappings.Constants.MaxLength35
import models.{AmendMode, FinalValidationMode, Mode, NormalMode, UserAnswers}
import controllers.helpers.SubcontractorNameDisplayHelper
import models.address.{Address, MandatoryFieldsConfigModel}
import models.address.AddressLookupJourneyIdentifier.partnershipQuestionsAddress
import models.requests.CisPath
import pages.add.partnership.{PartnershipAddressPage, PartnershipNamePage}
import play.api.Logging
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import queries.{AddressLookupAmendReturnQuery, Settable}
import repositories.SessionRepository
import services.AddressLookupService

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipAddressController @Inject() (
  override val messagesApi: MessagesApi,
  override protected val sessionRepository: SessionRepository,
  override protected val identify: IdentifierAction,
  override protected val getData: DataRetrievalAction,
  override protected val requireData: DataRequiredAction,
  resolveScheme: SchemeAction,
  getJourney: JourneyAction,
  override protected val addressLookupService: AddressLookupService,
  val controllerComponents: MessagesControllerComponents
)(implicit override protected val executionContext: ExecutionContext)
    extends AddressLookupJourneyController
    with Logging {

  override protected def journeyId = partnershipQuestionsAddress

  override protected def addressPage: Settable[Address] = PartnershipAddressPage

  override protected def subcontractorName(userAnswers: UserAnswers, mode: Mode)(implicit
    messages: Messages
  ): Option[String] =
    SubcontractorNameDisplayHelper.getPartnershipDisplayName(userAnswers, mode)

  override protected def standardCallback(mode: Mode): Call =
    routes.PartnershipAddressController.addressLookupCallback(id = "", mode = mode)

  override protected def changeCallback(mode: Mode): Call =
    routes.PartnershipAddressController.addressLookupCallbackChange(id = "", mode = mode)

  override protected def onCompletion(mode: Mode): Call =
    mode match {
      case FinalValidationMode =>
        controllers.finalvalidations.routes.FinalValidationCompleteController.onPageLoad()
      case _                   =>
        routes.AddPartnershipContactMethodsYesNoController.onPageLoad(mode)
    }

  override protected def onChangeCompletion(isAmend: Boolean): Call =
    if (isAmend) {
      controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onPageLoad()
    } else {
      routes.PartnershipCheckYourAnswersController.onPageLoad()
    }

  def redirectToAmendAddressLookup(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      (for {
        ua <- Future.fromTry(request.userAnswers.set(AddressLookupAmendReturnQuery, true))
        _  <- sessionRepository.set(ua)
      } yield Redirect(routes.PartnershipAddressController.redirectToAddressLookup(AmendMode, Some("change"))))
        .recover { case ex =>
          logger.error(
            "[PartnershipAddressController][redirectToAmendAddressLookup] - failed to set session data for amend",
            ex
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def redirectToAddressLookupWithId(
    cisPath: CisPath,
    uuid: UUID,
    changeRoute: Option[String] = None
  ): Action[AnyContent] =
    (identify andThen resolveScheme(cisPath) andThen getJourney(uuid)).async { implicit request =>
      val callback =
        if (changeRoute.isDefined)
          routes.PartnershipAddressController.addressLookupCallbackChangeWithId(cisPath, uuid, id = "")
        else
          routes.PartnershipAddressController.addressLookupCallbackWithId(cisPath, uuid, id = "")

      request.userAnswers.get(PartnershipNamePage(request.subJourneyId)).map(_.trim).filter(_.nonEmpty) match {
        case Some(name) =>
          addressLookupService
            .getJourneyUrl(
              journeyId,
              callback,
              optName = Some(name),
              mandatoryFieldsConfigModel = MandatoryFieldsConfigModel(
                addressLine1 = Some(true),
                town = Some(true),
                postcode = Some(true)
              ),
              line1MaxLength = Some(MaxLength35),
              line2MaxLength = Some(MaxLength35),
              line3MaxLength = Some(MaxLength35),
              townMaxLength = Some(MaxLength35)
            )
            .map(Redirect)
            .recover { case ex =>
              logger.error(
                "[PartnershipAddressController][redirectToAddressLookupWithId] - failed to get ALF journey URL",
                ex
              )
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
        case None       =>
          logger.error(
            "[PartnershipAddressController][redirectToAddressLookupWithId] - PartnershipNamePage missing from userAnswers"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def addressLookupCallbackWithId(cisPath: CisPath, uuid: UUID, id: String): Action[AnyContent] =
    (identify andThen resolveScheme(cisPath) andThen getJourney(uuid)).async { implicit request =>
      saveAddressAndRedirectWithId(
        id,
        routes.AddPartnershipContactMethodsYesNoController.onPageLoadWithId(cisPath, uuid, NormalMode)
      )
    }

  def addressLookupCallbackChangeWithId(cisPath: CisPath, uuid: UUID, id: String): Action[AnyContent] =
    (identify andThen resolveScheme(cisPath) andThen getJourney(uuid)).async { implicit request =>
      saveAddressAndRedirectWithId(
        id,
        routes.PartnershipCheckYourAnswersController.onPageLoadWithId(cisPath, uuid)
      )
    }

  private def saveAddressAndRedirectWithId(id: String, onSuccess: Call)(implicit
    request: models.requests.JourneyRequest[AnyContent]
  ) =
    (for {
      address <- addressLookupService.getAddressById(id)
      updated <-
        addressLookupService.saveAddressDetails(
          address,
          PartnershipAddressPage(request.subJourneyId),
          request.userAnswers
        )
    } yield
      if (updated) Redirect(onSuccess)
      else {
        logger.error(
          "[PartnershipAddressController][saveAddressAndRedirectWithId] - address not saved, update returned false"
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      })
      .recover { case ex =>
        logger.error(
          "[PartnershipAddressController][saveAddressAndRedirectWithId] - failed to retrieve or save address",
          ex
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }

}

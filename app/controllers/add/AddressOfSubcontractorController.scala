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
import forms.mappings.Constants.MaxLength35
import models.address.{Address, MandatoryFieldsConfigModel}
import models.address.AddressLookupJourneyIdentifier.individualQuestionsAddress
import models.{AmendMode, FinalValidationMode, Mode, NormalMode, UserAnswers}
import pages.add.{AddressOfSubcontractorPage, SubcontractorNamePage, TradingNameOfSubcontractorPage}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import queries.{AddressLookupAmendReturnQuery, Settable}
import repositories.SessionRepository
import services.AddressLookupService
import utils.SubcontractorNameExtractor

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddressOfSubcontractorController @Inject() (
  override val messagesApi: MessagesApi,
  override protected val sessionRepository: SessionRepository,
  override protected val identify: IdentifierAction,
  override protected val getData: DataRetrievalAction,
  override protected val requireData: DataRequiredAction,
  multiTabAction: MultiTabAction,
  override protected val addressLookupService: AddressLookupService,
  subcontractorNameExtractor: SubcontractorNameExtractor,
  val controllerComponents: MessagesControllerComponents
)(implicit override protected val executionContext: ExecutionContext)
    extends AddressLookupJourneyController {

  override protected def journeyId = individualQuestionsAddress

  override protected def addressPage: Settable[Address] = AddressOfSubcontractorPage

  override protected def subcontractorName(userAnswers: UserAnswers, mode: Mode)(implicit
    messages: Messages
  ): Option[String] =
    subcontractorNameExtractor.getSubcontractorName(userAnswers, mode)

  override protected def standardCallback(mode: Mode): Call =
    routes.AddressOfSubcontractorController.addressLookupCallback(id = "", mode = mode)

  override protected def changeCallback(mode: Mode): Call =
    routes.AddressOfSubcontractorController.addressLookupCallbackChange(id = "", mode = mode)

  override protected def onCompletion(mode: Mode): Call =
    mode match {
      case FinalValidationMode =>
        controllers.finalvalidations.routes.FinalValidationCompleteController.onPageLoad()
      case _                   =>
        routes.AddIndividualContactMethodsYesNoController.onPageLoad(mode)
    }

  override protected def onChangeCompletion(isAmend: Boolean): Call =
    if (isAmend) controllers.amend.routes.AmendIndividualCheckYourAnswersController.onPageLoad()
    else routes.CheckYourAnswersController.onPageLoad()

  def redirectToAmendAddressLookup(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      (for {
        ua <- Future.fromTry(request.userAnswers.set(AddressLookupAmendReturnQuery, true))
        _  <- sessionRepository.set(ua)
      } yield Redirect(routes.AddressOfSubcontractorController.redirectToAddressLookup(AmendMode, Some("change"))))
        .recover { case ex =>
          logger.error(
            "[AddressOfSubcontractorController][redirectToAmendAddressLookup] - failed to set amend flag",
            ex
          )
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def redirectToAddressLookupWithId(
    flowId: UUID,
    mode: Mode,
    changeRoute: Option[String] = None
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val id       = flowId.toString
      val callback =
        if (changeRoute.isDefined)
          routes.AddressOfSubcontractorController
            .addressLookupCallbackChangeWithId(flowId, id = "")
        else
          routes.AddressOfSubcontractorController.addressLookupCallbackWithId(flowId, id = "")

      request.userAnswers.get(TradingNameOfSubcontractorPage(id)).map(_.trim).filter(_.nonEmpty) match {
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
                "[TrustAddressController][redirectToAddressLookupWithId] - failed to get ALF journey URL",
                ex
              )
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
        case None       =>
          logger.error(
            "[TrustAddressController][redirectToAddressLookupWithId] - TrustNamePage missing from userAnswers"
          )
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def addressLookupCallbackWithId(flowId: UUID, id: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      saveAddressAndRedirectWithId(
        flowId.toString,
        id,
        routes.AddIndividualContactMethodsYesNoController
          .onPageLoadWithId(flowId, NormalMode)
      )
    }

  def addressLookupCallbackChangeWithId(flowId: UUID, id: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      saveAddressAndRedirectWithId(
        flowId.toString,
        id,
        routes.CheckYourAnswersController.onPageLoad() // TODO: NEED TO CREATE onPageLoadWithId
      )
    }

  private def saveAddressAndRedirectWithId(subJourneyId: String, addressId: String, onSuccess: Call)(implicit
    request: models.requests.DataRequest[AnyContent]
  ) =
    (for {
      address <- addressLookupService.getAddressById(addressId)
      updated <-
        addressLookupService.saveAddressDetails(
          address,
          AddressOfSubcontractorPage(subJourneyId),
          request.userAnswers
        )
    } yield
      if (updated) Redirect(onSuccess)
      else {
        logger.error(
          "[AddressOfSubcontractorController][saveAddressAndRedirectWithId] - address not saved, update returned false"
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      })
      .recover { case ex =>
        logger.error(
          "[TrustAddressController][saveAddressAndRedirectWithId] - failed to retrieve or save address",
          ex
        )
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
}

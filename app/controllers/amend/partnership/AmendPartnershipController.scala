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

package controllers.amend.partnership

import controllers.actions.*
import models.TypeOfSubcontractor.Partnership
import models.UserAnswers
import models.address.{Address, Country}
import models.amend.partnership.OriginalPartnershipAnswers
import models.contact.ContactMethodOptions
import models.response.SubcontractorResponse
import pages.add.*
import pages.add.partnership.*
import play.api.Logging
import play.api.libs.json.Writes
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.{CisIdQuery, OriginalPartnershipAnswersQuery}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AmendPartnershipController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  subcontractorService: SubcontractorService,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with Logging {

  private def recovery: Result =
    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

  def onPageLoad(cisId: String, subbieResourceRef: Long): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      subcontractorService
        .getSubcontractor(cisId, subbieResourceRef)
        .flatMap { response =>
          response.subcontractor match {
            case None =>
              logger.error(
                s"[AmendPartnershipController] No subcontractor returned for " +
                  s"cisId=$cisId, subbieResourceRef=$subbieResourceRef"
              )

              Future.successful(recovery)

            case Some(subcontractor) =>
              populateUserAnswers(
                request.userAnswers,
                cisId,
                subcontractor
              ).fold(
                error => {
                  logger.error(
                    s"[AmendPartnershipController] Failed to populate UserAnswers for " +
                      s"cisId=$cisId, subbieResourceRef=$subbieResourceRef",
                    error
                  )

                  Future.successful(recovery)
                },
                updatedAnswers =>
                  sessionRepository
                    .set(updatedAnswers)
                    .map { _ =>
                      Redirect(
                        controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController
                          .onPageLoad()
                      )
                    }
              )
          }
        }
        .recover { case error =>
          logger.error(
            s"[AmendPartnershipController] Failed to retrieve subcontractor. " +
              s"cisId=$cisId, subbieResourceRef=$subbieResourceRef",
            error
          )
          recovery
        }
    }

  protected def populateUserAnswers(
    userAnswers: UserAnswers,
    cisId: String,
    subcontractor: SubcontractorResponse
  ): Try[UserAnswers] = {
    val address              = toAddress(subcontractor)
    val methods              = contactMethods(subcontractor)
    val nominatedPartnerName =
      Seq(subcontractor.firstName, subcontractor.secondName, subcontractor.surname).flatten.mkString(" ").trim

    val originalAnswers =
      OriginalPartnershipAnswers(
        partnershipName = subcontractor.partnershipTradingName.orElse(subcontractor.tradingName),
        addressYesNo = Some(address.isDefined),
        address = address,
        partnershipContactMethodsYesNo = Some(methods.nonEmpty),
        partnershipContactMethodOptions = Option.when(methods.nonEmpty)(methods),
        email = subcontractor.emailAddress,
        phone = subcontractor.phoneNumber,
        mobile = subcontractor.mobilePhoneNumber,
        hasUtrYesNo = Some(subcontractor.utr.isDefined),
        utr = subcontractor.utr,
        nominatedPartnerName = Option.when(nominatedPartnerName.nonEmpty)(nominatedPartnerName),
        nominatedPartnerUtrYesNo = Some(subcontractor.partnerUtr.isDefined),
        nominatedPartnerUtr = subcontractor.partnerUtr,
        nominatedPartnerNinoYesNo = Some(subcontractor.nino.isDefined),
        nominatedPartnerNino = subcontractor.nino,
        nominatedPartnerCrnYesNo = Some(subcontractor.crn.isDefined),
        nominatedPartnerCrn = subcontractor.crn,
        nominatedPartnerWorksReferenceYesNo = Some(subcontractor.worksReferenceNumber.isDefined),
        nominatedPartnerWorksReference = subcontractor.worksReferenceNumber
      )
    val partnershipName = subcontractor.partnershipTradingName.orElse(subcontractor.tradingName)
    for {
      updated <- userAnswers.set(TypeOfSubcontractorPage, Partnership)
      updated <- setOptional(updated, PartnershipNamePage, partnershipName)
      updated <- updated.set(PartnershipAddressYesNoPage, address.isDefined)
      updated <- setOptional(updated, PartnershipAddressPage, address)
      updated <- updated.set(AddPartnershipContactMethodsYesNoPage, methods.nonEmpty)
      updated <- if (methods.nonEmpty) updated.set(PartnershipContactMethodOptionsPage, methods) else Try(updated)
      updated <- setOptional(updated, PartnershipEmailAddressPage, subcontractor.emailAddress)
      updated <- setOptional(updated, PartnershipPhoneNumberPage, subcontractor.phoneNumber)
      updated <- setOptional(updated, PartnershipMobileNumberPage, subcontractor.mobilePhoneNumber)
      updated <- updated.set(PartnershipHasUtrYesNoPage, subcontractor.utr.isDefined)
      updated <- setOptional(updated, PartnershipUniqueTaxpayerReferencePage, subcontractor.utr)
      updated <- setOptional(
                   updated,
                   PartnershipNominatedPartnerNamePage,
                   Option.when(nominatedPartnerName.nonEmpty)(nominatedPartnerName)
                 )
      updated <- updated.set(PartnershipNominatedPartnerUtrYesNoPage, subcontractor.partnerUtr.isDefined)
      updated <- setOptional(updated, PartnershipNominatedPartnerUtrPage, subcontractor.partnerUtr)
      updated <- updated.set(PartnershipNominatedPartnerNinoYesNoPage, subcontractor.nino.isDefined)
      updated <- setOptional(updated, PartnershipNominatedPartnerNinoPage, subcontractor.nino)
      updated <- updated.set(PartnershipNominatedPartnerCrnYesNoPage, subcontractor.crn.isDefined)
      updated <- setOptional(updated, PartnershipNominatedPartnerCrnPage, subcontractor.crn)
      updated <- updated.set(PartnershipWorksReferenceNumberYesNoPage, subcontractor.worksReferenceNumber.isDefined)
      updated <- setOptional(updated, PartnershipWorksReferenceNumberPage, subcontractor.worksReferenceNumber)
      updated <- updated.set(CisIdQuery, cisId)
      updated <- updated.set(OriginalPartnershipAnswersQuery, originalAnswers)
    } yield updated
  }

  private def contactMethods(
    subcontractor: SubcontractorResponse
  ): Set[ContactMethodOptions] =
    Set(
      subcontractor.emailAddress.map(_ => ContactMethodOptions.Email),
      subcontractor.phoneNumber.map(_ => ContactMethodOptions.Phone),
      subcontractor.mobilePhoneNumber.map(_ => ContactMethodOptions.Mobile)
    ).flatten

  private def toAddress(subcontractor: SubcontractorResponse): Option[Address] =
    subcontractor.addressLine1.map { line1 =>
      Address(
        addressLine1 = line1,
        addressLine2 = subcontractor.addressLine2,
        addressLine3 = subcontractor.addressLine3,
        addressLine4 = subcontractor.addressLine4,
        postcode = subcontractor.postcode,
        country = subcontractor.country.map(name => Country(None, Some(name)))
      )
    }

  private def setOptional[A: Writes](
    userAnswers: UserAnswers,
    page: pages.QuestionPage[A],
    value: Option[A]
  ): Try[UserAnswers] =
    value match {
      case Some(answer) =>
        userAnswers.set(page, answer)

      case None =>
        Try(userAnswers)
    }
}

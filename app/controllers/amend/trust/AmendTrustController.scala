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

package controllers.amend.trust

import controllers.actions.*
import models.TypeOfSubcontractor.Trust
import models.UserAnswers
import models.address.{Address, Country}
import models.amend.trust.OriginalTrustAnswers
import models.contact.ContactMethodOptions
import models.response.SubcontractorResponse
import pages.add.*
import pages.add.trust.*
import play.api.Logging
import controllers.amend.AmendControllerUtils._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.{CisIdQuery, OriginalTrustAnswersQuery}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AmendTrustController @Inject() (
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
                s"[AmendTrustController] No subcontractor returned for " +
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
                    s"[AmendTrustController] Failed to populate UserAnswers for " +
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
                        controllers.amend.trust.routes.AmendTrustCheckYourAnswersController
                          .onPageLoad()
                      )
                    }
              )
          }
        }
        .recover { case error =>
          logger.error(
            s"[AmendTrustController] Failed to retrieve subcontractor. " +
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
    val address = toAddress(subcontractor)
    val methods = contactMethods(subcontractor)

    val trustName =
      subcontractor.tradingName.orElse(
        subcontractor.partnershipTradingName
      )

    val original = originalAnswers(
      subcontractor = subcontractor,
      address = address,
      methods = methods,
      trustName = trustName
    )

    for {
      updated <- userAnswers.set(TypeOfSubcontractorPage, Trust)
      updated <- setOptional(updated, TrustNamePage, trustName)
      updated <- updated.set(TrustAddressYesNoPage, address.isDefined)
      updated <- setOptional(updated, TrustAddressPage, address)
      updated <- updated.set(AddTrustContactMethodsYesNoPage, methods.nonEmpty)
      updated <- if (methods.nonEmpty) updated.set(TrustContactMethodOptionsPage, methods) else Try(updated)
      updated <- setOptional(updated, TrustEmailAddressPage, subcontractor.emailAddress)
      updated <- setOptional(updated, TrustPhoneNumberPage, subcontractor.phoneNumber)
      updated <- setOptional(updated, TrustMobileNumberPage, subcontractor.mobilePhoneNumber)
      updated <- updated.set(TrustUtrYesNoPage, subcontractor.utr.isDefined)
      updated <- setOptional(updated, TrustUtrPage, subcontractor.utr)
      updated <- updated.set(TrustWorksReferenceYesNoPage, subcontractor.worksReferenceNumber.isDefined)
      updated <- setOptional(updated, TrustWorksReferencePage, subcontractor.worksReferenceNumber)
      updated <- updated.set(CisIdQuery, cisId)
      updated <- updated.set(OriginalTrustAnswersQuery, original)
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

  private def toAddress(
    subcontractor: SubcontractorResponse
  ): Option[Address] =
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

  private def originalAnswers(
    subcontractor: SubcontractorResponse,
    address: Option[Address],
    methods: Set[ContactMethodOptions],
    trustName: Option[String]
  ): OriginalTrustAnswers =
    OriginalTrustAnswers(
      trustName = trustName,
      addressYesNo = Some(address.isDefined),
      address = address,
      trustContactMethodsYesNo = Some(methods.nonEmpty),
      trustContactMethod = methods,
      email = subcontractor.emailAddress,
      phone = subcontractor.phoneNumber,
      mobile = subcontractor.mobilePhoneNumber,
      utrYesNo = Some(subcontractor.utr.isDefined),
      utr = subcontractor.utr,
      worksReferenceYesNo = Some(subcontractor.worksReferenceNumber.isDefined),
      worksReference = subcontractor.worksReferenceNumber
    )
}

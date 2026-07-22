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

package controllers.amend

import controllers.actions.*
import models.TypeOfSubcontractor.Individualorsoletrader
import models.UserAnswers
import models.add.SubcontractorName
import models.address.{Address, Country}
import models.amend.OriginalIndividualAnswers
import models.contact.ContactOptions.NoDetails
import models.response.SubcontractorResponse
import pages.add.*
import play.api.Logging
import play.api.libs.json.Writes
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.{CisIdQuery, OriginalIndividualAnswersQuery}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AmendIndividualController @Inject() (
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
                s"[AmendIndividualController] No subcontractor returned for " +
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
                    "[AmendIndividualController] Failed to populate UserAnswers",
                    error
                  )

                  Future.successful(recovery)
                },
                updatedAnswers =>
                  sessionRepository
                    .set(updatedAnswers)
                    .map { _ =>
                      Redirect(
                        controllers.amend.routes.AmendIndividualCheckYourAnswersController
                          .onPageLoad()
                      )
                    }
              )
          }
        }
        .recover { case error =>
          logger.error(
            s"[AmendIndividualController] Failed to retrieve subcontractor. " +
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

    val name = for {
      firstName <- subcontractor.firstName
      surname   <- subcontractor.surname
    } yield SubcontractorName(
      firstName = firstName,
      middleName = subcontractor.secondName,
      lastName = surname
    )

    val usesTradingName = subcontractor.tradingName.exists(_.trim.nonEmpty)

    val originalAnswers =
      OriginalIndividualAnswers(
        usesTradingName = Some(usesTradingName),
        tradingName = subcontractor.tradingName,
        subcontractorName = name,
        address = address,
        contactMethod = Some(NoDetails),
        contactValue = None,
        utr = subcontractor.utr,
        nino = subcontractor.nino,
        worksReference = subcontractor.worksReferenceNumber
      )

    for {
      updated <- userAnswers.set(TypeOfSubcontractorPage, Individualorsoletrader)
      updated <- updated.set(SubTradingNameYesNoPage, usesTradingName)
      updated <- setOptional(updated, TradingNameOfSubcontractorPage, subcontractor.tradingName)
      updated <- setOptional(updated, SubcontractorNamePage, name)
      updated <- updated.set(SubAddressYesNoPage, address.isDefined)
      updated <- setOptional(updated, AddressOfSubcontractorPage, address)
      updated <- updated.set(IndividualChooseContactDetailsPage, NoDetails)
      updated <- updated.set(AddIndividualContactMethodsYesNoPage, false)
      updated <- updated.set(UniqueTaxpayerReferenceYesNoPage, subcontractor.utr.isDefined)
      updated <- setOptional(updated, SubcontractorsUniqueTaxpayerReferencePage, subcontractor.utr)
      updated <- updated.set(NationalInsuranceNumberYesNoPage, subcontractor.nino.isDefined)
      updated <- setOptional(updated, SubNationalInsuranceNumberPage, subcontractor.nino)
      updated <- updated.set(WorksReferenceNumberYesNoPage, subcontractor.worksReferenceNumber.isDefined)
      updated <- setOptional(updated, WorksReferenceNumberPage, subcontractor.worksReferenceNumber)
      updated <- updated.set(CisIdQuery, cisId)
      updated <- updated.set(OriginalIndividualAnswersQuery, originalAnswers)
    } yield updated
  }

  private def toAddress(subcontractor: SubcontractorResponse): Option[Address] =
    subcontractor.addressLine1.map { addressLine1 =>
      Address(
        addressLine1 = addressLine1,
        addressLine2 = subcontractor.addressLine2,
        addressLine3 = subcontractor.addressLine3,
        addressLine4 = subcontractor.addressLine4,
        addressLine5 = None,
        postcode = subcontractor.postcode,
        country = subcontractor.country.map { country =>
          Country(
            code = None,
            name = Some(country)
          )
        }
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

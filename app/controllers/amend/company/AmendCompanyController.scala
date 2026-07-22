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

package controllers.amend.company

import controllers.actions.*
import models.TypeOfSubcontractor.Limitedcompany
import models.UserAnswers
import models.address.{Address, Country}
import models.amend.company.OriginalCompanyAnswers
import models.contact.ContactMethodOptions
import models.response.SubcontractorResponse
import pages.add.*
import pages.add.company.*
import play.api.Logging
import play.api.libs.json.Writes
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.{CisIdQuery, OriginalCompanyAnswersQuery}
import repositories.SessionRepository
import services.SubcontractorService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AmendCompanyController @Inject() (
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

  def onPageLoad(
    cisId: String,
    subbieResourceRef: Long
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      subcontractorService
        .getSubcontractor(cisId, subbieResourceRef)
        .flatMap { response =>
          response.subcontractor match {
            case None =>
              logger.error(
                s"[AmendCompanyController] No subcontractor returned for " +
                  s"cisId=$cisId, subbieResourceRef=$subbieResourceRef"
              )

              Future.successful(recovery)

            case Some(subcontractor) =>
              populateUserAnswers(
                request.userAnswers,
                cisId,
                subcontractor
              ).fold(
                _ => Future.successful(recovery),
                updatedAnswers =>
                  sessionRepository
                    .set(updatedAnswers)
                    .map(_ =>
                      Redirect(
                        controllers.amend.company.routes.AmendCompanyCheckYourAnswersController
                          .onPageLoad()
                      )
                    )
              )
          }
        }
        .recover { case error =>
          logger.error(
            s"[AmendCompanyController] Failed to retrieve subcontractor. " +
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

    val originalAnswers =
      OriginalCompanyAnswers(
        companyName = subcontractor.tradingName,
        address = address,
        companyContactMethod = Option.when(methods.nonEmpty)(methods),
        email = subcontractor.emailAddress,
        phone = subcontractor.phoneNumber,
        mobile = subcontractor.mobilePhoneNumber,
        crn = subcontractor.crn,
        utr = subcontractor.utr,
        worksReference = subcontractor.worksReferenceNumber
      )

    for {
      updated <- userAnswers.set(TypeOfSubcontractorPage, Limitedcompany)
      updated <- setOptional(updated, CompanyNamePage, subcontractor.tradingName)
      updated <- updated.set(CompanyAddressYesNoPage, address.isDefined)
      updated <- setOptional(updated, CompanyAddressPage, address)
      updated <- updated.set(AddCompanyContactMethodsYesNoPage, methods.nonEmpty)
      updated <- if (methods.nonEmpty) updated.set(CompanyContactMethodOptionsPage, methods) else Try(updated)
      updated <- setOptional(updated, CompanyEmailAddressPage, subcontractor.emailAddress)
      updated <- setOptional(updated, CompanyPhoneNumberPage, subcontractor.phoneNumber)
      updated <- setOptional(updated, CompanyMobileNumberPage, subcontractor.mobilePhoneNumber)
      updated <- updated.set(CompanyUtrYesNoPage, subcontractor.utr.isDefined)
      updated <- setOptional(updated, CompanyUtrPage, subcontractor.utr)
      updated <- updated.set(CompanyCrnYesNoPage, subcontractor.crn.isDefined)
      updated <- setOptional(updated, CompanyCrnPage, subcontractor.crn)
      updated <- updated.set(CompanyWorksReferenceYesNoPage, subcontractor.worksReferenceNumber.isDefined)
      updated <- setOptional(updated, CompanyWorksReferencePage, subcontractor.worksReferenceNumber)
      updated <- updated.set(CisIdQuery, cisId)
      updated <- updated.set(OriginalCompanyAnswersQuery, originalAnswers)
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

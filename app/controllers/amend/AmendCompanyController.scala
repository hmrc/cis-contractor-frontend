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
import models.UserAnswers
import models.address.{Address, Country}
import models.amend.OriginalCompanyAnswers
import models.contact.ContactMethodOptions.Email
import pages.add.*
import pages.add.company.*
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{CisIdQuery, OriginalCompanyAnswersQuery}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

// TODO: replace demo data with real backend fetch
class AmendCompanyController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController {
  private val companyName    = "test company"
  private val emailAddress   = "test@example.com"
  private val utr            = "7777777777"
  private val crn            = "AC012345"
  private val worksReference = "XLS345-MM"
  private val companyAddress = Address(
    addressLine1 = "12 Harbor View Road",
    addressLine2 = Some("Amity Island"),
    addressLine3 = Some("Bodmin"),
    addressLine4 = Some("Cornwall"),
    postcode = Some("PL31 2HL"),
    country = Some(Country(code = None, name = Some("England")))
  )

  private val companyOriginal = OriginalCompanyAnswers(
    companyName = Some(companyName),
    address = Some(companyAddress),
    contactMethods = Set(Email),
    email = Some(emailAddress),
    phone = None,
    mobile = None,
    utr = Some(utr),
    crn = Some(crn),
    worksReference = Some(worksReference)
  )
  protected def populateUserAnswers(ua: UserAnswers): Try[UserAnswers] = for {
    ua <- ua.set(CompanyNamePage, companyName)
    ua <- ua.set(CompanyAddressYesNoPage, true)
    ua <- ua.set(CompanyAddressPage, companyAddress)
    ua <- ua.set(CompanyContactMethodOptionsPage, Set(Email))
    ua <- ua.set(CompanyEmailAddressPage, emailAddress)
    ua <- ua.set(CompanyUtrYesNoPage, true)
    ua <- ua.set(CompanyUtrPage, utr)
    ua <- ua.set(CompanyCrnYesNoPage, true)
    ua <- ua.set(CompanyCrnPage, crn)
    ua <- ua.set(CompanyWorksReferenceYesNoPage, true)
    ua <- ua.set(CompanyWorksReferencePage, worksReference)
    ua <- ua.set(CisIdQuery, "1")
    ua <- ua.set(OriginalCompanyAnswersQuery, companyOriginal)
  } yield ua

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    populateUserAnswers(request.userAnswers).fold(
      _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())),
      ua =>
        sessionRepository
          .set(ua)
          .map(_ =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          ) // TODO - redirect to [Amend Company] Subcontractor details
    )
  }
}

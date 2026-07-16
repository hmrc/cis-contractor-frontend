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
import models.contact.ContactMethodOptions.{Email, Mobile, Phone}
import pages.add.*
import pages.add.trust.*
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{CisIdQuery, OriginalTrustAnswersQuery}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

// TODO: replace demo data with real backend fetch
class AmendTrustController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController {
  private val trustName      = "test trust"
  private val emailAddress   = "test@example.com"
  private val utr            = "1123456789"
  private val worksReference = "XLS345-MM"
  private val phoneNumber    = "1234567890"
  private val mobileNumber   = "6454543667"
  private val trustAddress   = Address(
    addressLine1 = "12 Harbor View Road",
    addressLine2 = Some("Amity Island"),
    addressLine3 = Some("Bodmin"),
    addressLine4 = Some("Cornwall"),
    postcode = Some("PL31 2HL"),
    country = Some(Country(code = None, name = Some("England")))
  )

  private val trustOriginal                                            = OriginalTrustAnswers(
    trustName = Some(trustName),
    addressYesNo = Some(true),
    address = Some(trustAddress),
    trustContactMethodsYesNo = Some(true),
    trustContactMethod = Set(Email, Phone, Mobile),
    email = Some(emailAddress),
    phone = Some(phoneNumber),
    mobile = Some(mobileNumber),
    utrYesNo = Some(true),
    utr = Some(utr),
    worksReferenceYesNo = Some(true),
    worksReference = Some(worksReference)
  )
  protected def populateUserAnswers(ua: UserAnswers): Try[UserAnswers] = for {
    ua <- ua.set(TypeOfSubcontractorPage, Trust)
    ua <- ua.set(TrustNamePage, trustName)
    ua <- ua.set(TrustAddressYesNoPage, true)
    ua <- ua.set(TrustAddressPage, trustAddress)
    ua <- ua.set(AddTrustContactMethodsYesNoPage, true)
    ua <- ua.set(TrustContactMethodOptionsPage, Set(Email, Phone, Mobile))
    ua <- ua.set(TrustEmailAddressPage, emailAddress)
    ua <- ua.set(TrustPhoneNumberPage, phoneNumber)
    ua <- ua.set(TrustMobileNumberPage, mobileNumber)
    ua <- ua.set(TrustUtrYesNoPage, true)
    ua <- ua.set(TrustUtrPage, utr)
    ua <- ua.set(TrustWorksReferenceYesNoPage, true)
    ua <- ua.set(TrustWorksReferencePage, worksReference)
    ua <- ua.set(CisIdQuery, "1")
    ua <- ua.set(OriginalTrustAnswersQuery, trustOriginal)
    ua <- ua.set(AmendedPagesPage, Set(AddTrustContactMethodsYesNoPage))
  } yield ua

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    populateUserAnswers(request.userAnswers).fold(
      _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())),
      ua =>
        sessionRepository
          .set(ua)
          .map(_ =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          ) // TODO - redirect to [Amend Trust] Subcontractor details
    )
  }
}

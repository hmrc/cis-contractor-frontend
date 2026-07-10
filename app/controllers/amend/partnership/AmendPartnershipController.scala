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
import models.contact.ContactMethodOptions.Email
import pages.add.*
import pages.add.partnership.*
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{CisIdQuery, OriginalPartnershipAnswersQuery}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

// TODO: replace demo data with real backend fetch
class AmendPartnershipController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController {
  private val partnershipName      = "test partnership"
  private val emailAddress         = "test@example.com"
  private val utr                  = "7777777777"
  private val nominatedPartnerUtr  = "8777777777"
  private val crn                  = "AC012345"
  private val worksReference       = "XLS345-MM"
  private val nominatedPartnerName = "test nominated partner"
  private val nominatedPartnerNino = "QQ123456C"
  private val partnershipAddress   = Address(
    addressLine1 = "12 Harbor View Road",
    addressLine2 = Some("Amity Island"),
    addressLine3 = Some("Bodmin"),
    addressLine4 = Some("Cornwall"),
    postcode = Some("PL31 2HL"),
    country = Some(Country(code = None, name = Some("England")))
  )

  private val partnershipOriginal                                      = OriginalPartnershipAnswers(
    partnershipName = Some(partnershipName),
    addressYesNo = Some(true),
    address = Some(partnershipAddress),
    partnershipContactMethodsYesNo = Some(true),
    partnershipContactMethodOptions = Some(Set(Email)),
    email = Some(emailAddress),
    phone = None,
    mobile = None,
    hasUtrYesNo = Some(true),
    utr = Some(utr),
    nominatedPartnerName = Some(nominatedPartnerName),
    nominatedPartnerUtrYesNo = Some(false),
    nominatedPartnerUtr = None,
    nominatedPartnerNinoYesNo = Some(true),
    nominatedPartnerNino = Some(nominatedPartnerNino),
    nominatedPartnerCrnYesNo = Some(true),
    nominatedPartnerCrn = Some(crn),
    nominatedPartnerWorksReferenceYesNo = Some(true),
    nominatedPartnerWorksReference = Some(worksReference)
  )
  protected def populateUserAnswers(ua: UserAnswers): Try[UserAnswers] = for {
    ua <- ua.set(TypeOfSubcontractorPage, Partnership)
    ua <- ua.set(PartnershipNamePage, partnershipName)
    ua <- ua.set(PartnershipAddressYesNoPage, true)
    ua <- ua.set(PartnershipAddressPage, partnershipAddress)
    ua <- ua.set(AddPartnershipContactMethodsYesNoPage, true)
    ua <- ua.set(PartnershipContactMethodOptionsPage, Set(Email))
    ua <- ua.set(PartnershipEmailAddressPage, emailAddress)
    ua <- ua.set(PartnershipHasUtrYesNoPage, true)
    ua <- ua.set(PartnershipUniqueTaxpayerReferencePage, utr)
    ua <- ua.set(PartnershipNominatedPartnerNamePage, nominatedPartnerName)
    ua <- ua.set(PartnershipNominatedPartnerUtrYesNoPage, true)
    ua <- ua.set(PartnershipNominatedPartnerUtrPage, nominatedPartnerUtr)
    ua <- ua.set(PartnershipNominatedPartnerNinoYesNoPage, true)
    ua <- ua.set(PartnershipNominatedPartnerNinoPage, nominatedPartnerNino)
    ua <- ua.set(PartnershipNominatedPartnerCrnYesNoPage, true)
    ua <- ua.set(PartnershipNominatedPartnerCrnPage, crn)
    ua <- ua.set(PartnershipWorksReferenceNumberYesNoPage, true)
    ua <- ua.set(PartnershipWorksReferenceNumberPage, worksReference)
    ua <- ua.set(CisIdQuery, "1")
    ua <- ua.set(OriginalPartnershipAnswersQuery, partnershipOriginal)
  } yield ua

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    populateUserAnswers(request.userAnswers).fold(
      _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())),
      ua =>
        sessionRepository
          .set(ua)
          .map(_ =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          ) // TODO - redirect to [Amend Partnership] Subcontractor details
    )
  }
}

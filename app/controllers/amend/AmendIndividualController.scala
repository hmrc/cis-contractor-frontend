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
import models.add.SubcontractorName
import models.address.{Address, Country}
import models.amend.OriginalIndividualAnswers
import models.contact.ContactOptions.NoDetails
import pages.add.*
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{CisIdQuery, OriginalIndividualAnswersQuery}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

// TODO: replace demo data with real backend fetch
class AmendIndividualController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController {

  private val individualAddress = Address(
    addressLine1 = "12 Harbor View Road",
    addressLine2 = Some("amity island"),
    addressLine3 = Some("Bodmin"),
    addressLine4 = Some("Cornwall"),
    postcode = Some("PL31 2HL"),
    country = Some(Country(code = None, name = Some("England")))
  )

  private val individualAddress1 = Address(
    addressLine1 = "12 Harbor View Road",
    addressLine2 = Some("blue island"),
    addressLine3 = Some("Bodmin"),
    addressLine4 = Some("Cornwall"),
    postcode = Some("PL31 2HL"),
    country = Some(Country(code = None, name = Some("England")))
  )

  private val individualName = SubcontractorName(
    firstName = "Martin",
    middleName = None,
    lastName = "Brody"
  )

  private val individualOriginal = OriginalIndividualAnswers(
    usesTradingName = None,
    tradingName = None,
    subcontractorName = None,
    address = Some(individualAddress1),
    contactMethod = Some(NoDetails),
    contactValue = None,
    utrYesNo = Some(false),
    utr = None,
    ninoYesNo = Some(true),
    nino = Some("QQ123456C"),
    worksReferenceYesNo = Some(true),
    worksReference = Some("XLS345-MM")
  )

  protected def populateUserAnswers(ua: models.UserAnswers): Try[models.UserAnswers] = for {
    ua <- ua.set(TypeOfSubcontractorPage, Individualorsoletrader)
    ua <- ua.set(SubTradingNameYesNoPage, false)
    ua <- ua.set(SubcontractorNamePage, individualName)
    ua <- ua.set(SubAddressYesNoPage, true)
    ua <- ua.set(AddressOfSubcontractorPage, individualAddress)
    ua <- ua.set(IndividualChooseContactDetailsPage, NoDetails)
    ua <- ua.set(UniqueTaxpayerReferenceYesNoPage, true)
    ua <- ua.set(SubcontractorsUniqueTaxpayerReferencePage, "3992651576")
    ua <- ua.set(NationalInsuranceNumberYesNoPage, false)
    ua <- ua.set(SubNationalInsuranceNumberPage, "QQ123456C")
    ua <- ua.set(WorksReferenceNumberYesNoPage, true)
    ua <- ua.set(WorksReferenceNumberPage, "XLS345-MM")
    ua <- ua.set(CisIdQuery, "1")
    ua <- ua.set(OriginalIndividualAnswersQuery, individualOriginal)
  } yield ua

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    populateUserAnswers(request.userAnswers).fold(
      _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())),
      ua =>
        sessionRepository
          .set(ua)
          .map(_ =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          ) // TODO - redirect to [Amend Individual] Subcontractor details DTR-6497
    )
  }
}

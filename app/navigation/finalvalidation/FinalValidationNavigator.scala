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

package navigation.finalvalidation

import controllers.add.routes as soleTraderRoutes
import controllers.add.company.routes as companyRoutes
import controllers.add.partnership.routes as partnershipRoutes
import controllers.add.trust.routes as trustRoutes
import models.{FinalValidationMode, JourneyNamespace, Mode, UserAnswers}
import models.finalvalidation.FinalValidationChangeTarget
import models.finalvalidation.FinalValidationChangeTarget.*
import models.TypeOfSubcontractor.*
import models.add.IndividualNamesOptions
import models.contact.ContactMethodOptions
import navigation.NavigatorForJourney
import pages.add.*
import pages.add.company.*
import pages.add.partnership.*
import pages.add.trust.*
import pages.Page
import pages.finalvalidation.FinalValidationChangeTargetPage
import play.api.Logging
import play.api.mvc.Call

import java.util.UUID
import javax.inject.{Inject, Singleton}

@Singleton
class FinalValidationNavigator @Inject() extends NavigatorForJourney with Logging {

  private val namespace = JourneyNamespace.SubcontractorFinalValidation

  def startPage(target: FinalValidationChangeTarget, userAnswers: UserAnswers): Call =
    userAnswers.get(TypeOfSubcontractorPage) match {
      case Some(Individualorsoletrader) => soleTraderStartPage(target)
      case Some(Limitedcompany)         => companyStartPage(target)
      case Some(Partnership)            => partnershipStartPage(target)
      case Some(Trust)                  => trustStartPage(target)
      case None                         =>
        logger.error("[FinalValidationNavigator][startPage] - TypeOfSubcontractorPage missing from userAnswers")
        recovery
    }

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    extractScopedId(page) match {
      // Multi-tab
      case Some(id) =>
        nextPage(page, mode, userAnswers, id)

      case None =>
        val target =
          userAnswers.get(FinalValidationChangeTargetPage)

        logger.info(
          s"[FinalValidationNavigator][nextPage] " +
            s"page=$page mode=$mode target=$target"
        )

        mode match {
          case FinalValidationMode =>
            userAnswers.get(FinalValidationChangeTargetPage) match {
              case Some(Names)               =>
                individualNamesNextPage(page, userAnswers)
              case Some(AddressYesNo)        =>
                addressYesNoNextPage(page, userAnswers)
              case Some(ContactDetailsYesNo) =>
                contactDetailsYesNoNextPage(page, userAnswers)
              case Some(UtrYesNo) | Some(NinoYesNo) | Some(CrnYesNo) | Some(PartnerUtrYesNo) | Some(
                    WorksReferenceNumberYesNo
                  ) =>
                identifierYesNoNextPage(page, userAnswers)
              case Some(_)                   =>
                complete
              case None                      =>
                logger.error("[FinalValidationNavigator][nextPage] FinalValidationChangeTargetPage missing")
                recovery
            }
          case _                   =>
            logger.error(s"[FinalValidationNavigator][nextPage] Unexpected mode=$mode")
            recovery
        }
    }

  private def complete: Call =
    controllers.finalvalidations.routes.FinalValidationCompleteController.onPageLoad()

  private def recovery: Call =
    controllers.routes.JourneyRecoveryController.onPageLoad()

  private def yesNoNextPage(
    answer: Option[Boolean],
    onYes: => Call
  ): Call =
    answer match {
      case Some(true)  => onYes
      case Some(false) => complete
      case None        =>
        logger.error("[FinalValidationNavigator][yesNoNextPage] - yes/no answer missing from userAnswers")
        recovery
    }

  private def identifierYesNoNextPage(page: Page, userAnswers: UserAnswers): Call =
    page match {
      // Individual / sole trader
      case UniqueTaxpayerReferenceYesNoPage         =>
        yesNoNextPage(
          userAnswers.get(UniqueTaxpayerReferenceYesNoPage),
          soleTraderRoutes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(FinalValidationMode)
        )
      case NationalInsuranceNumberYesNoPage         =>
        yesNoNextPage(
          userAnswers.get(NationalInsuranceNumberYesNoPage),
          soleTraderRoutes.SubNationalInsuranceNumberController.onPageLoad(FinalValidationMode)
        )
      case WorksReferenceNumberYesNoPage            =>
        yesNoNextPage(
          userAnswers.get(WorksReferenceNumberYesNoPage),
          soleTraderRoutes.WorksReferenceNumberController.onPageLoad(FinalValidationMode)
        )

      // company
      case CompanyUtrYesNoPage                      =>
        yesNoNextPage(
          userAnswers.get(CompanyUtrYesNoPage),
          companyRoutes.CompanyUtrController.onPageLoad(FinalValidationMode)
        )
      case CompanyCrnYesNoPage                      =>
        yesNoNextPage(
          userAnswers.get(CompanyCrnYesNoPage),
          companyRoutes.CompanyCrnController.onPageLoad(FinalValidationMode)
        )
      case CompanyWorksReferenceYesNoPage           =>
        yesNoNextPage(
          userAnswers.get(CompanyWorksReferenceYesNoPage),
          companyRoutes.CompanyWorksReferenceController.onPageLoad(FinalValidationMode)
        )

      // Trust
      case TrustUtrYesNoPage                        =>
        yesNoNextPage(
          userAnswers.get(TrustUtrYesNoPage),
          trustRoutes.TrustUtrController.onPageLoad(FinalValidationMode)
        )
      case TrustWorksReferenceYesNoPage             =>
        yesNoNextPage(
          userAnswers.get(TrustWorksReferenceYesNoPage),
          trustRoutes.TrustWorksReferenceController.onPageLoad(FinalValidationMode)
        )

      // Partnership
      case PartnershipHasUtrYesNoPage               =>
        yesNoNextPage(
          userAnswers.get(PartnershipHasUtrYesNoPage),
          partnershipRoutes.PartnershipUniqueTaxpayerReferenceController.onPageLoad(FinalValidationMode)
        )
      case PartnershipNominatedPartnerUtrYesNoPage  =>
        yesNoNextPage(
          userAnswers.get(PartnershipNominatedPartnerUtrYesNoPage),
          partnershipRoutes.PartnershipNominatedPartnerUtrController.onPageLoad(FinalValidationMode)
        )
      case PartnershipNominatedPartnerNinoYesNoPage =>
        yesNoNextPage(
          userAnswers.get(PartnershipNominatedPartnerNinoYesNoPage),
          partnershipRoutes.PartnershipNominatedPartnerNinoController.onPageLoad(FinalValidationMode)
        )
      case PartnershipNominatedPartnerCrnYesNoPage  =>
        yesNoNextPage(
          userAnswers.get(PartnershipNominatedPartnerCrnYesNoPage),
          partnershipRoutes.PartnershipNominatedPartnerCrnController.onPageLoad(FinalValidationMode)
        )
      case PartnershipWorksReferenceNumberYesNoPage =>
        yesNoNextPage(
          userAnswers.get(PartnershipWorksReferenceNumberYesNoPage),
          partnershipRoutes.PartnershipWorksReferenceNumberController.onPageLoad(FinalValidationMode)
        )
      case _                                        =>
        complete
    }

  private def individualNamesNextPage(
    page: Page,
    userAnswers: UserAnswers
  ): Call = {

    val selected =
      userAnswers
        .get(IndividualNamesOptionsPage)
        .getOrElse(Set.empty)

    page match {
      case IndividualNamesOptionsPage =>
        if (selected.contains(IndividualNamesOptions.SubcontractorName)) {
          soleTraderRoutes.SubcontractorNameController.onPageLoad(FinalValidationMode)
        } else if (selected.contains(IndividualNamesOptions.TradingName)) {
          soleTraderRoutes.TradingNameOfSubcontractorController.onPageLoad(FinalValidationMode)
        } else {
          logger.error(
            "[FinalValidationNavigator][individualNamesNextPage] - names option missing"
          )
          recovery
        }

      case SubcontractorNamePage =>
        if (selected.contains(IndividualNamesOptions.TradingName)) {
          soleTraderRoutes.TradingNameOfSubcontractorController.onPageLoad(FinalValidationMode)
        } else {
          complete
        }

      case TradingNameOfSubcontractorPage =>
        complete

      case _ =>
        logger.error(s"[FinalValidationNavigator][individualNamesNextPage] - navigator fallback reached, page=$page")
        recovery
    }
  }

  private def addressYesNoNextPage(page: Page, userAnswers: UserAnswers): Call =
    page match {
      case SubAddressYesNoPage         =>
        yesNoNextPage(
          userAnswers.get(SubAddressYesNoPage),
          soleTraderRoutes.AddressOfSubcontractorController.redirectToAddressLookup(FinalValidationMode, None)
        )
      case CompanyAddressYesNoPage     =>
        yesNoNextPage(
          userAnswers.get(CompanyAddressYesNoPage),
          companyRoutes.CompanyAddressController.redirectToAddressLookup(FinalValidationMode, None)
        )
      case PartnershipAddressYesNoPage =>
        yesNoNextPage(
          userAnswers.get(PartnershipAddressYesNoPage),
          partnershipRoutes.PartnershipAddressController.redirectToAddressLookup(FinalValidationMode, None)
        )
      case TrustAddressYesNoPage       =>
        yesNoNextPage(
          userAnswers.get(TrustAddressYesNoPage),
          trustRoutes.TrustAddressController.redirectToAddressLookup(FinalValidationMode, None)
        )
      case _                           =>
        logger.error(s"[FinalValidationNavigator][addressYesNoNextPage] - navigator fallback reached, page=$page")
        recovery
    }

  private def contactDetailsYesNoNextPage(page: Page, userAnswers: UserAnswers): Call =
    page match {
      // Individual / sole trader
      case AddIndividualContactMethodsYesNoPage  =>
        yesNoNextPage(
          userAnswers.get(AddIndividualContactMethodsYesNoPage),
          soleTraderRoutes.IndividualContactMethodOptionsController.onPageLoad(FinalValidationMode)
        )
      case IndividualContactMethodOptionsPage    =>
        nextSelectedIndividualContactMethodPageAfter(
          current = None,
          userAnswers
        )
      case IndividualEmailAddressPage            =>
        nextSelectedIndividualContactMethodPageAfter(
          current = Some(ContactMethodOptions.Email),
          userAnswers
        )
      case IndividualPhoneNumberPage             =>
        nextSelectedIndividualContactMethodPageAfter(
          current = Some(ContactMethodOptions.Phone),
          userAnswers
        )
      case IndividualMobileNumberPage            =>
        nextSelectedIndividualContactMethodPageAfter(
          current = Some(ContactMethodOptions.Mobile),
          userAnswers
        )

      // company
      case AddCompanyContactMethodsYesNoPage     =>
        yesNoNextPage(
          userAnswers.get(AddCompanyContactMethodsYesNoPage),
          companyRoutes.CompanyContactMethodOptionsController.onPageLoad(FinalValidationMode)
        )
      case CompanyContactMethodOptionsPage       =>
        nextSelectedCompanyContactMethodPageAfter(
          current = None,
          userAnswers
        )
      case CompanyEmailAddressPage               =>
        nextSelectedCompanyContactMethodPageAfter(
          current = Some(ContactMethodOptions.Email),
          userAnswers
        )
      case CompanyPhoneNumberPage                =>
        nextSelectedCompanyContactMethodPageAfter(
          current = Some(ContactMethodOptions.Phone),
          userAnswers
        )
      case CompanyMobileNumberPage               =>
        nextSelectedCompanyContactMethodPageAfter(
          current = Some(ContactMethodOptions.Mobile),
          userAnswers
        )

      // Partnership
      case AddPartnershipContactMethodsYesNoPage =>
        yesNoNextPage(
          userAnswers.get(AddPartnershipContactMethodsYesNoPage),
          partnershipRoutes.PartnershipContactMethodOptionsController.onPageLoad(FinalValidationMode)
        )
      case PartnershipContactMethodOptionsPage   =>
        nextSelectedPartnershipContactMethodPageAfter(
          current = None,
          userAnswers
        )
      case PartnershipEmailAddressPage           =>
        nextSelectedPartnershipContactMethodPageAfter(
          current = Some(ContactMethodOptions.Email),
          userAnswers
        )
      case PartnershipPhoneNumberPage            =>
        nextSelectedPartnershipContactMethodPageAfter(
          current = Some(ContactMethodOptions.Phone),
          userAnswers
        )
      case PartnershipMobileNumberPage           =>
        nextSelectedPartnershipContactMethodPageAfter(
          current = Some(ContactMethodOptions.Mobile),
          userAnswers
        )

      // Trust
      case AddTrustContactMethodsYesNoPage       =>
        yesNoNextPage(
          userAnswers.get(AddTrustContactMethodsYesNoPage),
          trustRoutes.TrustContactMethodOptionsController.onPageLoad(FinalValidationMode)
        )
      case TrustContactMethodOptionsPage         =>
        nextSelectedTrustContactMethodPageAfter(
          current = None,
          userAnswers
        )
      case TrustEmailAddressPage                 =>
        nextSelectedTrustContactMethodPageAfter(
          current = Some(ContactMethodOptions.Email),
          userAnswers
        )
      case TrustPhoneNumberPage                  =>
        nextSelectedTrustContactMethodPageAfter(
          current = Some(ContactMethodOptions.Phone),
          userAnswers
        )
      case TrustMobileNumberPage                 =>
        nextSelectedTrustContactMethodPageAfter(
          current = Some(ContactMethodOptions.Mobile),
          userAnswers
        )
      case _                                     =>
        logger.error(
          s"[FinalValidationNavigator][contactDetailsYesNoNextPage] - navigator fallback reached, page=$page"
        )
        recovery
    }

  private def nextSelectedContactMethodPageAfter(
    current: Option[ContactMethodOptions],
    selectedContactMethods: Option[Seq[ContactMethodOptions]]
  )(contactMethodPageCall: ContactMethodOptions => Call): Call =
    navigateFromContactMethodPage(current, selectedContactMethods) { remaining =>
      remaining.headOption.fold {
        complete
      } { contactMethod =>
        contactMethodPageCall(contactMethod)
      }
    }

  private def navigateFromContactMethodPage(
    current: Option[ContactMethodOptions],
    selectedContactMethods: Option[Seq[ContactMethodOptions]]
  )(terminalStep: Seq[ContactMethodOptions] => Call): Call =
    selectedContactMethods
      .filter(_.nonEmpty)
      .fold {
        logger.error("[FinalValidationNavigator][navigateFromContactMethodPage] - contact methods missing or empty")
        recovery
      } { selected =>
        current match {
          case Some(currentContactMethod) if !selected.contains(currentContactMethod) =>
            logger.error(
              "[FinalValidationNavigator][navigateFromContactMethodPage] - current contact method not selected"
            )
            recovery
          case _                                                                      =>
            val remaining =
              current match {
                case None                       =>
                  selected
                case Some(currentContactMethod) =>
                  val currentIndex = selected.indexWhere(_ == currentContactMethod)
                  selected.drop(currentIndex + 1)
              }

            terminalStep(remaining)
        }
      }

  private def nextSelectedIndividualContactMethodPageAfter(
    current: Option[ContactMethodOptions],
    userAnswers: UserAnswers
  ): Call =
    nextSelectedContactMethodPageAfter(
      current,
      selectedIndividualContactMethodsInOrder(userAnswers)
    )(
      individualContactMethodPageCall
    )

  private def selectedIndividualContactMethodsInOrder(userAnswers: UserAnswers): Option[Seq[ContactMethodOptions]] =
    userAnswers
      .get(IndividualContactMethodOptionsPage)
      .map(ContactMethodOptions.ordered)

  private def individualContactMethodPageCall(contactMethod: ContactMethodOptions): Call =
    contactMethod match {
      case ContactMethodOptions.Email  =>
        soleTraderRoutes.IndividualEmailAddressController.onPageLoad(FinalValidationMode)
      case ContactMethodOptions.Phone  =>
        soleTraderRoutes.IndividualPhoneNumberController.onPageLoad(FinalValidationMode)
      case ContactMethodOptions.Mobile =>
        soleTraderRoutes.IndividualMobileNumberController.onPageLoad(FinalValidationMode)
    }

  private def nextSelectedCompanyContactMethodPageAfter(
    current: Option[ContactMethodOptions],
    userAnswers: UserAnswers
  ): Call =
    nextSelectedContactMethodPageAfter(
      current,
      selectedCompanyContactMethodsInOrder(userAnswers)
    )(
      companyContactMethodPageCall
    )

  private def selectedCompanyContactMethodsInOrder(userAnswers: UserAnswers): Option[Seq[ContactMethodOptions]] =
    userAnswers
      .get(CompanyContactMethodOptionsPage)
      .map(ContactMethodOptions.ordered)

  private def companyContactMethodPageCall(contactMethod: ContactMethodOptions): Call =
    contactMethod match {
      case ContactMethodOptions.Email  => companyRoutes.CompanyEmailAddressController.onPageLoad(FinalValidationMode)
      case ContactMethodOptions.Phone  => companyRoutes.CompanyPhoneNumberController.onPageLoad(FinalValidationMode)
      case ContactMethodOptions.Mobile => companyRoutes.CompanyMobileNumberController.onPageLoad(FinalValidationMode)
    }

  private def nextSelectedPartnershipContactMethodPageAfter(
    current: Option[ContactMethodOptions],
    userAnswers: UserAnswers
  ): Call =
    nextSelectedContactMethodPageAfter(
      current,
      selectedPartnershipContactMethodsInOrder(userAnswers)
    )(
      partnershipContactMethodPageCall
    )

  private def selectedPartnershipContactMethodsInOrder(userAnswers: UserAnswers): Option[Seq[ContactMethodOptions]] =
    userAnswers
      .get(PartnershipContactMethodOptionsPage)
      .map(ContactMethodOptions.ordered)

  private def partnershipContactMethodPageCall(contactMethod: ContactMethodOptions): Call =
    contactMethod match {
      case ContactMethodOptions.Email  =>
        partnershipRoutes.PartnershipEmailAddressController.onPageLoad(FinalValidationMode)
      case ContactMethodOptions.Phone  =>
        partnershipRoutes.PartnershipPhoneNumberController.onPageLoad(FinalValidationMode)
      case ContactMethodOptions.Mobile =>
        partnershipRoutes.PartnershipMobileNumberController.onPageLoad(FinalValidationMode)
    }

  private def nextSelectedTrustContactMethodPageAfter(
    current: Option[ContactMethodOptions],
    userAnswers: UserAnswers
  ): Call =
    nextSelectedContactMethodPageAfter(
      current,
      selectedTrustContactMethodsInOrder(userAnswers)
    )(
      trustContactMethodPageCall
    )

  private def selectedTrustContactMethodsInOrder(userAnswers: UserAnswers): Option[Seq[ContactMethodOptions]] =
    userAnswers
      .get(TrustContactMethodOptionsPage)
      .map(ContactMethodOptions.ordered)

  private def trustContactMethodPageCall(contactMethod: ContactMethodOptions): Call =
    contactMethod match {
      case ContactMethodOptions.Email  => trustRoutes.TrustEmailAddressController.onPageLoad(FinalValidationMode)
      case ContactMethodOptions.Phone  => trustRoutes.TrustPhoneNumberController.onPageLoad(FinalValidationMode)
      case ContactMethodOptions.Mobile => trustRoutes.TrustMobileNumberController.onPageLoad(FinalValidationMode)
    }

  private def soleTraderStartPage(target: FinalValidationChangeTarget): Call =
    target match {
      case Names                     => soleTraderRoutes.IndividualNamesOptionsController.onPageLoad(FinalValidationMode)
      case SubcontractorName         => soleTraderRoutes.SubcontractorNameController.onPageLoad(FinalValidationMode)
      case TradingName               => soleTraderRoutes.TradingNameOfSubcontractorController.onPageLoad(FinalValidationMode)
      case AddressYesNo              => soleTraderRoutes.SubAddressYesNoController.onPageLoad(FinalValidationMode)
      case Address                   =>
        soleTraderRoutes.AddressOfSubcontractorController.redirectToAddressLookup(FinalValidationMode, None)
      case ContactDetailsYesNo       =>
        soleTraderRoutes.AddIndividualContactMethodsYesNoController.onPageLoad(FinalValidationMode)
      case EmailAddress              => soleTraderRoutes.IndividualEmailAddressController.onPageLoad(FinalValidationMode)
      case PhoneNumber               => soleTraderRoutes.IndividualPhoneNumberController.onPageLoad(FinalValidationMode)
      case MobilePhoneNumber         => soleTraderRoutes.IndividualMobileNumberController.onPageLoad(FinalValidationMode)
      case UtrYesNo                  => soleTraderRoutes.UniqueTaxpayerReferenceYesNoController.onPageLoad(FinalValidationMode)
      case Utr                       => soleTraderRoutes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(FinalValidationMode)
      case NinoYesNo                 => soleTraderRoutes.NationalInsuranceNumberYesNoController.onPageLoad(FinalValidationMode)
      case Nino                      => soleTraderRoutes.SubNationalInsuranceNumberController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumberYesNo =>
        soleTraderRoutes.WorksReferenceNumberYesNoController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumber      => soleTraderRoutes.WorksReferenceNumberController.onPageLoad(FinalValidationMode)
      case _                         =>
        logger.error(s"[FinalValidationNavigator][soleTraderStartPage] - navigator fallback reached, target=$target")
        recovery
    }

  private def companyStartPage(target: FinalValidationChangeTarget): Call =
    target match {
      case TradingName               => companyRoutes.CompanyNameController.onPageLoad(FinalValidationMode)
      case AddressYesNo              => companyRoutes.CompanyAddressYesNoController.onPageLoad(FinalValidationMode)
      case Address                   => companyRoutes.CompanyAddressController.redirectToAddressLookup(FinalValidationMode, None)
      case ContactDetailsYesNo       => companyRoutes.AddCompanyContactMethodsYesNoController.onPageLoad(FinalValidationMode)
      case EmailAddress              => companyRoutes.CompanyEmailAddressController.onPageLoad(FinalValidationMode)
      case PhoneNumber               => companyRoutes.CompanyPhoneNumberController.onPageLoad(FinalValidationMode)
      case MobilePhoneNumber         => companyRoutes.CompanyMobileNumberController.onPageLoad(FinalValidationMode)
      case UtrYesNo                  => companyRoutes.CompanyUtrYesNoController.onPageLoad(FinalValidationMode)
      case Utr                       => companyRoutes.CompanyUtrController.onPageLoad(FinalValidationMode)
      case CrnYesNo                  => companyRoutes.CompanyCrnYesNoController.onPageLoad(FinalValidationMode)
      case Crn                       => companyRoutes.CompanyCrnController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumberYesNo =>
        companyRoutes.CompanyWorksReferenceYesNoController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumber      => companyRoutes.CompanyWorksReferenceController.onPageLoad(FinalValidationMode)
      case _                         =>
        logger.error(s"[FinalValidationNavigator][companyStartPage] - navigator fallback reached, target=$target")
        recovery
    }

  private def trustStartPage(target: FinalValidationChangeTarget): Call =
    target match {
      case TradingName               => trustRoutes.TrustNameController.onPageLoad(FinalValidationMode)
      case AddressYesNo              => trustRoutes.TrustAddressYesNoController.onPageLoad(FinalValidationMode)
      case Address                   => trustRoutes.TrustAddressController.redirectToAddressLookup(FinalValidationMode, None)
      case ContactDetailsYesNo       => trustRoutes.AddTrustContactMethodsYesNoController.onPageLoad(FinalValidationMode)
      case EmailAddress              => trustRoutes.TrustEmailAddressController.onPageLoad(FinalValidationMode)
      case PhoneNumber               => trustRoutes.TrustPhoneNumberController.onPageLoad(FinalValidationMode)
      case MobilePhoneNumber         => trustRoutes.TrustMobileNumberController.onPageLoad(FinalValidationMode)
      case UtrYesNo                  => trustRoutes.TrustUtrYesNoController.onPageLoad(FinalValidationMode)
      case Utr                       => trustRoutes.TrustUtrController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumberYesNo => trustRoutes.TrustWorksReferenceYesNoController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumber      => trustRoutes.TrustWorksReferenceController.onPageLoad(FinalValidationMode)
      case _                         =>
        logger.error(s"[FinalValidationNavigator][trustStartPage] - navigator fallback reached, target=$target")
        recovery
    }

  private def partnershipStartPage(target: FinalValidationChangeTarget): Call =
    target match {
      case PartnershipTradingName    => partnershipRoutes.PartnershipNameController.onPageLoad(FinalValidationMode)
      case TradingName               => partnershipRoutes.PartnershipNominatedPartnerNameController.onPageLoad(FinalValidationMode)
      case AddressYesNo              => partnershipRoutes.PartnershipAddressYesNoController.onPageLoad(FinalValidationMode)
      case Address                   => partnershipRoutes.PartnershipAddressController.redirectToAddressLookup(FinalValidationMode, None)
      case ContactDetailsYesNo       =>
        partnershipRoutes.AddPartnershipContactMethodsYesNoController.onPageLoad(FinalValidationMode)
      case EmailAddress              => partnershipRoutes.PartnershipEmailAddressController.onPageLoad(FinalValidationMode)
      case PhoneNumber               => partnershipRoutes.PartnershipPhoneNumberController.onPageLoad(FinalValidationMode)
      case MobilePhoneNumber         => partnershipRoutes.PartnershipMobileNumberController.onPageLoad(FinalValidationMode)
      case UtrYesNo                  => partnershipRoutes.PartnershipHasUtrYesNoController.onPageLoad(FinalValidationMode)
      case Utr                       => partnershipRoutes.PartnershipUniqueTaxpayerReferenceController.onPageLoad(FinalValidationMode)
      case PartnerUtrYesNo           =>
        partnershipRoutes.PartnershipNominatedPartnerUtrYesNoController.onPageLoad(FinalValidationMode)
      case PartnerUtr                => partnershipRoutes.PartnershipNominatedPartnerUtrController.onPageLoad(FinalValidationMode)
      case NinoYesNo                 => partnershipRoutes.PartnershipNominatedPartnerNinoYesNoController.onPageLoad(FinalValidationMode)
      case Nino                      => partnershipRoutes.PartnershipNominatedPartnerNinoController.onPageLoad(FinalValidationMode)
      case CrnYesNo                  => partnershipRoutes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(FinalValidationMode)
      case Crn                       => partnershipRoutes.PartnershipNominatedPartnerCrnController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumberYesNo =>
        partnershipRoutes.PartnershipWorksReferenceNumberYesNoController.onPageLoad(FinalValidationMode)
      case WorksReferenceNumber      =>
        partnershipRoutes.PartnershipWorksReferenceNumberController.onPageLoad(FinalValidationMode)
      case _                         =>
        logger.error(
          s"[FinalValidationNavigator][partnershipStartPage] - navigator fallback reached, target=$target"
        )
        recovery
    }

  // Multi-tab
  def startPage(
    target: FinalValidationChangeTarget,
    userAnswers: UserAnswers,
    id: String
  ): Call =
    val flowId = UUID.fromString(id)

    userAnswers.get(TypeOfSubcontractorPage(id, namespace)) match {
      case Some(Individualorsoletrader) => soleTraderStartPage(target, flowId)
      case Some(Limitedcompany)         => companyStartPage(target, flowId)
      case Some(Partnership)            => partnershipStartPage(target, flowId)
      case Some(Trust)                  => trustStartPage(target, flowId)
      case None                         =>
        logger.error("[FinalValidationNavigator][startPage] - scoped TypeOfSubcontractorPage missing from userAnswers")
        recovery
    }

  private def nextPage(
    page: Page,
    mode: Mode,
    userAnswers: UserAnswers,
    id: String
  ): Call =
    val flowId = UUID.fromString(id)
    val target = userAnswers.get(FinalValidationChangeTargetPage(id))

    logger.info(s"[FinalValidationNavigator][nextPage] page=$page mode=$mode target=$target flowId=$id")

    mode match {
      case FinalValidationMode =>
        target match {
          case Some(Names) =>
            individualNamesNextPage(page, userAnswers, flowId)

          case Some(AddressYesNo) =>
            addressYesNoNextPage(page, userAnswers, flowId)

          case Some(ContactDetailsYesNo) =>
            contactDetailsYesNoNextPage(page, userAnswers, flowId)

          case Some(UtrYesNo) | Some(NinoYesNo) | Some(CrnYesNo) | Some(PartnerUtrYesNo) | Some(
                WorksReferenceNumberYesNo
              ) =>
            identifierYesNoNextPage(page, userAnswers, flowId)

          case Some(_) =>
            complete(flowId)

          case None =>
            logger.error("[FinalValidationNavigator][nextPage] - scoped FinalValidationChangeTargetPage missing")
            recovery
        }

      case _ =>
        logger.error(s"[FinalValidationNavigator][nextPage] - Unexpected mode=$mode")
        recovery
    }

  private def extractScopedId(page: Page): Option[String] =
    page match {

      // Individual / sole trader
      case IndividualNamesOptionsPage(id, `namespace`)                => Some(id)
      case SubcontractorNamePage(id, `namespace`)                     => Some(id)
      case TradingNameOfSubcontractorPage(id, `namespace`)            => Some(id)
      case SubAddressYesNoPage(id, `namespace`)                       => Some(id)
      case AddressOfSubcontractorPage(id, `namespace`)                => Some(id)
      case AddIndividualContactMethodsYesNoPage(id, `namespace`)      => Some(id)
      case IndividualContactMethodOptionsPage(id, `namespace`)        => Some(id)
      case IndividualEmailAddressPage(id, `namespace`)                => Some(id)
      case IndividualPhoneNumberPage(id, `namespace`)                 => Some(id)
      case IndividualMobileNumberPage(id, `namespace`)                => Some(id)
      case UniqueTaxpayerReferenceYesNoPage(id, `namespace`)          => Some(id)
      case SubcontractorsUniqueTaxpayerReferencePage(id, `namespace`) => Some(id)
      case NationalInsuranceNumberYesNoPage(id, `namespace`)          => Some(id)
      case SubNationalInsuranceNumberPage(id, `namespace`)            => Some(id)
      case WorksReferenceNumberYesNoPage(id, `namespace`)             => Some(id)
      case WorksReferenceNumberPage(id, `namespace`)                  => Some(id)

      // Company
      case CompanyNamePage(id, `namespace`)                   => Some(id)
      case CompanyAddressYesNoPage(id, `namespace`)           => Some(id)
      case CompanyAddressPage(id, `namespace`)                => Some(id)
      case AddCompanyContactMethodsYesNoPage(id, `namespace`) => Some(id)
      case CompanyContactMethodOptionsPage(id, `namespace`)   => Some(id)
      case CompanyEmailAddressPage(id, `namespace`)           => Some(id)
      case CompanyPhoneNumberPage(id, `namespace`)            => Some(id)
      case CompanyMobileNumberPage(id, `namespace`)           => Some(id)
      case CompanyUtrYesNoPage(id, `namespace`)               => Some(id)
      case CompanyUtrPage(id, `namespace`)                    => Some(id)
      case CompanyCrnYesNoPage(id, `namespace`)               => Some(id)
      case CompanyCrnPage(id, `namespace`)                    => Some(id)
      case CompanyWorksReferenceYesNoPage(id, `namespace`)    => Some(id)
      case CompanyWorksReferencePage(id, `namespace`)         => Some(id)

      // Partnership
      case PartnershipNamePage(id, `namespace`)                      => Some(id)
      case PartnershipAddressYesNoPage(id, `namespace`)              => Some(id)
      case PartnershipAddressPage(id, `namespace`)                   => Some(id)
      case AddPartnershipContactMethodsYesNoPage(id, `namespace`)    => Some(id)
      case PartnershipContactMethodOptionsPage(id, `namespace`)      => Some(id)
      case PartnershipEmailAddressPage(id, `namespace`)              => Some(id)
      case PartnershipPhoneNumberPage(id, `namespace`)               => Some(id)
      case PartnershipMobileNumberPage(id, `namespace`)              => Some(id)
      case PartnershipHasUtrYesNoPage(id, `namespace`)               => Some(id)
      case PartnershipUniqueTaxpayerReferencePage(id, `namespace`)   => Some(id)
      case PartnershipNominatedPartnerNamePage(id, `namespace`)      => Some(id)
      case PartnershipNominatedPartnerUtrYesNoPage(id, `namespace`)  => Some(id)
      case PartnershipNominatedPartnerUtrPage(id, `namespace`)       => Some(id)
      case PartnershipNominatedPartnerNinoYesNoPage(id, `namespace`) => Some(id)
      case PartnershipNominatedPartnerNinoPage(id, `namespace`)      => Some(id)
      case PartnershipNominatedPartnerCrnYesNoPage(id, `namespace`)  => Some(id)
      case PartnershipNominatedPartnerCrnPage(id, `namespace`)       => Some(id)
      case PartnershipWorksReferenceNumberYesNoPage(id, `namespace`) => Some(id)
      case PartnershipWorksReferenceNumberPage(id, `namespace`)      => Some(id)

      // Trust
      case TrustNamePage(id, `namespace`)                   => Some(id)
      case TrustAddressYesNoPage(id, `namespace`)           => Some(id)
      case TrustAddressPage(id, `namespace`)                => Some(id)
      case AddTrustContactMethodsYesNoPage(id, `namespace`) => Some(id)
      case TrustContactMethodOptionsPage(id, `namespace`)   => Some(id)
      case TrustEmailAddressPage(id, `namespace`)           => Some(id)
      case TrustPhoneNumberPage(id, `namespace`)            => Some(id)
      case TrustMobileNumberPage(id, `namespace`)           => Some(id)
      case TrustUtrYesNoPage(id, `namespace`)               => Some(id)
      case TrustUtrPage(id, `namespace`)                    => Some(id)
      case TrustWorksReferenceYesNoPage(id, `namespace`)    => Some(id)
      case TrustWorksReferencePage(id, `namespace`)         => Some(id)

      case _ => None
    }

  private def complete(flowId: UUID): Call =
    controllers.finalvalidations.routes.FinalValidationCompleteController.onPageLoadWithId(flowId)

  private def yesNoNextPage(
    answer: Option[Boolean],
    onYes: => Call,
    flowId: UUID
  ): Call =
    answer match {
      case Some(true)  => onYes
      case Some(false) => complete(flowId)
      case None        =>
        logger.error("[FinalValidationNavigator][yesNoNextPage] - scoped yes/no answer missing from userAnswers")
        recovery
    }

  private def soleTraderStartPage(
    target: FinalValidationChangeTarget,
    flowId: UUID
  ): Call =
    target match {
      case Names                     =>
        soleTraderRoutes.IndividualNamesOptionsController.onPageLoadWithId(flowId, FinalValidationMode)
      case SubcontractorName         =>
        soleTraderRoutes.SubcontractorNameController.onPageLoadWithId(flowId, FinalValidationMode)
      case TradingName               =>
        soleTraderRoutes.TradingNameOfSubcontractorController.onPageLoadWithId(flowId, FinalValidationMode)
      case AddressYesNo              =>
        soleTraderRoutes.SubAddressYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case Address                   =>
        soleTraderRoutes.AddressOfSubcontractorController
          .redirectToAddressLookupWithId(flowId, FinalValidationMode, None)
      case ContactDetailsYesNo       =>
        soleTraderRoutes.AddIndividualContactMethodsYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case EmailAddress              =>
        soleTraderRoutes.IndividualEmailAddressController.onPageLoadWithId(flowId, FinalValidationMode)
      case PhoneNumber               =>
        soleTraderRoutes.IndividualPhoneNumberController.onPageLoadWithId(flowId, FinalValidationMode)
      case MobilePhoneNumber         =>
        soleTraderRoutes.IndividualMobileNumberController.onPageLoadWithId(flowId, FinalValidationMode)
      case UtrYesNo                  =>
        soleTraderRoutes.UniqueTaxpayerReferenceYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case Utr                       =>
        soleTraderRoutes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoadWithId(flowId, FinalValidationMode)
      case NinoYesNo                 =>
        soleTraderRoutes.NationalInsuranceNumberYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case Nino                      =>
        soleTraderRoutes.SubNationalInsuranceNumberController.onPageLoadWithId(flowId, FinalValidationMode)
      case WorksReferenceNumberYesNo =>
        soleTraderRoutes.WorksReferenceNumberYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case WorksReferenceNumber      =>
        soleTraderRoutes.WorksReferenceNumberController.onPageLoadWithId(flowId, FinalValidationMode)
      case _                         =>
        logger.error(s"[FinalValidationNavigator][soleTraderStartPage] - scoped fallback, target=$target")
        recovery
    }

  private def companyStartPage(
    target: FinalValidationChangeTarget,
    flowId: UUID
  ): Call =
    target match {
      case TradingName               =>
        companyRoutes.CompanyNameController.onPageLoadWithId(flowId, FinalValidationMode)
      case AddressYesNo              =>
        companyRoutes.CompanyAddressYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case Address                   =>
        companyRoutes.CompanyAddressController.redirectToAddressLookupWithId(flowId, FinalValidationMode, None)
      case ContactDetailsYesNo       =>
        companyRoutes.AddCompanyContactMethodsYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case EmailAddress              =>
        companyRoutes.CompanyEmailAddressController.onPageLoadWithId(flowId, FinalValidationMode)
      case PhoneNumber               =>
        companyRoutes.CompanyPhoneNumberController.onPageLoadWithId(flowId, FinalValidationMode)
      case MobilePhoneNumber         =>
        companyRoutes.CompanyMobileNumberController.onPageLoadWithId(flowId, FinalValidationMode)
      case UtrYesNo                  =>
        companyRoutes.CompanyUtrYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case Utr                       =>
        companyRoutes.CompanyUtrController.onPageLoadWithId(flowId, FinalValidationMode)
      case CrnYesNo                  =>
        companyRoutes.CompanyCrnYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case Crn                       =>
        companyRoutes.CompanyCrnController.onPageLoadWithId(flowId, FinalValidationMode)
      case WorksReferenceNumberYesNo =>
        companyRoutes.CompanyWorksReferenceYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case WorksReferenceNumber      =>
        companyRoutes.CompanyWorksReferenceController.onPageLoadWithId(flowId, FinalValidationMode)
      case _                         =>
        logger.error(s"[FinalValidationNavigator][companyStartPage] - scoped fallback, target=$target")
        recovery
    }

  private def partnershipStartPage(
    target: FinalValidationChangeTarget,
    flowId: UUID
  ): Call =
    target match {
      case PartnershipTradingName    =>
        partnershipRoutes.PartnershipNameController.onPageLoadWithId(flowId, FinalValidationMode)
      case TradingName               =>
        partnershipRoutes.PartnershipNominatedPartnerNameController.onPageLoadWithId(flowId, FinalValidationMode)
      case AddressYesNo              =>
        partnershipRoutes.PartnershipAddressYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case Address                   =>
        partnershipRoutes.PartnershipAddressController.redirectToAddressLookupWithId(flowId, FinalValidationMode, None)
      case ContactDetailsYesNo       =>
        partnershipRoutes.AddPartnershipContactMethodsYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case EmailAddress              =>
        partnershipRoutes.PartnershipEmailAddressController.onPageLoadWithId(flowId, FinalValidationMode)
      case PhoneNumber               =>
        partnershipRoutes.PartnershipPhoneNumberController.onPageLoadWithId(flowId, FinalValidationMode)
      case MobilePhoneNumber         =>
        partnershipRoutes.PartnershipMobileNumberController.onPageLoadWithId(flowId, FinalValidationMode)
      case UtrYesNo                  =>
        partnershipRoutes.PartnershipHasUtrYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case Utr                       =>
        partnershipRoutes.PartnershipUniqueTaxpayerReferenceController.onPageLoadWithId(flowId, FinalValidationMode)
      case PartnerUtrYesNo           =>
        partnershipRoutes.PartnershipNominatedPartnerUtrYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case PartnerUtr                =>
        partnershipRoutes.PartnershipNominatedPartnerUtrController.onPageLoadWithId(flowId, FinalValidationMode)
      case NinoYesNo                 =>
        partnershipRoutes.PartnershipNominatedPartnerNinoYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case Nino                      =>
        partnershipRoutes.PartnershipNominatedPartnerNinoController.onPageLoadWithId(flowId, FinalValidationMode)
      case CrnYesNo                  =>
        partnershipRoutes.PartnershipNominatedPartnerCrnYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case Crn                       =>
        partnershipRoutes.PartnershipNominatedPartnerCrnController.onPageLoadWithId(flowId, FinalValidationMode)
      case WorksReferenceNumberYesNo =>
        partnershipRoutes.PartnershipWorksReferenceNumberYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case WorksReferenceNumber      =>
        partnershipRoutes.PartnershipWorksReferenceNumberController.onPageLoadWithId(flowId, FinalValidationMode)
      case _                         =>
        logger.error(s"[FinalValidationNavigator][partnershipStartPage] - scoped fallback, target=$target")
        recovery
    }

  private def trustStartPage(
    target: FinalValidationChangeTarget,
    flowId: UUID
  ): Call =
    target match {
      case TradingName               =>
        trustRoutes.TrustNameController.onPageLoadWithId(flowId, FinalValidationMode)
      case AddressYesNo              =>
        trustRoutes.TrustAddressYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case Address                   =>
        trustRoutes.TrustAddressController.redirectToAddressLookupWithId(flowId, FinalValidationMode, None)
      case ContactDetailsYesNo       =>
        trustRoutes.AddTrustContactMethodsYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case EmailAddress              =>
        trustRoutes.TrustEmailAddressController.onPageLoadWithId(flowId, FinalValidationMode)
      case PhoneNumber               =>
        trustRoutes.TrustPhoneNumberController.onPageLoadWithId(flowId, FinalValidationMode)
      case MobilePhoneNumber         =>
        trustRoutes.TrustMobileNumberController.onPageLoadWithId(flowId, FinalValidationMode)
      case UtrYesNo                  =>
        trustRoutes.TrustUtrYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case Utr                       =>
        trustRoutes.TrustUtrController.onPageLoadWithId(flowId, FinalValidationMode)
      case WorksReferenceNumberYesNo =>
        trustRoutes.TrustWorksReferenceYesNoController.onPageLoadWithId(flowId, FinalValidationMode)
      case WorksReferenceNumber      =>
        trustRoutes.TrustWorksReferenceController.onPageLoadWithId(flowId, FinalValidationMode)
      case _                         =>
        logger.error(s"[FinalValidationNavigator][trustStartPage] - scoped fallback, target=$target")
        recovery
    }

  private def identifierYesNoNextPage(
    page: Page,
    userAnswers: UserAnswers,
    flowId: UUID
  ): Call =
    val id = flowId.toString

    page match {
      // Individual / sole trader
      case UniqueTaxpayerReferenceYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(UniqueTaxpayerReferenceYesNoPage(id, namespace)),
          soleTraderRoutes.SubcontractorsUniqueTaxpayerReferenceController
            .onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      case NationalInsuranceNumberYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(NationalInsuranceNumberYesNoPage(id, namespace)),
          soleTraderRoutes.SubNationalInsuranceNumberController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      case WorksReferenceNumberYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(WorksReferenceNumberYesNoPage(id, namespace)),
          soleTraderRoutes.WorksReferenceNumberController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      // Company
      case CompanyUtrYesNoPage(_, `namespace`)           =>
        yesNoNextPage(
          userAnswers.get(CompanyUtrYesNoPage(id, namespace)),
          companyRoutes.CompanyUtrController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      case CompanyCrnYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(CompanyCrnYesNoPage(id, namespace)),
          companyRoutes.CompanyCrnController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      case CompanyWorksReferenceYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(CompanyWorksReferenceYesNoPage(id, namespace)),
          companyRoutes.CompanyWorksReferenceController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      // Partnership
      case PartnershipHasUtrYesNoPage(_, `namespace`)     =>
        yesNoNextPage(
          userAnswers.get(PartnershipHasUtrYesNoPage(id, namespace)),
          partnershipRoutes.PartnershipUniqueTaxpayerReferenceController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      case PartnershipNominatedPartnerUtrYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(PartnershipNominatedPartnerUtrYesNoPage(id, namespace)),
          partnershipRoutes.PartnershipNominatedPartnerUtrController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      case PartnershipNominatedPartnerNinoYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(PartnershipNominatedPartnerNinoYesNoPage(id, namespace)),
          partnershipRoutes.PartnershipNominatedPartnerNinoController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      case PartnershipNominatedPartnerCrnYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(PartnershipNominatedPartnerCrnYesNoPage(id, namespace)),
          partnershipRoutes.PartnershipNominatedPartnerCrnController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      case PartnershipWorksReferenceNumberYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(PartnershipWorksReferenceNumberYesNoPage(id, namespace)),
          partnershipRoutes.PartnershipWorksReferenceNumberController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      // Trust
      case TrustUtrYesNoPage(_, `namespace`)                        =>
        yesNoNextPage(
          userAnswers.get(TrustUtrYesNoPage(id, namespace)),
          trustRoutes.TrustUtrController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      case TrustWorksReferenceYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(TrustWorksReferenceYesNoPage(id, namespace)),
          trustRoutes.TrustWorksReferenceController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      case _ =>
        complete(flowId)
    }

  private def individualNamesNextPage(
    page: Page,
    userAnswers: UserAnswers,
    flowId: UUID
  ): Call = {

    val id = flowId.toString

    val selected =
      userAnswers
        .get(IndividualNamesOptionsPage(id, namespace))
        .getOrElse(Set.empty)

    page match {
      case IndividualNamesOptionsPage(_, `namespace`) =>
        if (selected.contains(IndividualNamesOptions.SubcontractorName)) {
          soleTraderRoutes.SubcontractorNameController.onPageLoadWithId(flowId, FinalValidationMode)
        } else if (selected.contains(IndividualNamesOptions.TradingName)) {
          soleTraderRoutes.TradingNameOfSubcontractorController.onPageLoadWithId(flowId, FinalValidationMode)
        } else {
          logger.error("[FinalValidationNavigator][individualNamesNextPage] - scoped names option missing")
          recovery
        }

      case SubcontractorNamePage(_, `namespace`) =>
        if (selected.contains(IndividualNamesOptions.TradingName)) {
          soleTraderRoutes.TradingNameOfSubcontractorController.onPageLoadWithId(flowId, FinalValidationMode)
        } else {
          complete(flowId)
        }

      case TradingNameOfSubcontractorPage(_, `namespace`) =>
        complete(flowId)

      case _ =>
        logger.error(
          s"[FinalValidationNavigator][individualNamesNextPage] - unexpected scoped page=$page"
        )
        recovery
    }
  }

  private def addressYesNoNextPage(
    page: Page,
    userAnswers: UserAnswers,
    flowId: UUID
  ): Call =
    val id = flowId.toString

    page match {
      case SubAddressYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(SubAddressYesNoPage(id, namespace)),
          soleTraderRoutes.AddressOfSubcontractorController
            .redirectToAddressLookupWithId(flowId, FinalValidationMode, None),
          flowId
        )

      case CompanyAddressYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(CompanyAddressYesNoPage(id, namespace)),
          companyRoutes.CompanyAddressController.redirectToAddressLookupWithId(flowId, FinalValidationMode, None),
          flowId
        )

      case PartnershipAddressYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(PartnershipAddressYesNoPage(id, namespace)),
          partnershipRoutes.PartnershipAddressController
            .redirectToAddressLookupWithId(flowId, FinalValidationMode, None),
          flowId
        )

      case TrustAddressYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(TrustAddressYesNoPage(id, namespace)),
          trustRoutes.TrustAddressController.redirectToAddressLookupWithId(flowId, FinalValidationMode, None),
          flowId
        )

      case _ =>
        logger.error(s"[FinalValidationNavigator][addressYesNoNextPage] - unexpected scoped page=$page")
        recovery
    }

  private def contactDetailsYesNoNextPage(
    page: Page,
    userAnswers: UserAnswers,
    flowId: UUID
  ): Call =
    val id = flowId.toString

    page match {
      // Individual / sole trader
      case AddIndividualContactMethodsYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(AddIndividualContactMethodsYesNoPage(id, namespace)),
          soleTraderRoutes.IndividualContactMethodOptionsController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      case IndividualContactMethodOptionsPage(_, `namespace`) =>
        nextSelectedIndividualContactMethodPageAfter(None, userAnswers, flowId)

      case IndividualEmailAddressPage(_, `namespace`) =>
        nextSelectedIndividualContactMethodPageAfter(Some(ContactMethodOptions.Email), userAnswers, flowId)

      case IndividualPhoneNumberPage(_, `namespace`) =>
        nextSelectedIndividualContactMethodPageAfter(Some(ContactMethodOptions.Phone), userAnswers, flowId)

      case IndividualMobileNumberPage(_, `namespace`)        =>
        nextSelectedIndividualContactMethodPageAfter(Some(ContactMethodOptions.Mobile), userAnswers, flowId)

      // Company
      case AddCompanyContactMethodsYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(AddCompanyContactMethodsYesNoPage(id, namespace)),
          companyRoutes.CompanyContactMethodOptionsController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      case CompanyContactMethodOptionsPage(_, `namespace`) =>
        nextSelectedCompanyContactMethodPageAfter(None, userAnswers, flowId)

      case CompanyEmailAddressPage(_, `namespace`) =>
        nextSelectedCompanyContactMethodPageAfter(Some(ContactMethodOptions.Email), userAnswers, flowId)

      case CompanyPhoneNumberPage(_, `namespace`) =>
        nextSelectedCompanyContactMethodPageAfter(Some(ContactMethodOptions.Phone), userAnswers, flowId)

      case CompanyMobileNumberPage(_, `namespace`)               =>
        nextSelectedCompanyContactMethodPageAfter(Some(ContactMethodOptions.Mobile), userAnswers, flowId)

      // Partnership
      case AddPartnershipContactMethodsYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(AddPartnershipContactMethodsYesNoPage(id, namespace)),
          partnershipRoutes.PartnershipContactMethodOptionsController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      case PartnershipContactMethodOptionsPage(_, `namespace`) =>
        nextSelectedPartnershipContactMethodPageAfter(None, userAnswers, flowId)

      case PartnershipEmailAddressPage(_, `namespace`) =>
        nextSelectedPartnershipContactMethodPageAfter(Some(ContactMethodOptions.Email), userAnswers, flowId)

      case PartnershipPhoneNumberPage(_, `namespace`) =>
        nextSelectedPartnershipContactMethodPageAfter(Some(ContactMethodOptions.Phone), userAnswers, flowId)

      case PartnershipMobileNumberPage(_, `namespace`)     =>
        nextSelectedPartnershipContactMethodPageAfter(Some(ContactMethodOptions.Mobile), userAnswers, flowId)

      // Trust
      case AddTrustContactMethodsYesNoPage(_, `namespace`) =>
        yesNoNextPage(
          userAnswers.get(AddTrustContactMethodsYesNoPage(id, namespace)),
          trustRoutes.TrustContactMethodOptionsController.onPageLoadWithId(flowId, FinalValidationMode),
          flowId
        )

      case TrustContactMethodOptionsPage(_, `namespace`) =>
        nextSelectedTrustContactMethodPageAfter(None, userAnswers, flowId)

      case TrustEmailAddressPage(_, `namespace`) =>
        nextSelectedTrustContactMethodPageAfter(Some(ContactMethodOptions.Email), userAnswers, flowId)

      case TrustPhoneNumberPage(_, `namespace`) =>
        nextSelectedTrustContactMethodPageAfter(Some(ContactMethodOptions.Phone), userAnswers, flowId)

      case TrustMobileNumberPage(_, `namespace`) =>
        nextSelectedTrustContactMethodPageAfter(Some(ContactMethodOptions.Mobile), userAnswers, flowId)

      case _ =>
        logger.error(
          s"[FinalValidationNavigator][contactDetailsYesNoNextPage] - unexpected scoped page=$page"
        )
        recovery
    }

  private def nextSelectedContactMethodPageAfter(
    current: Option[ContactMethodOptions],
    selectedContactMethods: Option[Seq[ContactMethodOptions]],
    flowId: UUID
  )(contactMethodPageCall: ContactMethodOptions => Call): Call =
    navigateFromContactMethodPage(current, selectedContactMethods) { remaining =>
      remaining.headOption.fold {
        complete(flowId)
      } { contactMethod =>
        contactMethodPageCall(contactMethod)
      }
    }

  private def nextSelectedIndividualContactMethodPageAfter(
    current: Option[ContactMethodOptions],
    userAnswers: UserAnswers,
    flowId: UUID
  ): Call =
    nextSelectedContactMethodPageAfter(
      current,
      userAnswers
        .get(IndividualContactMethodOptionsPage(flowId.toString, namespace))
        .map(ContactMethodOptions.ordered),
      flowId
    ) {
      case ContactMethodOptions.Email  =>
        soleTraderRoutes.IndividualEmailAddressController.onPageLoadWithId(flowId, FinalValidationMode)
      case ContactMethodOptions.Phone  =>
        soleTraderRoutes.IndividualPhoneNumberController.onPageLoadWithId(flowId, FinalValidationMode)
      case ContactMethodOptions.Mobile =>
        soleTraderRoutes.IndividualMobileNumberController.onPageLoadWithId(flowId, FinalValidationMode)
    }

  private def nextSelectedCompanyContactMethodPageAfter(
    current: Option[ContactMethodOptions],
    userAnswers: UserAnswers,
    flowId: UUID
  ): Call =
    nextSelectedContactMethodPageAfter(
      current,
      userAnswers
        .get(CompanyContactMethodOptionsPage(flowId.toString, namespace))
        .map(ContactMethodOptions.ordered),
      flowId
    ) {
      case ContactMethodOptions.Email  =>
        companyRoutes.CompanyEmailAddressController.onPageLoadWithId(flowId, FinalValidationMode)
      case ContactMethodOptions.Phone  =>
        companyRoutes.CompanyPhoneNumberController.onPageLoadWithId(flowId, FinalValidationMode)
      case ContactMethodOptions.Mobile =>
        companyRoutes.CompanyMobileNumberController.onPageLoadWithId(flowId, FinalValidationMode)
    }

  private def nextSelectedPartnershipContactMethodPageAfter(
    current: Option[ContactMethodOptions],
    userAnswers: UserAnswers,
    flowId: UUID
  ): Call =
    nextSelectedContactMethodPageAfter(
      current,
      userAnswers
        .get(PartnershipContactMethodOptionsPage(flowId.toString, namespace))
        .map(ContactMethodOptions.ordered),
      flowId
    ) {
      case ContactMethodOptions.Email  =>
        partnershipRoutes.PartnershipEmailAddressController.onPageLoadWithId(flowId, FinalValidationMode)
      case ContactMethodOptions.Phone  =>
        partnershipRoutes.PartnershipPhoneNumberController.onPageLoadWithId(flowId, FinalValidationMode)
      case ContactMethodOptions.Mobile =>
        partnershipRoutes.PartnershipMobileNumberController.onPageLoadWithId(flowId, FinalValidationMode)
    }

  private def nextSelectedTrustContactMethodPageAfter(
    current: Option[ContactMethodOptions],
    userAnswers: UserAnswers,
    flowId: UUID
  ): Call =
    nextSelectedContactMethodPageAfter(
      current,
      userAnswers
        .get(TrustContactMethodOptionsPage(flowId.toString, namespace))
        .map(ContactMethodOptions.ordered),
      flowId
    ) {
      case ContactMethodOptions.Email  =>
        trustRoutes.TrustEmailAddressController.onPageLoadWithId(flowId, FinalValidationMode)
      case ContactMethodOptions.Phone  =>
        trustRoutes.TrustPhoneNumberController.onPageLoadWithId(flowId, FinalValidationMode)
      case ContactMethodOptions.Mobile =>
        trustRoutes.TrustMobileNumberController.onPageLoadWithId(flowId, FinalValidationMode)
    }
}

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

package navigation.add

import javax.inject.{Inject, Singleton}
import navigation.NavigatorForJourney
import controllers.routes
import models.add.IndividualNamesOptions.{SubcontractorName, TradingName}
import models.amend.AmendIndividualRemoveDetail
import models.contact.ContactMethodOptions
import models.{AmendMode, CheckMode, Mode, NormalMode, UserAnswers}
import pages.{Page, QuestionPage}
import pages.add.*
import play.api.mvc.Call

import java.util.UUID

@Singleton
class IndividualNavigator @Inject() () extends NavigatorForJourney {

//  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
//    case NormalMode =>
//      normalRoutes(page)(userAnswers)
//    case CheckMode  =>
//      checkRouteMap(page)(userAnswers)
//    case AmendMode  =>
//      amendRouteMap(page)(userAnswers)
//  }

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    extractId(page) match {
      case Some(id) =>
        val flowId = UUID.fromString(id)
        mode match {
          case NormalMode => normalRoutesWithId(flowId, id)(page)(userAnswers)
          case CheckMode  => checkRouteMapWithId(flowId, id)(page)(userAnswers)
          case _          => nextPageWithoutId(page, mode, userAnswers)
        }
      case None     => nextPageWithoutId(page, mode, userAnswers)
    }

  private def nextPageWithoutId(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
    case AmendMode  =>
      amendRouteMap(page)(userAnswers)
  }

  private def extractId(page: Page): Option[String] =
    page match {
      case IndividualNamesOptionsPage(id)                => Some(id)
      case TradingNameOfSubcontractorPage(id)            => Some(id)
      case SubcontractorNamePage(id)                     => Some(id)
      case AddressOfSubcontractorPage(id)                => Some(id)
      case SubAddressYesNoPage(id)                       => Some(id)
      case IndividualContactMethodOptionsPage(id)        => Some(id)
      case AddIndividualContactMethodsYesNoPage(id)      => Some(id)
      case IndividualEmailAddressPage(id)                => Some(id)
      case IndividualPhoneNumberPage(id)                 => Some(id)
      case IndividualMobileNumberPage(id)                => Some(id)
      case SubcontractorsUniqueTaxpayerReferencePage(id) => Some(id)
      case UniqueTaxpayerReferenceYesNoPage(id)          => Some(id)
      case SubNationalInsuranceNumberPage(id)            => Some(id)
      case NationalInsuranceNumberYesNoPage(id)          => Some(id)
      case WorksReferenceNumberPage(id)                  => Some(id)
      case WorksReferenceNumberYesNoPage(id)             => Some(id)
      case _                                             => None
    }

  private def normalRoutesWithId(flowId: UUID, id: String): Page => UserAnswers => Call = {
    case IndividualNamesOptionsPage(_)             =>
      userAnswers => navigatorFromIndividualNamesOptionsPage(flowId, id, NormalMode)(userAnswers)
    case SubcontractorNamePage(_)                  =>
      userAnswers => navigatorFromSubcontractorNamePage(flowId, id, NormalMode)(userAnswers)
    case TradingNameOfSubcontractorPage(_)         =>
      _ => controllers.add.routes.SubAddressYesNoController.onPageLoadWithId(flowId, NormalMode)
    case SubAddressYesNoPage(_)                    => userAnswers => navigatorFromSubAddressYesNoPage(flowId, id, NormalMode)(userAnswers)
    case UniqueTaxpayerReferenceYesNoPage          =>
      userAnswers => navigatorFromUniqueTaxpayerReferenceYesNoPage(NormalMode)(userAnswers)
    case SubcontractorsUniqueTaxpayerReferencePage =>
      _ => controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode)
    case NationalInsuranceNumberYesNoPage          =>
      userAnswers => navigatorFromNationalInsuranceNumberYesNoPage(NormalMode)(userAnswers)
    case SubNationalInsuranceNumberPage            =>
      _ => controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(NormalMode)
    case WorksReferenceNumberYesNoPage             =>
      userAnswers => navigatorFromWorksReferenceNumberYesNoPage(NormalMode)(userAnswers)
    case WorksReferenceNumberPage                  =>
      _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case AddIndividualContactMethodsYesNoPage      =>
      userAnswers => navigatorFromAddIndividualContactMethodsYesNoPage(NormalMode)(userAnswers)
    case IndividualContactMethodOptionsPage        =>
      userAnswers => nextSelectedContactMethodPageAfter(current = None)(userAnswers)
    case IndividualMobileNumberPage                =>
      userAnswers => nextSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Mobile))(userAnswers)
    case IndividualPhoneNumberPage                 =>
      userAnswers => nextSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Phone))(userAnswers)
    case IndividualEmailAddressPage                =>
      userAnswers => nextSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Email))(userAnswers)
    case _                                         => _ => routes.IndexController.onPageLoad()
  }

  private def checkRouteMapWithId(flowId: UUID, id: String): Page => UserAnswers => Call = {
    case IndividualNamesOptionsPage(_)        =>
      userAnswers => navigatorFromIndividualNamesOptionsPage(flowId, id, CheckMode)(userAnswers)
    case SubcontractorNamePage(_)             =>
      userAnswers => navigatorFromSubcontractorNamePage(flowId, id, CheckMode)(userAnswers)
    case SubAddressYesNoPage(_)               => navigatorFromSubAddressYesNoPage(flowId, id, CheckMode)(_)
    case NationalInsuranceNumberYesNoPage     => navigatorFromNationalInsuranceNumberYesNoPage(CheckMode)(_)
    case UniqueTaxpayerReferenceYesNoPage     => navigatorFromUniqueTaxpayerReferenceYesNoPage(CheckMode)(_)
    case WorksReferenceNumberYesNoPage        => navigatorFromWorksReferenceNumberYesNoPage(CheckMode)(_)
    case AddIndividualContactMethodsYesNoPage =>
      userAnswers => navigatorFromAddIndividualContactMethodsYesNoPage(CheckMode)(userAnswers)
    case IndividualContactMethodOptionsPage   =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(current = None)(userAnswers)
    case IndividualEmailAddressPage           =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Email))(userAnswers)
    case IndividualPhoneNumberPage            =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Phone))(userAnswers)
    case IndividualMobileNumberPage           =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Mobile))(userAnswers)
    case _                                    => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
  }

  // TODO: NEED TO WORK ON IndividualCheckYourAnswersController before using this inside the normalRoutesWithId? and inside checkRouteMapWithId
//  private def cyaRouteWithId(flowId: UUID): Call =
//    controllers.info.routes.IndividualCheckYourAnswersController.onPageLoadWithId(flowId)

  private def cyaRoute(mode: Mode): Call = mode match {
    case AmendMode => controllers.amend.routes.AmendIndividualCheckYourAnswersController.onPageLoad()
    case _         => controllers.add.routes.CheckYourAnswersController.onPageLoad()
  }

  private val normalRoutes: Page => UserAnswers => Call = {
    case IndividualNamesOptionsPage                => userAnswers => navigatorFromIndividualNamesOptionsPage(NormalMode)(userAnswers)
    case SubcontractorNamePage                     => userAnswers => navigatorFromSubcontractorNamePage(NormalMode)(userAnswers)
    case TradingNameOfSubcontractorPage            => _ => controllers.add.routes.SubAddressYesNoController.onPageLoad(NormalMode)
    case SubAddressYesNoPage                       =>
      userAnswers => navigatorFromSubAddressYesNoPage(NormalMode)(userAnswers)
    case UniqueTaxpayerReferenceYesNoPage          =>
      userAnswers => navigatorFromUniqueTaxpayerReferenceYesNoPage(NormalMode)(userAnswers)
    case SubcontractorsUniqueTaxpayerReferencePage =>
      _ => controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode)
    case NationalInsuranceNumberYesNoPage          =>
      userAnswers => navigatorFromNationalInsuranceNumberYesNoPage(NormalMode)(userAnswers)
    case SubNationalInsuranceNumberPage            =>
      _ => controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(NormalMode)
    case WorksReferenceNumberYesNoPage             =>
      userAnswers => navigatorFromWorksReferenceNumberYesNoPage(NormalMode)(userAnswers)
    case WorksReferenceNumberPage                  =>
      _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
    case AddIndividualContactMethodsYesNoPage      =>
      userAnswers => navigatorFromAddIndividualContactMethodsYesNoPage(NormalMode)(userAnswers)
    case IndividualContactMethodOptionsPage        =>
      userAnswers => nextSelectedContactMethodPageAfter(current = None)(userAnswers)
    case IndividualMobileNumberPage                =>
      userAnswers => nextSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Mobile))(userAnswers)
    case IndividualPhoneNumberPage                 =>
      userAnswers => nextSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Phone))(userAnswers)
    case IndividualEmailAddressPage                =>
      userAnswers => nextSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Email))(userAnswers)
    case _                                         => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case IndividualNamesOptionsPage           => userAnswers => navigatorFromIndividualNamesOptionsPage(CheckMode)(userAnswers)
    case SubcontractorNamePage                => userAnswers => navigatorFromSubcontractorNamePage(CheckMode)(userAnswers)
    case SubAddressYesNoPage                  => navigatorFromSubAddressYesNoPage(CheckMode)(_)
    case NationalInsuranceNumberYesNoPage     => navigatorFromNationalInsuranceNumberYesNoPage(CheckMode)(_)
    case UniqueTaxpayerReferenceYesNoPage     => navigatorFromUniqueTaxpayerReferenceYesNoPage(CheckMode)(_)
    case WorksReferenceNumberYesNoPage        => navigatorFromWorksReferenceNumberYesNoPage(CheckMode)(_)
    case AddIndividualContactMethodsYesNoPage =>
      userAnswers => navigatorFromAddIndividualContactMethodsYesNoPage(CheckMode)(userAnswers)
    case IndividualContactMethodOptionsPage   =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(current = None)(userAnswers)
    case IndividualEmailAddressPage           =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Email))(userAnswers)
    case IndividualPhoneNumberPage            =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Phone))(userAnswers)
    case IndividualMobileNumberPage           =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Mobile))(userAnswers)
    case _                                    => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
  }

  private val amendRouteMap: Page => UserAnswers => Call = {
    case IndividualNamesOptionsPage           => navigatorFromIndividualNamesOptionsPage(AmendMode)(_)
    case SubcontractorNamePage                => navigatorFromSubcontractorNamePage(AmendMode)(_)
    case SubAddressYesNoPage                  => navigatorFromSubAddressYesNoPage(AmendMode)(_)
    case UniqueTaxpayerReferenceYesNoPage     => navigatorFromUniqueTaxpayerReferenceYesNoPage(AmendMode)(_)
    case WorksReferenceNumberYesNoPage        => navigatorFromWorksReferenceNumberYesNoPage(AmendMode)(_)
    case NationalInsuranceNumberYesNoPage     => navigatorFromNationalInsuranceNumberYesNoPage(AmendMode)(_)
    case IndividualContactMethodOptionsPage   =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(current = None, mode = AmendMode)(userAnswers)
    case IndividualEmailAddressPage           =>
      userAnswers =>
        nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Email), mode = AmendMode)(
          userAnswers
        )
    case IndividualPhoneNumberPage            =>
      userAnswers =>
        nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Phone), mode = AmendMode)(
          userAnswers
        )
    case IndividualMobileNumberPage           =>
      userAnswers =>
        nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Mobile), mode = AmendMode)(
          userAnswers
        )
    case AddIndividualContactMethodsYesNoPage => navigatorFromAddIndividualContactMethodsYesNoPage(AmendMode)(_)
    case _                                    => _ => cyaRoute(AmendMode)
  }

  private def navigatorFromIndividualNamesOptionsPage(flowId: UUID, id: String, mode: Mode)(
    ua: UserAnswers
  ): Call =
    (ua.get(IndividualNamesOptionsPage(id)), mode) match {
      case (Some(nameOptions), NormalMode) if nameOptions.contains(SubcontractorName) =>
        controllers.add.routes.SubcontractorNameController.onPageLoadWithId(flowId, NormalMode)

      case (Some(nameOptions), NormalMode) if nameOptions.contains(TradingName) =>
        controllers.add.routes.TradingNameOfSubcontractorController.onPageLoadWithId(flowId, NormalMode)

      case (Some(nameOptions), CheckMode)
          if nameOptions.contains(SubcontractorName) && ua.get(SubcontractorNamePage(id)).isEmpty =>
        controllers.add.routes.SubcontractorNameController.onPageLoadWithId(flowId, CheckMode)

      case (Some(nameOptions), CheckMode)
          if nameOptions.contains(TradingName) && ua.get(TradingNameOfSubcontractorPage(id)).isEmpty =>
        controllers.add.routes.TradingNameOfSubcontractorController.onPageLoadWithId(flowId, CheckMode)

      case (Some(_), CheckMode) =>
        controllers.add.routes.CheckYourAnswersController.onPageLoad()

      case _ =>
        logger.error(
          s"[IndividualNavigator][navigatorFromIndividualNamesOptionsPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromIndividualNamesOptionsPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(IndividualNamesOptionsPage), mode) match {
      case (Some(nameOptions), NormalMode) if nameOptions.contains(SubcontractorName) =>
        controllers.add.routes.SubcontractorNameController.onPageLoad(NormalMode)

      case (Some(nameOptions), NormalMode) if nameOptions.contains(TradingName) =>
        controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(NormalMode)

      case (Some(nameOptions), AmendMode)
          if nameOptions.contains(SubcontractorName)
            && !nameOptions.contains(TradingName)
            && ua.get(SubcontractorNamePage).isDefined
            && ua.get(TradingNameOfSubcontractorPage).isDefined =>
        controllers.amend.routes.AmendIndividualRemoveDetailYesNoController
          .onPageLoad(AmendIndividualRemoveDetail.TradingName.key)

      case (Some(nameOptions), AmendMode)
          if nameOptions.contains(TradingName)
            && !nameOptions.contains(SubcontractorName)
            && ua.get(SubcontractorNamePage).isDefined
            && ua.get(TradingNameOfSubcontractorPage).isDefined =>
        controllers.amend.routes.AmendIndividualRemoveDetailYesNoController
          .onPageLoad(AmendIndividualRemoveDetail.SubcontractorName.key)

      case (Some(nameOptions), CheckMode | AmendMode)
          if nameOptions.contains(SubcontractorName) && ua.get(SubcontractorNamePage).isEmpty =>
        controllers.add.routes.SubcontractorNameController.onPageLoad(mode)

      case (Some(nameOptions), CheckMode | AmendMode)
          if nameOptions.contains(TradingName) && ua.get(TradingNameOfSubcontractorPage).isEmpty =>
        controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(mode)

      case (Some(nameOptions), CheckMode) =>
        controllers.add.routes.CheckYourAnswersController.onPageLoad()

      case (Some(nameOptions), AmendMode) =>
        controllers.amend.routes.AmendIndividualCheckYourAnswersController.onPageLoad()

      case _ =>
        logger.error(s"[IndividualNavigator][navigatorFromIndividualNamesOptionsPage] - unexpected state, mode=$mode")
        routes.JourneyRecoveryController.onPageLoad()

    }

  private def navigatorFromSubcontractorNamePage(flowId: UUID, id: String, mode: Mode)(ua: UserAnswers): Call =
    (ua.get(IndividualNamesOptionsPage(id)), mode) match {
      case (Some(nameOptions), NormalMode) if nameOptions.contains(TradingName) =>
        controllers.add.routes.TradingNameOfSubcontractorController.onPageLoadWithId(flowId, NormalMode)

      case (Some(_), NormalMode) =>
        controllers.add.routes.SubAddressYesNoController.onPageLoadWithId(flowId, NormalMode)

      case (Some(nameOptions), CheckMode)
          if nameOptions.contains(TradingName) && ua.get(TradingNameOfSubcontractorPage(id)).isEmpty =>
        controllers.add.routes.TradingNameOfSubcontractorController.onPageLoadWithId(flowId, mode)

      case (Some(_), CheckMode) =>
        controllers.add.routes.CheckYourAnswersController.onPageLoad()

      case _ =>
        logger.error(s"[IndividualNavigator][navigatorFromSubcontractorNamePage] - unexpected state, mode=$mode")
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromSubcontractorNamePage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(IndividualNamesOptionsPage), mode) match {

      case (Some(nameOptions), NormalMode) if nameOptions.contains(TradingName) =>
        controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(NormalMode)

      case (Some(_), NormalMode) =>
        controllers.add.routes.SubAddressYesNoController.onPageLoad(NormalMode)

      case (Some(nameOptions), CheckMode | AmendMode)
          if nameOptions.contains(TradingName) && ua.get(TradingNameOfSubcontractorPage).isEmpty =>
        controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(mode)

      case (Some(nameOptions), CheckMode) =>
        controllers.add.routes.CheckYourAnswersController.onPageLoad()

      case (Some(nameOptions), AmendMode) =>
        controllers.amend.routes.AmendIndividualCheckYourAnswersController.onPageLoad()

      case _ =>
        logger.error(s"[IndividualNavigator][navigatorFromSubcontractorNamePage] - unexpected state, mode=$mode")
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromSubAddressYesNoPage(flowId: UUID, id: String, mode: Mode)(ua: UserAnswers): Call =
    addressLookupYesNoRoute(
      mode,
      ua.get(SubAddressYesNoPage(id)),
      ua.get(AddressOfSubcontractorPage(id)).isDefined,
      onYes = controllers.add.routes.AddressOfSubcontractorController.redirectToAddressLookupWithId(flowId, None),
      onYesChange = controllers.add.routes.AddressOfSubcontractorController
        .redirectToAddressLookupWithId(flowId, Some(CheckMode.toString)),
      onNo = controllers.add.routes.AddIndividualContactMethodsYesNoController
        .onPageLoadWithId(flowId, NormalMode),
      checkYourAnswers = controllers.add.routes.CheckYourAnswersController.onPageLoad() // TODO: cyaRouteWithId method
    )

  private def navigatorFromSubAddressYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    mode match {
      case AmendMode =>
        ua.get(SubAddressYesNoPage) match {
          case Some(true)  =>
            controllers.add.routes.AddressOfSubcontractorController.redirectToAmendAddressLookup()
          case Some(false) =>
            cyaRoute(mode)
          case None        =>
            logger.error(
              "[IndividualNavigator][navigatorFromSubAddressYesNoPage] - SubAddressYesNoPage missing from userAnswers"
            )
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
      case _         =>
        addressLookupYesNoRoute(
          mode,
          ua.get(SubAddressYesNoPage),
          ua.get(AddressOfSubcontractorPage).isDefined,
          onYes = controllers.add.routes.AddressOfSubcontractorController.redirectToAddressLookup(NormalMode, None),
          onYesChange = controllers.add.routes.AddressOfSubcontractorController
            .redirectToAddressLookup(NormalMode, Some(CheckMode.toString)),
          onNo = controllers.add.routes.AddIndividualContactMethodsYesNoController.onPageLoad(NormalMode),
          checkYourAnswers = controllers.add.routes.CheckYourAnswersController.onPageLoad()
        )
    }

  private def navigatorFromNationalInsuranceNumberYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(NationalInsuranceNumberYesNoPage), mode) match {

      case (Some(true), NormalMode)             =>
        controllers.add.routes.SubNationalInsuranceNumberController.onPageLoad(NormalMode)
      case (Some(false), NormalMode)            =>
        controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(NormalMode)
      case (Some(true), CheckMode | AmendMode)  =>
        ua.get(SubNationalInsuranceNumberPage)
          .fold(controllers.add.routes.SubNationalInsuranceNumberController.onPageLoad(mode)) { _ =>
            cyaRoute(mode)
          }
      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)
      case _                                    =>
        logger.error(
          s"[IndividualNavigator][navigatorFromNationalInsuranceNumberYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromUniqueTaxpayerReferenceYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(UniqueTaxpayerReferenceYesNoPage), mode) match {
      case (Some(true), NormalMode) =>
        controllers.add.routes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode)

      case (Some(true), CheckMode | AmendMode) =>
        ua.get(SubcontractorsUniqueTaxpayerReferencePage)
          .fold(controllers.add.routes.SubcontractorsUniqueTaxpayerReferenceController.onPageLoad(mode)) { _ =>
            cyaRoute(mode)
          }

      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case _ =>
        logger.error(
          s"[IndividualNavigator][navigatorFromUniqueTaxpayerReferenceYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromWorksReferenceNumberYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(WorksReferenceNumberYesNoPage), mode) match {

      case (Some(true), NormalMode) =>
        controllers.add.routes.WorksReferenceNumberController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.routes.CheckYourAnswersController.onPageLoad()

      case (Some(true), CheckMode | AmendMode) =>
        ua.get(WorksReferenceNumberPage)
          .fold(controllers.add.routes.WorksReferenceNumberController.onPageLoad(mode)) { _ =>
            cyaRoute(mode)
          }

      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case _ =>
        logger.error(
          s"[IndividualNavigator][navigatorFromWorksReferenceNumberYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromAddIndividualContactMethodsYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(AddIndividualContactMethodsYesNoPage), mode) match {

      case (Some(true), NormalMode) =>
        controllers.add.routes.IndividualContactMethodOptionsController.onPageLoad(mode)

      case (Some(false), NormalMode) =>
        controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)

      case (Some(true), CheckMode | AmendMode) =>
        ua
          .get(IndividualContactMethodOptionsPage)
          .fold(controllers.add.routes.IndividualContactMethodOptionsController.onPageLoad(mode)) { _ =>
            cyaRoute(mode)
          }

      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case _ =>
        logger.error(
          s"[IndividualNavigator][navigatorFromAddIndividualContactMethodsYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def nextSelectedContactMethodPageAfter(
    current: Option[ContactMethodOptions]
  )(userAnswers: UserAnswers): Call =
    navigateFromContactMethodPage(current, userAnswers) { remaining =>
      remaining.headOption.fold {
        controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(NormalMode)
      }(contactMethodPageCall(_, NormalMode))
    }

  private def nextMissingSelectedContactMethodPageAfter(
    current: Option[ContactMethodOptions],
    mode: Mode = CheckMode
  )(userAnswers: UserAnswers): Call =
    navigateFromContactMethodPage(current, userAnswers) { remaining =>
      remaining
        .find(isMissingAnswer(_)(userAnswers))
        .map(contactMethodPageCall(_, mode))
        .getOrElse(
          cyaRoute(mode)
        )
    }

  private def navigateFromContactMethodPage(
    current: Option[ContactMethodOptions],
    userAnswers: UserAnswers
  )(terminalStep: Seq[ContactMethodOptions] => Call): Call =
    selectedContactMethodsInOrder(userAnswers)
      .filter(_.nonEmpty)
      .fold {
        logger.error(
          "[IndividualNavigator][navigateFromContactMethodPage] - ContactMethodOptions missing or empty"
        )
        routes.JourneyRecoveryController.onPageLoad()
      } { selectedContactMethods =>
        current match {
          case Some(currentContactMethod) if !selectedContactMethods.contains(currentContactMethod) =>
            logger.error(
              "[IndividualNavigator][navigateFromContactMethodPage] - current contact method not selected"
            )
            routes.JourneyRecoveryController.onPageLoad()

          case _ =>
            val remaining: Seq[ContactMethodOptions] =
              current match {
                case None =>
                  selectedContactMethods

                case Some(currentContactMethod) =>
                  val currentIndex = selectedContactMethods.indexWhere(_ == currentContactMethod)
                  selectedContactMethods.drop(currentIndex + 1)
              }

            terminalStep(remaining)
        }
      }

  private def selectedContactMethodsInOrder(userAnswers: UserAnswers): Option[Seq[ContactMethodOptions]] =
    userAnswers.get(IndividualContactMethodOptionsPage).map {
      ContactMethodOptions.ordered
    }

  private def contactMethodPageCall(contactMethod: ContactMethodOptions, mode: Mode): Call =
    contactMethod match {
      case ContactMethodOptions.Email  =>
        controllers.add.routes.IndividualEmailAddressController.onPageLoad(mode)
      case ContactMethodOptions.Phone  =>
        controllers.add.routes.IndividualPhoneNumberController.onPageLoad(mode)
      case ContactMethodOptions.Mobile =>
        controllers.add.routes.IndividualMobileNumberController.onPageLoad(mode)
    }

  private def contactMethodPage(contactMethod: ContactMethodOptions): QuestionPage[String] =
    contactMethod match {
      case ContactMethodOptions.Email  => IndividualEmailAddressPage
      case ContactMethodOptions.Phone  => IndividualPhoneNumberPage
      case ContactMethodOptions.Mobile => IndividualMobileNumberPage
    }

  private def isMissingAnswer(contactMethod: ContactMethodOptions)(userAnswers: UserAnswers): Boolean =
    userAnswers.get(contactMethodPage(contactMethod)).isEmpty
}

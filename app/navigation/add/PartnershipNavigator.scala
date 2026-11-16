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

import controllers.routes
import models.contact.ContactMethodOptions
import models.{AmendMode, CheckMode, Mode, NormalMode, UserAnswers}
import navigation.NavigatorForJourney
import pages.{Page, QuestionPage}
import pages.add.partnership.*
import play.api.mvc.Call

import java.util.UUID
import javax.inject.{Inject, Singleton}

@Singleton
class PartnershipNavigator @Inject() () extends NavigatorForJourney {

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

  private def extractId(page: Page): Option[String] =
    page match {
      case PartnershipNamePage(id)                      => Some(id)
      case PartnershipAddressYesNoPage(id)              => Some(id)
      case PartnershipAddressPage(id)                   => Some(id)
      case AddPartnershipContactMethodsYesNoPage(id)    => Some(id)
      case PartnershipContactMethodOptionsPage(id)      => Some(id)
      case PartnershipEmailAddressPage(id)              => Some(id)
      case PartnershipPhoneNumberPage(id)               => Some(id)
      case PartnershipMobileNumberPage(id)              => Some(id)
      case PartnershipHasUtrYesNoPage(id)               => Some(id)
      case PartnershipUniqueTaxpayerReferencePage(id)   => Some(id)
      case PartnershipNominatedPartnerNamePage(id)      => Some(id)
      case PartnershipNominatedPartnerUtrYesNoPage(id)  => Some(id)
      case PartnershipNominatedPartnerUtrPage(id)       => Some(id)
      case PartnershipNominatedPartnerNinoYesNoPage(id) => Some(id)
      case PartnershipNominatedPartnerNinoPage(id)      => Some(id)
      case PartnershipNominatedPartnerCrnYesNoPage(id)  => Some(id)
      case PartnershipNominatedPartnerCrnPage(id)       => Some(id)
      case PartnershipWorksReferenceNumberYesNoPage(id) => Some(id)
      case PartnershipWorksReferenceNumberPage(id)      => Some(id)
      case _                                            => None
    }

  private def cyaRouteWithId(flowId: UUID): Call =
    controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoadWithId(flowId)

  private def normalRoutesWithId(flowId: UUID, id: String): Page => UserAnswers => Call = {
    case PartnershipNamePage(_)                      =>
      _ => controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoadWithId(flowId, NormalMode)
    case PartnershipAddressYesNoPage(_)              =>
      userAnswers => navigatorFromPartnershipAddressYesNoPage(flowId, id, NormalMode)(userAnswers)
    case AddPartnershipContactMethodsYesNoPage(_)    =>
      userAnswers => navigatorFromAddPartnershipContactMethodsYesNoPage(flowId, id, NormalMode)(userAnswers)
    case PartnershipContactMethodOptionsPage(_)      =>
      userAnswers => nextSelectedContactMethodPageAfter(flowId, id, current = None)(userAnswers)
    case PartnershipEmailAddressPage(_)              =>
      userAnswers =>
        nextSelectedContactMethodPageAfter(flowId, id, current = Some(ContactMethodOptions.Email))(userAnswers)
    case PartnershipPhoneNumberPage(_)               =>
      userAnswers =>
        nextSelectedContactMethodPageAfter(flowId, id, current = Some(ContactMethodOptions.Phone))(userAnswers)
    case PartnershipMobileNumberPage(_)              =>
      userAnswers =>
        nextSelectedContactMethodPageAfter(flowId, id, current = Some(ContactMethodOptions.Mobile))(userAnswers)
    case PartnershipHasUtrYesNoPage(_)               =>
      userAnswers => navigatorFromPartnershipHasUtrYesNoPage(flowId, id, NormalMode)(userAnswers)
    case PartnershipUniqueTaxpayerReferencePage(_)   =>
      _ =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerNameController
          .onPageLoadWithId(flowId, NormalMode)
    case PartnershipNominatedPartnerNamePage(_)      =>
      _ =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerUtrYesNoController
          .onPageLoadWithId(flowId, NormalMode)
    case PartnershipWorksReferenceNumberYesNoPage(_) =>
      userAnswers => navigatorFromPartnershipWorksReferenceNumberYesNoPage(flowId, id, NormalMode)(userAnswers)
    case PartnershipNominatedPartnerNinoPage(_)      =>
      userAnswers => navigatorFromPartnershipNominatedPartnerNinoPage(flowId, id, NormalMode)(userAnswers)
    case PartnershipWorksReferenceNumberPage(_)      =>
      _ => cyaRouteWithId(flowId)
    case PartnershipNominatedPartnerCrnPage(_)       =>
      userAnswers => navigatorFromPartnershipNominatedPartnerCrnPage(flowId, id, NormalMode)(userAnswers)
    case PartnershipNominatedPartnerNinoYesNoPage(_) =>
      userAnswers => navigatorFromPartnershipNominatedPartnerNinoYesNoPage(flowId, id, NormalMode)(userAnswers)
    case PartnershipNominatedPartnerCrnYesNoPage(_)  =>
      userAnswers => navigatorFromPartnershipNominatedPartnerCrnYesNoPage(flowId, id, NormalMode)(userAnswers)
    case PartnershipNominatedPartnerUtrPage(_)       =>
      _ =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController
          .onPageLoadWithId(flowId, NormalMode)
    case PartnershipNominatedPartnerUtrYesNoPage(_)  =>
      userAnswers => navigatorFromPartnershipNominatedPartnerUtrYesNoPage(flowId, id, NormalMode)(userAnswers)
    case _                                           => _ => routes.IndexController.onPageLoad()
  }

  private def checkRouteMapWithId(flowId: UUID, id: String): Page => UserAnswers => Call = {
    case PartnershipHasUtrYesNoPage(_)               =>
      navigatorFromPartnershipHasUtrYesNoPage(flowId, id, CheckMode)(_)
    case PartnershipWorksReferenceNumberYesNoPage(_) =>
      navigatorFromPartnershipWorksReferenceNumberYesNoPage(flowId, id, CheckMode)(_)
    case PartnershipNominatedPartnerCrnYesNoPage(_)  =>
      navigatorFromPartnershipNominatedPartnerCrnYesNoPage(flowId, id, CheckMode)(_)
    case PartnershipNamePage(_)                      =>
      _ => cyaRouteWithId(flowId)
    case PartnershipAddressYesNoPage(_)              =>
      userAnswers => navigatorFromPartnershipAddressYesNoPage(flowId, id, CheckMode)(userAnswers)
    case AddPartnershipContactMethodsYesNoPage(_)    =>
      userAnswers => navigatorFromAddPartnershipContactMethodsYesNoPage(flowId, id, CheckMode)(userAnswers)
    case PartnershipContactMethodOptionsPage(_)      =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(flowId, id, current = None, CheckMode)(userAnswers)
    case PartnershipEmailAddressPage(_)              =>
      userAnswers =>
        nextMissingSelectedContactMethodPageAfter(
          flowId,
          id,
          current = Some(ContactMethodOptions.Email),
          CheckMode
        )(
          userAnswers
        )
    case PartnershipPhoneNumberPage(_)               =>
      userAnswers =>
        nextMissingSelectedContactMethodPageAfter(
          flowId,
          id,
          current = Some(ContactMethodOptions.Phone),
          CheckMode
        )(
          userAnswers
        )
    case PartnershipMobileNumberPage(_)              =>
      userAnswers =>
        nextMissingSelectedContactMethodPageAfter(
          flowId,
          id,
          current = Some(ContactMethodOptions.Mobile),
          CheckMode
        )(
          userAnswers
        )
    case PartnershipNominatedPartnerNinoYesNoPage(_) =>
      userAnswers => navigatorFromPartnershipNominatedPartnerNinoYesNoPage(flowId, id, CheckMode)(userAnswers)
    case PartnershipNominatedPartnerNinoPage(_)      =>
      navigatorFromPartnershipNominatedPartnerNinoPage(flowId, id, CheckMode)(_)
    case PartnershipNominatedPartnerNamePage(_)      =>
      _ => cyaRouteWithId(flowId)
    case PartnershipNominatedPartnerUtrYesNoPage(_)  =>
      userAnswers => navigatorFromPartnershipNominatedPartnerUtrYesNoPage(flowId, id, CheckMode)(userAnswers)
    case PartnershipNominatedPartnerUtrPage(_)       =>
      _ => cyaRouteWithId(flowId)
    case PartnershipNominatedPartnerCrnPage(_)       =>
      navigatorFromPartnershipNominatedPartnerCrnPage(flowId, id, CheckMode)(_)
    case PartnershipWorksReferenceNumberPage(_)      =>
      _ => cyaRouteWithId(flowId)
    case PartnershipUniqueTaxpayerReferencePage(_)   =>
      _ => cyaRouteWithId(flowId)
    case _                                           => _ => cyaRouteWithId(flowId)
  }

  private def navigatorFromPartnershipNominatedPartnerNinoPage(
    flowId: UUID,
    id: String,
    mode: Mode
  )(ua: UserAnswers): Call =
    (ua.get(PartnershipNominatedPartnerNinoPage(id)), mode) match {
      case (Some(_), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController
          .onPageLoadWithId(flowId, NormalMode)
      case (Some(_), CheckMode)  => cyaRouteWithId(flowId)
      case _                     =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromPartnershipNominatedPartnerNinoPage] - answer missing, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipNominatedPartnerCrnPage(
    flowId: UUID,
    id: String,
    mode: Mode
  )(ua: UserAnswers): Call =
    (ua.get(PartnershipNominatedPartnerCrnPage(id)), mode) match {
      case (Some(_), NormalMode) =>
        controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController
          .onPageLoadWithId(flowId, NormalMode)
      case (Some(_), CheckMode)  => cyaRouteWithId(flowId)
      case _                     =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromPartnershipNominatedPartnerCrnPage] - answer missing, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipHasUtrYesNoPage(
    flowId: UUID,
    id: String,
    mode: Mode
  )(ua: UserAnswers): Call =
    (ua.get(PartnershipHasUtrYesNoPage(id)), mode) match {
      case (Some(true), CheckMode) if ua.get(PartnershipUniqueTaxpayerReferencePage(id)).isDefined =>
        cyaRouteWithId(flowId)
      case (Some(true), CheckMode)                                                                 =>
        controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController
          .onPageLoadWithId(flowId, mode)
      case (Some(false), CheckMode)                                                                => cyaRouteWithId(flowId)
      case (Some(true), NormalMode)                                                                =>
        controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController
          .onPageLoadWithId(flowId, NormalMode)
      case (Some(false), NormalMode)                                                               =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerNameController
          .onPageLoadWithId(flowId, NormalMode)
      case _                                                                                       =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromPartnershipHasUtrYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipWorksReferenceNumberYesNoPage(
    flowId: UUID,
    id: String,
    mode: Mode
  )(ua: UserAnswers): Call =
    (ua.get(PartnershipWorksReferenceNumberYesNoPage(id)), mode) match {
      case (Some(true), CheckMode)   =>
        ua.get(PartnershipWorksReferenceNumberPage(id))
          .fold(
            controllers.add.partnership.routes.PartnershipWorksReferenceNumberController
              .onPageLoadWithId(flowId, mode)
          )(_ => cyaRouteWithId(flowId))
      case (Some(false), CheckMode)  => cyaRouteWithId(flowId)
      case (Some(true), NormalMode)  =>
        controllers.add.partnership.routes.PartnershipWorksReferenceNumberController
          .onPageLoadWithId(flowId, NormalMode)
      case (Some(false), NormalMode) => cyaRouteWithId(flowId)
      case _                         =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromPartnershipWorksReferenceNumberYesNoPage] - unexpected, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipNominatedPartnerUtrYesNoPage(
    flowId: UUID,
    id: String,
    mode: Mode
  )(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipNominatedPartnerUtrYesNoPage(id)), mode) match {
      case (Some(true), NormalMode)  =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController
          .onPageLoadWithId(flowId, NormalMode)
      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController
          .onPageLoadWithId(flowId, NormalMode)
      case (Some(true), CheckMode)   =>
        userAnswers
          .get(PartnershipNominatedPartnerUtrPage(id))
          .fold(
            controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController
              .onPageLoadWithId(flowId, mode)
          )(_ => cyaRouteWithId(flowId))
      case (Some(false), CheckMode)  => cyaRouteWithId(flowId)
      case _                         =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromPartnershipNominatedPartnerUtrYesNoPage] - answer missing, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipNominatedPartnerCrnYesNoPage(
    flowId: UUID,
    id: String,
    mode: Mode
  )(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipNominatedPartnerCrnYesNoPage(id)), mode) match {
      case (Some(true), NormalMode)  =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnController
          .onPageLoadWithId(flowId, NormalMode)
      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController
          .onPageLoadWithId(flowId, NormalMode)
      case (Some(true), CheckMode)   =>
        userAnswers
          .get(PartnershipNominatedPartnerCrnPage(id))
          .fold(
            controllers.add.partnership.routes.PartnershipNominatedPartnerCrnController
              .onPageLoadWithId(flowId, mode)
          )(_ => cyaRouteWithId(flowId))
      case (Some(false), CheckMode)  => cyaRouteWithId(flowId)
      case _                         =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromPartnershipNominatedPartnerCrnYesNoPage] - answer missing, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipAddressYesNoPage(
    flowId: UUID,
    id: String,
    mode: Mode
  )(userAnswers: UserAnswers): Call =
    addressLookupYesNoRoute(
      mode,
      userAnswers.get(PartnershipAddressYesNoPage(id)),
      userAnswers.get(PartnershipAddressPage(id)).isDefined,
      onYes = controllers.add.partnership.routes.PartnershipAddressController
        .redirectToAddressLookupWithId(flowId, None),
      onYesChange = controllers.add.partnership.routes.PartnershipAddressController
        .redirectToAddressLookupWithId(flowId, Some(CheckMode.toString)),
      onNo = controllers.add.partnership.routes.AddPartnershipContactMethodsYesNoController
        .onPageLoadWithId(flowId, NormalMode),
      checkYourAnswers = cyaRouteWithId(flowId)
    )

  private def navigatorFromPartnershipNominatedPartnerNinoYesNoPage(
    flowId: UUID,
    id: String,
    mode: Mode
  )(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipNominatedPartnerNinoYesNoPage(id)), mode) match {
      case (Some(true), NormalMode)  =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController
          .onPageLoadWithId(flowId, NormalMode)
      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController
          .onPageLoadWithId(flowId, NormalMode)
      case (Some(true), CheckMode)   =>
        userAnswers
          .get(PartnershipNominatedPartnerNinoPage(id))
          .fold(
            controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController
              .onPageLoadWithId(flowId, mode)
          )(_ => cyaRouteWithId(flowId))
      case (Some(false), CheckMode)  => cyaRouteWithId(flowId)
      case _                         =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromPartnershipNominatedPartnerNinoYesNoPage] - answer missing, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromAddPartnershipContactMethodsYesNoPage(
    flowId: UUID,
    id: String,
    mode: Mode
  )(userAnswers: UserAnswers): Call =
    (userAnswers.get(AddPartnershipContactMethodsYesNoPage(id)), mode) match {
      case (Some(true), NormalMode)  =>
        controllers.add.partnership.routes.PartnershipContactMethodOptionsController
          .onPageLoadWithId(flowId, NormalMode)
      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoadWithId(flowId, NormalMode)
      case (Some(true), CheckMode)   =>
        userAnswers
          .get(PartnershipContactMethodOptionsPage(id))
          .fold(
            controllers.add.partnership.routes.PartnershipContactMethodOptionsController
              .onPageLoadWithId(flowId, mode)
          )(_ => cyaRouteWithId(flowId))
      case (Some(false), CheckMode)  => cyaRouteWithId(flowId)
      case _                         =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromAddPartnershipContactMethodsYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def nextSelectedContactMethodPageAfter(
    flowId: UUID,
    id: String,
    current: Option[ContactMethodOptions]
  )(userAnswers: UserAnswers): Call =
    navigateFromContactMethodPage(id, current, userAnswers) { remaining =>
      remaining.headOption.fold {
        controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoadWithId(flowId, NormalMode)
      }(contactMethodPageCall(flowId, _, NormalMode))
    }

  private def nextMissingSelectedContactMethodPageAfter(
    flowId: UUID,
    id: String,
    current: Option[ContactMethodOptions],
    mode: Mode
  )(userAnswers: UserAnswers): Call =
    navigateFromContactMethodPage(id, current, userAnswers) { remaining =>
      remaining
        .find(isMissingAnswer(id, _)(userAnswers))
        .map(contactMethodPageCall(flowId, _, mode))
        .getOrElse(cyaRouteWithId(flowId))
    }

  private def navigateFromContactMethodPage(
    id: String,
    current: Option[ContactMethodOptions],
    userAnswers: UserAnswers
  )(terminalStep: Seq[ContactMethodOptions] => Call): Call =
    selectedContactMethodsInOrder(id, userAnswers)
      .filter(_.nonEmpty)
      .fold {
        logger.error(
          "[PartnershipNavigator][navigateFromContactMethodPage] - ContactMethodOptions missing or empty"
        )
        routes.JourneyRecoveryController.onPageLoad()
      } { selectedContactMethods =>
        current match {
          case Some(currentContactMethod) if !selectedContactMethods.contains(currentContactMethod) =>
            logger.error(
              "[PartnershipNavigator][navigateFromContactMethodPage] - current contact method not selected"
            )
            routes.JourneyRecoveryController.onPageLoad()
          case _                                                                                    =>
            val remaining =
              current match {
                case None                       => selectedContactMethods
                case Some(currentContactMethod) =>
                  val currentIndex = selectedContactMethods.indexWhere(_ == currentContactMethod)
                  selectedContactMethods.drop(currentIndex + 1)
              }
            terminalStep(remaining)
        }
      }

  private def selectedContactMethodsInOrder(
    id: String,
    userAnswers: UserAnswers
  ): Option[Seq[ContactMethodOptions]] =
    userAnswers.get(PartnershipContactMethodOptionsPage(id)).map(ContactMethodOptions.ordered)

  private def contactMethodPageCall(
    flowId: UUID,
    contactMethod: ContactMethodOptions,
    mode: Mode
  ): Call =
    contactMethod match {
      case ContactMethodOptions.Email  =>
        controllers.add.partnership.routes.PartnershipEmailAddressController.onPageLoadWithId(flowId, mode)
      case ContactMethodOptions.Phone  =>
        controllers.add.partnership.routes.PartnershipPhoneNumberController.onPageLoadWithId(flowId, mode)
      case ContactMethodOptions.Mobile =>
        controllers.add.partnership.routes.PartnershipMobileNumberController.onPageLoadWithId(flowId, mode)
    }

  private def isMissingAnswer(id: String, contactMethod: ContactMethodOptions)(userAnswers: UserAnswers): Boolean =
    userAnswers.get(contactMethodPage(id, contactMethod)).isEmpty

  private def contactMethodPage(id: String, contactMethod: ContactMethodOptions): QuestionPage[String] =
    contactMethod match {
      case ContactMethodOptions.Email  => PartnershipEmailAddressPage(id)
      case ContactMethodOptions.Phone  => PartnershipPhoneNumberPage(id)
      case ContactMethodOptions.Mobile => PartnershipMobileNumberPage(id)
    }

  private def nextPageWithoutId(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
    case AmendMode  =>
      amendRouteMap(page)(userAnswers)
  }

  private def cyaRoute(mode: Mode): Call = mode match {
    case AmendMode =>
      controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onPageLoad()
    case _         => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
  }

  private val normalRoutes: Page => UserAnswers => Call = {
    case PartnershipNamePage                      =>
      _ => controllers.add.partnership.routes.PartnershipAddressYesNoController.onPageLoad(NormalMode)
    case PartnershipAddressYesNoPage              =>
      userAnswers => navigatorFromPartnershipAddressYesNoPage(NormalMode)(userAnswers)
    case AddPartnershipContactMethodsYesNoPage    =>
      userAnswers => navigatorFromAddPartnershipContactMethodsYesNoPage(NormalMode)(userAnswers)
    case PartnershipContactMethodOptionsPage      =>
      userAnswers => nextSelectedContactMethodPageAfter(current = None)(userAnswers)
    case PartnershipEmailAddressPage              =>
      userAnswers => nextSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Email))(userAnswers)
    case PartnershipPhoneNumberPage               =>
      userAnswers => nextSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Phone))(userAnswers)
    case PartnershipMobileNumberPage              =>
      userAnswers => nextSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Mobile))(userAnswers)
    case PartnershipHasUtrYesNoPage               => userAnswers => navigatorFromPartnershipHasUtrYesNoPage(NormalMode)(userAnswers)
    case PartnershipUniqueTaxpayerReferencePage   =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerNameController.onPageLoad(NormalMode)
    case PartnershipNominatedPartnerNamePage      =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerUtrYesNoController.onPageLoad(NormalMode)
    case PartnershipWorksReferenceNumberYesNoPage =>
      userAnswers => navigatorFromPartnershipWorksReferenceNumberYesNoPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerNinoPage      =>
      userAnswers => navigatorFromPartnershipNominatedPartnerNinoPage(NormalMode)(userAnswers)
    case PartnershipWorksReferenceNumberPage      =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipNominatedPartnerCrnPage       =>
      userAnswers => navigatorFromPartnershipNominatedPartnerCrnPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerNinoYesNoPage =>
      userAnswers => navigatorFromPartnershipNominatedPartnerNinoYesNoPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerCrnYesNoPage  =>
      userAnswers => navigatorFromPartnershipNominatedPartnerCrnYesNoPage(NormalMode)(userAnswers)
    case PartnershipNominatedPartnerUtrPage       =>
      _ => controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController.onPageLoad(NormalMode)
    case PartnershipNominatedPartnerUtrYesNoPage  =>
      userAnswers => navigatorFromPartnershipNominatedPartnerUtrYesNoPage(NormalMode)(userAnswers)
    case _                                        => _ => routes.IndexController.onPageLoad()
  }

  private val amendRouteMap: Page => UserAnswers => Call = {
    case PartnershipWorksReferenceNumberYesNoPage => navigatorFromPartnershipWorksReferenceNumberYesNoPage(AmendMode)(_)
    case PartnershipWorksReferenceNumberPage      =>
      _ => cyaRoute(AmendMode)
    case PartnershipNominatedPartnerCrnYesNoPage  => navigatorFromPartnershipNominatedPartnerCrnYesNoPage(AmendMode)(_)
    case PartnershipNominatedPartnerCrnPage       => navigatorFromPartnershipNominatedPartnerCrnPage(AmendMode)(_)
    case PartnershipUniqueTaxpayerReferencePage   =>
      _ => cyaRoute(AmendMode)
    case PartnershipHasUtrYesNoPage               => navigatorFromPartnershipHasUtrYesNoPage(AmendMode)(_)
    case PartnershipNominatedPartnerUtrYesNoPage  =>
      userAnswers => navigatorFromPartnershipNominatedPartnerUtrYesNoPage(AmendMode)(userAnswers)
    case PartnershipNominatedPartnerUtrPage       =>
      _ => cyaRoute(AmendMode)
    case PartnershipNominatedPartnerNinoYesNoPage =>
      userAnswers => navigatorFromPartnershipNominatedPartnerNinoYesNoPage(AmendMode)(userAnswers)
    case PartnershipNominatedPartnerNinoPage      => navigatorFromPartnershipNominatedPartnerNinoPage(AmendMode)(_)
    case PartnershipNamePage                      =>
      _ => cyaRoute(AmendMode)
    case PartnershipNominatedPartnerNamePage      =>
      _ => cyaRoute(AmendMode)
    case PartnershipAddressYesNoPage              =>
      userAnswers => navigatorFromPartnershipAddressYesNoPage(AmendMode)(userAnswers)
    case AddPartnershipContactMethodsYesNoPage    =>
      userAnswers => navigatorFromAddPartnershipContactMethodsYesNoPage(AmendMode)(userAnswers)
    case PartnershipContactMethodOptionsPage      =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(current = None, AmendMode)(userAnswers)
    case PartnershipEmailAddressPage              =>
      userAnswers =>
        nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Email), mode = AmendMode)(
          userAnswers
        )
    case PartnershipPhoneNumberPage               =>
      userAnswers =>
        nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Phone), mode = AmendMode)(
          userAnswers
        )
    case PartnershipMobileNumberPage              =>
      userAnswers =>
        nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Mobile), mode = AmendMode)(
          userAnswers
        )
    case _                                        => _ => cyaRoute(AmendMode)
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case PartnershipHasUtrYesNoPage               => navigatorFromPartnershipHasUtrYesNoPage(CheckMode)(_)
    case PartnershipWorksReferenceNumberYesNoPage => navigatorFromPartnershipWorksReferenceNumberYesNoPage(CheckMode)(_)
    case PartnershipNominatedPartnerCrnYesNoPage  => navigatorFromPartnershipNominatedPartnerCrnYesNoPage(CheckMode)(_)
    case PartnershipNamePage                      =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipAddressYesNoPage              =>
      userAnswers => navigatorFromPartnershipAddressYesNoPage(CheckMode)(userAnswers)
    case AddPartnershipContactMethodsYesNoPage    =>
      userAnswers => navigatorFromAddPartnershipContactMethodsYesNoPage(CheckMode)(userAnswers)
    case PartnershipContactMethodOptionsPage      =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(current = None)(userAnswers)
    case PartnershipEmailAddressPage              =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Email))(userAnswers)
    case PartnershipPhoneNumberPage               =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Phone))(userAnswers)
    case PartnershipMobileNumberPage              =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Mobile))(userAnswers)
    case PartnershipNominatedPartnerNinoYesNoPage =>
      userAnswers => navigatorFromPartnershipNominatedPartnerNinoYesNoPage(CheckMode)(userAnswers)
    case PartnershipNominatedPartnerNinoPage      => navigatorFromPartnershipNominatedPartnerNinoPage(CheckMode)(_)
    case PartnershipNominatedPartnerNamePage      =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipNominatedPartnerUtrYesNoPage  =>
      userAnswers => navigatorFromPartnershipNominatedPartnerUtrYesNoPage(CheckMode)(userAnswers)
    case PartnershipNominatedPartnerUtrPage       =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipNominatedPartnerCrnPage       => navigatorFromPartnershipNominatedPartnerCrnPage(CheckMode)(_)
    case PartnershipWorksReferenceNumberPage      =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case PartnershipUniqueTaxpayerReferencePage   =>
      _ => controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
    case _                                        => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
  }

  private def navigatorFromPartnershipNominatedPartnerNinoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(PartnershipNominatedPartnerNinoPage), mode) match {
      case (Some(_), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(NormalMode)

      case (Some(_), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case (None, _) =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromPartnershipNominatedPartnerNinoPage] - answer missing, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipNominatedPartnerCrnPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(PartnershipNominatedPartnerCrnPage), mode) match {
      case (Some(_), NormalMode) =>
        controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController.onPageLoad(NormalMode)

      case (Some(_), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case (None, _) =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromPartnershipNominatedPartnerCrnPage] - answer missing, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipHasUtrYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(PartnershipHasUtrYesNoPage), mode) match {

      case (Some(true), CheckMode | AmendMode) if ua.get(PartnershipUniqueTaxpayerReferencePage).isDefined =>
        cyaRoute(mode)

      case (Some(true), CheckMode | AmendMode) =>
        controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController.onPageLoad(mode)

      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case (Some(true), NormalMode) =>
        controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerNameController.onPageLoad(NormalMode)

      case _ =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromPartnershipHasUtrYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipWorksReferenceNumberYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(PartnershipWorksReferenceNumberYesNoPage), mode) match {

      case (Some(true), CheckMode | AmendMode) =>
        ua.get(PartnershipWorksReferenceNumberPage)
          .fold(controllers.add.partnership.routes.PartnershipWorksReferenceNumberController.onPageLoad(mode)) { _ =>
            cyaRoute(mode)
          }

      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case (Some(true), NormalMode) =>
        controllers.add.partnership.routes.PartnershipWorksReferenceNumberController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()

      case _ =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromPartnershipWorksReferenceNumberYesNoPage] - unexpected, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipNominatedPartnerUtrYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipNominatedPartnerUtrYesNoPage), mode) match {
      case (Some(true), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerNinoYesNoController.onPageLoad(NormalMode)

      case (Some(true), CheckMode | AmendMode) =>
        userAnswers
          .get(PartnershipNominatedPartnerUtrPage)
          .fold(
            controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController.onPageLoad(mode)
          ) { _ =>
            cyaRoute(mode)
          }

      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case (None, _) =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromPartnershipNominatedPartnerUtrYesNoPage] - answer missing, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipNominatedPartnerCrnYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipNominatedPartnerCrnYesNoPage), mode) match {
      case (Some(true), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController.onPageLoad(NormalMode)

      case (Some(true), CheckMode | AmendMode) =>
        userAnswers
          .get(PartnershipNominatedPartnerCrnPage)
          .fold(
            controllers.add.partnership.routes.PartnershipNominatedPartnerCrnController.onPageLoad(mode)
          ) { _ =>
            cyaRoute(mode)
          }

      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case (None, _) =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromPartnershipNominatedPartnerCrnYesNoPage] - answer missing, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromPartnershipAddressYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    mode match {
      case AmendMode =>
        userAnswers.get(PartnershipAddressYesNoPage) match {
          case Some(true)  =>
            controllers.add.partnership.routes.PartnershipAddressController.redirectToAmendAddressLookup()
          case Some(false) =>
            cyaRoute(mode)
          case None        =>
            logger.error(
              s"[PartnershipNavigator][navigatorFromPartnershipAddressYesNoPage] - answer missing, mode=$mode"
            )
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
      case _         =>
        addressLookupYesNoRoute(
          mode,
          userAnswers.get(PartnershipAddressYesNoPage),
          userAnswers.get(PartnershipAddressPage).isDefined,
          onYes =
            controllers.add.partnership.routes.PartnershipAddressController.redirectToAddressLookup(NormalMode, None),
          onYesChange = controllers.add.partnership.routes.PartnershipAddressController
            .redirectToAddressLookup(NormalMode, Some(CheckMode.toString)),
          onNo = controllers.add.partnership.routes.AddPartnershipContactMethodsYesNoController.onPageLoad(NormalMode),
          checkYourAnswers = controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
        )
    }

  private def navigatorFromPartnershipNominatedPartnerNinoYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(PartnershipNominatedPartnerNinoYesNoPage), mode) match {
      case (Some(true), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipNominatedPartnerCrnYesNoController.onPageLoad(NormalMode)

      case (Some(true), CheckMode | AmendMode) =>
        userAnswers
          .get(PartnershipNominatedPartnerNinoPage)
          .fold(
            controllers.add.partnership.routes.PartnershipNominatedPartnerNinoController.onPageLoad(mode)
          ) { _ =>
            cyaRoute(mode)
          }

      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case (None, _) =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromPartnershipNominatedPartnerNinoYesNoPage] - answer missing, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromAddPartnershipContactMethodsYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(AddPartnershipContactMethodsYesNoPage), mode) match {

      case (Some(true), NormalMode) =>
        controllers.add.partnership.routes.PartnershipContactMethodOptionsController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(NormalMode)

      case (Some(true), CheckMode | AmendMode) =>
        userAnswers
          .get(PartnershipContactMethodOptionsPage)
          .fold(controllers.add.partnership.routes.PartnershipContactMethodOptionsController.onPageLoad(mode)) { _ =>
            cyaRoute(mode)
          }

      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case _ =>
        logger.error(
          s"[PartnershipNavigator][navigatorFromAddPartnershipContactMethodsYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def nextSelectedContactMethodPageAfter(
    current: Option[ContactMethodOptions]
  )(userAnswers: UserAnswers): Call =
    navigateFromContactMethodPage(current, userAnswers) { remaining =>
      remaining.headOption.fold {
        controllers.add.partnership.routes.PartnershipHasUtrYesNoController.onPageLoad(NormalMode)
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
          "[PartnershipNavigator][navigateFromContactMethodPage] - ContactMethodOptions missing or empty"
        )
        routes.JourneyRecoveryController.onPageLoad()
      } { selectedContactMethods =>
        current match {
          case Some(currentContactMethod) if !selectedContactMethods.contains(currentContactMethod) =>
            logger.error(
              "[PartnershipNavigator][navigateFromContactMethodPage] - current contact method not selected"
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
    userAnswers.get(PartnershipContactMethodOptionsPage).map {
      ContactMethodOptions.ordered
    }

  private def contactMethodPageCall(contactMethod: ContactMethodOptions, mode: Mode): Call =
    contactMethod match {
      case ContactMethodOptions.Email  =>
        controllers.add.partnership.routes.PartnershipEmailAddressController.onPageLoad(mode)
      case ContactMethodOptions.Phone  =>
        controllers.add.partnership.routes.PartnershipPhoneNumberController.onPageLoad(mode)
      case ContactMethodOptions.Mobile =>
        controllers.add.partnership.routes.PartnershipMobileNumberController.onPageLoad(mode)
    }

  private def contactMethodPage(contactMethod: ContactMethodOptions): QuestionPage[String] =
    contactMethod match {
      case ContactMethodOptions.Email  => PartnershipEmailAddressPage
      case ContactMethodOptions.Phone  => PartnershipPhoneNumberPage
      case ContactMethodOptions.Mobile => PartnershipMobileNumberPage
    }

  private def isMissingAnswer(contactMethod: ContactMethodOptions)(userAnswers: UserAnswers): Boolean =
    userAnswers.get(contactMethodPage(contactMethod)).isEmpty
}

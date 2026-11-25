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
import models.JourneyNamespace.AddSubcontractor
import navigation.NavigatorForJourney
import pages.{Page, QuestionPage}
import pages.add.trust.*
import play.api.mvc.Call

import java.util.UUID
import javax.inject.{Inject, Singleton}

@Singleton
class TrustNavigator @Inject() () extends NavigatorForJourney {

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
      case TrustNamePage(id, AddSubcontractor) => Some(id)
      case TrustAddressYesNoPage(id, AddSubcontractor) => Some(id)
      case TrustAddressPage(id, AddSubcontractor) => Some(id)
      case AddTrustContactMethodsYesNoPage(id, AddSubcontractor) => Some(id)
      case TrustContactMethodOptionsPage(id, AddSubcontractor) => Some(id)
      case TrustEmailAddressPage(id, AddSubcontractor) => Some(id)
      case TrustPhoneNumberPage(id, AddSubcontractor) => Some(id)
      case TrustMobileNumberPage(id, AddSubcontractor) => Some(id)
      case TrustUtrYesNoPage(id, AddSubcontractor) => Some(id)
      case TrustUtrPage(id, AddSubcontractor) => Some(id)
      case TrustWorksReferenceYesNoPage(id, AddSubcontractor) => Some(id)
      case TrustWorksReferencePage(id, AddSubcontractor) => Some(id)
      case _ => None
    }

  private def cyaRouteWithId(flowId: UUID): Call =
    controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoadWithId(flowId)

  private def nextPageWithoutId(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
    case AmendMode  =>
      amendRouteMap(page)(userAnswers)
  }

  private def normalRoutesWithId(flowId: UUID, id: String): Page => UserAnswers => Call = {
    case TrustNamePage(_, AddSubcontractor)                   =>
      _ => controllers.add.trust.routes.TrustAddressYesNoController.onPageLoadWithId(flowId, NormalMode)
    case TrustAddressYesNoPage(_, AddSubcontractor)           =>
      userAnswers => navigatorFromTrustAddressYesNoPage(flowId, id, NormalMode)(userAnswers)
    case AddTrustContactMethodsYesNoPage(_, AddSubcontractor) =>
      userAnswers => navigatorFromAddTrustContactMethodsYesNoPage(flowId, id, NormalMode)(userAnswers)
    case TrustContactMethodOptionsPage(_, AddSubcontractor)   =>
      userAnswers => nextSelectedContactMethodPageAfter(flowId, id, current = None)(userAnswers)
    case TrustEmailAddressPage(_, AddSubcontractor)           =>
      userAnswers =>
        nextSelectedContactMethodPageAfter(flowId, id, current = Some(ContactMethodOptions.Email))(userAnswers)
    case TrustPhoneNumberPage(_, AddSubcontractor)            =>
      userAnswers =>
        nextSelectedContactMethodPageAfter(flowId, id, current = Some(ContactMethodOptions.Phone))(userAnswers)
    case TrustMobileNumberPage(_, AddSubcontractor)           =>
      userAnswers =>
        nextSelectedContactMethodPageAfter(flowId, id, current = Some(ContactMethodOptions.Mobile))(userAnswers)
    case TrustUtrYesNoPage(_, AddSubcontractor)               =>
      userAnswers => navigatorFromTrustUtrYesNoPage(flowId, id, NormalMode)(userAnswers)
    case TrustUtrPage(_, AddSubcontractor)                    =>
      _ => controllers.add.trust.routes.TrustWorksReferenceYesNoController.onPageLoadWithId(flowId, NormalMode)
    case TrustWorksReferenceYesNoPage(_, AddSubcontractor)    =>
      userAnswers => navigatorFromTrustWorksReferenceYesNoPage(flowId, id, NormalMode)(userAnswers)
    case TrustWorksReferencePage(_, AddSubcontractor)         =>
      _ => cyaRouteWithId(flowId)
    case _                                  => _ => routes.IndexController.onPageLoad()
  }

  private def checkRouteMapWithId(flowId: UUID, id: String): Page => UserAnswers => Call = {
    case TrustNamePage(_, AddSubcontractor)                   => _ => cyaRouteWithId(flowId)
    case TrustAddressYesNoPage(_, AddSubcontractor)           =>
      userAnswers => navigatorFromTrustAddressYesNoPage(flowId, id, CheckMode)(userAnswers)
    case AddTrustContactMethodsYesNoPage(_, AddSubcontractor) =>
      userAnswers => navigatorFromAddTrustContactMethodsYesNoPage(flowId, id, CheckMode)(userAnswers)
    case TrustContactMethodOptionsPage(_, AddSubcontractor)   =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(flowId, id, current = None, CheckMode)(userAnswers)
    case TrustEmailAddressPage(_, AddSubcontractor)           =>
      userAnswers =>
        nextMissingSelectedContactMethodPageAfter(
          flowId,
          id,
          current = Some(ContactMethodOptions.Email),
          CheckMode
        )(userAnswers)
    case TrustPhoneNumberPage(_, AddSubcontractor)            =>
      userAnswers =>
        nextMissingSelectedContactMethodPageAfter(
          flowId,
          id,
          current = Some(ContactMethodOptions.Phone),
          CheckMode
        )(userAnswers)
    case TrustMobileNumberPage(_, AddSubcontractor)           =>
      userAnswers =>
        nextMissingSelectedContactMethodPageAfter(
          flowId,
          id,
          current = Some(ContactMethodOptions.Mobile),
          CheckMode
        )(userAnswers)
    case TrustUtrYesNoPage(_, AddSubcontractor)               =>
      navigatorFromTrustUtrYesNoPage(flowId, id, CheckMode)(_)
    case TrustUtrPage(_, AddSubcontractor)                    => _ => cyaRouteWithId(flowId)
    case TrustWorksReferenceYesNoPage(_, AddSubcontractor)    =>
      navigatorFromTrustWorksReferenceYesNoPage(flowId, id, CheckMode)(_)
    case TrustWorksReferencePage(_, AddSubcontractor)         => _ => cyaRouteWithId(flowId)
    case _                                  => _ => cyaRouteWithId(flowId)
  }

  private def navigatorFromTrustUtrYesNoPage(flowId: UUID, id: String, mode: Mode)(ua: UserAnswers): Call =
    (ua.get(TrustUtrYesNoPage(id, AddSubcontractor)), mode) match {
      case (Some(true), NormalMode)  =>
        controllers.add.trust.routes.TrustUtrController.onPageLoadWithId(flowId, mode)
      case (Some(false), NormalMode) =>
        controllers.add.trust.routes.TrustWorksReferenceYesNoController.onPageLoadWithId(flowId, NormalMode)
      case (Some(true), CheckMode)   =>
        ua.get(TrustUtrPage(id, AddSubcontractor))
          .fold(controllers.add.trust.routes.TrustUtrController.onPageLoadWithId(flowId, mode))(_ =>
            cyaRouteWithId(flowId)
          )
      case (Some(false), CheckMode)  =>
        cyaRouteWithId(flowId)
      case _                         =>
        logger.error(s"[TrustNavigator][navigatorFromTrustUtrYesNoPage] - unexpected state, mode=$mode")
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromTrustAddressYesNoPage(flowId: UUID, id: String, mode: Mode)(ua: UserAnswers): Call =
    addressLookupYesNoRoute(
      mode,
      ua.get(TrustAddressYesNoPage(id, AddSubcontractor)),
      ua.get(TrustAddressPage(id, AddSubcontractor)).isDefined,
      onYes = controllers.add.trust.routes.TrustAddressController.redirectToAddressLookupWithId(flowId, NormalMode, None),
      onYesChange = controllers.add.trust.routes.TrustAddressController
        .redirectToAddressLookupWithId(flowId, NormalMode, Some(CheckMode.toString)),
      onNo = controllers.add.trust.routes.AddTrustContactMethodsYesNoController.onPageLoadWithId(flowId, NormalMode),
      checkYourAnswers = cyaRouteWithId(flowId)
    )

  private def navigatorFromTrustWorksReferenceYesNoPage(flowId: UUID, id: String, mode: Mode)(ua: UserAnswers): Call =
    (ua.get(TrustWorksReferenceYesNoPage(id, AddSubcontractor)), mode) match {
      case (Some(true), NormalMode)  =>
        controllers.add.trust.routes.TrustWorksReferenceController.onPageLoadWithId(flowId, NormalMode)
      case (Some(false), NormalMode) =>
        cyaRouteWithId(flowId)
      case (Some(true), CheckMode)   =>
        ua.get(TrustWorksReferencePage(id, AddSubcontractor))
          .fold(controllers.add.trust.routes.TrustWorksReferenceController.onPageLoadWithId(flowId, mode))(_ =>
            cyaRouteWithId(flowId)
          )
      case (Some(false), CheckMode)  =>
        cyaRouteWithId(flowId)
      case _                         =>
        logger.error(s"[TrustNavigator][navigatorFromTrustWorksReferenceYesNoPage] - unexpected state, mode=$mode")
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromAddTrustContactMethodsYesNoPage(
    flowId: UUID,
    id: String,
    mode: Mode
  )(ua: UserAnswers): Call =
    (ua.get(AddTrustContactMethodsYesNoPage(id, AddSubcontractor)), mode) match {
      case (Some(true), NormalMode)  =>
        controllers.add.trust.routes.TrustContactMethodOptionsController.onPageLoadWithId(flowId, NormalMode)
      case (Some(false), NormalMode) =>
        controllers.add.trust.routes.TrustUtrYesNoController.onPageLoadWithId(flowId, NormalMode)
      case (Some(true), CheckMode)   =>
        ua.get(TrustContactMethodOptionsPage(id, AddSubcontractor))
          .fold(
            controllers.add.trust.routes.TrustContactMethodOptionsController.onPageLoadWithId(flowId, mode)
          )(_ => cyaRouteWithId(flowId))
      case (Some(false), CheckMode)  =>
        cyaRouteWithId(flowId)
      case _                         =>
        logger.error(
          s"[TrustNavigator][navigatorFromAddTrustContactMethodsYesNoPage] - unexpected state, mode=$mode"
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
        controllers.add.trust.routes.TrustUtrYesNoController.onPageLoadWithId(flowId, NormalMode)
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
          "[TrustNavigator][navigateFromContactMethodPage] - ContactMethodOptions missing or empty"
        )
        routes.JourneyRecoveryController.onPageLoad()
      } { selectedContactMethods =>
        current match {
          case Some(currentContactMethod) if !selectedContactMethods.contains(currentContactMethod) =>
            logger.error(
              "[TrustNavigator][navigateFromContactMethodPage] - current contact method not selected"
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
    userAnswers.get(TrustContactMethodOptionsPage(id, AddSubcontractor)).map(ContactMethodOptions.ordered)

  private def contactMethodPageCall(
    flowId: UUID,
    contactMethod: ContactMethodOptions,
    mode: Mode
  ): Call =
    contactMethod match {
      case ContactMethodOptions.Email  =>
        controllers.add.trust.routes.TrustEmailAddressController.onPageLoadWithId(flowId, mode)
      case ContactMethodOptions.Phone  =>
        controllers.add.trust.routes.TrustPhoneNumberController.onPageLoadWithId(flowId, mode)
      case ContactMethodOptions.Mobile =>
        controllers.add.trust.routes.TrustMobileNumberController.onPageLoadWithId(flowId, mode)
    }

  private def isMissingAnswer(id: String, contactMethod: ContactMethodOptions)(userAnswers: UserAnswers): Boolean =
    userAnswers.get(contactMethodPage(id, contactMethod)).isEmpty

  private def contactMethodPage(id: String, contactMethod: ContactMethodOptions): QuestionPage[String] =
    contactMethod match {
      case ContactMethodOptions.Email  => TrustEmailAddressPage(id, AddSubcontractor)
      case ContactMethodOptions.Phone  => TrustPhoneNumberPage(id, AddSubcontractor)
      case ContactMethodOptions.Mobile => TrustMobileNumberPage(id, AddSubcontractor)
    }

  private def cyaRoute(mode: Mode): Call = mode match {
    case AmendMode =>
      controllers.amend.trust.routes.AmendTrustCheckYourAnswersController.onPageLoad()
    case _         => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
  }

  private val normalRoutes: Page => UserAnswers => Call = {

    case TrustNamePage                   => _ => controllers.add.trust.routes.TrustAddressYesNoController.onPageLoad(NormalMode)
    case TrustAddressYesNoPage           => userAnswers => navigatorFromTrustAddressYesNoPage(NormalMode)(userAnswers)
    case AddTrustContactMethodsYesNoPage =>
      userAnswers => navigatorFromAddTrustContactMethodsYesNoPage(NormalMode)(userAnswers)
    case TrustContactMethodOptionsPage   =>
      userAnswers => nextSelectedContactMethodPageAfter(current = None)(userAnswers)
    case TrustEmailAddressPage           =>
      userAnswers => nextSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Email))(userAnswers)
    case TrustPhoneNumberPage            =>
      userAnswers => nextSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Phone))(userAnswers)
    case TrustMobileNumberPage           =>
      userAnswers => nextSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Mobile))(userAnswers)
    case TrustUtrYesNoPage               => userAnswers => navigatorFromTrustUtrYesNoPage(NormalMode)(userAnswers)
    case TrustUtrPage                    => _ => controllers.add.trust.routes.TrustWorksReferenceYesNoController.onPageLoad(NormalMode)
    case TrustWorksReferenceYesNoPage    =>
      userAnswers => navigatorFromTrustWorksReferenceYesNoPage(NormalMode)(userAnswers)
    case TrustWorksReferencePage         => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
    case _                               => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {

    case TrustNamePage                   => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
    case TrustAddressYesNoPage           => navigatorFromTrustAddressYesNoPage(CheckMode)(_)
    case AddTrustContactMethodsYesNoPage => navigatorFromAddTrustContactMethodsYesNoPage(CheckMode)(_)
    case TrustContactMethodOptionsPage   => nextMissingSelectedContactMethodPageAfter(current = None)(_)
    case TrustEmailAddressPage           =>
      nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Email))(_)
    case TrustPhoneNumberPage            =>
      nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Phone))(_)
    case TrustMobileNumberPage           =>
      nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Mobile))(_)
    case TrustUtrYesNoPage               => navigatorFromTrustUtrYesNoPage(CheckMode)(_)
    case TrustUtrPage                    => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
    case TrustWorksReferenceYesNoPage    => navigatorFromTrustWorksReferenceYesNoPage(CheckMode)(_)
    case TrustWorksReferencePage         => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
    case _                               => _ => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
  }

  private val amendRouteMap: Page => UserAnswers => Call = {
    case TrustAddressYesNoPage           => navigatorFromTrustAddressYesNoPage(AmendMode)(_)
    case AddTrustContactMethodsYesNoPage => navigatorFromAddTrustContactMethodsYesNoPage(AmendMode)(_)
    case TrustContactMethodOptionsPage   => nextMissingSelectedContactMethodPageAfter(current = None, AmendMode)(_)
    case TrustEmailAddressPage           =>
      nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Email), AmendMode)(_)
    case TrustPhoneNumberPage            =>
      nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Phone), AmendMode)(_)
    case TrustMobileNumberPage           =>
      nextMissingSelectedContactMethodPageAfter(current = Some(ContactMethodOptions.Mobile), AmendMode)(_)
    case TrustWorksReferenceYesNoPage    => navigatorFromTrustWorksReferenceYesNoPage(AmendMode)(_)
    case TrustUtrYesNoPage               => navigatorFromTrustUtrYesNoPage(AmendMode)(_)
    case _                               => _ => cyaRoute(AmendMode)
  }

  private def navigatorFromTrustUtrYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(TrustUtrYesNoPage), mode) match {
      case (Some(true), NormalMode)             => controllers.add.trust.routes.TrustUtrController.onPageLoad(mode)
      case (Some(false), NormalMode)            =>
        controllers.add.trust.routes.TrustWorksReferenceYesNoController.onPageLoad(NormalMode)
      case (Some(true), CheckMode | AmendMode)  =>
        ua.get(TrustUtrPage)
          .fold(controllers.add.trust.routes.TrustUtrController.onPageLoad(mode)) { _ =>
            cyaRoute(mode)
          }
      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)
      case _                                    =>
        logger.error(s"[TrustNavigator][navigatorFromTrustUtrYesNoPage] - unexpected state, mode=$mode")
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromTrustAddressYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    mode match {
      case AmendMode =>
        ua.get(TrustAddressYesNoPage) match {
          case Some(true)  =>
            controllers.add.trust.routes.TrustAddressController.redirectToAmendAddressLookup()
          case Some(false) =>
            cyaRoute(mode)
          case None        =>
            logger.error(
              "[TrustNavigator][navigatorFromTrustAddressYesNoPage] - TrustAddressYesNoPage missing from userAnswers"
            )
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
      case _         =>
        addressLookupYesNoRoute(
          mode,
          ua.get(TrustAddressYesNoPage),
          ua.get(TrustAddressPage).isDefined,
          onYes = controllers.add.trust.routes.TrustAddressController.redirectToAddressLookup(NormalMode, None),
          onYesChange = controllers.add.trust.routes.TrustAddressController
            .redirectToAddressLookup(NormalMode, Some(CheckMode.toString)),
          onNo = controllers.add.trust.routes.AddTrustContactMethodsYesNoController.onPageLoad(NormalMode),
          checkYourAnswers = controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
        )
    }

  private def navigatorFromTrustWorksReferenceYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(TrustWorksReferenceYesNoPage), mode) match {
      case (Some(true), NormalMode)             =>
        controllers.add.trust.routes.TrustWorksReferenceController.onPageLoad(NormalMode)
      case (Some(false), NormalMode)            =>
        controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
      case (Some(true), CheckMode | AmendMode)  =>
        ua.get(TrustWorksReferencePage)
          .fold(controllers.add.trust.routes.TrustWorksReferenceController.onPageLoad(mode)) { _ =>
            cyaRoute(mode)
          }
      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)
      case _                                    =>
        logger.error(s"[TrustNavigator][navigatorFromTrustWorksReferenceYesNoPage] - unexpected state, mode=$mode")
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromAddTrustContactMethodsYesNoPage(mode: Mode)(ua: UserAnswers): Call =
    (ua.get(AddTrustContactMethodsYesNoPage), mode) match {

      case (Some(true), NormalMode) =>
        controllers.add.trust.routes.TrustContactMethodOptionsController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.trust.routes.TrustUtrYesNoController.onPageLoad(NormalMode)

      case (Some(true), CheckMode | AmendMode) =>
        ua.get(TrustContactMethodOptionsPage)
          .fold(controllers.add.trust.routes.TrustContactMethodOptionsController.onPageLoad(mode)) { _ =>
            cyaRoute(mode)
          }

      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case _ =>
        logger.error(
          s"[TrustNavigator][navigatorFromAddTrustContactMethodsYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def nextSelectedContactMethodPageAfter(
    current: Option[ContactMethodOptions]
  )(userAnswers: UserAnswers): Call =
    navigateFromContactMethodPage(current, userAnswers) { remaining =>
      remaining.headOption.fold {
        controllers.add.trust.routes.TrustUtrYesNoController.onPageLoad(NormalMode)
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
          "[TrustNavigator][navigateFromContactMethodPage] - ContactMethodOptions missing or empty"
        )
        routes.JourneyRecoveryController.onPageLoad()
      } { selectedContactMethods =>
        current match {
          case Some(currentContactMethod) if !selectedContactMethods.contains(currentContactMethod) =>
            logger.error(
              "[TrustNavigator][navigateFromContactMethodPage] - current contact method not selected"
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
    userAnswers.get(TrustContactMethodOptionsPage).map {
      ContactMethodOptions.ordered
    }

  private def contactMethodPageCall(contactMethod: ContactMethodOptions, mode: Mode): Call =
    contactMethod match {
      case ContactMethodOptions.Email  =>
        controllers.add.trust.routes.TrustEmailAddressController.onPageLoad(mode)
      case ContactMethodOptions.Phone  =>
        controllers.add.trust.routes.TrustPhoneNumberController.onPageLoad(mode)
      case ContactMethodOptions.Mobile =>
        controllers.add.trust.routes.TrustMobileNumberController.onPageLoad(mode)
    }

  private def contactMethodPage(contactMethod: ContactMethodOptions): QuestionPage[String] =
    contactMethod match {
      case ContactMethodOptions.Email  => TrustEmailAddressPage
      case ContactMethodOptions.Phone  => TrustPhoneNumberPage
      case ContactMethodOptions.Mobile => TrustMobileNumberPage
    }

  private def isMissingAnswer(contactMethod: ContactMethodOptions)(userAnswers: UserAnswers): Boolean =
    userAnswers.get(contactMethodPage(contactMethod)).isEmpty

}

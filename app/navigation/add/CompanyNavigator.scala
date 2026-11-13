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
import pages.add.company.*
import play.api.mvc.Call

import java.util.UUID
import javax.inject.{Inject, Singleton}

@Singleton
class CompanyNavigator @Inject() () extends NavigatorForJourney {

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
      case CompanyNamePage(id)                   => Some(id)
      case CompanyAddressYesNoPage(id)           => Some(id)
      case CompanyAddressPage(id)                => Some(id)
      case AddCompanyContactMethodsYesNoPage(id) => Some(id)
      case CompanyContactMethodOptionsPage(id)   => Some(id)
      case CompanyEmailAddressPage(id)           => Some(id)
      case CompanyPhoneNumberPage(id)            => Some(id)
      case CompanyMobileNumberPage(id)           => Some(id)
      case CompanyUtrYesNoPage(id)               => Some(id)
      case CompanyUtrPage(id)                    => Some(id)
      case CompanyCrnYesNoPage(id)               => Some(id)
      case CompanyCrnPage(id)                    => Some(id)
      case CompanyWorksReferenceYesNoPage(id)    => Some(id)
      case CompanyWorksReferencePage(id)         => Some(id)
      case _                                     => None
    }

  private def cyaRouteWithId(flowId: UUID): Call =
    controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoadWithId(flowId)

  private def normalRoutesWithId(flowId: UUID, id: String): Page => UserAnswers => Call = {
    case CompanyNamePage(_) =>
      _ =>
        controllers.add.company.routes.CompanyAddressYesNoController
          .onPageLoadWithId(flowId, NormalMode)

    case CompanyAddressYesNoPage(_) =>
      userAnswers => navigatorFromCompanyAddressYesNoPage(flowId, id, NormalMode)(userAnswers)

    case CompanyAddressPage(_) =>
      _ =>
        controllers.add.company.routes.AddCompanyContactMethodsYesNoController
          .onPageLoadWithId(flowId, NormalMode)

    case AddCompanyContactMethodsYesNoPage(_) =>
      userAnswers => navigatorFromAddCompanyContactMethodsYesNoPage(flowId, id, NormalMode)(userAnswers)

    case CompanyContactMethodOptionsPage(_) =>
      userAnswers => nextSelectedContactMethodPageAfter(flowId, id, None)(userAnswers)

    case CompanyEmailAddressPage(_) =>
      userAnswers => nextSelectedContactMethodPageAfter(flowId, id, Some(ContactMethodOptions.Email))(userAnswers)

    case CompanyPhoneNumberPage(_) =>
      userAnswers => nextSelectedContactMethodPageAfter(flowId, id, Some(ContactMethodOptions.Phone))(userAnswers)

    case CompanyMobileNumberPage(_) =>
      userAnswers => nextSelectedContactMethodPageAfter(flowId, id, Some(ContactMethodOptions.Mobile))(userAnswers)

    case CompanyUtrYesNoPage(_) =>
      userAnswers => navigatorFromCompanyUtrYesNoPage(flowId, id, NormalMode)(userAnswers)

    case CompanyUtrPage(_) =>
      _ =>
        controllers.add.company.routes.CompanyCrnYesNoController
          .onPageLoadWithId(flowId, NormalMode)

    case CompanyCrnYesNoPage(_) =>
      userAnswers => navigatorFromCompanyCrnYesNoPage(flowId, id, NormalMode)(userAnswers)

    case CompanyCrnPage(_) =>
      _ =>
        controllers.add.company.routes.CompanyWorksReferenceYesNoController
          .onPageLoadWithId(flowId, NormalMode)

    case CompanyWorksReferenceYesNoPage(_) =>
      userAnswers => navigatorFromCompanyWorksReferenceYesNoPage(flowId, id, NormalMode)(userAnswers)

    case CompanyWorksReferencePage(_) =>
      _ => cyaRouteWithId(flowId)

    case _ =>
      _ => routes.IndexController.onPageLoad()
  }

  private def checkRouteMapWithId(flowId: UUID, id: String): Page => UserAnswers => Call = {
    case CompanyUtrYesNoPage(_) =>
      navigatorFromCompanyUtrYesNoPage(flowId, id, CheckMode)(_)

    case CompanyCrnYesNoPage(_) =>
      navigatorFromCompanyCrnYesNoPage(flowId, id, CheckMode)(_)

    case CompanyWorksReferenceYesNoPage(_) =>
      navigatorFromCompanyWorksReferenceYesNoPage(flowId, id, CheckMode)(_)

    case CompanyNamePage(_) =>
      _ => cyaRouteWithId(flowId)

    case CompanyAddressYesNoPage(_) =>
      userAnswers => navigatorFromCompanyAddressYesNoPage(flowId, id, CheckMode)(userAnswers)

    case CompanyAddressPage(_) =>
      _ => cyaRouteWithId(flowId)

    case AddCompanyContactMethodsYesNoPage(_) =>
      userAnswers => navigatorFromAddCompanyContactMethodsYesNoPage(flowId, id, CheckMode)(userAnswers)

    case CompanyContactMethodOptionsPage(_) =>
      userAnswers => nextMissingSelectedContactMethodPageAfter(flowId, id, None, CheckMode)(userAnswers)

    case CompanyEmailAddressPage(_) =>
      userAnswers =>
        nextMissingSelectedContactMethodPageAfter(
          flowId,
          id,
          Some(ContactMethodOptions.Email),
          CheckMode
        )(userAnswers)

    case CompanyPhoneNumberPage(_) =>
      userAnswers =>
        nextMissingSelectedContactMethodPageAfter(
          flowId,
          id,
          Some(ContactMethodOptions.Phone),
          CheckMode
        )(userAnswers)

    case CompanyMobileNumberPage(_) =>
      userAnswers =>
        nextMissingSelectedContactMethodPageAfter(
          flowId,
          id,
          Some(ContactMethodOptions.Mobile),
          CheckMode
        )(userAnswers)

    case CompanyUtrPage(_) =>
      _ => cyaRouteWithId(flowId)

    case CompanyCrnPage(_) =>
      _ => cyaRouteWithId(flowId)

    case CompanyWorksReferencePage(_) =>
      _ => cyaRouteWithId(flowId)

    case _ =>
      _ => cyaRouteWithId(flowId)
  }

  private def navigatorFromCompanyAddressYesNoPage(
    flowId: UUID,
    id: String,
    mode: Mode
  )(userAnswers: UserAnswers): Call =
    addressLookupYesNoRoute(
      mode,
      userAnswers.get(CompanyAddressYesNoPage(id)),
      userAnswers.get(CompanyAddressPage(id)).isDefined,
      onYes = controllers.add.company.routes.CompanyAddressController
        .redirectToAddressLookupWithId(flowId, None),
      onYesChange = controllers.add.company.routes.CompanyAddressController
        .redirectToAddressLookupWithId(flowId, Some(CheckMode.toString)),
      onNo = controllers.add.company.routes.AddCompanyContactMethodsYesNoController
        .onPageLoadWithId(flowId, NormalMode),
      checkYourAnswers = cyaRouteWithId(flowId)
    )

  private def navigatorFromAddCompanyContactMethodsYesNoPage(
    flowId: UUID,
    id: String,
    mode: Mode
  )(userAnswers: UserAnswers): Call =
    (userAnswers.get(AddCompanyContactMethodsYesNoPage(id)), mode) match {
      case (Some(true), NormalMode) =>
        controllers.add.company.routes.CompanyContactMethodOptionsController
          .onPageLoadWithId(flowId, NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyUtrYesNoController
          .onPageLoadWithId(flowId, NormalMode)

      case (Some(true), CheckMode) =>
        userAnswers
          .get(CompanyContactMethodOptionsPage(id))
          .fold(
            controllers.add.company.routes.CompanyContactMethodOptionsController
              .onPageLoadWithId(flowId, mode)
          )(_ => cyaRouteWithId(flowId))

      case (Some(false), CheckMode) =>
        cyaRouteWithId(flowId)

      case _ =>
        logger.error(
          s"[CompanyNavigator][navigatorFromAddCompanyContactMethodsYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromCompanyUtrYesNoPage(
    flowId: UUID,
    id: String,
    mode: Mode
  )(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyUtrYesNoPage(id)), mode) match {
      case (Some(true), CheckMode) if userAnswers.get(CompanyUtrPage(id)).isDefined =>
        cyaRouteWithId(flowId)

      case (Some(true), CheckMode) =>
        controllers.add.company.routes.CompanyUtrController
          .onPageLoadWithId(flowId, mode)

      case (Some(false), CheckMode) =>
        cyaRouteWithId(flowId)

      case (Some(true), NormalMode) =>
        controllers.add.company.routes.CompanyUtrController
          .onPageLoadWithId(flowId, NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyCrnYesNoController
          .onPageLoadWithId(flowId, NormalMode)

      case _ =>
        logger.error(
          s"[CompanyNavigator][navigatorFromCompanyUtrYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromCompanyCrnYesNoPage(
    flowId: UUID,
    id: String,
    mode: Mode
  )(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyCrnYesNoPage(id)), mode) match {
      case (Some(true), CheckMode) if userAnswers.get(CompanyCrnPage(id)).isDefined =>
        cyaRouteWithId(flowId)

      case (Some(true), CheckMode) =>
        controllers.add.company.routes.CompanyCrnController
          .onPageLoadWithId(flowId, mode)

      case (Some(false), CheckMode) =>
        cyaRouteWithId(flowId)

      case (Some(true), NormalMode) =>
        controllers.add.company.routes.CompanyCrnController
          .onPageLoadWithId(flowId, NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyWorksReferenceYesNoController
          .onPageLoadWithId(flowId, NormalMode)

      case _ =>
        logger.error(
          s"[CompanyNavigator][navigatorFromCompanyCrnYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromCompanyWorksReferenceYesNoPage(
    flowId: UUID,
    id: String,
    mode: Mode
  )(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyWorksReferenceYesNoPage(id)), mode) match {
      case (Some(true), CheckMode) =>
        userAnswers
          .get(CompanyWorksReferencePage(id))
          .fold(
            controllers.add.company.routes.CompanyWorksReferenceController
              .onPageLoadWithId(flowId, mode)
          )(_ => cyaRouteWithId(flowId))

      case (Some(false), CheckMode) =>
        cyaRouteWithId(flowId)

      case (Some(true), NormalMode) =>
        controllers.add.company.routes.CompanyWorksReferenceController
          .onPageLoadWithId(flowId, NormalMode)

      case (Some(false), NormalMode) =>
        cyaRouteWithId(flowId)

      case _ =>
        logger.error(
          s"[CompanyNavigator][navigatorFromCompanyWorksReferenceYesNoPage] - unexpected state, mode=$mode"
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
        controllers.add.company.routes.CompanyUtrYesNoController
          .onPageLoadWithId(flowId, NormalMode)
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
          "[CompanyNavigator][navigateFromContactMethodPage] - ContactMethodOptions missing or empty"
        )
        routes.JourneyRecoveryController.onPageLoad()
      } { selectedContactMethods =>
        current match {
          case Some(currentContactMethod) if !selectedContactMethods.contains(currentContactMethod) =>
            logger.error(
              "[CompanyNavigator][navigateFromContactMethodPage] - current contact method not selected"
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

  private def selectedContactMethodsInOrder(
    id: String,
    userAnswers: UserAnswers
  ): Option[Seq[ContactMethodOptions]] =
    userAnswers.get(CompanyContactMethodOptionsPage(id)).map(ContactMethodOptions.ordered)

  private def contactMethodPageCall(
    flowId: UUID,
    contactMethod: ContactMethodOptions,
    mode: Mode
  ): Call =
    contactMethod match {
      case ContactMethodOptions.Email =>
        controllers.add.company.routes.CompanyEmailAddressController
          .onPageLoadWithId(flowId, mode)

      case ContactMethodOptions.Phone =>
        controllers.add.company.routes.CompanyPhoneNumberController
          .onPageLoadWithId(flowId, mode)

      case ContactMethodOptions.Mobile =>
        controllers.add.company.routes.CompanyMobileNumberController
          .onPageLoadWithId(flowId, mode)
    }

  private def contactMethodPage(
    id: String,
    contactMethod: ContactMethodOptions
  ): QuestionPage[String] =
    contactMethod match {
      case ContactMethodOptions.Email  => CompanyEmailAddressPage(id)
      case ContactMethodOptions.Phone  => CompanyPhoneNumberPage(id)
      case ContactMethodOptions.Mobile => CompanyMobileNumberPage(id)
    }

  private def isMissingAnswer(
    id: String,
    contactMethod: ContactMethodOptions
  )(userAnswers: UserAnswers): Boolean =
    userAnswers.get(contactMethodPage(id, contactMethod)).isEmpty

  private def nextPageWithoutId(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)

    case CheckMode =>
      checkRouteMap(page)(userAnswers)

    case AmendMode =>
      amendRouteMap(page)(userAnswers)

    case _ =>
      super.nextPage(page, mode, userAnswers)
  }

  private def cyaRoute(mode: Mode): Call = mode match {
    case AmendMode =>
      controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onPageLoad()

    case _ =>
      controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
  }

  private val normalRoutes: Page => UserAnswers => Call = {
    case CompanyNamePage =>
      _ => controllers.add.company.routes.CompanyAddressYesNoController.onPageLoad(NormalMode)

    case CompanyAddressYesNoPage =>
      userAnswers => navigatorFromCompanyAddressYesNoPage(NormalMode)(userAnswers)

    case AddCompanyContactMethodsYesNoPage =>
      userAnswers => navigatorFromAddCompanyContactMethodsYesNoPage(NormalMode)(userAnswers)

    case CompanyContactMethodOptionsPage =>
      userAnswers => nextSelectedContactMethodPageAfter(current = None)(userAnswers)

    case CompanyEmailAddressPage =>
      userAnswers =>
        nextSelectedContactMethodPageAfter(
          current = Some(ContactMethodOptions.Email)
        )(userAnswers)

    case CompanyPhoneNumberPage =>
      userAnswers =>
        nextSelectedContactMethodPageAfter(
          current = Some(ContactMethodOptions.Phone)
        )(userAnswers)

    case CompanyMobileNumberPage =>
      userAnswers =>
        nextSelectedContactMethodPageAfter(
          current = Some(ContactMethodOptions.Mobile)
        )(userAnswers)

    case CompanyUtrYesNoPage =>
      userAnswers => navigatorFromCompanyUtrYesNoPage(NormalMode)(userAnswers)

    case CompanyUtrPage =>
      _ => controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(NormalMode)

    case CompanyCrnYesNoPage =>
      userAnswers => navigatorFromCompanyCrnYesNoPage(NormalMode)(userAnswers)

    case CompanyCrnPage =>
      _ => controllers.add.company.routes.CompanyWorksReferenceYesNoController.onPageLoad(NormalMode)

    case CompanyWorksReferenceYesNoPage =>
      userAnswers => navigatorFromCompanyWorksReferenceYesNoPage(NormalMode)(userAnswers)

    case CompanyWorksReferencePage =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()

    case _ =>
      _ => routes.IndexController.onPageLoad()
  }

  private val amendRouteMap: Page => UserAnswers => Call = {
    case CompanyNamePage =>
      _ => cyaRoute(AmendMode)

    case AddCompanyContactMethodsYesNoPage =>
      navigatorFromAddCompanyContactMethodsYesNoPage(AmendMode)(_)

    case CompanyContactMethodOptionsPage =>
      nextMissingSelectedContactMethodPageAfter(
        current = None,
        AmendMode
      )(_)

    case CompanyEmailAddressPage =>
      nextMissingSelectedContactMethodPageAfter(
        current = Some(ContactMethodOptions.Email),
        AmendMode
      )(_)

    case CompanyPhoneNumberPage =>
      nextMissingSelectedContactMethodPageAfter(
        current = Some(ContactMethodOptions.Phone),
        AmendMode
      )(_)

    case CompanyMobileNumberPage =>
      nextMissingSelectedContactMethodPageAfter(
        current = Some(ContactMethodOptions.Mobile),
        AmendMode
      )(_)

    case CompanyCrnPage =>
      _ => cyaRoute(AmendMode)

    case CompanyAddressYesNoPage =>
      navigatorFromCompanyAddressYesNoPage(AmendMode)(_)

    case CompanyCrnYesNoPage =>
      userAnswers => navigatorFromCompanyCrnYesNoPage(AmendMode)(userAnswers)

    case CompanyUtrYesNoPage =>
      userAnswers => navigatorFromCompanyUtrYesNoPage(AmendMode)(userAnswers)

    case CompanyUtrPage =>
      _ => cyaRoute(AmendMode)

    case CompanyWorksReferenceYesNoPage =>
      userAnswers => navigatorFromCompanyWorksReferenceYesNoPage(AmendMode)(userAnswers)

    case CompanyWorksReferencePage =>
      _ => cyaRoute(AmendMode)

    case _ =>
      _ => cyaRoute(AmendMode)
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case CompanyNamePage =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()

    case CompanyAddressYesNoPage =>
      navigatorFromCompanyAddressYesNoPage(CheckMode)(_)

    case AddCompanyContactMethodsYesNoPage =>
      navigatorFromAddCompanyContactMethodsYesNoPage(CheckMode)(_)

    case CompanyContactMethodOptionsPage =>
      nextMissingSelectedContactMethodPageAfter(current = None)(_)

    case CompanyEmailAddressPage =>
      nextMissingSelectedContactMethodPageAfter(
        current = Some(ContactMethodOptions.Email)
      )(_)

    case CompanyPhoneNumberPage =>
      nextMissingSelectedContactMethodPageAfter(
        current = Some(ContactMethodOptions.Phone)
      )(_)

    case CompanyMobileNumberPage =>
      nextMissingSelectedContactMethodPageAfter(
        current = Some(ContactMethodOptions.Mobile)
      )(_)

    case CompanyUtrYesNoPage =>
      userAnswers => navigatorFromCompanyUtrYesNoPage(CheckMode)(userAnswers)

    case CompanyUtrPage =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()

    case CompanyCrnYesNoPage =>
      navigatorFromCompanyCrnYesNoPage(CheckMode)(_)

    case CompanyCrnPage =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()

    case CompanyWorksReferenceYesNoPage =>
      userAnswers => navigatorFromCompanyWorksReferenceYesNoPage(CheckMode)(userAnswers)

    case CompanyWorksReferencePage =>
      _ => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()

    case _ =>
      _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
  }

  private def navigatorFromCompanyAddressYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    mode match {
      case AmendMode =>
        userAnswers.get(CompanyAddressYesNoPage) match {
          case Some(true) =>
            controllers.add.company.routes.CompanyAddressController.redirectToAmendAddressLookup()

          case Some(false) =>
            cyaRoute(mode)

          case None =>
            logger.error(
              "[CompanyNavigator][navigatorFromCompanyAddressYesNoPage] - CompanyAddressYesNoPage missing"
            )
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }

      case _ =>
        addressLookupYesNoRoute(
          mode,
          userAnswers.get(CompanyAddressYesNoPage),
          userAnswers.get(CompanyAddressPage).isDefined,
          onYes = controllers.add.company.routes.CompanyAddressController
            .redirectToAddressLookup(NormalMode, None),
          onYesChange = controllers.add.company.routes.CompanyAddressController
            .redirectToAddressLookup(NormalMode, Some(CheckMode.toString)),
          onNo = controllers.add.company.routes.AddCompanyContactMethodsYesNoController
            .onPageLoad(NormalMode),
          checkYourAnswers = controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
        )
    }

  private def navigatorFromCompanyUtrYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyUtrYesNoPage), mode) match {
      case (Some(true), NormalMode) =>
        controllers.add.company.routes.CompanyUtrController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyCrnYesNoController.onPageLoad(NormalMode)

      case (Some(true), CheckMode | AmendMode) =>
        userAnswers
          .get(CompanyUtrPage)
          .fold(controllers.add.company.routes.CompanyUtrController.onPageLoad(mode)) { _ =>
            cyaRoute(mode)
          }

      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case _ =>
        logger.error(
          s"[CompanyNavigator][navigatorFromCompanyUtrYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromCompanyCrnYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyCrnYesNoPage), mode) match {
      case (Some(true), NormalMode) =>
        controllers.add.company.routes.CompanyCrnController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyWorksReferenceYesNoController.onPageLoad(NormalMode)

      case (Some(true), CheckMode | AmendMode) =>
        userAnswers
          .get(CompanyCrnPage)
          .fold(controllers.add.company.routes.CompanyCrnController.onPageLoad(mode)) { _ =>
            cyaRoute(mode)
          }

      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case _ =>
        logger.error(
          s"[CompanyNavigator][navigatorFromCompanyCrnYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromCompanyWorksReferenceYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(CompanyWorksReferenceYesNoPage), mode) match {
      case (Some(true), NormalMode) =>
        controllers.add.company.routes.CompanyWorksReferenceController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()

      case (Some(true), CheckMode | AmendMode) =>
        userAnswers
          .get(CompanyWorksReferencePage)
          .fold(controllers.add.company.routes.CompanyWorksReferenceController.onPageLoad(mode)) { _ =>
            cyaRoute(mode)
          }

      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case _ =>
        logger.error(
          s"[CompanyNavigator][navigatorFromCompanyWorksReferenceYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigatorFromAddCompanyContactMethodsYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(AddCompanyContactMethodsYesNoPage), mode) match {
      case (Some(true), NormalMode) =>
        controllers.add.company.routes.CompanyContactMethodOptionsController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        controllers.add.company.routes.CompanyUtrYesNoController.onPageLoad(NormalMode)

      case (Some(true), CheckMode | AmendMode) =>
        userAnswers
          .get(CompanyContactMethodOptionsPage)
          .fold(controllers.add.company.routes.CompanyContactMethodOptionsController.onPageLoad(mode)) { _ =>
            cyaRoute(mode)
          }

      case (Some(false), CheckMode | AmendMode) =>
        cyaRoute(mode)

      case _ =>
        logger.error(
          s"[CompanyNavigator][navigatorFromAddCompanyContactMethodsYesNoPage] - unexpected state, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def nextSelectedContactMethodPageAfter(
    current: Option[ContactMethodOptions]
  )(userAnswers: UserAnswers): Call =
    navigateFromContactMethodPage(current, userAnswers) { remaining =>
      remaining.headOption.fold {
        controllers.add.company.routes.CompanyUtrYesNoController.onPageLoad(NormalMode)
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
        .getOrElse(cyaRoute(mode))
    }

  private def navigateFromContactMethodPage(
    current: Option[ContactMethodOptions],
    userAnswers: UserAnswers
  )(terminalStep: Seq[ContactMethodOptions] => Call): Call =
    selectedContactMethodsInOrder(userAnswers)
      .filter(_.nonEmpty)
      .fold {
        logger.error(
          "[CompanyNavigator][navigateFromContactMethodPage] - ContactMethodOptions missing or empty"
        )
        routes.JourneyRecoveryController.onPageLoad()
      } { selectedContactMethods =>
        current match {
          case Some(currentContactMethod) if !selectedContactMethods.contains(currentContactMethod) =>
            logger.error(
              "[CompanyNavigator][navigateFromContactMethodPage] - current contact method not selected"
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

  private def selectedContactMethodsInOrder(
    userAnswers: UserAnswers
  ): Option[Seq[ContactMethodOptions]] =
    userAnswers.get(CompanyContactMethodOptionsPage).map(ContactMethodOptions.ordered)

  private def contactMethodPageCall(
    contactMethod: ContactMethodOptions,
    mode: Mode
  ): Call =
    contactMethod match {
      case ContactMethodOptions.Email =>
        controllers.add.company.routes.CompanyEmailAddressController.onPageLoad(mode)

      case ContactMethodOptions.Phone =>
        controllers.add.company.routes.CompanyPhoneNumberController.onPageLoad(mode)

      case ContactMethodOptions.Mobile =>
        controllers.add.company.routes.CompanyMobileNumberController.onPageLoad(mode)
    }

  private def contactMethodPage(
    contactMethod: ContactMethodOptions
  ): QuestionPage[String] =
    contactMethod match {
      case ContactMethodOptions.Email  => CompanyEmailAddressPage
      case ContactMethodOptions.Phone  => CompanyPhoneNumberPage
      case ContactMethodOptions.Mobile => CompanyMobileNumberPage
    }

  private def isMissingAnswer(
    contactMethod: ContactMethodOptions
  )(userAnswers: UserAnswers): Boolean =
    userAnswers.get(contactMethodPage(contactMethod)).isEmpty
}

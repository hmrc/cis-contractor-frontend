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

package navigation.verify

import controllers.routes
import jakarta.inject.Singleton
import models.{AmendMode, CheckMode, Mode, NormalMode, UserAnswers}
import navigation.NavigatorForJourney
import pages.Page
import pages.verify.*
import models.verify.ContractorEmailConfirmationStored.{CurrentEmail, DifferentEmail, DoNotSend}
import pages.insufficient.ProceedInsufficientSubcontractorNameYesNoPage
import pages.unmatched.ProceedSubcontractorVerifyRequestPage
import play.api.mvc.Call

import java.util.UUID
import javax.inject.Inject

@Singleton
class VerifyNavigator @Inject() () extends NavigatorForJourney {

  override def nextPage(page: Page, mode: Mode, userAnswers: models.UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
    case AmendMode  =>
      logger.error(s"[VerifyNavigator][nextPage] - AmendMode not supported, page=$page")
      routes.JourneyRecoveryController.onPageLoad()
  }

  private def normalRoutes: Page => UserAnswers => Call = {

    case ContractorEmailConfirmationNotStoredPage(id)     =>
      userAnswers => navigatorFromContractorEmailConfirmationNotStoredPage(NormalMode, Some(id))(userAnswers)
    case ContractorEmailConfirmationNotStoredPage         =>
      userAnswers => navigatorFromContractorEmailConfirmationNotStoredPage(NormalMode)(userAnswers)
    case SelectSubcontractorPage(id)                      =>
      userAnswers => navigatorFromSelectSubcontractorPage(NormalMode, Some(id))(userAnswers)
    case SelectSubcontractorPage                          =>
      userAnswers => navigatorFromSelectSubcontractorPage(NormalMode)(userAnswers)
    case VerifyYourSubcontractorsYesNoPage(id)            =>
      userAnswers => navigatorFromVerifyYourSubcontractorsYesNoPage(NormalMode, Some(id))(userAnswers)
    case VerifyYourSubcontractorsYesNoPage                =>
      userAnswers => navigatorFromVerifyYourSubcontractorsYesNoPage(NormalMode)(userAnswers)
    case ReverifyExistingSubcontractorsYesNoPage(id)      =>
      userAnswers => navigatorFromReverifyExistingSubcontractorsYesNoPage(NormalMode, Some(id))(userAnswers)
    case ReverifyExistingSubcontractorsYesNoPage          =>
      userAnswers => navigatorFromReverifyExistingSubcontractorsYesNoPage(NormalMode)(userAnswers)
    case ContractorEmailConfirmationStoredPage(id)        =>
      userAnswers => navigatorFromContractorEmailConfirmationStoredPage(NormalMode, Some(id))(userAnswers)
    case ContractorEmailConfirmationStoredPage            =>
      userAnswers => navigatorFromContractorEmailConfirmationStoredPage(NormalMode)(userAnswers)
    case SelectSubcontractorsToReverifyPage(id)           =>
      userAnswers => navigatorFromSelectSubcontractorsToReverifyPage(NormalMode, Some(id))(userAnswers)
    case SelectSubcontractorsToReverifyPage               =>
      userAnswers => navigatorFromSelectSubcontractorsToReverifyPage(NormalMode)(userAnswers)
    case EmailAddressPage(id)                             =>
      _ => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoadWithId(UUID.fromString(id))
    case EmailAddressPage                                 =>
      _ => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()
    case VerificationDeclarationPage                      =>
      _ => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()
    case ProceedInsufficientSubcontractorNameYesNoPage(_) =>
      _ => controllers.verify.routes.ReviewInsufficientInfoSubcontractorsController.onPageLoad()
    case ProceedSubcontractorVerifyRequestPage(_)         =>
      _ => controllers.verify.routes.ReviewUnmatchedSubcontractorsController.onPageLoad()
    case unexpected                                       =>
      _ =>
        logger.error(s"[VerifyNavigator][normalRoutes] - navigator fallback reached, page=$unexpected")
        controllers.routes.JourneyRecoveryController.onPageLoad()
  }

  private def checkRouteMap: Page => UserAnswers => Call = {
    case ContractorEmailConfirmationNotStoredPage(id) =>
      userAnswers => navigatorFromContractorEmailConfirmationNotStoredPage(CheckMode, Some(id))(userAnswers)
    case ContractorEmailConfirmationNotStoredPage     =>
      userAnswers => navigatorFromContractorEmailConfirmationNotStoredPage(CheckMode)(userAnswers)
    case SelectSubcontractorPage(id)                  =>
      userAnswers => navigatorFromSelectSubcontractorPage(CheckMode, Some(id))(userAnswers)
    case SelectSubcontractorPage                      =>
      userAnswers => navigatorFromSelectSubcontractorPage(CheckMode)(userAnswers)
    case VerifyYourSubcontractorsYesNoPage(id)        =>
      userAnswers => navigatorFromVerifyYourSubcontractorsYesNoPage(CheckMode, Some(id))(userAnswers)
    case VerifyYourSubcontractorsYesNoPage            =>
      userAnswers => navigatorFromVerifyYourSubcontractorsYesNoPage(CheckMode)(userAnswers)
    case ReverifyExistingSubcontractorsYesNoPage(id)  =>
      userAnswers => navigatorFromReverifyExistingSubcontractorsYesNoPage(CheckMode, Some(id))(userAnswers)
    case ReverifyExistingSubcontractorsYesNoPage      =>
      userAnswers => navigatorFromReverifyExistingSubcontractorsYesNoPage(CheckMode)(userAnswers)
    case ContractorEmailConfirmationStoredPage(id)    =>
      userAnswers => navigatorFromContractorEmailConfirmationStoredPage(CheckMode, Some(id))(userAnswers)
    case ContractorEmailConfirmationStoredPage        =>
      userAnswers => navigatorFromContractorEmailConfirmationStoredPage(CheckMode)(userAnswers)
    case SelectSubcontractorsToReverifyPage(id)       =>
      userAnswers => navigatorFromSelectSubcontractorsToReverifyPage(CheckMode, Some(id))(userAnswers)
    case SelectSubcontractorsToReverifyPage           =>
      userAnswers => navigatorFromSelectSubcontractorsToReverifyPage(CheckMode)(userAnswers)
    case EmailAddressPage(id)                         =>
      _ => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoadWithId(UUID.fromString(id))
    case EmailAddressPage                             =>
      _ => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()
    case VerificationDeclarationPage                  =>
      _ => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad()
    case unexpected                                   =>
      _ =>
        logger.error(s"[VerifyNavigator][checkRouteMap] - navigator fallback reached, page=$unexpected")
        controllers.routes.JourneyRecoveryController.onPageLoad()
  }

  private def navigatorFromContractorEmailConfirmationNotStoredPage(
    mode: Mode,
    subJourneyId: Option[String] = None
  )(ua: UserAnswers): Call = {
    val page = subJourneyId
      .map(ContractorEmailConfirmationNotStoredPage.apply)
      .getOrElse(ContractorEmailConfirmationNotStoredPage)
    (ua.get(page), mode) match {

      case (Some(true), m) =>
        emailAddressCall(m, subJourneyId)

      case (Some(false), NormalMode) =>
        cyaCall(subJourneyId)

      case (Some(false), CheckMode) =>
        cyaCall(subJourneyId)

      case _ =>
        logger.error(
          s"[VerifyNavigator][navigatorFromContractorEmailConfirmationNotStoredPage] - answer missing, mode=$mode"
        )
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def navigatorFromSelectSubcontractorPage(mode: Mode, subJourneyId: Option[String] = None)(
    ua: UserAnswers
  ): Call = {
    val newestPage   =
      subJourneyId.map(NewestVerificationBatchResponsePage.apply).getOrElse(NewestVerificationBatchResponsePage)
    val selectPage   = subJourneyId.map(SelectSubcontractorPage.apply).getOrElse(SelectSubcontractorPage)
    val rebuildPage  =
      subJourneyId.map(RebuildVerificationFromWarningPage.apply).getOrElse(RebuildVerificationFromWarningPage)
    val reverifyPage =
      subJourneyId.map(SelectSubcontractorsToReverifyPage.apply).getOrElse(SelectSubcontractorsToReverifyPage)

    mode match {

      case NormalMode =>
        val hasSubcontractorsToReverify =
          ua.get(newestPage).exists(_.subcontractors.exists(_.isVerified))

        if (hasSubcontractorsToReverify) {
          reverifyYesNoCall(NormalMode, subJourneyId)
        } else {
          currentBatchCall(NormalMode, subJourneyId)
        }

      case CheckMode =>
        val noSubcontractorsSelected = ua.get(selectPage).forall(_.isEmpty)

        val rebuildingFromWarning = ua.get(rebuildPage).contains(true)

        val noReverificationSelected = ua.get(reverifyPage).forall(_.isEmpty)

        if (rebuildingFromWarning) {
          reverifyYesNoCall(CheckMode, subJourneyId)
        } else if (noSubcontractorsSelected && noReverificationSelected) {
          controllers.verify.routes.NoSubcontractorsSelectedWarningController.onPageLoadCheckMode()
        } else {
          currentBatchCall(CheckMode, subJourneyId)
        }

      case AmendMode =>
        logger.error("[VerifyNavigator][navigatorFromSelectSubcontractorPage] - AmendMode not supported")
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def navigatorFromReverifyExistingSubcontractorsYesNoPage(
    mode: Mode,
    subJourneyId: Option[String] = None
  )(ua: UserAnswers): Call = {
    val page         =
      subJourneyId
        .map(ReverifyExistingSubcontractorsYesNoPage.apply)
        .getOrElse(ReverifyExistingSubcontractorsYesNoPage)
    val reverifyPage =
      subJourneyId.map(SelectSubcontractorsToReverifyPage.apply).getOrElse(SelectSubcontractorsToReverifyPage)
    val selectPage   = subJourneyId.map(SelectSubcontractorPage.apply).getOrElse(SelectSubcontractorPage)

    (ua.get(page), mode) match {

      case (Some(true), NormalMode) =>
        selectToReverifyCall(NormalMode, subJourneyId)

      case (Some(true), CheckMode) =>
        if (ua.get(reverifyPage).exists(_.nonEmpty)) {
          checkReadinessCall(CheckMode, subJourneyId)
        } else {
          selectToReverifyCall(CheckMode, subJourneyId)
        }

      case (Some(false), NormalMode) =>
        if (ua.get(selectPage).forall(_.isEmpty)) {
          controllers.verify.routes.NoSubcontractorsSelectedWarningController.onPageLoad()
        } else {
          currentBatchCall(NormalMode, subJourneyId)
        }

      case (Some(false), CheckMode) =>
        if (ua.get(selectPage).forall(_.isEmpty)) {
          controllers.verify.routes.NoSubcontractorsSelectedWarningController.onPageLoadCheckMode()
        } else {
          currentBatchCall(CheckMode, subJourneyId)
        }

      case _ =>
        logger.error(
          s"[VerifyNavigator][navigatorFromReverifyExistingSubcontractorsYesNoPage] - answer missing, mode=$mode"
        )
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def navigatorFromVerifyYourSubcontractorsYesNoPage(mode: Mode, subJourneyId: Option[String] = None)(
    ua: UserAnswers
  ): Call = {
    val page = subJourneyId.map(VerifyYourSubcontractorsYesNoPage.apply).getOrElse(VerifyYourSubcontractorsYesNoPage)
    (ua.get(page), mode) match {

      case (Some(true), _) =>
        selectToReverifyCall(mode, subJourneyId)

      case (Some(false), NormalMode) =>
        controllers.routes.IndexController.onPageLoad()

      case (Some(false), CheckMode) =>
        controllers.verify.routes.NoSubcontractorsSelectedWarningController.onPageLoadCheckMode()

      case _ =>
        logger.error(s"[VerifyNavigator][navigatorFromVerifyYourSubcontractorsYesNoPage] - answer missing, mode=$mode")
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def navigatorFromSelectSubcontractorsToReverifyPage(
    mode: Mode,
    subJourneyId: Option[String] = None
  )(ua: UserAnswers): Call = {
    val selectPage   = subJourneyId.map(SelectSubcontractorPage.apply).getOrElse(SelectSubcontractorPage)
    val reverifyPage =
      subJourneyId.map(SelectSubcontractorsToReverifyPage.apply).getOrElse(SelectSubcontractorsToReverifyPage)

    val hasSelections =
      ua.get(selectPage).exists(_.nonEmpty) ||
        ua.get(reverifyPage).exists(_.nonEmpty)

    if (hasSelections) {
      mode match {
        case NormalMode =>
          currentBatchCall(NormalMode, subJourneyId)

        case CheckMode =>
          currentBatchCall(CheckMode, subJourneyId)

        case AmendMode =>
          logger.error("[VerifyNavigator][navigatorFromSelectSubcontractorsToReverifyPage] - AmendMode not supported")
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    } else {
      mode match {
        case NormalMode =>
          controllers.verify.routes.NoSubcontractorsSelectedWarningController.onPageLoad()

        case CheckMode =>
          controllers.verify.routes.NoSubcontractorsSelectedWarningController.onPageLoadCheckMode()

        case AmendMode =>
          logger.error("[VerifyNavigator][navigatorFromSelectSubcontractorsToReverifyPage] - AmendMode not supported")
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    }
  }

  private def navigatorFromContractorEmailConfirmationStoredPage(
    mode: Mode,
    subJourneyId: Option[String] = None
  )(ua: UserAnswers): Call = {
    val page =
      subJourneyId.map(ContractorEmailConfirmationStoredPage.apply).getOrElse(ContractorEmailConfirmationStoredPage)
    (ua.get(page), mode) match {

      case (Some(CurrentEmail), NormalMode) =>
        cyaCall(subJourneyId)

      case (Some(CurrentEmail), CheckMode) =>
        cyaCall(subJourneyId)

      case (Some(DifferentEmail), m) =>
        emailAddressCall(m, subJourneyId)

      case (Some(DoNotSend), NormalMode) =>
        cyaCall(subJourneyId)

      case (Some(DoNotSend), CheckMode) =>
        cyaCall(subJourneyId)

      case _ =>
        logger.error(
          s"[VerifyNavigator][navigatorFromContractorEmailConfirmationStoredPage] - answer missing, mode=$mode"
        )
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def currentBatchCall(mode: Mode, subJourneyId: Option[String]): Call =
    subJourneyId
      .map(id =>
        controllers.verify.routes.CurrentVerificationBatchController.onPageLoadWithId(UUID.fromString(id), mode)
      )
      .getOrElse(controllers.verify.routes.CurrentVerificationBatchController.onPageLoad(mode))

  private def reverifyYesNoCall(mode: Mode, subJourneyId: Option[String]): Call =
    subJourneyId
      .map(id =>
        controllers.verify.routes.ReverifyExistingSubcontractorsYesNoController
          .onPageLoadWithId(UUID.fromString(id), mode)
      )
      .getOrElse(controllers.verify.routes.ReverifyExistingSubcontractorsYesNoController.onPageLoad(mode))

  private def selectToReverifyCall(mode: Mode, subJourneyId: Option[String]): Call =
    subJourneyId
      .map(id =>
        controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoadWithId(UUID.fromString(id), mode)
      )
      .getOrElse(controllers.verify.routes.SelectSubcontractorsToReverifyController.onPageLoad(mode))

  private def checkReadinessCall(mode: Mode, subJourneyId: Option[String]): Call =
    subJourneyId
      .map(id =>
        controllers.verify.routes.CheckVerificationBatchReadinessController
          .checkVerificationBatchReadinessWithId(UUID.fromString(id), mode)
      )
      .getOrElse(
        controllers.verify.routes.CheckVerificationBatchReadinessController.checkVerificationBatchReadiness(mode)
      )

  private def emailAddressCall(mode: Mode, subJourneyId: Option[String]): Call =
    subJourneyId
      .map(id => controllers.verify.routes.EmailAddressController.onPageLoadWithId(UUID.fromString(id), mode))
      .getOrElse(controllers.verify.routes.EmailAddressController.onPageLoad(mode))

  private def cyaCall(subJourneyId: Option[String]): Call =
    subJourneyId
      .map(id => controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoadWithId(UUID.fromString(id)))
      .getOrElse(controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad())
}

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

package navigation.contractordetails

import controllers.contractordetails.{routes => contractorDetailsRoutes}
import controllers.routes
import models.{AmendMode, CheckMode, Mode, NormalMode, UserAnswers}
import navigation.NavigatorForJourney
import pages.Page
import pages.contractordetails.*
import play.api.mvc.Call

import java.util.UUID
import javax.inject.{Inject, Singleton}

@Singleton
class ContractorDetailsNavigator @Inject() () extends NavigatorForJourney {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    extractId(page) match {
      case Some(id) =>
        val flowId = UUID.fromString(id)

        mode match {
          case NormalMode =>
            normalRoutesWithId(flowId, id)(page)(userAnswers)

          case CheckMode | AmendMode =>
            checkRoutesWithId(flowId, id)(page)(userAnswers)
        }

      case None =>
        nextPageWithoutId(page, mode, userAnswers)
    }

  private def extractId(page: Page): Option[String] =
    page match {
      case ContractorUtrPage(id)               => Some(id)
      case AddSchemeNameYesNoPage(id)          => Some(id)
      case SchemeNamePage(id)                  => Some(id)
      case AddEmailAddressYesNoPage(id)        => Some(id)
      case EnterContractorEmailAddressPage(id) => Some(id)
      case _                                   => None
    }

  private def checkAnswersWithId(flowId: UUID): Call =
    contractorDetailsRoutes.ContractorDetailsCheckAnswersController
      .onPageLoadWithId(flowId)

  private def finalValidationWithId(flowId: UUID): Call =
    controllers.finalvalidations.routes.ContractorDetailsFinalValidationController.onPageLoad()

  private def normalRoutesWithId(
    flowId: UUID,
    id: String
  ): Page => UserAnswers => Call = {

    case ContractorUtrPage(_) =>
      _ =>
        contractorDetailsRoutes.AddSchemeNameYesNoController
          .onPageLoadWithId(flowId, NormalMode)

    case AddSchemeNameYesNoPage(_) =>
      userAnswers => nextForAddSchemeNameYesNoWithId(flowId, id, NormalMode)(userAnswers)

    case SchemeNamePage(_) =>
      _ =>
        contractorDetailsRoutes.AddEmailAddressYesNoController
          .onPageLoadWithId(flowId, NormalMode)

    case AddEmailAddressYesNoPage(_) =>
      userAnswers => nextForAddEmailAddressYesNoWithId(flowId, id, NormalMode)(userAnswers)

    case EnterContractorEmailAddressPage(_) =>
      _ => checkAnswersWithId(flowId)

    case _ =>
      _ => routes.IndexController.onPageLoad()
  }

  private def checkRoutesWithId(
    flowId: UUID,
    id: String
  ): Page => UserAnswers => Call = {

    case ContractorUtrPage(_) =>
      _ => checkAnswersWithId(flowId)

    case AddSchemeNameYesNoPage(_) =>
      userAnswers => nextForAddSchemeNameYesNoWithId(flowId, id, CheckMode)(userAnswers)

    case SchemeNamePage(_) =>
      _ => checkAnswersWithId(flowId)

    case AddEmailAddressYesNoPage(_) =>
      userAnswers => nextForAddEmailAddressYesNoWithId(flowId, id, CheckMode)(userAnswers)

    case EnterContractorEmailAddressPage(_) =>
      _ => checkAnswersWithId(flowId)

    case _ =>
      _ => checkAnswersWithId(flowId)
  }

  private def nextForAddSchemeNameYesNoWithId(
    flowId: UUID,
    id: String,
    mode: Mode
  )(userAnswers: UserAnswers): Call =
    (userAnswers.get(AddSchemeNameYesNoPage(id)), mode) match {

      case (Some(true), NormalMode) =>
        contractorDetailsRoutes.SchemeNameController
          .onPageLoadWithId(flowId, NormalMode)

      case (Some(false), NormalMode) =>
        contractorDetailsRoutes.AddEmailAddressYesNoController
          .onPageLoadWithId(flowId, NormalMode)

      case (Some(true), CheckMode | AmendMode) =>
        contractorDetailsRoutes.SchemeNameController
          .onPageLoadWithId(flowId, CheckMode)

      case (Some(false), CheckMode | AmendMode) =>
        checkAnswersWithId(flowId)

      case _ =>
        logger.error(
          s"[ContractorDetailsNavigator][nextForAddSchemeNameYesNoWithId] - answer missing or invalid, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def nextForAddEmailAddressYesNoWithId(
    flowId: UUID,
    id: String,
    mode: Mode
  )(userAnswers: UserAnswers): Call =
    (userAnswers.get(AddEmailAddressYesNoPage(id)), mode) match {

      case (Some(true), NormalMode) =>
        contractorDetailsRoutes.EnterContractorEmailAddressController.onPageLoadWithId(flowId, NormalMode)

      case (Some(false), NormalMode) =>
        checkAnswersWithId(flowId)

      case (Some(true), CheckMode | AmendMode) =>
        contractorDetailsRoutes.EnterContractorEmailAddressController.onPageLoadWithId(flowId, CheckMode)

      case (Some(false), CheckMode | AmendMode) =>
        checkAnswersWithId(flowId)

      case _ =>
        logger.error(
          s"[ContractorDetailsNavigator][nextForAddEmailAddressYesNoWithId] - answer missing or invalid, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def nextPageWithoutId(
    page: Page,
    mode: Mode,
    userAnswers: UserAnswers
  ): Call =
    if (
      userAnswers.get(ContractorDetailsValidationTargetPage).isDefined &&
      finalValidationPage(page)
    ) {
      controllers.finalvalidations.routes.ContractorDetailsFinalValidationController.onPageLoad()
    } else {
      mode match {
        case NormalMode =>
          normalRoutes(page)(userAnswers)

        case CheckMode | AmendMode =>
          checkRoutes(page)(userAnswers)
      }
    }

  private def normalRoutes: Page => UserAnswers => Call = {
    case ContractorUtrPage =>
      _ => contractorDetailsRoutes.AddSchemeNameYesNoController.onPageLoad(NormalMode)

    case AddSchemeNameYesNoPage =>
      nextForAddSchemeNameYesNo(NormalMode)

    case SchemeNamePage =>
      _ => contractorDetailsRoutes.AddEmailAddressYesNoController.onPageLoad(NormalMode)

    case AddEmailAddressYesNoPage =>
      nextForAddEmailAddressYesNo(NormalMode)

    case EnterContractorEmailAddressPage =>
      _ => checkAnswers

    case _ =>
      _ => routes.IndexController.onPageLoad()
  }

  private def checkRoutes: Page => UserAnswers => Call = {
    case AddSchemeNameYesNoPage =>
      nextForAddSchemeNameYesNo(CheckMode)

    case AddEmailAddressYesNoPage =>
      nextForAddEmailAddressYesNo(CheckMode)

    case ContractorUtrPage =>
      _ => checkAnswers

    case SchemeNamePage =>
      _ => checkAnswers

    case EnterContractorEmailAddressPage =>
      _ => checkAnswers

    case _ =>
      _ => routes.IndexController.onPageLoad()
  }

  private def checkAnswers: Call =
    contractorDetailsRoutes.ContractorDetailsCheckAnswersController.onPageLoad()

  private def nextForAddSchemeNameYesNo(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(AddSchemeNameYesNoPage), mode) match {

      case (Some(true), NormalMode) =>
        contractorDetailsRoutes.SchemeNameController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        contractorDetailsRoutes.AddEmailAddressYesNoController.onPageLoad(NormalMode)

      case (Some(true), CheckMode | AmendMode) =>
        contractorDetailsRoutes.SchemeNameController.onPageLoad(CheckMode)

      case (Some(false), CheckMode | AmendMode) =>
        checkAnswers

      case _ =>
        logger.error(
          s"[ContractorDetailsNavigator][nextForAddSchemeNameYesNo] - answer missing or invalid, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def nextForAddEmailAddressYesNo(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(AddEmailAddressYesNoPage), mode) match {

      case (Some(true), NormalMode) =>
        contractorDetailsRoutes.EnterContractorEmailAddressController.onPageLoad(NormalMode)

      case (Some(false), NormalMode) =>
        checkAnswers

      case (Some(true), CheckMode | AmendMode) =>
        contractorDetailsRoutes.EnterContractorEmailAddressController.onPageLoad(CheckMode)

      case (Some(false), CheckMode | AmendMode) =>
        checkAnswers

      case _ =>
        logger.error(
          s"[ContractorDetailsNavigator][nextForAddEmailAddressYesNo] - answer missing or invalid, mode=$mode"
        )
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def finalValidationPage(page: Page): Boolean =
    page == ContractorUtrPage ||
      page == SchemeNamePage ||
      page == EnterContractorEmailAddressPage
}

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
import models.TypeOfSubcontractor.{Individualorsoletrader, Limitedcompany, Partnership, Trust}
import models.add.ValidatedSubcontractor
import models.add.company.ValidatedCompany
import models.add.partnership.ValidatedPartnership
import models.add.trust.ValidatedTrust
import models.{AmendMode, CheckMode, Mode, NormalMode, TypeOfSubcontractor, UserAnswers}
import navigation.NavigatorForJourney
import pages.Page
import pages.add.TypeOfSubcontractorPage
import play.api.mvc.Call

import java.util.UUID
import javax.inject.{Inject, Singleton}

@Singleton
class SharedNavigator @Inject() () extends NavigatorForJourney {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
    case AmendMode  =>
      logger.error(s"[SharedNavigator][nextPage] - AmendMode not supported for page=$page")
      routes.JourneyRecoveryController.onPageLoad()
  }

  private val normalRoutes: Page => UserAnswers => Call = {
    case TypeOfSubcontractorPage(id) =>
      userAnswers => navigatorFromTypeOfSubcontractorPage(NormalMode, Some(id))(userAnswers)
    case TypeOfSubcontractorPage     => userAnswers => navigatorFromTypeOfSubcontractorPage(NormalMode)(userAnswers)
    case _                           => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case TypeOfSubcontractorPage(id) => navigatorFromTypeOfSubcontractorPage(CheckMode, Some(id))(_)
    case TypeOfSubcontractorPage     => navigatorFromTypeOfSubcontractorPage(CheckMode)(_)
    case _                           => _ => controllers.add.routes.CheckYourAnswersController.onPageLoad()
  }

  private def navigatorFromTypeOfSubcontractorPage(mode: Mode, id: Option[String] = None)(
    userAnswers: UserAnswers
  ): Call = {
    val selectedType =
      id.map(value => userAnswers.get(TypeOfSubcontractorPage(value)))
        .getOrElse(userAnswers.get(TypeOfSubcontractorPage))
    selectedType match {
      case None                    =>
        logger.error(
          "[SharedNavigator][navigatorFromTypeOfSubcontractorPage] - TypeOfSubcontractorPage missing"
        )
        routes.JourneyRecoveryController.onPageLoad()
      case Some(subcontractorType) =>
        mode match {
          case NormalMode => firstJourneyPageFor(subcontractorType, id)
          case _          =>
            if (journeyComplete(subcontractorType, userAnswers, id)) {
              checkYourAnswersFor(subcontractorType, id)
            } else {
              firstJourneyPageFor(subcontractorType, id)
            }
        }
    }
  }

  private def firstJourneyPageFor(subcontractorType: TypeOfSubcontractor, id: Option[String] = None): Call =
    (subcontractorType, id) match {
      case (Partnership, Some(value))  =>
        controllers.add.partnership.routes.PartnershipNameController
          .onPageLoadWithId(UUID.fromString(value), NormalMode)
      case (Individualorsoletrader, _) =>
        controllers.add.routes.IndividualNamesOptionsController.onPageLoad(NormalMode)
      case (Limitedcompany, _)         => controllers.add.company.routes.CompanyNameController.onPageLoad(NormalMode)
      case (Partnership, _)            => controllers.add.partnership.routes.PartnershipNameController.onPageLoad(NormalMode)
      case (Trust, _)                  => controllers.add.trust.routes.TrustNameController.onPageLoad(NormalMode)
    }

  private def checkYourAnswersFor(subcontractorType: TypeOfSubcontractor, id: Option[String] = None): Call =
    (subcontractorType, id) match {
      case (Partnership, Some(value))  =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController
          .onPageLoadWithId(UUID.fromString(value))
      case (Individualorsoletrader, _) => controllers.add.routes.CheckYourAnswersController.onPageLoad()
      case (Limitedcompany, _)         => controllers.add.company.routes.CompanyCheckYourAnswersController.onPageLoad()
      case (Partnership, _)            =>
        controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad()
      case (Trust, _)                  => controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad()
    }

  private def journeyComplete(
    subcontractorType: TypeOfSubcontractor,
    userAnswers: UserAnswers,
    id: Option[String] = None
  ): Boolean =
    subcontractorType match {
      case Individualorsoletrader => ValidatedSubcontractor.build(userAnswers).isRight
      case Limitedcompany         => ValidatedCompany.build(userAnswers).isRight
      case Partnership            =>
        id.map(ValidatedPartnership.build(userAnswers, _)).getOrElse(ValidatedPartnership.build(userAnswers)).isRight
      case Trust                  => ValidatedTrust.build(userAnswers).isRight
    }

}

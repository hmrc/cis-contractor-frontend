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

package viewmodels.amend

import config.FrontendAppConfig
import models.amend.AmendJourneyType

case class AmendConfirmationLink(
  url: String,
  textKey: String,
  showBeforeYouGo: Boolean
)

object AmendConfirmationLinks {

  def build(
             journeyType: AmendJourneyType,
             cisId: String,
             appConfig: FrontendAppConfig
           ): AmendConfirmationLink =
    journeyType match {

      case AmendJourneyType.Standard =>
        AmendConfirmationLink(
          url = appConfig.manageYourSubcontractorsUrl(cisId),
          textKey = "amendConfirmation.yourSubcontractors",
          showBeforeYouGo = true
        )

      case AmendJourneyType.InsufficientInfo =>
        AmendConfirmationLink(
          url =
            controllers.verify.routes
              .ReviewInsufficientInfoSubcontractorsController
              .onPageLoad()
              .url,
          textKey =
            "insufficientSubcontractorDetailsUpdated.cannotVerifyAllSubcontractors",
          showBeforeYouGo = false
        )

      case AmendJourneyType.UnmatchedInfo =>
        AmendConfirmationLink(
          url =
            controllers.verify.routes
              .ReviewUnmatchedSubcontractorsRoutingController
              .onPageLoad()
              .url,
          textKey =
            "unmatched.unmatchedSubcontractorDetailsUpdated.reviewUnmatchedSubcontractors",
          showBeforeYouGo = false
        )
    }
}

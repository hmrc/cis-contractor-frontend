/*
 * Copyright 2025 HM Revenue & Customs
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

package navigation

import javax.inject.{Inject, Singleton}
import pages.*
import models.*
import pages.add.IndividualJourney
import pages.add.company.CompanyJourney
import pages.add.partnership.PartnershipJourney
import pages.add.trust.TrustJourney
import play.api.mvc.Call

@Singleton
class Navigator @Inject() (
  individualNavigator: navigation.add.IndividualNavigator,
  companyNavigator: navigation.add.CompanyNavigator,
  partnershipNavigator: navigation.add.PartnershipNavigator,
  trustNavigator: navigation.add.TrustNavigator,
  sharedNavigator: navigation.add.SharedNavigator
) {

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    navigatorFor(page).nextPage(page, mode, userAnswers)

  private def navigatorFor(page: Page): NavigatorForJourney =
    page match {
      case _: IndividualJourney  => individualNavigator
      case _: CompanyJourney     => companyNavigator
      case _: PartnershipJourney => partnershipNavigator
      case _: TrustJourney       => trustNavigator
      case _                     => sharedNavigator
    }
}

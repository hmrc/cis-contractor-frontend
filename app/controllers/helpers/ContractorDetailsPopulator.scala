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

package controllers.helpers

import models.{Scheme, UserAnswers}
import pages.contractordetails.*

object ContractorDetailsPopulator {

  def populate(
    userAnswers: UserAnswers,
    scheme: Scheme
  ): UserAnswers =
    (
      for {
        ua1 <- userAnswers.set(
                 ContractorUtrPage,
                 scheme.utr.get
               )

        ua2 <- ua1.set(
                 AddSchemeNameYesNoPage,
                 scheme.name.isDefined
               )

        ua3 <- ua2.set(
                 SchemeNamePage,
                 scheme.name.getOrElse("")
               )

        ua4 <- ua3.set(
                 AddEmailAddressYesNoPage,
                 scheme.emailAddress.isDefined
               )

        ua5 <- ua4.set(
                 EnterContractorEmailAddressPage,
                 scheme.emailAddress.getOrElse("")
               )
      } yield ua5
    ).getOrElse(userAnswers)
}

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

import scala.util.{Success, Try}

object ContractorDetailsPopulator {

  private def nonBlank(value: Option[String]): Option[String] =
    value.map(_.trim).filter(_.nonEmpty)

  def populate(
    userAnswers: UserAnswers,
    scheme: Scheme
  ): Try[UserAnswers] = {

    val utr   = nonBlank(scheme.utr)
    val name  = nonBlank(scheme.name)
    val email = nonBlank(scheme.emailAddress)

    for {
      contractorUtr <- Try(
                         utr.getOrElse(
                           throw new IllegalArgumentException(
                             "Cannot populate contractor details without UTR"
                           )
                         )
                       )

      ua1 <- userAnswers.set(ContractorUtrPage, contractorUtr)

      ua2 <- ua1.set(AddSchemeNameYesNoPage, name.isDefined)

      ua3 <-
        name match {
          case Some(value) =>
            ua2.set(SchemeNamePage, value)

          case None =>
            Success(ua2)
        }

      ua4 <- ua3.set(AddEmailAddressYesNoPage, email.isDefined)

      ua5 <-
        email match {
          case Some(value) =>
            ua4.set(
              EnterContractorEmailAddressPage,
              value
            )

          case None => Success(ua4)
        }

    } yield ua5
  }
}

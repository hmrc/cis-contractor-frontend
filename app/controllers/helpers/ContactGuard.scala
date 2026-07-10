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

import play.api.mvc.Result
import controllers.routes
import models.contact.{ContactMethodOptions, ContactOptions}
import play.api.mvc.Results.Redirect

trait ContactGuard {

  def requireContactChoice[A](
    name: Option[A],
    contactChoice: Option[ContactOptions],
    expected: ContactOptions
  )(onSuccess: A => Result): Result =
    (for {
      n  <- name
      cc <- contactChoice
      if cc == expected
    } yield onSuccess(n))
      .getOrElse(
        Redirect(routes.JourneyRecoveryController.onPageLoad())
      )

  def requireContactMethodInSet[A](
    name: Option[A],
    contactMethods: Option[Set[ContactMethodOptions]],
    expected: ContactMethodOptions
  )(onSuccess: A => Result): Result =
    (for {
      n       <- name
      methods <- contactMethods
      if methods.contains(expected)
    } yield onSuccess(n))
      .getOrElse(
        Redirect(routes.JourneyRecoveryController.onPageLoad())
      )
}

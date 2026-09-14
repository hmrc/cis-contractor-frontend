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

package controllers.actions

import controllers.routes
import models.{AmendMode, Mode}
import models.requests.DataRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import queries.AmendSubbieResourceRefQuery

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RedirectUnmatchSubbieRefActionFilter(
  mode: Mode,
  subbieResourceRef: Long,
  protected val executionContext: ExecutionContext
) extends ActionFilter[DataRequest] {
  override protected def filter[A](request: DataRequest[A]): Future[Option[Result]] =
    if (mode == AmendMode && !request.userAnswers.get(AmendSubbieResourceRefQuery).contains(subbieResourceRef)) {
      Future.successful(Option(Redirect(routes.JourneyRecoveryController.onPageLoad())))
    } else {
      Future.successful(None)
    }
}

class RedirectUnmatchSubbieRefActionFilterProvider @Inject() (executionContext: ExecutionContext) {
  def apply(mode: Mode, subbieResourceRef: Long): RedirectUnmatchSubbieRefActionFilter =
    new RedirectUnmatchSubbieRefActionFilter(mode, subbieResourceRef, executionContext)
}

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

import base.SpecBase
import config.FrontendAppConfig
import models.requests.DataRequest
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MultiTabActionSpec extends SpecBase with MockitoSugar {

  private def dataRequest =
    DataRequest(FakeRequest(), userAnswersId, userAnswersWithCisId)

  "MultiTabAction" - {

    "allows the request when multi-tab is enabled" in {
      val appConfig = mock[FrontendAppConfig]
      when(appConfig.multiTabEnabled).thenReturn(true)

      val result = new MultiTabAction(appConfig).invokeBlock(
        dataRequest,
        _ => Future.successful(Results.Ok)
      )

      status(result) mustBe OK
    }

    "sends the user to manage subcontractors when multi-tab is disabled" in {
      val appConfig  = mock[FrontendAppConfig]
      val landingUrl = "http://localhost:6996/construction-industry-scheme/management/manage-subcontractors/1"
      when(appConfig.multiTabEnabled).thenReturn(false)
      when(appConfig.manageSubcontractorsLandingUrl("1")).thenReturn(landingUrl)

      val result = new MultiTabAction(appConfig).invokeBlock(
        dataRequest,
        _ => Future.successful(Results.Ok)
      )

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(landingUrl)
    }
  }
}

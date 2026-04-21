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

package controllers.verify

import base.SpecBase
import models.NormalMode
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery
import views.html.verify.NoSubcontractorsAddedView

class NoSubcontractorsAddedControllerSpec extends SpecBase {

  "NoSubcontractorsAdded Controller" - {

    "must return OK and the correct view for a GET" in {

      val cisId = "T1234567"

      val userAnswers =
        emptyUserAnswers.set(CisIdQuery, cisId).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      val addSubcontractorsUrl =
        controllers.add.routes.TypeOfSubcontractorController
          .onPageLoad(NormalMode)
          .url

      val manageSubcontractorsUrl =
        s"${applicationConfig.manageSubcontractorsUrl}/$cisId"

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.verify.routes.NoSubcontractorsAddedController.onPageLoad().url
          )

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[NoSubcontractorsAddedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(addSubcontractorsUrl, manageSubcontractorsUrl)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to Journey Recovery when CisId is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.verify.routes.NoSubcontractorsAddedController.onPageLoad().url
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

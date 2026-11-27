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

package controllers

import base.{SpecBase, WithFakeCisTaxpayer}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.VerifyCisIdQuery
import views.html.UnmatchedSubcontractorsView

import java.util.UUID

class UnmatchedSubcontractorsControllerSpec extends SpecBase with WithFakeCisTaxpayer {

  private val flowId = UUID.fromString("11111111-1111-1111-1111-111111111111")
  private val cisId  = "AB12"

  "UnmatchedSubcontractors Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.UnmatchedSubcontractorsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnmatchedSubcontractorsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          controllers.verify.routes.CheckVerificationResultsController.onPageLoad().url
        )(request, messages(application)).toString
      }
    }

    "must return OK for a GET with no existing data" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.UnmatchedSubcontractorsController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK and the correct view for a GET with id" in {
      val answers     =
        emptyUserAnswers.set(VerifyCisIdQuery(flowId.toString), cisId).success.value
      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.UnmatchedSubcontractorsController.onPageLoadWithId(flowId).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnmatchedSubcontractorsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          controllers.verify.routes.CheckVerificationResultsController.onPageLoadWithId(flowId).url
        )(request, messages(application)).toString
      }
    }
  }
}

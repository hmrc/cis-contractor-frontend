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
import play.utils.UriEncoding
import queries.{CisIdQuery, VerifyCisIdQuery}
import views.html.NoUnmatchedSubcontractorsView

import java.util.UUID

class NoUnmatchedSubcontractorsControllerSpec extends SpecBase with WithFakeCisTaxpayer {

  "NoUnmatchedSubcontractors Controller" - {

    "must return OK and the correct view for a GET" in {
      val cisId       = "1"
      val userAnswers = emptyUserAnswers.set(CisIdQuery, cisId).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val manageSubcontractorsUrl =
        s"${applicationConfig.manageSubcontractorsUrl}/${UriEncoding.encodePathSegment(cisId, "UTF-8")}"

      running(application) {
        val request = FakeRequest(GET, routes.NoUnmatchedSubcontractorsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NoUnmatchedSubcontractorsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(manageSubcontractorsUrl)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery when CisId is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            routes.NoUnmatchedSubcontractorsController.onPageLoad().url
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.NoUnmatchedSubcontractorsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the correct view for a GET with id" in {
      val flowId      = UUID.fromString("11111111-1111-1111-1111-111111111111")
      val cisId       = "AB12"
      val userAnswers = emptyUserAnswers.set(VerifyCisIdQuery(flowId.toString), cisId).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val manageSubcontractorsUrl =
        s"${applicationConfig.manageSubcontractorsUrl}/${UriEncoding.encodePathSegment(cisId, "UTF-8")}"

      running(application) {
        val request = FakeRequest(GET, routes.NoUnmatchedSubcontractorsController.onPageLoadWithId(flowId).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NoUnmatchedSubcontractorsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(manageSubcontractorsUrl)(request, messages(application)).toString
      }
    }
  }
}

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

package controllers.contractordetails

import base.SpecBase
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.contractordetails.ContractorDetailsView

import scala.concurrent.Future

class ContractorDetailsControllerSpec extends SpecBase with MockitoSugar {

  "ContractorDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.contractordetails.routes.ContractorDetailsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContractorDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must create user answers and redirect to enter contractor UTR on a POST" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(
        additionalBindings = Seq(bind[SessionRepository].toInstance(mockSessionRepository))
      ).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.contractordetails.routes.ContractorDetailsController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.contractordetails.routes.ContractorUtrController.onPageLoad(NormalMode).url
      }
    }

    "must keep existing user answers and redirect to enter contractor UTR on a POST" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        additionalBindings = Seq(bind[SessionRepository].toInstance(mockSessionRepository))
      ).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.contractordetails.routes.ContractorDetailsController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.contractordetails.routes.ContractorUtrController.onPageLoad(NormalMode).url
      }
    }
  }
}

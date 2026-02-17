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

package controllers.add.partnership

import base.SpecBase
import controllers.routes
import forms.add.partnership.PartnershipNominatedPartnerUtrFormProvider
import models.{NormalMode, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.partnership.{PartnershipNominatedPartnerNamePage, PartnershipNominatedPartnerUtrPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class PartnershipNominatedPartnerUtrControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new PartnershipNominatedPartnerUtrFormProvider()
  private val form         = formProvider()

  private val partnershipName = "Some Partners LLP"

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(PartnershipNominatedPartnerNamePage, partnershipName).success.value

  lazy private val nominatedPartnerUtrRoute =
    controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController.onPageLoad(NormalMode).url

  "PartnershipNominatedPartnerUtrController" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers =
        UserAnswers(userAnswersId).set(PartnershipNominatedPartnerNamePage, partnershipName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, nominatedPartnerUtrRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[views.html.add.partnership.PartnershipNominatedPartnerUtrView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, partnershipName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(PartnershipNominatedPartnerUtrPage, "answer")
        .flatMap(_.set(PartnershipNominatedPartnerNamePage, partnershipName))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, nominatedPartnerUtrRoute)

        val view = application.injector.instanceOf[views.html.add.partnership.PartnershipNominatedPartnerUtrView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, partnershipName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must bind the form and redirect to Check Your Answers on POST when valid UTR is submitted" in {

      val validValue = "5860920998"

      val application =
        applicationBuilder(userAnswers = Some(uaWithName))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, nominatedPartnerUtrRoute)
            .withFormUrlEncodedBody(("value", validValue))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(
          result
        ).value mustEqual controllers.add.partnership.routes.PartnershipNominatedPartnerUtrController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, nominatedPartnerUtrRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[views.html.add.partnership.PartnershipNominatedPartnerUtrView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, partnershipName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, nominatedPartnerUtrRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, nominatedPartnerUtrRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

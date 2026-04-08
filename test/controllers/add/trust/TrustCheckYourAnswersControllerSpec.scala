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

package controllers.add.trust

import base.SpecBase
import controllers.routes
import models.add.TypeOfSubcontractor
import models.contact.ContactOptions
import pages.add.TypeOfSubcontractorPage
import pages.add.trust.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.add.trust.TrustCheckYourAnswersView

class TrustCheckYourAnswersControllerSpec extends SpecBase {

  "TrustCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET when answers are valid" in {

      val validUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
          .success
          .value
          .set(TrustNamePage, "Test Trust")
          .success
          .value
          .set(TrustAddressYesNoPage, false)
          .success
          .value
          .set(TrustContactOptionsPage, ContactOptions.NoDetails)
          .success
          .value
          .set(TrustUtrYesNoPage, false)
          .success
          .value
          .set(TrustWorksReferenceYesNoPage, false)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(validUa)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TrustCheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET when validation fails (incomplete / URL-hopped CYA)" in {

      val invalidUa =
        emptyUserAnswers
          .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Trust)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(invalidUa)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.trust.routes.TrustCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

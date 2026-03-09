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
import models.add.TypeOfSubcontractor
import pages.add.TypeOfSubcontractorPage
import pages.add.partnership.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class PartnershipCheckYourAnswersControllerSpec extends SpecBase {

  "PartnershipCheckYourAnswers Controller" - {

    // TODO: extend and fix
    "must return OK and the correct view for a GET when partnership data is present" ignore {

      val ua = emptyUserAnswers
        .set(TypeOfSubcontractorPage, TypeOfSubcontractor.Partnership)
        .success
        .value
        .set(PartnershipNamePage, "Test Partnership")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Test Partnership")
      }
    }

    // TODO: extend and fix
    "must return OK and render empty summary when no partnership data is present" ignore {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Check your answers")
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.add.partnership.routes.PartnershipCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("/there-is-a-problem")
      }
    }
  }
}

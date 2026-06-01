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

///*
// * Copyright 2026 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */

package controllers.contractordetails

import base.SpecBase
import org.scalatestplus.mockito.MockitoSugar
import pages.contractordetails.{ContractorUtrPage, EnterContractorEmailAddressPage, SchemeNamePage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.checkAnswers.contractordetails.*
import views.html.contractordetails.ContractorDetailsCheckAnswersView

class ContractorDetailsCheckAnswersControllerSpec extends SpecBase with MockitoSugar {

  "ContractorDetailsCheckAnswersController" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers =
        emptyUserAnswers
          .set(ContractorUtrPage, "1234567890")
          .success
          .value
          .set(SchemeNamePage, "Scheme ABC")
          .success
          .value
          .set(EnterContractorEmailAddressPage, "test@mail.com")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.contractordetails.routes.ContractorDetailsCheckAnswersController.onPageLoad().url
          )

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContractorDetailsCheckAnswersView]

        val expectedSummaryRows = Seq(
          ContractorUtrSummary.row(userAnswers)(messages(application)),
          SchemeNameSummary.row(userAnswers)(messages(application)),
          EnterContractorEmailAddressSummary.row(userAnswers)(messages(application))
        ).flatten

        val expectedViewModel =
          ContractorDetailsCheckAnswersViewModel(
            accountsOfficeReference = "123 PA 87654321",
            uniqueTaxpayerReference = "1234444555",
            schemeName = "\tScheme 123",
            email = "test@business.com"
          )

        status(result) mustEqual OK

        val rendered =
          view(expectedViewModel, expectedSummaryRows)(
            request,
            applicationConfig,
            messages(application)
          ).toString

        contentAsString(result) mustEqual rendered
      }
    }
  }
}

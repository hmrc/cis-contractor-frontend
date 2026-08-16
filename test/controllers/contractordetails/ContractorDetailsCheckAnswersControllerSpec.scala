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
import org.scalatestplus.mockito.MockitoSugar
import pages.contractordetails.{AccountsOfficeReferencePage, ContractorUtrPage, EnterContractorEmailAddressPage, SchemeNamePage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class ContractorDetailsCheckAnswersControllerSpec extends SpecBase with MockitoSugar {

  val accountsOfficeReference = "123 PA 87654321"

  "ContractorDetailsCheckAnswersController" - {

    "must return OK and the correct view for a GET when answers exist" in {

      val userAnswers =
        emptyUserAnswers
          .set(AccountsOfficeReferencePage, "123 PA 87654321")
          .success
          .value
          .set(ContractorUtrPage, "1234567890")
          .success
          .value
          .set(SchemeNamePage, "Scheme ABC")
          .success
          .value
          .set(EnterContractorEmailAddressPage, "test@mail.com")
          .success
          .value

      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(
          GET,
          controllers.contractordetails.routes.ContractorDetailsCheckAnswersController.onPageLoad().url
        )

        val result = route(application, request).value
        val body   = contentAsString(result)

        status(result) mustEqual OK

        body must include("123 PA 87654321")
        body must include("1234567890")
        body must include("Scheme ABC")
        body must include("test@mail.com")
      }
    }

    "must redirect to JourneyRecovery when AccountsOfficeReferencePage is missing" in {

      val application =
        applicationBuilder(Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(
          GET,
          routes.ContractorDetailsCheckAnswersController.onPageLoad().url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must render the page when only AccountsOfficeReferencePage exists" in {

      val userAnswers =
        emptyUserAnswers
          .set(AccountsOfficeReferencePage, "123 PA 87654321")
          .success
          .value

      val application =
        applicationBuilder(Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(
          GET,
          routes.ContractorDetailsCheckAnswersController.onPageLoad().url
        )

        val result = route(application, request).value

        status(result) mustEqual OK

        val body = contentAsString(result)

        body must include("123 PA 87654321")
      }
    }
  }
}

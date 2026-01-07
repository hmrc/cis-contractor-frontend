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

package controllers.add

import base.SpecBase
import controllers.routes
import pages.add.*
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.checkAnswers.add.*
import viewmodels.govuk.SummaryListFluency
import viewmodels.govuk.summarylist.*
import views.html.add.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET with empty answers" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        implicit val messages: Messages =
          application.injector.instanceOf[MessagesApi].preferred(request)

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val expectedList = SummaryListViewModel(rows = Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(expectedList)(request, messages).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must include the decision ('Yes/No') rows when the corresponding answers are false" in {
      val uaWithNoAnswers =
        emptyUserAnswers
          .set(SubTradingNameYesNoPage, false).success.value
          .set(SubAddressYesNoPage, false).success.value
          .set(NationalInsuranceNumberYesNoPage, false).success.value
          .set(UniqueTaxpayerReferenceYesNoPage, false).success.value
          .set(WorksReferenceNumberYesNoPage, false).success.value
          .set(SubcontractorContactDetailsYesNoPage, false).success.value

      val application = applicationBuilder(userAnswers = Some(uaWithNoAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        implicit val messages: Messages =
          application.injector.instanceOf[MessagesApi].preferred(request)

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val subTradingNameYesNoRow =
          uaWithNoAnswers.get(SubTradingNameYesNoPage).filter(_ == false).flatMap(_ => SubTradingNameYesNoSummary.row(uaWithNoAnswers))
        val subAddressYesNoRow =
          uaWithNoAnswers.get(SubAddressYesNoPage).filter(_ == false).flatMap(_ => SubAddressYesNoSummary.row(uaWithNoAnswers))
        val ninoYesNoRow =
          uaWithNoAnswers.get(NationalInsuranceNumberYesNoPage).filter(_ == false).flatMap(_ => NationalInsuranceNumberYesNoSummary.row(uaWithNoAnswers))
        val utrYesNoRow =
          uaWithNoAnswers.get(UniqueTaxpayerReferenceYesNoPage).filter(_ == false).flatMap(_ => UniqueTaxpayerReferenceYesNoSummary.row(uaWithNoAnswers))
        val wrnYesNoRow =
          uaWithNoAnswers.get(WorksReferenceNumberYesNoPage).filter(_ == false).flatMap(_ => WorksReferenceNumberYesNoSummary.row(uaWithNoAnswers))
        val subContactDetailsYesNoRow =
          uaWithNoAnswers.get(SubcontractorContactDetailsYesNoPage).filter(_ == false).flatMap(_ => SubcontractorContactDetailsYesNoSummary.row(uaWithNoAnswers))

        val expectedList = SummaryListViewModel(
          rows = Seq(
            TypeOfSubcontractorSummary.row(uaWithNoAnswers),
            SubcontractorNameSummary.row(uaWithNoAnswers),
            subTradingNameYesNoRow,
            TradingNameOfSubcontractorSummary.row(uaWithNoAnswers),
            subAddressYesNoRow,
            AddressOfSubcontractorSummary.row(uaWithNoAnswers),
            ninoYesNoRow,
            SubNationalInsuranceNumberSummary.row(uaWithNoAnswers),
            utrYesNoRow,
            SubcontractorsUniqueTaxpayerReferenceSummary.row(uaWithNoAnswers),
            wrnYesNoRow,
            WorksReferenceNumberSummary.row(uaWithNoAnswers),
            subContactDetailsYesNoRow,
            SubContactDetailsSummary.row(uaWithNoAnswers)
          ).flatten
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(expectedList)(request, messages).toString
      }
    }

    "must not include the decision ('Yes/No') rows when the corresponding answers are true" in {
      val uaWithYesAnswers =
        emptyUserAnswers
          .set(SubTradingNameYesNoPage, true).success.value
          .set(SubAddressYesNoPage, true).success.value
          .set(NationalInsuranceNumberYesNoPage, true).success.value
          .set(UniqueTaxpayerReferenceYesNoPage, true).success.value
          .set(WorksReferenceNumberYesNoPage, true).success.value
          .set(SubcontractorContactDetailsYesNoPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(uaWithYesAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.add.routes.CheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        implicit val messages: Messages =
          application.injector.instanceOf[MessagesApi].preferred(request)

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val expectedList = SummaryListViewModel(
          rows = Seq(
            TypeOfSubcontractorSummary.row(uaWithYesAnswers),
            SubcontractorNameSummary.row(uaWithYesAnswers),
            TradingNameOfSubcontractorSummary.row(uaWithYesAnswers),
            AddressOfSubcontractorSummary.row(uaWithYesAnswers),
            SubNationalInsuranceNumberSummary.row(uaWithYesAnswers),
            SubcontractorsUniqueTaxpayerReferenceSummary.row(uaWithYesAnswers),
            WorksReferenceNumberSummary.row(uaWithYesAnswers),
            SubContactDetailsSummary.row(uaWithYesAnswers)
          ).flatten
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(expectedList)(request, messages).toString
      }
    }

    "must redirect back to Check Your Answers on submit (POST)" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      // update this test later when submit will be implemented
      running(application) {
        val request = FakeRequest(POST, controllers.add.routes.CheckYourAnswersController.onSubmit().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.add.routes.CheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery on submit (POST) if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, controllers.add.routes.CheckYourAnswersController.onSubmit().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}


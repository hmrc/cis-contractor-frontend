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
import forms.add.partnership.PartnershipUtrFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.partnership.{PartnershipNamePage, PartnershipUniqueTaxpayerReferencePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SubcontractorService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.add.partnership.PartnershipUniqueTaxpayerReferenceView

import scala.concurrent.Future

class PartnershipUniqueTaxpayerReferenceControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new PartnershipUtrFormProvider()
  private val form         = formProvider()

  private val partnershipName = "Some Partners LLP"

  private def uaWithName: UserAnswers =
    emptyUserAnswers.set(PartnershipNamePage, partnershipName).success.value

  lazy private val partnershipUniqueTaxpayerReferenceRoute =
    controllers.add.partnership.routes.PartnershipUniqueTaxpayerReferenceController.onPageLoad(NormalMode).url

  lazy private val partnershipWorksReferenceNumberYesNoRoute =
    controllers.add.partnership.routes.PartnershipWorksReferenceNumberYesNoController.onPageLoad(NormalMode).url

  "PartnershipUniqueTaxpayerReferenceControllerSpec Controller" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers =
        UserAnswers(userAnswersId).set(PartnershipNamePage, partnershipName).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnershipUniqueTaxpayerReferenceRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnershipUniqueTaxpayerReferenceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, partnershipName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(PartnershipUniqueTaxpayerReferencePage, "answer")
        .flatMap(_.set(PartnershipNamePage, partnershipName))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnershipUniqueTaxpayerReferenceRoute)

        val view = application.injector.instanceOf[PartnershipUniqueTaxpayerReferenceView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, partnershipName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must bind the form and redirect to JourneyRecovery Page on POST when valid UTR is submitted" in {

      val validValue = "5860920998"

      val mockSubcontractorService = mock[SubcontractorService]

      when(mockSubcontractorService.isDuplicateUTR(any[UserAnswers], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(false))

      val application =
        applicationBuilder(userAnswers = Some(uaWithName))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipUniqueTaxpayerReferenceRoute)
            .withFormUrlEncodedBody(("value", validValue))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual partnershipWorksReferenceNumberYesNoRoute
      }

      verify(mockSubcontractorService).isDuplicateUTR(any[UserAnswers], any[String])(any[HeaderCarrier])
      verifyNoMoreInteractions(mockSubcontractorService)
    }

    "must return a Bad Request and show duplicate error when when utr already exists" in {

      val duplicatedUTR = "8888888888"

      val mockSubcontractorService = mock[SubcontractorService]

      when(mockSubcontractorService.isDuplicateUTR(any[UserAnswers], any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(uaWithName))
          .overrides(
            bind[SubcontractorService].toInstance(mockSubcontractorService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipUniqueTaxpayerReferenceRoute)
            .withFormUrlEncodedBody(
              ("value", duplicatedUTR),
              ("partnershipName", partnershipName)
            )

        val boundForm = form
          .bind(
            Map(
              ("value", duplicatedUTR),
              ("partnershipName", partnershipName)
            )
          )

        val formWithDuplicateError =
          boundForm.withError("value", "partnershipUniqueTaxpayerReference.error.duplicate")

        val view = application.injector.instanceOf[PartnershipUniqueTaxpayerReferenceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(formWithDuplicateError, NormalMode, partnershipName)(
          request,
          messages(application)
        ).toString
      }

      verify(mockSubcontractorService).isDuplicateUTR(any[UserAnswers], any[String])(any[HeaderCarrier])
      verifyNoMoreInteractions(mockSubcontractorService)
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(uaWithName)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipUniqueTaxpayerReferenceRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PartnershipUniqueTaxpayerReferenceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, partnershipName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, partnershipUniqueTaxpayerReferenceRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, partnershipUniqueTaxpayerReferenceRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

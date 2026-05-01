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

package controllers.verify

import base.SpecBase
import controllers.routes
import models._
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import models.response.GetNewestVerificationBatchResponse
import pages.verify.{EmailAddressPage, NewestVerificationBatchResponsePage}
import play.api.test.Helpers.*
import repositories.SessionRepository

import scala.concurrent.Future

class EmailAddressControllerSpec extends SpecBase with MockitoSugar {

  private val onwardRoute: Call = Call("GET", "/foo")

  private lazy val routeUrl =
    controllers.verify.routes.EmailAddressController.onPageLoad(NormalMode).url

  private def scheme(email: Option[String]) = ContractorScheme(
    accountsOfficeReference = Some("accRef"),
    utr = None,
    name = None,
    emailAddress = email
  )

  private def response(email: Option[String]) = GetNewestVerificationBatchResponse(
    scheme = Some(scheme(email)),
    subcontractors = Seq.empty,
    verificationBatch = None,
    verifications = Seq.empty,
    submission = None,
    monthlyReturn = None
  )

  private def ua(email: Option[String]): UserAnswers =
    emptyUserAnswers
      .set(
        NewestVerificationBatchResponsePage,
        response(email)
      )
      .success
      .value

  "EmailAddressController" - {

    "must return OK and show stored hint when email exists" in {
      val app = applicationBuilder(userAnswers = Some(ua(Some("stored@test.com")))).build()

      running(app) {
        val request = FakeRequest(GET, routeUrl)
        val result  = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result) must include(
          messages(app)("verify.emailAddress.hint")
        )
      }
    }

    "must return OK and show notStored hint when email missing" in {
      val app = applicationBuilder(userAnswers = Some(ua(None))).build()

      running(app) {
        val request = FakeRequest(GET, routeUrl)
        val result  = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result) must include(
          messages(app)("verify.emailAddress.hint.notStored")
        )
      }
    }

    "must redirect on valid submit" in {

      val mockSessionRepo = mock[SessionRepository]
      val mockNavigator   = mock[Navigator]

      when(mockSessionRepo.set(any())) thenReturn Future.successful(true)
      when(mockNavigator.nextPage(any(), any(), any())) thenReturn onwardRoute

      val app =
        applicationBuilder(userAnswers = Some(ua(Some("stored@test.com"))))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepo),
            bind[Navigator].toInstance(mockNavigator)
          )
          .build()

      running(app) {
        val request =
          FakeRequest(POST, routeUrl)
            .withFormUrlEncodedBody("value" -> "new@test.com")

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must prefill the form when EmailAddressPage has a value" in {

      val userAnswers =
        emptyUserAnswers
          .set(EmailAddressPage, "stored@test.com")
          .success
          .value

      val app = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(app) {
        val request = FakeRequest(GET, routeUrl)
        val result  = route(app, request).value

        status(result) mustEqual OK

        val html = contentAsString(result)

        html must include("""value="stored@test.com"""")
      }
    }

    "must return BAD_REQUEST on invalid submit" in {

      val app = applicationBuilder(userAnswers = Some(ua(Some("stored@test.com")))).build()

      running(app) {
        val request =
          FakeRequest(POST, routeUrl)
            .withFormUrlEncodedBody("value" -> "")

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must redirect to Journey Recovery when no UserAnswers exist (GET)" in {

      val app = applicationBuilder(userAnswers = None).build()

      running(app) {
        val request = FakeRequest(GET, routeUrl)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when no UserAnswers exist (POST)" in {

      val app = applicationBuilder(userAnswers = None).build()

      running(app) {
        val request =
          FakeRequest(POST, routeUrl)
            .withFormUrlEncodedBody("value" -> "test@test.com")

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

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

import base.{SpecBase, WithFakeCisTaxpayer}
import forms.contractordetails.RemoveDetailYesNoFormProvider
import models.UserAnswers
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.contractordetails.RemoveDetailYesNoPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.contractordetails.RemoveDetailYesNoView

import scala.concurrent.Future

class RemoveDetailYesNoControllerSpec extends WithFakeCisTaxpayer with MockitoSugar {

  def onwardRoute  = Call("GET", "/contractor-details/contractor-details-added")
  val formProvider = new RemoveDetailYesNoFormProvider()

  "removeDetailYesNo Controller" - {

    "when contractorDetail is neither 'email-address' or 'scheme-name'" - {

      "must redirect to Journey Recovery on GET" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {

          val request =
            FakeRequest(GET, routes.RemoveDetailYesNoController.onPageLoad("invalid").url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery on POST" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {

          val request =
            FakeRequest(POST, routes.RemoveDetailYesNoController.onSubmit("invalid").url)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

    }

    Seq(
      ("email-address", "email-address"),
      ("scheme-name", "scheme-name")
    ).foreach { case (contractorDetail, selectedDetail) =>
      s"when contractorDetail is '$contractorDetail'" - {
        val form = formProvider(selectedDetail)

        lazy val removeDetailYesNoRoute = routes.RemoveDetailYesNoController.onPageLoad(selectedDetail).url

        "must return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RemoveDetailYesNoView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(
              selectedDetail,
              form,
              routes.RemoveDetailYesNoController.onSubmit(selectedDetail)
            )(
              request,
              messages(application)
            ).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers = UserAnswers(userAnswersId).set(RemoveDetailYesNoPage(selectedDetail), true).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val view = application.injector.instanceOf[RemoveDetailYesNoView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(
              selectedDetail,
              form.fill(true),
              routes.RemoveDetailYesNoController.onSubmit(selectedDetail)
            )(
              request,
              messages(application)
            ).toString
          }
        }

        "must redirect to the next page when valid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(
                POST,
                routes.RemoveDetailYesNoController
                  .onSubmit(selectedDetail)
                  .url
              ).withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              routes.ContractorDetailsCheckAnswersController
                .onPageLoad()
                .url
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request =
              FakeRequest(POST, removeDetailYesNoRoute)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[RemoveDetailYesNoView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(
              selectedDetail,
              boundForm,
              routes.RemoveDetailYesNoController.onSubmit(selectedDetail)
            )(
              request,
              messages(application)
            ).toString
          }
        }

        "must redirect to Journey Recovery for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, removeDetailYesNoRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to Journey Recovery for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, removeDetailYesNoRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must return OK for a GET with id when the subjourney exists" in {

          val uuid =
            java.util.UUID.fromString(
              "11111111-1111-1111-1111-111111111111"
            )

          val id =
            uuid.toString

          val answers =
            emptyUserAnswers
              .set(queries.CisIdQuery(id), "AB12")
              .success
              .value

          val repo =
            mockSessionRepository(Some(answers))

          val application =
            applicationBuilder(userAnswers = Some(answers))
              .overrides(
                bind[SessionRepository].toInstance(repo)
              )
              .build()

          running(application) {

            val request =
              FakeRequest(
                GET,
                routes.RemoveDetailYesNoController
                  .onPageLoadWithId(uuid, selectedDetail)
                  .url
              )

            val result =
              route(application, request).value

            val view =
              application.injector
                .instanceOf[RemoveDetailYesNoView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual view(
              selectedDetail,
              form,
              routes.RemoveDetailYesNoController
                .onSubmitWithId(uuid, selectedDetail)
            )(
              request,
              messages(application)
            ).toString
          }
        }

        "must populate the view correctly for a GET with id when previously answered" in {

          val uuid =
            java.util.UUID.fromString(
              "11111111-1111-1111-1111-111111111111"
            )

          val id =
            uuid.toString

          val answers =
            emptyUserAnswers
              .set(queries.CisIdQuery(id), "AB12")
              .flatMap(
                _.set(
                  RemoveDetailYesNoPage(
                    selectedDetail,
                    Some(id)
                  ),
                  true
                )
              )
              .success
              .value

          val repo =
            mockSessionRepository(Some(answers))

          val application =
            applicationBuilder(userAnswers = Some(answers))
              .overrides(
                bind[SessionRepository].toInstance(repo)
              )
              .build()

          running(application) {

            val request =
              FakeRequest(
                GET,
                routes.RemoveDetailYesNoController
                  .onPageLoadWithId(uuid, selectedDetail)
                  .url
              )

            val result =
              route(application, request).value

            val view =
              application.injector
                .instanceOf[RemoveDetailYesNoView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual view(
              selectedDetail,
              form.fill(true),
              routes.RemoveDetailYesNoController
                .onSubmitWithId(uuid, selectedDetail)
            )(
              request,
              messages(application)
            ).toString
          }
        }

        "must redirect to ContractorDetailsCheckAnswers with id when valid data is submitted" in {

          val uuid =
            java.util.UUID.fromString(
              "11111111-1111-1111-1111-111111111111"
            )

          val id =
            uuid.toString

          val answers =
            emptyUserAnswers
              .set(queries.CisIdQuery(id), "AB12")
              .success
              .value

          val repo =
            mockSessionRepository(Some(answers))

          when(repo.set(any()))
            .thenReturn(Future.successful(true))

          val application =
            applicationBuilder(userAnswers = Some(answers))
              .overrides(
                bind[SessionRepository].toInstance(repo)
              )
              .build()

          running(application) {

            val request =
              FakeRequest(
                POST,
                routes.RemoveDetailYesNoController
                  .onSubmitWithId(uuid, selectedDetail)
                  .url
              ).withFormUrlEncodedBody(
                "value" -> "true"
              )

            val result =
              route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              routes.ContractorDetailsCheckAnswersController
                .onPageLoadWithId(uuid)
                .url
          }
        }

      }
    }

  }

}

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

package controllers.amend.partnership

import base.SpecBase
import forms.amend.partnership.AmendPartnershipRemoveDetailYesNoFormProvider
import models.amend.partnership.AmendPartnershipRemoveDetail
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.partnership.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.amend.partnership.AmendPartnershipRemoveDetailYesNoView

import scala.concurrent.Future

class AmendPartnershipRemoveDetailYesNoControllerSpec extends SpecBase with MockitoSugar {

  val formProvider =
    new AmendPartnershipRemoveDetailYesNoFormProvider()

  private val partnershipName =
    "Test Partnership"

  private val nominatedPartnerName =
    "Test Nominated Partner"

  private def uaWithPartnershipName: UserAnswers =
    emptyUserAnswers
      .set(PartnershipNamePage, partnershipName)
      .success
      .value

  private def uaWithNominatedPartnerName: UserAnswers =
    uaWithPartnershipName
      .set(PartnershipNominatedPartnerNamePage, nominatedPartnerName)
      .success
      .value

  private def uaWithDetail(
    detail: String
  ): UserAnswers = {

    val userAnswers =
      detail match {

        case "address" =>
          uaWithPartnershipName
            .set(PartnershipAddressYesNoPage, true)
            .success
            .value

        case "contact-details" =>
          uaWithPartnershipName
            .set(AddPartnershipContactMethodsYesNoPage, true)
            .success
            .value

        case "utr" =>
          uaWithPartnershipName
            .set(PartnershipHasUtrYesNoPage, true)
            .success
            .value

        case "works-reference-number" =>
          uaWithPartnershipName
            .set(PartnershipWorksReferenceNumberYesNoPage, true)
            .success
            .value

        case "nominated-partner-utr" =>
          uaWithNominatedPartnerName
            .set(PartnershipNominatedPartnerUtrYesNoPage, true)
            .success
            .value

        case "nominated-partner-nino" =>
          uaWithNominatedPartnerName
            .set(PartnershipNominatedPartnerNinoYesNoPage, true)
            .success
            .value

        case "nominated-partner-company-registration-number" =>
          uaWithNominatedPartnerName
            .set(PartnershipNominatedPartnerCrnYesNoPage, true)
            .success
            .value
      }

    userAnswers
  }

  "AmendPartnershipRemoveDetailYesNo Controller" - {

    Seq(
      ("address", partnershipName),
      ("contact-details", partnershipName),
      ("utr", partnershipName),
      ("works-reference-number", partnershipName),
      ("nominated-partner-utr", nominatedPartnerName),
      ("nominated-partner-nino", nominatedPartnerName),
      ("nominated-partner-company-registration-number", nominatedPartnerName)
    ).foreach { case (detail, detailName) =>
      s"when detail is '$detail'" - {

        val form = formProvider()

        lazy val removeDetailYesNoRoute =
          controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
            .onPageLoad(detail)
            .url

        "must return OK and the correct view for a GET" in {

          val application =
            applicationBuilder(
              userAnswers = Some(uaWithDetail(detail))
            ).build()

          running(application) {

            val request =
              FakeRequest(GET, removeDetailYesNoRoute)

            val result =
              route(application, request).value

            val view =
              application.injector
                .instanceOf[AmendPartnershipRemoveDetailYesNoView]

            val detailType =
              AmendPartnershipRemoveDetail
                .fromKey(detail)
                .value

            val detailTitle =
              messages(application)(detailType.messageKey)

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                form,
                detail,
                detailTitle,
                detailName
              )(
                request,
                messages(application)
              ).toString
          }
        }

        "must redirect to the partnership Check Your Answers page when Yes is submitted" in {

          val mockSessionRepository =
            mock[SessionRepository]

          when(mockSessionRepository.set(any()))
            .thenReturn(Future.successful(true))

          val application =
            applicationBuilder(
              userAnswers = Some(uaWithDetail(detail))
            )
              .overrides(
                bind[SessionRepository]
                  .toInstance(mockSessionRepository)
              )
              .build()

          running(application) {

            val request =
              FakeRequest(
                POST,
                controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
                  .onSubmit(detail)
                  .url
              )
                .withFormUrlEncodedBody(
                  ("value", "true")
                )

            val result =
              route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.add.partnership.routes.PartnershipCheckYourAnswersController
                .onPageLoad()
                .url
          }
        }

        "must redirect to the partnership Check Your Answers page when No is submitted" in {

          val mockSessionRepository =
            mock[SessionRepository]

          when(mockSessionRepository.set(any()))
            .thenReturn(Future.successful(true))

          val application =
            applicationBuilder(
              userAnswers = Some(uaWithDetail(detail))
            )
              .overrides(
                bind[SessionRepository]
                  .toInstance(mockSessionRepository)
              )
              .build()

          running(application) {

            val request =
              FakeRequest(
                POST,
                controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
                  .onSubmit(detail)
                  .url
              )
                .withFormUrlEncodedBody(
                  ("value", "false")
                )

            val result =
              route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.add.partnership.routes.PartnershipCheckYourAnswersController
                .onPageLoad()
                .url
          }
        }

        "must return Bad Request with errors when invalid data is submitted" in {

          val application =
            applicationBuilder(
              userAnswers = Some(uaWithDetail(detail))
            ).build()

          running(application) {

            val request =
              FakeRequest(
                POST,
                controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
                  .onSubmit(detail)
                  .url
              )
                .withFormUrlEncodedBody(
                  ("value", "")
                )

            val boundForm =
              form.bind(
                Map("value" -> "")
              )

            val view =
              application.injector
                .instanceOf[AmendPartnershipRemoveDetailYesNoView]

            val detailType =
              AmendPartnershipRemoveDetail
                .fromKey(detail)
                .value

            val detailTitle =
              messages(application)(detailType.messageKey)

            val result =
              route(application, request).value

            status(result) mustEqual BAD_REQUEST

            contentAsString(result) mustEqual
              view(
                boundForm,
                detail,
                detailTitle,
                detailName
              )(
                request,
                messages(application)
              ).toString
          }
        }

        "must redirect to Journey Recovery on GET when no existing UserAnswers are found" in {

          val application =
            applicationBuilder(
              userAnswers = None
            ).build()

          running(application) {

            val request =
              FakeRequest(
                GET,
                removeDetailYesNoRoute
              )

            val result =
              route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.routes.JourneyRecoveryController
                .onPageLoad()
                .url
          }
        }

        "must redirect to Journey Recovery on POST when no existing UserAnswers are found" in {

          val application =
            applicationBuilder(
              userAnswers = None
            ).build()

          running(application) {

            val request =
              FakeRequest(
                POST,
                controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
                  .onSubmit(detail)
                  .url
              )
                .withFormUrlEncodedBody(
                  ("value", "true")
                )

            val result =
              route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.routes.JourneyRecoveryController
                .onPageLoad()
                .url
          }
        }

        "must redirect to Journey Recovery on GET when the partnership or nominated partner name is missing" in {

          val application =
            applicationBuilder(
              userAnswers = Some(emptyUserAnswers)
            ).build()

          running(application) {

            val request =
              FakeRequest(
                GET,
                removeDetailYesNoRoute
              )

            val result =
              route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.routes.JourneyRecoveryController
                .onPageLoad()
                .url
          }
        }

        "must redirect to Journey Recovery on POST when the partnership or nominated partner name is missing" in {

          val application =
            applicationBuilder(
              userAnswers = Some(emptyUserAnswers)
            ).build()

          running(application) {

            val request =
              FakeRequest(
                POST,
                controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
                  .onSubmit(detail)
                  .url
              )
                .withFormUrlEncodedBody(
                  ("value", "true")
                )

            val result =
              route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.routes.JourneyRecoveryController
                .onPageLoad()
                .url
          }
        }

        "must redirect to Journey Recovery on GET when the requested detail is not present" in {

          val userAnswers =
            if (
              detail == "nominated-partner-utr" ||
              detail == "nominated-partner-nino" ||
              detail == "nominated-partner-company-registration-number"
            ) {
              uaWithNominatedPartnerName
            } else {
              uaWithPartnershipName
            }

          val application =
            applicationBuilder(
              userAnswers = Some(userAnswers)
            ).build()

          running(application) {

            val request =
              FakeRequest(
                GET,
                removeDetailYesNoRoute
              )

            val result =
              route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.routes.JourneyRecoveryController
                .onPageLoad()
                .url
          }
        }

        "must redirect to Journey Recovery on POST when the requested detail is not present" in {

          val userAnswers =
            if (
              detail == "nominated-partner-utr" ||
              detail == "nominated-partner-nino" ||
              detail == "nominated-partner-company-registration-number"
            ) {
              uaWithNominatedPartnerName
            } else {
              uaWithPartnershipName
            }

          val application =
            applicationBuilder(
              userAnswers = Some(userAnswers)
            ).build()

          running(application) {

            val request =
              FakeRequest(
                POST,
                controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
                  .onSubmit(detail)
                  .url
              )
                .withFormUrlEncodedBody(
                  ("value", "true")
                )

            val result =
              route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.routes.JourneyRecoveryController
                .onPageLoad()
                .url
          }
        }
      }
    }

    "when detail is invalid" - {

      "must redirect to Journey Recovery on GET" in {

        val application =
          applicationBuilder(
            userAnswers = Some(uaWithPartnershipName)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
                .onPageLoad("invalid")
                .url
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url
        }
      }

      "must redirect to Journey Recovery on POST" in {

        val application =
          applicationBuilder(
            userAnswers = Some(uaWithPartnershipName)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
                .onSubmit("invalid")
                .url
            )
              .withFormUrlEncodedBody(
                ("value", "true")
              )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController
              .onPageLoad()
              .url
        }
      }
    }
  }
}

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
import models.UserAnswers
import models.amend.partnership.AmendPartnershipRemoveDetail
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.add.partnership.*
import pages.amend.ShowVerificationDetailsPage
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

  private val nominatedPartnerDetails: Seq[AmendPartnershipRemoveDetail] =
    Seq(
      AmendPartnershipRemoveDetail.NominatedPartnerUtr,
      AmendPartnershipRemoveDetail.NominatedPartnerNino,
      AmendPartnershipRemoveDetail.NominatedPartnerCompanyRegistrationNumber
    )

  private def uaWithDetail(
    detail: AmendPartnershipRemoveDetail
  ): UserAnswers =
    detail match {

      case AmendPartnershipRemoveDetail.Address =>
        uaWithPartnershipName
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value

      case AmendPartnershipRemoveDetail.ContactDetails =>
        uaWithPartnershipName
          .set(AddPartnershipContactMethodsYesNoPage, true)
          .success
          .value

      case AmendPartnershipRemoveDetail.Utr =>
        uaWithPartnershipName
          .set(PartnershipHasUtrYesNoPage, true)
          .success
          .value
          .set(ShowVerificationDetailsPage, false)
          .success
          .value

      case AmendPartnershipRemoveDetail.WorksReferenceNumber =>
        uaWithPartnershipName
          .set(PartnershipWorksReferenceNumberYesNoPage, true)
          .success
          .value

      case AmendPartnershipRemoveDetail.NominatedPartnerUtr =>
        uaWithNominatedPartnerName
          .set(PartnershipNominatedPartnerUtrYesNoPage, true)
          .success
          .value
          .set(ShowVerificationDetailsPage, false)
          .success
          .value

      case AmendPartnershipRemoveDetail.NominatedPartnerNino =>
        uaWithNominatedPartnerName
          .set(PartnershipNominatedPartnerNinoYesNoPage, true)
          .success
          .value

      case AmendPartnershipRemoveDetail.NominatedPartnerCompanyRegistrationNumber =>
        uaWithNominatedPartnerName
          .set(PartnershipNominatedPartnerCrnYesNoPage, true)
          .success
          .value
    }

  private def uaWithDetailPresentButNameMissing(
    detail: AmendPartnershipRemoveDetail
  ): UserAnswers =
    detail match {

      case AmendPartnershipRemoveDetail.Address =>
        emptyUserAnswers
          .set(PartnershipAddressYesNoPage, true)
          .success
          .value

      case AmendPartnershipRemoveDetail.ContactDetails =>
        emptyUserAnswers
          .set(AddPartnershipContactMethodsYesNoPage, true)
          .success
          .value

      case AmendPartnershipRemoveDetail.Utr =>
        emptyUserAnswers
          .set(PartnershipHasUtrYesNoPage, true)
          .success
          .value

      case AmendPartnershipRemoveDetail.WorksReferenceNumber =>
        emptyUserAnswers
          .set(PartnershipWorksReferenceNumberYesNoPage, true)
          .success
          .value

      case AmendPartnershipRemoveDetail.NominatedPartnerUtr =>
        emptyUserAnswers
          .set(PartnershipNominatedPartnerUtrYesNoPage, true)
          .success
          .value

      case AmendPartnershipRemoveDetail.NominatedPartnerNino =>
        emptyUserAnswers
          .set(PartnershipNominatedPartnerNinoYesNoPage, true)
          .success
          .value

      case AmendPartnershipRemoveDetail.NominatedPartnerCompanyRegistrationNumber =>
        emptyUserAnswers
          .set(PartnershipNominatedPartnerCrnYesNoPage, true)
          .success
          .value
    }

  "AmendPartnershipRemoveDetailYesNo Controller" - {

    Seq(
      (AmendPartnershipRemoveDetail.Address, partnershipName),
      (AmendPartnershipRemoveDetail.ContactDetails, partnershipName),
      (AmendPartnershipRemoveDetail.Utr, partnershipName),
      (AmendPartnershipRemoveDetail.WorksReferenceNumber, partnershipName),
      (AmendPartnershipRemoveDetail.NominatedPartnerUtr, nominatedPartnerName),
      (AmendPartnershipRemoveDetail.NominatedPartnerNino, nominatedPartnerName),
      (AmendPartnershipRemoveDetail.NominatedPartnerCompanyRegistrationNumber, nominatedPartnerName)
    ).foreach { case (detail, detailName) =>
      val detailKey =
        detail.key

      s"when detail is '$detailKey'" - {

        val form = formProvider()

        lazy val removeDetailYesNoRoute =
          controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
            .onPageLoad(detailKey)
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

            val detailTitle =
              messages(application)(detail.messageKey)

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                form,
                detailKey,
                detailTitle,
                detailName
              )(
                request,
                messages(application)
              ).toString
          }
        }

        "must redirect to the amend partnership Check Your Answers page when Yes is submitted" in {

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
                  .onSubmit(detailKey)
                  .url
              )
                .withFormUrlEncodedBody(
                  ("value", "true")
                )

            val result =
              route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController
                .onPageLoad()
                .url
          }
        }

        "must redirect to the amend partnership Check Your Answers page when No is submitted" in {

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
                  .onSubmit(detailKey)
                  .url
              )
                .withFormUrlEncodedBody(
                  ("value", "false")
                )

            val result =
              route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController
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
                  .onSubmit(detailKey)
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

            val detailTitle =
              messages(application)(detail.messageKey)

            val result =
              route(application, request).value

            status(result) mustEqual BAD_REQUEST

            contentAsString(result) mustEqual
              view(
                boundForm,
                detailKey,
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
                  .onSubmit(detailKey)
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
              userAnswers = Some(uaWithDetailPresentButNameMissing(detail))
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
              userAnswers = Some(uaWithDetailPresentButNameMissing(detail))
            ).build()

          running(application) {

            val request =
              FakeRequest(
                POST,
                controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
                  .onSubmit(detailKey)
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
            if (nominatedPartnerDetails.contains(detail)) {
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

        "must redirect to Journey Recovery when saving the answer fails" in {

          val mockSessionRepository =
            mock[SessionRepository]

          when(mockSessionRepository.set(any()))
            .thenReturn(Future.failed(new RuntimeException("Failed to save")))

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
                  .onSubmit(detailKey)
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

        "must redirect to Journey Recovery on POST when the requested detail is not present" in {

          val userAnswers =
            if (nominatedPartnerDetails.contains(detail)) {
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
                  .onSubmit(detailKey)
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

    "when detail is utr" - {
      lazy val removeDetailYesNoUtrRoute: String =
        controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
          .onPageLoad(AmendPartnershipRemoveDetail.Utr.key)
          .url

      val verifiedSubcontractorUa =
        uaWithPartnershipName
          .set(PartnershipUniqueTaxpayerReferencePage, "7777777777")
          .success
          .value
          .set(PartnershipHasUtrYesNoPage, true)
          .success
          .value
          .set(ShowVerificationDetailsPage, true)
          .success
          .value

      "must redirect to Journey Recovery for a GET when subcontractor is verified" in {

        val application = applicationBuilder(userAnswers = Some(verifiedSubcontractorUa)).build()

        running(application) {
          val request = FakeRequest(GET, removeDetailYesNoUtrRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery for a POST when subcontractor is verified" in {

        val application = applicationBuilder(userAnswers = Some(verifiedSubcontractorUa)).build()

        running(application) {
          val request =
            FakeRequest(POST, removeDetailYesNoUtrRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "when detail is NominatedPartnerUtr" - {
      lazy val removeDetailYesNoUtrRoute: String =
        controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
          .onPageLoad(AmendPartnershipRemoveDetail.NominatedPartnerUtr.key)
          .url

      val verifiedSubcontractorUa =
        uaWithNominatedPartnerName
          .set(PartnershipNominatedPartnerUtrPage, "7777777777")
          .success
          .value
          .set(PartnershipNominatedPartnerUtrYesNoPage, true)
          .success
          .value
          .set(ShowVerificationDetailsPage, true)
          .success
          .value

      "must redirect to Journey Recovery for a GET when subcontractor is verified" in {

        val application = applicationBuilder(userAnswers = Some(verifiedSubcontractorUa)).build()

        running(application) {
          val request = FakeRequest(GET, removeDetailYesNoUtrRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery for a POST when subcontractor is verified" in {

        val application = applicationBuilder(userAnswers = Some(verifiedSubcontractorUa)).build()

        running(application) {
          val request =
            FakeRequest(POST, removeDetailYesNoUtrRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
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

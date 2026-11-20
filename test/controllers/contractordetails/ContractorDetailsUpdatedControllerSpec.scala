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
import controllers.actions.MultiTabAction
import models.{Scheme, UserAnswers}
import models.requests.DataRequest
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString, eq as eqTo}
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.CisIdPage
import pages.contractordetails.ContractorSchemePage
import play.api.inject.bind
import play.api.mvc.{ActionFilter, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import queries.CisIdQuery
import services.ContractorDetailsService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.contractordetails.ContractorDetailsUpdatedView

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class ContractorDetailsUpdatedControllerSpec extends SpecBase with MockitoSugar {

  private val cisId = "123456789012"

  private val flowId =
    UUID.fromString("11111111-1111-1111-1111-111111111111")

  private val latestScheme = Scheme(
    schemeId = 123,
    instanceId = "instanceId",
    accountsOfficeReference = "AORef",
    taxOfficeNumber = "123",
    taxOfficeReference = "456",
    utr = Some("1234567890"),
    version = Some(2)
  )

  private def contractorUserAnswersWithCisId: UserAnswers =
    emptyUserAnswers
      .set(CisIdPage, cisId)
      .success
      .value

  private def contractorUserAnswersWithCisIdAndSubjourney: UserAnswers =
    emptyUserAnswers
      .set(CisIdPage, cisId)
      .flatMap(_.set(CisIdQuery(flowId.toString), cisId))
      .success
      .value

  /*
   * The controller spec tests the controller, not MultiTabAction's
   * authorisation logic. This filter allows the multi-tab request through.
   */
  private def allowMultiTabAction(): MultiTabAction = {
    val mockMultiTabAction = mock[MultiTabAction]

    val allowFilter = new ActionFilter[DataRequest] {
      override protected def executionContext: ExecutionContext =
        ExecutionContext.global

      override protected def filter[A](
        request: DataRequest[A]
      ): Future[Option[Result]] =
        Future.successful(None)
    }

    when(mockMultiTabAction.forJourney(flowId))
      .thenReturn(allowFilter)

    mockMultiTabAction
  }

  "ContractorDetailsUpdated Controller" - {

    Seq(
      (
        "AGENT",
        true,
        applicationConfig.constructionIndustryAgentAccountUrl + cisId
      ),
      (
        "ORGANISATION",
        false,
        applicationConfig.constructionIndustryOrgAccountUrl
      )
    ).foreach { case (accountType, isAgent, expectedCisAccountUrl) =>
      s"when account type is $accountType" - {

        "must refresh the scheme and return OK for a standard GET" in {

          val mockContractorDetailsService =
            mock[ContractorDetailsService]

          val mockSessionRepository =
            mock[SessionRepository]

          when(
            mockContractorDetailsService.getScheme(eqTo(cisId))(any[HeaderCarrier])
          ).thenReturn(Future.successful(latestScheme))

          when(
            mockSessionRepository.set(any[UserAnswers])
          ).thenReturn(Future.successful(true))

          val application = applicationBuilder(
            userAnswers = Some(contractorUserAnswersWithCisId),
            isAgent = isAgent,
            additionalBindings = Seq(
              bind[ContractorDetailsService]
                .toInstance(mockContractorDetailsService),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
          ).build()

          running(application) {
            val request = FakeRequest(
              GET,
              routes.ContractorDetailsUpdatedController.onPageLoad().url
            )

            val result = route(application, request).value

            val view =
              application.injector.instanceOf[ContractorDetailsUpdatedView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual view(
              expectedCisAccountUrl,
              routes.ContractorDetailsCheckAnswersController.onPageLoad().url
            )(
              viewRequest(request, isAgent),
              messages(application)
            ).toString

            verify(mockContractorDetailsService)
              .getScheme(eqTo(cisId))(any[HeaderCarrier])

            val captor =
              ArgumentCaptor.forClass(classOf[UserAnswers])

            verify(mockSessionRepository).set(captor.capture())

            captor.getValue.get(ContractorSchemePage) mustBe Some(latestScheme)
          }
        }

        "must refresh the scheme and return OK for a multi-tab GET" in {

          val mockContractorDetailsService =
            mock[ContractorDetailsService]

          val answers =
            contractorUserAnswersWithCisIdAndSubjourney

          val repo =
            mockSessionRepository(Some(answers))

          val mockMultiTabAction =
            allowMultiTabAction()

          when(
            mockContractorDetailsService.getScheme(eqTo(cisId))(any[HeaderCarrier])
          ).thenReturn(Future.successful(latestScheme))

          when(
            repo.set(any[UserAnswers])
          ).thenReturn(Future.successful(true))

          val application = applicationBuilder(
            userAnswers = Some(answers),
            isAgent = isAgent,
            additionalBindings = Seq(
              bind[ContractorDetailsService]
                .toInstance(mockContractorDetailsService),
              bind[SessionRepository]
                .toInstance(repo),
              bind[MultiTabAction]
                .toInstance(mockMultiTabAction)
            )
          ).build()

          running(application) {
            val request = FakeRequest(
              GET,
              routes.ContractorDetailsUpdatedController
                .onPageLoadWithId(flowId)
                .url
            )

            val result = route(application, request).value

            val view =
              application.injector.instanceOf[ContractorDetailsUpdatedView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual view(
              expectedCisAccountUrl,
              routes.ContractorDetailsCheckAnswersController
                .onPageLoadWithId(flowId)
                .url
            )(
              viewRequest(request, isAgent),
              messages(application)
            ).toString

            verify(mockContractorDetailsService)
              .getScheme(eqTo(cisId))(any[HeaderCarrier])

            val captor =
              ArgumentCaptor.forClass(classOf[UserAnswers])

            verify(repo).set(captor.capture())

            captor.getValue
              .get(ContractorSchemePage) mustBe Some(latestScheme)
          }
        }
      }
    }

    "must redirect to journey recovery when CisIdPage is missing" in {

      val mockContractorDetailsService =
        mock[ContractorDetailsService]

      val mockSessionRepository =
        mock[SessionRepository]

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        additionalBindings = Seq(
          bind[ContractorDetailsService]
            .toInstance(mockContractorDetailsService),
          bind[SessionRepository]
            .toInstance(mockSessionRepository)
        )
      ).build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.ContractorDetailsUpdatedController.onPageLoad().url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(
          mockContractorDetailsService,
          never()
        ).getScheme(anyString())(any[HeaderCarrier])

        verify(
          mockSessionRepository,
          never()
        ).set(any[UserAnswers])
      }
    }

    "must redirect to journey recovery when getScheme fails" in {

      val mockContractorDetailsService =
        mock[ContractorDetailsService]

      val mockSessionRepository =
        mock[SessionRepository]

      when(
        mockContractorDetailsService.getScheme(anyString())(any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("boom")))

      val application = applicationBuilder(
        userAnswers = Some(contractorUserAnswersWithCisId),
        additionalBindings = Seq(
          bind[ContractorDetailsService]
            .toInstance(mockContractorDetailsService),
          bind[SessionRepository]
            .toInstance(mockSessionRepository)
        )
      ).build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.ContractorDetailsUpdatedController.onPageLoad().url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

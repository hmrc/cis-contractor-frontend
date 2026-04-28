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

package services

import base.SpecBase
import connectors.ConstructionIndustrySchemeConnector
import generators.ModelGenerators
import models.response.{GetCurrentVerificationBatchResponse, GetNewestVerificationBatchResponse}
import models.{Subcontractor, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, verify, verifyNoMoreInteractions, when}
import org.scalatest.RecoverMethods.recoverToExceptionIf
import org.scalatestplus.mockito.MockitoSugar
import pages.QuestionPage
import pages.verification.NewestVerificationBatchResponsePage
import pages.verify.{CurrentVerificationBatchResponsePage, UnverifiedSubcontractorsPage}
import play.api.libs.json.{JsPath, Writes}
import queries.CisIdQuery
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

final class VerificationServiceSpec extends SpecBase with MockitoSugar with ModelGenerators {

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.global

  private val instanceId = "INST-123"

  private val verifiedSubcontractor: Subcontractor =
    arbitrarySubcontractor.arbitrary.sample.value.copy(
      subcontractorId = 1L,
      verified = Some("Y")
    )

  private val unverifiedSub1: Subcontractor =
    arbitrarySubcontractor.arbitrary.sample.value.copy(
      subcontractorId = 2L,
      verified = Some("N")
    )

  private val unverifiedSub2: Subcontractor =
    arbitrarySubcontractor.arbitrary.sample.value.copy(
      subcontractorId = 3L,
      verified = None
    )

  private val responseWithSubcontractors =
    GetNewestVerificationBatchResponse(
      scheme = Nil,
      subcontractors = Seq(
        verifiedSubcontractor,
        unverifiedSub1,
        unverifiedSub2
      ),
      verificationBatch = Nil,
      verifications = Nil,
      submission = Nil,
      monthlyReturn = Nil
    )

  "VerificationService.refreshNewestVerificationBatch" - {

    "must fetch newest verification batch, store response and unverified subcontractors, persist to session repo and return updated answers" in {

      val mockConnector = mock[ConstructionIndustrySchemeConnector]
      val mockRepo      = mock[SessionRepository]
      val service       = new VerificationService(mockConnector, mockRepo)

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, instanceId)
          .success
          .value

      when(mockConnector.getNewestVerificationBatch(eqTo(instanceId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(responseWithSubcontractors))

      when(mockRepo.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val result = service.refreshNewestVerificationBatch(userAnswers).futureValue

      result.get(NewestVerificationBatchResponsePage) mustBe Some(responseWithSubcontractors)

      result.get(UnverifiedSubcontractorsPage) mustBe
        Some(Seq(unverifiedSub1, unverifiedSub2))

      verify(mockConnector).getNewestVerificationBatch(eqTo(instanceId))(any[HeaderCarrier])
      verify(mockRepo).set(any[UserAnswers])
      verifyNoMoreInteractions(mockConnector)
    }

    "must fail when CisIdQuery is missing and not call connector or repository" in {

      val mockConnector = mock[ConstructionIndustrySchemeConnector]
      val mockRepo      = mock[SessionRepository]
      val service       = new VerificationService(mockConnector, mockRepo)

      val ex = service.refreshNewestVerificationBatch(emptyUserAnswers).failed.futureValue
      ex.getMessage must include("InstanceIdQuery not found in session data")

      verify(mockConnector, never()).getNewestVerificationBatch(any[String])(any[HeaderCarrier])
      verify(mockRepo, never()).set(any[UserAnswers])
    }

    "must propagate connector failure and not persist session" in {

      val mockConnector = mock[ConstructionIndustrySchemeConnector]
      val mockRepo      = mock[SessionRepository]
      val service       = new VerificationService(mockConnector, mockRepo)

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, instanceId)
          .success
          .value

      when(mockConnector.getNewestVerificationBatch(eqTo(instanceId))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("Failure")))

      val ex = service.refreshNewestVerificationBatch(userAnswers).failed.futureValue
      ex.getMessage must include("Failure")

      verify(mockRepo, never()).set(any[UserAnswers])
    }

    "must propagate failure when setting NewestVerificationBatchResponsePage fails and not write to session repo" in {

      final case class BadType(value: String)
      case object BadSetPage extends QuestionPage[BadType] {
        override def path: JsPath     = JsPath \ "badSetPage"
        override def toString: String = "badSetPage"
      }

      given Writes[BadType] = Writes[BadType] { _ =>
        throw new RuntimeException("writes-failed")
      }

      val mockConnector = mock[ConstructionIndustrySchemeConnector]
      val mockRepo      = mock[SessionRepository]

      class TestService(conn: ConstructionIndustrySchemeConnector, repo: SessionRepository)(implicit
        ec: ExecutionContext
      ) extends VerificationService(conn, repo) {

        def refreshAndForceBadSet(ua: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers] =
          for {
            instanceId <- ua.get(CisIdQuery)
                            .map(Future.successful)
                            .getOrElse(Future.failed(new RuntimeException("InstanceIdQuery not found")))
            resp       <- conn.getNewestVerificationBatch(instanceId)
            _          <- Future.fromTry(ua.set(NewestVerificationBatchResponsePage, resp))
            bad        <- Future.fromTry(ua.set(BadSetPage, BadType("x")))
            _          <- repo.set(bad)
          } yield bad
      }

      val service = new TestService(mockConnector, mockRepo)

      val ua =
        emptyUserAnswers
          .set(CisIdQuery, instanceId)
          .success
          .value

      when(mockConnector.getNewestVerificationBatch(eqTo(instanceId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(responseWithSubcontractors))

      val ex =
        recoverToExceptionIf[RuntimeException] {
          service.refreshAndForceBadSet(ua)
        }.futureValue

      ex.getMessage must include("writes-failed")
    }
  }

  "VerificationBatchService.getCurrentVerificationBatch" - {

    val instanceId = "INST-123"

    val response =
      GetCurrentVerificationBatchResponse(
        subcontractors = Nil,
        verificationBatch = Nil,
        verifications = Nil
      )

    "must fetch current verification batch, store in UserAnswers, persist to session repo, and return updated answers" in {
      val mockConnector = mock[ConstructionIndustrySchemeConnector]
      val mockRepo      = mock[SessionRepository]
      val service       = new VerificationService(mockConnector, mockRepo)

      val ua =
        emptyUserAnswers
          .set(CisIdQuery, instanceId)
          .success
          .value

      when(mockConnector.getCurrentVerificationBatch(eqTo(instanceId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(response))

      when(mockRepo.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val result = service.getCurrentVerificationBatch(ua).futureValue

      result.get(CurrentVerificationBatchResponsePage) mustBe Some(response)

      verify(mockConnector).getCurrentVerificationBatch(eqTo(instanceId))(any[HeaderCarrier])

      verify(mockRepo).set(
        org.mockito.ArgumentMatchers.argThat { (saved: UserAnswers) =>
          saved.get(CurrentVerificationBatchResponsePage).contains(response)
        }
      )

      verifyNoMoreInteractions(mockConnector)
    }

    "must fail when CisIdQuery (instance id) is missing and not call connector nor repo" in {
      val mockConnector = mock[ConstructionIndustrySchemeConnector]
      val mockRepo      = mock[SessionRepository]
      val service       = new VerificationService(mockConnector, mockRepo)

      val ex = service.getCurrentVerificationBatch(emptyUserAnswers).failed.futureValue
      ex.getMessage must include("InstanceIdQuery not found in session data")

      verify(mockConnector, never()).getCurrentVerificationBatch(any[String])(any[HeaderCarrier])
      verify(mockRepo, never()).set(any[UserAnswers])
      verifyNoMoreInteractions(mockConnector)
    }

    "must propagate connector failure and not write to session repo" in {
      val mockConnector = mock[ConstructionIndustrySchemeConnector]
      val mockRepo      = mock[SessionRepository]
      val service       = new VerificationService(mockConnector, mockRepo)

      val ua =
        emptyUserAnswers
          .set(CisIdQuery, instanceId)
          .success
          .value

      when(mockConnector.getCurrentVerificationBatch(eqTo(instanceId))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val ex = service.getCurrentVerificationBatch(ua).failed.futureValue
      ex.getMessage must include("boom")

      verify(mockConnector).getCurrentVerificationBatch(eqTo(instanceId))(any[HeaderCarrier])
      verify(mockRepo, never()).set(any[UserAnswers])
      verifyNoMoreInteractions(mockConnector)
    }

    "must propagate failure when setting CurrentVerificationBatchResponsePage fails and not write to session repo" in {

      final case class BadType(value: String)
      case object BadSetPage extends QuestionPage[BadType] {
        override def path: JsPath = JsPath \ "badSetPage"

        override def toString: String = "badSetPage"
      }

      given Writes[BadType] = Writes[BadType] { _ =>
        throw new RuntimeException("writes-failed")
      }

      val mockConnector = mock[ConstructionIndustrySchemeConnector]
      val mockRepo      = mock[SessionRepository]

      class TestService(conn: ConstructionIndustrySchemeConnector, repo: SessionRepository)(implicit
        ec: ExecutionContext
      ) extends VerificationService(conn, repo) {

        def refreshAndForceBadSet(ua: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers] =
          for {
            instanceId <- ua.get(CisIdQuery)
                            .map(Future.successful)
                            .getOrElse(Future.failed(new RuntimeException("InstanceIdQuery not found in session data")))
            resp       <- conn.getCurrentVerificationBatch(instanceId)

            _ <- Future.fromTry(ua.set(CurrentVerificationBatchResponsePage, resp))

            bad <- Future.fromTry(ua.set(BadSetPage, BadType("x")))

            _ <- repo.set(bad)
          } yield bad
      }

      val service = new TestService(mockConnector, mockRepo)

      val ua =
        emptyUserAnswers
          .set(CisIdQuery, instanceId)
          .success
          .value

      when(mockConnector.getCurrentVerificationBatch(eqTo(instanceId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(response))

      val ex = service.refreshAndForceBadSet(ua).failed.futureValue
      ex.getMessage must include("writes-failed")

      verify(mockConnector).getCurrentVerificationBatch(eqTo(instanceId))(any[HeaderCarrier])
      verify(mockRepo, never()).set(any[UserAnswers])
      verifyNoMoreInteractions(mockConnector)
    }
  }
}

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

package controllers.actions

import base.SpecBase
import models.requests.{CisIdDataRequest, DataRequest}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.CisIdQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CisIdRequiredActionSpec extends SpecBase {

  private class TestCisIdRequiredAction extends CisIdRequiredAction {

    def testRefine[A](
      request: DataRequest[A]
    ): Future[Either[Result, CisIdDataRequest[A]]] =
      refine(request)
  }

  private val action = new TestCisIdRequiredAction()

  "CisIdRequiredAction" - {

    "return a CisIdDataRequest when CIS ID is present" in {
      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, "12345")
          .success
          .value

      val request =
        DataRequest(
          request = FakeRequest(GET, "/"),
          userId = "user-id",
          userAnswers = userAnswers
        )

      val result = action.testRefine(request).futureValue

      result mustBe a[Right[?, ?]]

      val cisIdRequest = result.toOption.value

      cisIdRequest.cisId mustBe "12345"
      cisIdRequest.userAnswers mustBe userAnswers
      cisIdRequest.userId mustBe "user-id"
    }

    "redirect to Journey Recovery when CIS ID is missing" in {
      val request =
        DataRequest(
          request = FakeRequest(GET, "/"),
          userId = "user-id",
          userAnswers = emptyUserAnswers
        )

      val result = action.testRefine(request).futureValue

      result mustBe a[Left[?, ?]]

      result match {
        case Left(redirect) =>
          redirect mustBe Redirect(
            controllers.routes.JourneyRecoveryController.onPageLoad()
          )

        case Right(_) =>
          fail("Expected a redirect but got a successful result")
      }
    }
  }
}

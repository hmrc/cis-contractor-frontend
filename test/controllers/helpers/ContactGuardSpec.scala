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

package controllers.helpers

import base.SpecBase
import controllers.routes
import models.contact.ContactOptions.{Email, Phone}
import play.api.mvc.{Result, Results}
import play.api.test.Helpers.*

import scala.concurrent.Future

class ContactGuardSpec extends SpecBase with ContactGuard with Results {

  private val testName = "Test Name"

  private def successResult(name: String): Result =
    Ok(name)

  "ContactGuard.requireContactChoice" - {

    "must return success result when name and matching contact choice are present" in {

      val result =
        requireContactChoice(
          name = Some(testName),
          contactChoice = Some(Email),
          expected = Email
        )(successResult)

      status(Future.successful(result)) mustEqual OK
      contentAsString(Future.successful(result)) mustEqual testName
    }

    "must redirect to Journey Recovery when name is missing" in {

      val result =
        requireContactChoice(
          name = None,
          contactChoice = Some(Email),
          expected = Email
        )(successResult)

      status(Future.successful(result)) mustEqual SEE_OTHER
      redirectLocation(Future.successful(result)).value mustEqual
        routes.JourneyRecoveryController.onPageLoad().url
    }

    "must redirect to Journey Recovery when contact choice is missing" in {

      val result =
        requireContactChoice(
          name = Some(testName),
          contactChoice = None,
          expected = Email
        )(successResult)

      status(Future.successful(result)) mustEqual SEE_OTHER
      redirectLocation(Future.successful(result)).value mustEqual
        routes.JourneyRecoveryController.onPageLoad().url
    }

    "must redirect to Journey Recovery when contact choice does not match expected" in {

      val result =
        requireContactChoice(
          name = Some(testName),
          contactChoice = Some(Phone),
          expected = Email
        )(successResult)

      status(Future.successful(result)) mustEqual SEE_OTHER
      redirectLocation(Future.successful(result)).value mustEqual
        routes.JourneyRecoveryController.onPageLoad().url
    }
  }
}

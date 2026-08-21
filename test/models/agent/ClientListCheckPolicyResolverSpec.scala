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

package models.agent

import models.agent.ClientListCheckPolicy.{Exempt, GroupA}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.routing.{HandlerDef, Router}
import play.api.test.FakeRequest
import base.SpecBase
import controllers.actions.ClientListCheckPolicyResolver

class ClientListCheckPolicyResolverSpec extends SpecBase {

  private val resolver = new ClientListCheckPolicyResolver()

  private def request(
    method: String,
    controller: String,
    action: String
  ) = {
    val handlerDef = mock[HandlerDef]

    when(handlerDef.controller).thenReturn(controller)
    when(handlerDef.method).thenReturn(action)

    FakeRequest(method, "/test")
      .addAttr(Router.Attrs.HandlerDef, handlerDef)
  }

  "ClientListCheckPolicyResolver" - {

    "must return GroupA for a GET onPageLoad route" in {
      resolver.resolve(
        request(
          "GET",
          "controllers.SomeController",
          "onPageLoad"
        )
      ) mustBe GroupA
    }

    "must return Exempt for an exempt controller" in {
      resolver.resolve(
        request(
          "GET",
          "controllers.SystemErrorController",
          "onPageLoad"
        )
      ) mustBe Exempt
    }

    "must return Exempt for a non-GET request" in {
      resolver.resolve(
        request(
          "POST",
          "controllers.SomeController",
          "onPageLoad"
        )
      ) mustBe Exempt
    }

    "must return Exempt for an unmatched GET route" in {
      resolver.resolve(
        request(
          "GET",
          "controllers.SomeController",
          "onSubmit"
        )
      ) mustBe Exempt
    }
  }
}

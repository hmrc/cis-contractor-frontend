/*
 * Copyright 2025 HM Revenue & Customs
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

package utils

import ch.qos.logback.classic.Level
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.MarkerContext
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderNames, HttpResponse, SessionKeys}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

class LoggingUtilSpec extends AnyWordSpec with Matchers with LogCapturing {

  private object TestLogger extends LoggingUtil

  private implicit val mc: MarkerContext = MarkerContext.NoMarker

  private val requestWithIdentifiers: Request[_] = FakeRequest()
    .withHeaders(HeaderNames.trueClientIp -> "1.2.3.4")
    .withSession(SessionKeys.sessionId -> "session-123")

  private val httpResponseWithIdentifiers: HttpResponse = HttpResponse(
    status = 200,
    body = "",
    headers = Map(
      HeaderNames.trueClientIp -> Seq("1.2.3.4"),
      HeaderNames.xSessionId   -> Seq("session-123")
    )
  )

  private val testException = new RuntimeException("boom")

  "LoggingUtil identifiers (from request)" must {

    "include both the true client IP and the session id when present" in {
      val request: Request[_] = FakeRequest()
        .withHeaders(HeaderNames.trueClientIp -> "1.2.3.4")
        .withSession(SessionKeys.sessionId -> "session-123")

      TestLogger.identifiers(request) mustBe "trueClientIp: 1.2.3.4 sessionId: session-123 "
    }

    "include only the session id when the true client IP is absent" in {
      val request: Request[_] = FakeRequest().withSession(SessionKeys.sessionId -> "session-123")

      TestLogger.identifiers(request) mustBe "sessionId: session-123 "
    }

    "be empty when neither identifier is present" in {
      TestLogger.identifiers(FakeRequest()) mustBe ""
    }
  }

  "LoggingUtil identifiers (from HttpResponse)" must {

    "include both the true client IP and the session id when present" in {
      val response = HttpResponse(
        status = 200,
        body = "",
        headers = Map(
          HeaderNames.trueClientIp -> Seq("1.2.3.4"),
          HeaderNames.xSessionId   -> Seq("session-123")
        )
      )

      val result = TestLogger.identifiersFromHttpResponse(response)

      result mustBe "trueClientIp: 1.2.3.4 sessionId: session-123 "
    }

    "be empty when neither identifier is present" in {
      val response = HttpResponse(status = 200, body = "", headers = Map.empty)

      TestLogger.identifiersFromHttpResponse(response) mustBe ""
    }
  }

  "LoggingUtil request-based logging" must {

    implicit val request: Request[_] = requestWithIdentifiers

    "log at INFO level with the message and request identifiers via infoLog" in {
      withCaptureOfLoggingFrom(TestLogger) { logEvents =>
        TestLogger.infoLog("info message")

        val event = logEvents.head
        event.getLevel mustBe Level.INFO
        event.getFormattedMessage must include("info message")
        event.getFormattedMessage must include("trueClientIp: 1.2.3.4")
        event.getFormattedMessage must include("sessionId: session-123")
      }
    }

    "log at WARN level with the message and request identifiers via warnLog" in {
      withCaptureOfLoggingFrom(TestLogger) { logEvents =>
        TestLogger.warnLog("warn message")

        val event = logEvents.head
        event.getLevel mustBe Level.WARN
        event.getFormattedMessage must include("warn message")
        event.getFormattedMessage must include("trueClientIp: 1.2.3.4")
        event.getThrowableProxy mustBe null
      }
    }

    "log at WARN level with the throwable via warnLog" in {
      withCaptureOfLoggingFrom(TestLogger) { logEvents =>
        TestLogger.warnLog("warn with error", testException)

        val event = logEvents.head
        event.getLevel mustBe Level.WARN
        event.getFormattedMessage must include("warn with error")
        event.getThrowableProxy.getMessage mustBe "boom"
      }
    }

    "log at ERROR level with the message and request identifiers via errorLog" in {
      withCaptureOfLoggingFrom(TestLogger) { logEvents =>
        TestLogger.errorLog("error message")

        val event = logEvents.head
        event.getLevel mustBe Level.ERROR
        event.getFormattedMessage must include("error message")
        event.getFormattedMessage must include("sessionId: session-123")
        event.getThrowableProxy mustBe null
      }
    }

    "log at ERROR level with the throwable via errorLog" in {
      withCaptureOfLoggingFrom(TestLogger) { logEvents =>
        TestLogger.errorLog("error with throwable", testException)

        val event = logEvents.head
        event.getLevel mustBe Level.ERROR
        event.getFormattedMessage must include("error with throwable")
        event.getThrowableProxy.getMessage mustBe "boom"
      }
    }
  }

  "LoggingUtil connector logging (from HttpResponse)" must {

    implicit val response: HttpResponse = httpResponseWithIdentifiers

    "log at INFO level with the message and response identifiers via infoConnectorLog" in {
      withCaptureOfLoggingFrom(TestLogger) { logEvents =>
        TestLogger.infoConnectorLog("connector info")

        val event = logEvents.head
        event.getLevel mustBe Level.INFO
        event.getFormattedMessage must include("connector info")
        event.getFormattedMessage must include("trueClientIp: 1.2.3.4")
        event.getFormattedMessage must include("sessionId: session-123")
      }
    }

    "log at WARN level with the message and response identifiers via warnConnectorLog" in {
      withCaptureOfLoggingFrom(TestLogger) { logEvents =>
        TestLogger.warnConnectorLog("connector warn")

        val event = logEvents.head
        event.getLevel mustBe Level.WARN
        event.getFormattedMessage must include("connector warn")
        event.getFormattedMessage must include("trueClientIp: 1.2.3.4")
      }
    }

    "log at ERROR level with the message and response identifiers via errorConnectorLog" in {
      withCaptureOfLoggingFrom(TestLogger) { logEvents =>
        TestLogger.errorConnectorLog("connector error")

        val event = logEvents.head
        event.getLevel mustBe Level.ERROR
        event.getFormattedMessage must include("connector error")
        event.getFormattedMessage must include("sessionId: session-123")
      }
    }
  }
}

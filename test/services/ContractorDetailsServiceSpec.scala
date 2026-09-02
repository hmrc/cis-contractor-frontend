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
import models.Scheme
import models.requests.{UpdateSchemeRequest, UpdateSchemeVersionRequest}
import models.response.UpdateSchemeVersionResponse
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.Future

class ContractorDetailsServiceSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val mockCisConnector = mock[ConstructionIndustrySchemeConnector]

  private val service =
    new ContractorDetailsService(mockCisConnector)

  private val scheme = Scheme(
    schemeId = 123,
    instanceId = "cisId",
    accountsOfficeReference = "123 PA 87654321",
    taxOfficeNumber = "123",
    taxOfficeReference = "45678",
    utr = Some("1234567890"),
    name = Some("Test Scheme"),
    emailAddress = Some("test@example.com"),
    createDate = Some(Instant.now())
  )

  "getScheme" - {

    "return the scheme returned by the connector" in {

      when(
        mockCisConnector.getScheme(eqTo("cisId"))(eqTo(hc))
      ).thenReturn(
        Future.successful(scheme)
      )

      val result =
        service.getScheme("cisId").futureValue

      result mustBe scheme
    }

    "propagate connector failures" in {

      val exception =
        new RuntimeException("connector failure")

      when(
        mockCisConnector.getScheme(eqTo("cisId"))(eqTo(hc))
      ).thenReturn(
        Future.failed(exception)
      )

      val result =
        service.getScheme("cisId").failed.futureValue

      result mustBe exception
    }
  }

  "updateSchemeVersion" - {

    "return the new version returned by the connector" in {
      val request  = UpdateSchemeVersionRequest(currentVersion = 1, instanceId = "cisId")
      val response = UpdateSchemeVersionResponse(newVersion = 2)

      when(
        mockCisConnector.updateSchemeVersion(eqTo(request))(eqTo(hc))
      ).thenReturn(
        Future.successful(response)
      )

      service.updateSchemeVersion(request).futureValue mustBe response
    }
  }

  "updateScheme" - {

    "return Unit when the connector succeeds" in {
      val request =
        UpdateSchemeRequest(
          schemeId = 123,
          instanceId = "cisId",
          taxOfficeNumber = "123",
          taxOfficeReference = "45678",
          accountsOfficeReference = "123 PA 87654321",
          prePopCount = 1,
          prePopSuccessful = "Y",
          uniqueTaxReference = "1234567890",
          name = "Test Scheme",
          emailAddress = "test@example.com",
          version = 2
        )

      when(
        mockCisConnector.updateScheme(eqTo(request))(eqTo(hc))
      ).thenReturn(
        Future.successful(())
      )

      service.updateScheme(request).futureValue mustBe (())
    }
  }
}

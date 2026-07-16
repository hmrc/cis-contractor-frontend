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

package services

import base.SpecBase
import config.AddressLookupConfiguration
import connectors.AddressLookupConnector
import constants.AddressLookupConstants
import models.address.AddressLookupJourneyIdentifier.individualQuestionsAddress
import models.address.{Address, AddressLookupConfigurationModel, Country, MandatoryFieldsConfigModel}
import models.requests.DataRequest
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.add.AddressOfSubcontractorPage
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import queries.AddressLookupAmendReturnQuery
import models.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class AddressLookupServiceSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val request: Request[_]  = FakeRequest()

  private val testId = "test-address-id"

  private val testAddress = Address(
    addressLine1 = "10 Downing Street",
    addressLine2 = Some("Westminster"),
    postcode = Some("SW1A 2AA"),
    country = Some(Country(Some("GB"), Some("United Kingdom")))
  )

  private val testCall = Call("GET", "http://localhost:9028/lookup-address/journey/begin")

  private val mandatoryFields =
    MandatoryFieldsConfigModel(addressLine1 = Some(true), town = Some(true), postcode = Some(true))

  private def newService(
    connector: AddressLookupConnector = mock[AddressLookupConnector],
    alfConfig: AddressLookupConfiguration = mock[AddressLookupConfiguration],
    sessionRepository: SessionRepository = mock[SessionRepository]
  ) = (new AddressLookupService(connector, alfConfig, sessionRepository), connector, alfConfig, sessionRepository)

  "AddressLookupService" - {

    "getAddressById" - {

      "must delegate to the connector and return the address" in {
        val (service, connector, _, _) = newService()
        when(connector.getAddress(eqTo(testId))(any())).thenReturn(Future.successful(testAddress))

        service.getAddressById(testId).futureValue mustBe testAddress
      }

      "must normalise (trim) the address returned by the connector" in {
        val (service, connector, _, _) = newService()
        val untrimmedAddress           = Address(
          addressLine1 = "  10 Downing Street  ",
          addressLine2 = Some("  Westminster  "),
          addressLine3 = Some("   "),
          postcode = Some("  SW1A 2AA  "),
          country = Some(Country(Some("  GB  "), Some("  United Kingdom  ")))
        )
        when(connector.getAddress(eqTo(testId))(any())).thenReturn(Future.successful(untrimmedAddress))

        service.getAddressById(testId).futureValue mustBe Address(
          addressLine1 = "10 Downing Street",
          addressLine2 = Some("Westminster"),
          addressLine3 = None,
          postcode = Some("SW1A 2AA"),
          country = Some(Country(Some("GB"), Some("United Kingdom")))
        )
      }

      "must propagate a failure from the connector" in {
        val (service, connector, _, _) = newService()
        when(connector.getAddress(eqTo(testId))(any())).thenReturn(Future.failed(new RuntimeException("boom")))

        service.getAddressById(testId).failed.futureValue mustBe a[RuntimeException]
      }
    }

    "getJourneyUrl" - {

      "must build the config and return the on-ramp Call from the connector" in {
        val (service, connector, alfConfig, _)           = newService()
        val builtConfig: AddressLookupConfigurationModel = AddressLookupConstants.testAlfConfig

        when(
          alfConfig.apply(
            eqTo(individualQuestionsAddress),
            eqTo(testCall),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
          )(any())
        )
          .thenReturn(builtConfig)
        when(connector.getOnRampUrl(eqTo(builtConfig))(any(), any())).thenReturn(Future.successful(testCall))

        service
          .getJourneyUrl(individualQuestionsAddress, testCall, mandatoryFieldsConfigModel = mandatoryFields)
          .futureValue mustBe testCall
      }
    }

    "saveAddressDetails" - {

      implicit val dataRequest: DataRequest[_] =
        DataRequest(FakeRequest(), userAnswersId, emptyUserAnswers)

      "must persist the address against the page and return true when the save succeeds" in {
        val (service, _, _, sessionRepository) = newService()
        val captor                             = ArgumentCaptor.forClass(classOf[models.UserAnswers])
        when(sessionRepository.set(any())).thenReturn(Future.successful(true))

        service.saveAddressDetails(testAddress, AddressOfSubcontractorPage).futureValue mustBe true

        verify(sessionRepository).set(captor.capture())
        captor.getValue.get(AddressOfSubcontractorPage) mustBe Some(testAddress)
      }

      "must return false when the repository fails to save" in {
        val (service, _, _, sessionRepository) = newService()
        when(sessionRepository.set(any())).thenReturn(Future.successful(false))

        service.saveAddressDetails(testAddress, AddressOfSubcontractorPage).futureValue mustBe false
        verify(sessionRepository).set(any())
      }

      "must remove AddressLookupAmendReturnQuery when persisting the address" in {
        val (service, _, _, sessionRepository) = newService()

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])

        when(sessionRepository.set(any())).thenReturn(Future.successful(true))

        implicit val dataRequest: DataRequest[_] =
          DataRequest(
            FakeRequest(),
            userAnswersId,
            emptyUserAnswers
              .set(AddressLookupAmendReturnQuery, true)
              .success
              .value
          )

        service
          .saveAddressDetails(testAddress, AddressOfSubcontractorPage)
          .futureValue mustBe true

        verify(sessionRepository).set(captor.capture())

        val savedAnswers = captor.getValue

        savedAnswers.get(AddressOfSubcontractorPage) mustBe Some(testAddress)
        savedAnswers.get(AddressLookupAmendReturnQuery) mustBe None
      }
    }
  }
}

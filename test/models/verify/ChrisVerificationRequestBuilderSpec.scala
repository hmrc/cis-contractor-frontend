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

package models.verify

import base.SpecBase
import connectors.ConstructionIndustrySchemeConnector
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import models.response.*
import models.requests.*
import models.*
import models.verify.ContractorEmailConfirmationStored.DifferentEmail
import pages.verify.{ContractorEmailConfirmationStoredPage, CurrentVerificationBatchResponsePage, EmailAddressPage}
import queries.CisIdQuery
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ChrisVerificationRequestBuilderSpec extends SpecBase with MockitoSugar {
  implicit private val ec: ExecutionContext = ExecutionContext.global
  implicit private val hc: HeaderCarrier    = HeaderCarrier()

  private val mockConnector = mock[ConstructionIndustrySchemeConnector]
  private val builder       = new ChrisVerificationRequestBuilder(mockConnector)

  "ChrisVerificationRequestBuilder" - {

    "must build a ChrisVerificationRequest" in {
      val subcontractor =
        SubcontractorCurrentVerification(
          subcontractorId = 10L,
          subbieResourceRef = Some(20L),
          firstName = Some("Test"),
          secondName = None,
          surname = Some("Subcontractor"),
          tradingName = None,
          utr = Some("1234567890"),
          nino = None,
          crn = None,
          partnerUtr = None,
          partnershipTradingName = None,
          subcontractorType = Some("Individual"),
          addressLine1 = None,
          addressLine2 = None,
          addressLine3 = None,
          addressLine4 = None,
          country = None,
          postcode = None,
          worksReferenceNumber = None
        )

      val currentVerificationBatchResponse =
        GetCurrentVerificationBatchResponse(
          subcontractors = Seq(subcontractor),
          verificationBatch = Some(
            VerificationBatchCurrentVerification(
              verificationBatchId = 1001L,
              verifBatchResourceRef = Some(2001L)
            )
          ),
          verifications = Seq(
            VerificationCurrentVerification(
              verificationId = 3001L,
              verificationBatchId = Some(1001L),
              subcontractorId = Some(10L),
              verificationResourceRef = Some(4001L)
            )
          )
        )

      val ua =
        emptyUserAnswers
          .set(CisIdQuery, "1")
          .success
          .value
          .set(ContractorEmailConfirmationStoredPage, DifferentEmail)
          .success
          .value
          .set(EmailAddressPage, "test@test.com")
          .success
          .value
          .set(CurrentVerificationBatchResponsePage, currentVerificationBatchResponse)
          .success
          .value

      when(mockConnector.getScheme(any())(any()))
        .thenReturn(
          Future.successful(
            Scheme(
              schemeId = 1,
              instanceId = "1",
              accountsOfficeReference = "AO123",
              taxOfficeNumber = "123",
              taxOfficeReference = "AB456",
              utr = Some("1234567890"),
              name = Some("Test Contractor"),
              emailAddress = Some("test@test.com")
            )
          )
        )

      val result =
        builder
          .build(
            ua = ua,
            isAgent = false,
            employerReference = EmployerReference("123", "AB456")
          )
          .futureValue

      result.instanceId mustBe "1"
      result.isAgent mustBe false
      result.clientTaxOfficeNumber mustBe "123"
      result.clientTaxOfficeRef mustBe "AB456"
      result.contractorUTR mustBe "1234567890"
      result.contractorAORef mustBe "AO123"
      result.verificationBatchId mustBe "1001"
      result.verificationBatchResourceRef mustBe "2001"
      result.emailRecipient.value mustBe "test@test.com"

      result.subcontractors mustBe Seq(subcontractor)

      result.verifications mustBe Seq(
        VerificationDetails(
          subcontractorName = "TBC",
          verificationResourceRef = "4001",
          proceedVerification = true
        )
      )
    }
  }
}

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
import models.{SubcontractorCurrentVerification, VerificationBatchCurrentVerification, VerificationCurrentVerification}
import models.requests.ProceedInsufficientVerificationRequest
import models.response.GetCurrentVerificationBatchResponse
import org.mockito.Mockito.{never, reset, verify, when}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.Messages
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ReviewInsufficientInfoServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private implicit val messages: Messages =
    app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(FakeRequest())

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.global

  private val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
  private val service                                            = new ReviewInsufficientInfoService(mockConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  private def mkSub(
    id: Long,
    firstName: Option[String] = None,
    surname: Option[String] = None,
    tradingName: Option[String] = None,
    partnershipTradingName: Option[String] = None,
    subcontractorType: Option[String] = None,
    utr: Option[String] = None,
    partnerUtr: Option[String] = None,
    crn: Option[String] = None,
    nino: Option[String] = None
  ): SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = id,
      subbieResourceRef = None,
      firstName = firstName,
      secondName = None,
      surname = surname,
      tradingName = tradingName,
      utr = utr,
      nino = nino,
      crn = crn,
      partnerUtr = partnerUtr,
      partnershipTradingName = partnershipTradingName,
      subcontractorType = subcontractorType,
      addressLine1 = None,
      addressLine2 = None,
      addressLine3 = None,
      addressLine4 = None,
      country = None,
      postcode = None,
      emailAddress = None,
      phoneNumber = None,
      mobilePhoneNumber = None,
      worksReferenceNumber = None,
      matched = None,
      autoVerified = None,
      verified = None,
      verificationNumber = None,
      taxTreatment = None,
      verificationDate = None,
      version = None,
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = None
    )

  private def mkVerification(subcontractorId: Long): VerificationCurrentVerification =
    VerificationCurrentVerification(
      verificationId = subcontractorId,
      verificationBatchId = None,
      subcontractorId = Some(subcontractorId),
      verificationResourceRef = None,
      subcontractorName = None,
      verificationNumber = None,
      taxTreatment = None,
      actionIndicator = None,
      proceed = None,
      matched = None
    )

  private def build(subs: SubcontractorCurrentVerification*) =
    service.buildViewModel(
      GetCurrentVerificationBatchResponse(
        subcontractors = subs,
        verificationBatch = None,
        verifications = Nil
      )
    )

  "ReviewInsufficientInfoService.buildViewModel" - {

    "must place a subcontractor with all required information into the ready list" in {
      val readyCompany =
        mkSub(id = 1L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = Some("1234567890"))

      val vm = build(readyCompany)

      vm.ready.map(_.name) mustBe Seq("Acme Ltd")
      vm.missing mustBe empty
      vm.allReady mustBe true
    }

    "must place a subcontractor missing its UTR into the missing list" in {
      val missingCompany =
        mkSub(id = 2L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = None)

      val vm = build(missingCompany)

      vm.missing.map(_.name) mustBe Seq("Acme Ltd")
      vm.ready mustBe empty
      vm.hasMissing mustBe true
      vm.allReady mustBe false
    }

    "must split a mixed batch into missing and ready" in {
      val missing =
        mkSub(
          id = 1L,
          surname = Some("Brody"),
          firstName = Some("Martin"),
          subcontractorType = Some("soletrader"),
          utr = None
        )
      val ready   =
        mkSub(id = 2L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = Some("1234567890"))

      val vm = build(missing, ready)

      vm.missing.map(_.name) mustBe Seq("Brody, Martin")
      vm.ready.map(_.name) mustBe Seq("Acme Ltd")
    }

    "must show 'None provided' for a missing UTR" in {
      val missing =
        mkSub(id = 1L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = None)

      val vm = build(missing)

      vm.missing.head.utr mustBe messages("verify.reviewInsufficientInfo.noneProvided")
    }

    "must render an individual's name as 'surname, firstName'" in {
      val sub =
        mkSub(
          id = 1L,
          surname = Some("Brody"),
          firstName = Some("Martin"),
          subcontractorType = Some("soletrader"),
          utr = None
        )

      val vm = build(sub)

      vm.missing.head.name mustBe "Brody, Martin"
    }

    "must fall back to trading name when individual name parts are blank" in {
      val sub =
        mkSub(
          id = 1L,
          firstName = Some(" "),
          surname = Some(" "),
          tradingName = Some("Doe Trading"),
          subcontractorType = Some("soletrader"),
          utr = None
        )

      val vm = build(sub)

      vm.missing.head.name mustBe "Doe Trading"
    }

    "must use 'No name provided' when no name can be derived" in {
      val sub = mkSub(id = 1L, subcontractorType = Some("company"), utr = None)

      val vm = build(sub)

      vm.missing.head.name mustBe messages("verify.noName")
    }

    "must use placeholder '#' urls for the name and action links (not yet wired)" in {
      val sub =
        mkSub(id = 1L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = None)

      val row = build(sub).missing.head

      row.nameLink.url mustBe "#"
      row.editLink.url mustBe "#"
      row.proceedLink.url mustBe controllers.insufficient.routes.ProceedInsufficientSubcontractorNameYesNoController
        .onPageLoad(1L)
        .url
      row.removeLink.url mustBe "#"
    }

    "must return empty lists for an empty batch" in {
      val vm = build()

      vm.missing mustBe empty
      vm.ready mustBe empty
      vm.allReady mustBe false
    }

    "must include all subcontractors in the batch, split by readiness" in {
      val missing =
        mkSub(id = 1L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = None)
      val ready   =
        mkSub(id = 2L, tradingName = Some("Other Ltd"), subcontractorType = Some("company"), utr = Some("1234567890"))

      val vm = build(missing, ready)

      vm.missing.map(_.name) mustBe Seq("Acme Ltd")
      vm.ready.map(_.name) mustBe Seq("Other Ltd")
    }

    "must only include subcontractors that are members of the current verification batch" in {
      val inBatch    =
        mkSub(id = 1L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = None)
      val notInBatch =
        mkSub(id = 2L, tradingName = Some("Other Ltd"), subcontractorType = Some("company"), utr = None)

      val vm =
        service.buildViewModel(
          GetCurrentVerificationBatchResponse(
            subcontractors = Seq(inBatch, notInBatch),
            verificationBatch = None,
            verifications = Seq(mkVerification(inBatch.subcontractorId))
          )
        )

      vm.missing.map(_.name) mustBe Seq("Acme Ltd")
      vm.ready mustBe empty
    }
  }

  "ReviewInsufficientInfoService.proceedInsufficientVerification" - {

    val subcontractorId              = 123L
    val cisId                        = "cis-123"
    val verificationBatchResourceRef = 99L
    val verificationResourceRef      = 10L

    val currentBatchResponse: GetCurrentVerificationBatchResponse =
      GetCurrentVerificationBatchResponse(
        subcontractors = Seq(
          SubcontractorCurrentVerification(
            subcontractorId = subcontractorId,
            subbieResourceRef = Some(1111L),
            firstName = None,
            secondName = None,
            surname = None,
            tradingName = None,
            utr = None,
            nino = None,
            crn = None,
            partnerUtr = None,
            partnershipTradingName = None,
            subcontractorType = None,
            addressLine1 = None,
            addressLine2 = None,
            addressLine3 = None,
            addressLine4 = None,
            country = None,
            postcode = None,
            emailAddress = None,
            phoneNumber = None,
            mobilePhoneNumber = None,
            worksReferenceNumber = None,
            matched = None,
            autoVerified = None,
            verified = None,
            verificationNumber = None,
            taxTreatment = None,
            verificationDate = None,
            version = None,
            updatedTaxTreatment = None,
            lastMonthlyReturnDate = None,
            pendingVerifications = None
          )
        ),
        verificationBatch = Some(
          VerificationBatchCurrentVerification(
            verificationBatchId = 999L,
            verifBatchResourceRef = Some(verificationBatchResourceRef)
          )
        ),
        verifications = Seq(
          VerificationCurrentVerification(
            verificationId = 1L,
            verificationBatchId = Some(999L),
            subcontractorId = Some(subcontractorId),
            verificationResourceRef = Some(verificationResourceRef),
            subcontractorName = None,
            verificationNumber = None,
            taxTreatment = None,
            actionIndicator = None,
            proceed = None,
            matched = None
          )
        )
      )

    "must call the connector with the correct request when resource refs are available" in {

      val request = ProceedInsufficientVerificationRequest(
        instanceId = cisId,
        verificationBatchResourceRef = verificationBatchResourceRef,
        verificationResourceRef = verificationResourceRef,
        proceed = "Y"
      )

      when(mockConnector.proceedInsufficientVerification(eqTo(request))(any[HeaderCarrier]))
        .thenReturn(Future.successful(()))

      service.proceedInsufficientVerification(cisId, subcontractorId, currentBatchResponse).futureValue mustBe ()

      verify(mockConnector).proceedInsufficientVerification(request)
    }

    "must fail when subcontractorId is missing" in {

      val result = service.proceedInsufficientVerification(cisId, 99, currentBatchResponse)

      result.failed.futureValue mustBe a[RuntimeException]

      verify(mockConnector, never).proceedInsufficientVerification(any[ProceedInsufficientVerificationRequest])(
        any[HeaderCarrier]
      )
    }
  }
}

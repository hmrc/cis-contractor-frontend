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
import models.Subcontractor
import models.response.GetNewestVerificationBatchResponse
import play.api.i18n.Messages
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext

class ReviewInsufficientInfoServiceSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private implicit val messages: Messages =
    app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(FakeRequest())

  private val mockConnector: ConstructionIndustrySchemeConnector = mock[ConstructionIndustrySchemeConnector]
  private val service                                            = new ReviewInsufficientInfoService(mockConnector)

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
  ): Subcontractor =
    Subcontractor(
      subcontractorId = id,
      firstName = firstName,
      secondName = None,
      surname = surname,
      tradingName = tradingName,
      partnershipTradingName = partnershipTradingName,
      verified = None,
      verificationNumber = None,
      taxTreatment = None,
      verificationDate = None,
      lastMonthlyReturnDate = None,
      createDate = None,
      subcontractorType = subcontractorType,
      subbieResourceRef = None,
      utr = utr,
      partnerUtr = partnerUtr,
      crn = crn,
      nino = nino
    )

  private def batchOf(subs: Subcontractor*): GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = None,
      subcontractors = subs,
      verificationBatch = None,
      verifications = Nil,
      submission = None,
      monthlyReturn = None,
      monthlyReturnSubmission = None
    )

  "ReviewInsufficientInfoService.buildViewModel" - {

    "must place a subcontractor with all required information into the ready list" in {
      val readyCompany =
        mkSub(id = 1L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = Some("1234567890"))

      val vm = service.buildViewModel(batchOf(readyCompany))

      vm.ready.map(_.name) mustBe Seq("Acme Ltd")
      vm.missing mustBe empty
      vm.allReady mustBe true
    }

    "must place a subcontractor missing its UTR into the missing list" in {
      val missingCompany =
        mkSub(id = 2L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = None)

      val vm = service.buildViewModel(batchOf(missingCompany))

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

      val vm = service.buildViewModel(batchOf(missing, ready))

      vm.missing.map(_.name) mustBe Seq("Brody, Martin")
      vm.ready.map(_.name) mustBe Seq("Acme Ltd")
    }

    "must show 'None provided' for a missing UTR" in {
      val missing =
        mkSub(id = 1L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = None)

      val vm = service.buildViewModel(batchOf(missing))

      vm.missing.head.utr mustBe messages("verify.reviewInsufficientInfo.utr.noneProvided")
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

      val vm = service.buildViewModel(batchOf(sub))

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

      val vm = service.buildViewModel(batchOf(sub))

      vm.missing.head.name mustBe "Doe Trading"
    }

    "must use 'No name provided' when no name can be derived" in {
      val sub = mkSub(id = 1L, subcontractorType = Some("company"), utr = None)

      val vm = service.buildViewModel(batchOf(sub))

      vm.missing.head.name mustBe messages("verify.noName")
    }

    "must use placeholder '#' urls for the name and action links (not yet wired)" in {
      val sub =
        mkSub(id = 1L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = None)

      val row = service.buildViewModel(batchOf(sub)).missing.head

      row.nameLink.url mustBe "#"
      row.editLink.url mustBe "#"
      row.proceedLink.url mustBe "#"
      row.removeLink.url mustBe "#"
    }

    "must return empty lists for an empty batch" in {
      val vm = service.buildViewModel(batchOf())

      vm.missing mustBe empty
      vm.ready mustBe empty
      vm.allReady mustBe false
    }
  }
}

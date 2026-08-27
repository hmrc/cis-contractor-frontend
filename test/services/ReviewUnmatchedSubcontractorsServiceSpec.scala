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
import models.SubcontractorCurrentVerification
import models.VerificationCurrentVerification
import models.response.GetCurrentVerificationBatchResponse
import play.api.i18n.Messages
import play.api.test.FakeRequest

class ReviewUnmatchedSubcontractorsServiceSpec extends SpecBase {

  private implicit val messages: Messages =
    app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(FakeRequest())

  private val service = new ReviewUnmatchedSubcontractorsService()

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

  private def mkVerification(
    subcontractorId: Long,
    subcontractorName: Option[String] = None,
    proceed: Option[String] = None,
    actionIndicator: Option[String] = None
  ): VerificationCurrentVerification =
    VerificationCurrentVerification(
      verificationId = subcontractorId,
      verificationBatchId = None,
      subcontractorId = Some(subcontractorId),
      verificationResourceRef = None,
      subcontractorName = subcontractorName,
      verificationNumber = None,
      taxTreatment = None,
      actionIndicator = actionIndicator,
      proceed = proceed,
      matched = None
    )

  private def build(
    subs: Seq[SubcontractorCurrentVerification],
    verifications: Seq[VerificationCurrentVerification] = Nil
  ) =
    service.buildViewModel(
      GetCurrentVerificationBatchResponse(
        subcontractors = subs,
        verificationBatch = None,
        verifications = verifications
      )
    )

  "ReviewUnmatchedSubcontractorsService.buildViewModel" - {

    "must place an edited subcontractor (actionIndicator = 'EDIT') into the ready list" in {
      val readyCompany =
        mkSub(id = 1L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = Some("1234567890"))
      val verification = mkVerification(subcontractorId = 1L, actionIndicator = Some("EDIT"))

      val vm = build(Seq(readyCompany), Seq(verification))

      vm.ready.map(_.name) mustBe Seq("Acme Ltd")
      vm.unmatched mustBe empty
      vm.allReady mustBe true
    }

    "must place a subcontractor with no edit or proceed decision into the unmatched list" in {
      val unmatchedCompany =
        mkSub(id = 2L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = Some("1234567890"))

      val vm = build(Seq(unmatchedCompany))

      vm.unmatched.map(_.name) mustBe Seq("Acme Ltd")
      vm.ready mustBe empty
      vm.hasUnmatched mustBe true
      vm.allReady mustBe false
    }

    "must split a mixed batch into unmatched and ready" in {
      val unmatched    =
        mkSub(
          id = 1L,
          surname = Some("Brody"),
          firstName = Some("Martin"),
          subcontractorType = Some("soletrader"),
          utr = None
        )
      val ready        =
        mkSub(id = 2L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = Some("1234567890"))
      val verification = mkVerification(subcontractorId = 2L, proceed = Some("Y"))

      val vm = build(Seq(unmatched, ready), Seq(verification))

      vm.unmatched.map(_.name) mustBe Seq("Brody, Martin")
      vm.ready.map(_.name) mustBe Seq("Acme Ltd")
    }

    "must use the verification's SUBCONTRACTOR_NAME for the displayed name when present" in {
      val sub          =
        mkSub(id = 1L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = None)
      val verification = mkVerification(subcontractorId = 1L, subcontractorName = Some("Verified Trading Name"))

      val vm = build(Seq(sub), Seq(verification))

      vm.unmatched.head.name mustBe "Verified Trading Name"
    }

    "must fall back to the derived subcontractor name when the verification name is blank" in {
      val sub          =
        mkSub(id = 1L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = None)
      val verification = mkVerification(subcontractorId = 1L, subcontractorName = Some("   "))

      val vm = build(Seq(sub), Seq(verification))

      vm.unmatched.head.name mustBe "Acme Ltd"
    }

    "must move a subcontractor with proceed = 'Y' into the ready list" in {
      val sub          =
        mkSub(id = 1L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = None)
      val verification = mkVerification(subcontractorId = 1L, proceed = Some("Y"))

      val vm = build(Seq(sub), Seq(verification))

      vm.ready.map(_.name) mustBe Seq("Acme Ltd")
      vm.unmatched mustBe empty
    }

    "must show 'None provided' for a missing UTR" in {
      val unmatched =
        mkSub(id = 1L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = None)

      val vm = build(Seq(unmatched))

      vm.unmatched.head.utr mustBe messages("verify.reviewUnmatched.noneProvided")
    }

    "must render an individual's name as 'surname, firstName' when no verification name exists" in {
      val sub =
        mkSub(
          id = 1L,
          surname = Some("Brody"),
          firstName = Some("Martin"),
          subcontractorType = Some("soletrader"),
          utr = None
        )

      val vm = build(Seq(sub))

      vm.unmatched.head.name mustBe "Brody, Martin"
    }

    "must use 'No name provided' when no name can be derived" in {
      val sub = mkSub(id = 1L, subcontractorType = Some("company"), utr = None)

      val vm = build(Seq(sub))

      vm.unmatched.head.name mustBe messages("verify.noName")
    }

    "must use placeholder '#' urls for the name and action links (not yet wired)" in {
      val sub =
        mkSub(id = 1L, tradingName = Some("Acme Ltd"), subcontractorType = Some("company"), utr = None)

      val row = build(Seq(sub)).unmatched.head

      row.nameLink.url mustBe "#"
      row.editLink.url mustBe "#"
      row.proceedLink.url mustBe "#"
      row.removeLink.url mustBe "#"
    }

    "must return empty lists for an empty batch" in {
      val vm = build(Nil)

      vm.unmatched mustBe empty
      vm.ready mustBe empty
      vm.allReady mustBe false
    }
  }
}

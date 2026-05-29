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
import models.verify.ContractorEmailConfirmationStored.{CurrentEmail, DifferentEmail, DoNotSend}
import models.{ContractorScheme, InvalidAnswer, MissingAnswer, RichJsObject, SubcontractorViewModel}
import models.response.GetNewestVerificationBatchResponse
import org.scalatest.matchers.must.Matchers
import pages.verify.*
import play.api.libs.json.{JsError, JsSuccess, Json, Writes}

class ValidatedVerifySpec extends SpecBase with Matchers {

  private val brodyMartin = SubcontractorViewModel("1", "Brody, Martin")
  private val grantAlan   = SelectedSubcontractors("4", "Grant, Alan")

  private def batchResponseWithEmail(email: String): GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = Some(ContractorScheme(accountsOfficeReference = None, emailAddress = Some(email))),
      subcontractors = Seq.empty,
      verificationBatch = None,
      verifications = Seq.empty,
      submission = None,
      monthlyReturn = None,
      monthlyReturnSubmission = None
    )

  private val batchResponseWithoutEmail: GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = Some(ContractorScheme(accountsOfficeReference = None, emailAddress = None)),
      subcontractors = Seq.empty,
      verificationBatch = None,
      verifications = Seq.empty,
      submission = None,
      monthlyReturn = None,
      monthlyReturnSubmission = None
    )

  private val minRequiredLessEmail =
    emptyUserAnswers
      .set(SelectSubcontractorPage, Set(brodyMartin))
      .success
      .value
      .set(ReverifyExistingSubcontractorsYesNoPage, false)
      .success
      .value

  private val minRequired =
    minRequiredLessEmail
      .set(ContractorEmailConfirmationStoredPage, DoNotSend)
      .success
      .value

  private def withStaleValue[A](
    ua: models.UserAnswers,
    page: pages.QuestionPage[A],
    value: A
  )(implicit w: Writes[A]): models.UserAnswers =
    ua.data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(updated, _) => ua.copy(data = updated)
      case JsError(_)            => ua
    }

  "ValidatedVerify.build" - {

    // ─── Success cases ───────────────────────────────────────────────────────

    "build successfully with minimum required answers (reverify=false, DoNotSend)" in {
      ValidatedVerify.build(minRequired) mustBe Right(
        ValidatedVerify(
          selectedSubcontractors = Set(brodyMartin),
          subcontractorsToReverify = None,
          emailToUse = None
        )
      )
    }

    "build successfully when reverify=true and subcontractors to reverify are provided" in {
      val ua =
        minRequired
          .set(ReverifyExistingSubcontractorsYesNoPage, true)
          .success
          .value
          .set(SelectSubcontractorsToReverifyPage, Set(grantAlan))
          .success
          .value

      ValidatedVerify.build(ua) mustBe Right(
        ValidatedVerify(
          selectedSubcontractors = Set(brodyMartin),
          subcontractorsToReverify = Some(Set(grantAlan)),
          emailToUse = None
        )
      )
    }

    "resolve emailToUse from scheme address when ContractorEmailConfirmationStored is CurrentEmail" in {
      val ua =
        minRequired
          .set(NewestVerificationBatchResponsePage, batchResponseWithEmail("scheme@example.com"))
          .success
          .value
          .set(ContractorEmailConfirmationStoredPage, CurrentEmail)
          .success
          .value

      ValidatedVerify.build(ua).map(_.emailToUse) mustBe Right(Some("scheme@example.com"))
    }

    "resolve emailToUse from EmailAddressPage when ContractorEmailConfirmationStored is DifferentEmail" in {
      val ua =
        minRequired
          .set(NewestVerificationBatchResponsePage, batchResponseWithEmail("scheme@example.com"))
          .success
          .value
          .set(ContractorEmailConfirmationStoredPage, DifferentEmail)
          .success
          .value
          .set(EmailAddressPage, "override@example.com")
          .success
          .value

      ValidatedVerify.build(ua).map(_.emailToUse) mustBe Right(Some("override@example.com"))
    }

    "resolve emailToUse to None when ContractorEmailConfirmationStored is DoNotSend" in {
      val ua =
        minRequired
          .set(NewestVerificationBatchResponsePage, batchResponseWithEmail("scheme@example.com"))
          .success
          .value
          .set(ContractorEmailConfirmationStoredPage, DoNotSend)
          .success
          .value

      ValidatedVerify.build(ua).map(_.emailToUse) mustBe Right(None)
    }

    "resolve emailToUse from EmailAddressPage when ContractorEmailConfirmationNotStored is true" in {
      val ua =
        minRequiredLessEmail
          .set(ContractorEmailConfirmationNotStoredPage, true)
          .success
          .value
          .set(EmailAddressPage, "new@example.com")
          .success
          .value

      ValidatedVerify.build(ua).map(_.emailToUse) mustBe Right(Some("new@example.com"))
    }

    "resolve emailToUse to None when ContractorEmailConfirmationNotStored is false" in {
      val ua =
        minRequiredLessEmail
          .set(ContractorEmailConfirmationNotStoredPage, false)
          .success
          .value

      ValidatedVerify.build(ua).map(_.emailToUse) mustBe Right(None)
    }

    "not fail when optional ReverifyExistingSubcontractorsYesNoPage is missing" in {
      val ua = emptyUserAnswers
        .set(SelectSubcontractorPage, Set(brodyMartin))
        .success
        .value
        .set(ContractorEmailConfirmationStoredPage, ContractorEmailConfirmationStored.DoNotSend)
        .success
        .value

      ValidatedVerify.build(ua) mustBe Right(
        ValidatedVerify(
          selectedSubcontractors = Set(brodyMartin),
          subcontractorsToReverify = None,
          emailToUse = None
        )
      )
    }

    // ─── Failure: missing required pages ─────────────────────────────────────

    "fail when SelectSubcontractorPage is missing" in {
      ValidatedVerify.build(emptyUserAnswers) mustBe Left(MissingAnswer(SelectSubcontractorPage))
    }

    "fail when reverify=true but SelectSubcontractorsToReverifyPage is absent" in {
      val ua =
        minRequired
          .set(ReverifyExistingSubcontractorsYesNoPage, true)
          .success
          .value

      ValidatedVerify.build(ua) mustBe Left(InvalidAnswer(SelectSubcontractorsToReverifyPage))
    }

    // ─── Failure: empty sets ──────────────────────────────────────────────────

    "fail when selectedSubcontractors is an empty set" in {
      val ua =
        emptyUserAnswers
          .set(SelectSubcontractorPage, Set.empty[SubcontractorViewModel])
          .success
          .value
          .set(ReverifyExistingSubcontractorsYesNoPage, false)
          .success
          .value

      ValidatedVerify.build(ua) mustBe Left(InvalidAnswer(SelectSubcontractorPage))
    }

    "fail when reverify=true but subcontractorsToReverify is an empty set" in {
      val ua =
        minRequired
          .set(ReverifyExistingSubcontractorsYesNoPage, true)
          .success
          .value
          .set(SelectSubcontractorsToReverifyPage, Set.empty[SelectedSubcontractors])
          .success
          .value

      ValidatedVerify.build(ua) mustBe Left(InvalidAnswer(SelectSubcontractorsToReverifyPage))
    }

    // ─── Failure: stale session data ─────────────────────────────────────────

    "fail when reverify=false but subcontractors to reverify are still present (stale session)" in {
      val ua = withStaleValue(minRequired, SelectSubcontractorsToReverifyPage, Set(grantAlan))

      ValidatedVerify.build(ua) mustBe Left(InvalidAnswer(SelectSubcontractorsToReverifyPage))
    }

    // ─── Failure: email pairing ───────────────────────────────────────────────

    "fail when CurrentEmail is selected but the scheme has no email address" in {
      val ua =
        minRequired
          .set(NewestVerificationBatchResponsePage, batchResponseWithoutEmail)
          .success
          .value
          .set(ContractorEmailConfirmationStoredPage, CurrentEmail)
          .success
          .value

      ValidatedVerify.build(ua) mustBe Left(MissingAnswer(ContractorEmailConfirmationStoredPage))
    }

    "fail when DifferentEmail is selected but EmailAddressPage is absent" in {
      val ua =
        minRequired
          .set(NewestVerificationBatchResponsePage, batchResponseWithEmail("scheme@example.com"))
          .success
          .value
          .set(ContractorEmailConfirmationStoredPage, DifferentEmail)
          .success
          .value

      ValidatedVerify.build(ua) mustBe Left(MissingAnswer(EmailAddressPage))
    }

    "fail when ContractorEmailConfirmationNotStored is true but EmailAddressPage is absent" in {
      val ua =
        minRequiredLessEmail
          .set(ContractorEmailConfirmationNotStoredPage, true)
          .success
          .value

      ValidatedVerify.build(ua) mustBe Left(MissingAnswer(EmailAddressPage))
    }

    "fail when neither email confirmation page is present" in {
      ValidatedVerify.build(minRequiredLessEmail) mustBe Left(MissingAnswer(ContractorEmailConfirmationStoredPage))
    }
  }
}

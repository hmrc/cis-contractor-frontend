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

package controllers.verify

import base.SpecBase
import models.verify.ContractorEmailConfirmationStored
import models.verify.ContractorEmailConfirmationStored.{CurrentEmail, DifferentEmail, DoNotSend}
import models.verify.SelectedSubcontractors
import models.{ContractorScheme, Subcontractor, SubcontractorViewModel}
import models.response.GetNewestVerificationBatchResponse
import org.scalatestplus.mockito.MockitoSugar
import org.jsoup.Jsoup
import pages.verify.*
import play.api.i18n.{ Messages, MessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class VerifyCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {
  private lazy val onPageLoadRoute = controllers.verify.routes.VerifyCheckYourAnswersController.onPageLoad().url
  private lazy val onSubmitRoute   = controllers.verify.routes.VerifyCheckYourAnswersController.onSubmit().url

  private val brodyMartin         = SubcontractorViewModel("1", "Brody, Martin")
  private val hooperAndAssociates = SubcontractorViewModel("2", "Hooper And Associates")
  private val quintTransportation = SubcontractorViewModel("3", "Quint Transportation")

  private val grantAlan     = SelectedSubcontractors("4", "Grant, Alan")
  private val ingenResearch = SelectedSubcontractors("5", "InGen Research")

  private val aSubcontractor: Subcontractor = Subcontractor(
    subcontractorId = 1L,
    firstName = None,
    secondName = None,
    surname = None,
    tradingName = Some("Brody & Co"),
    partnershipTradingName = None,
    verified = None,
    verificationNumber = None,
    taxTreatment = None,
    verificationDate = None,
    lastMonthlyReturnDate = None,
    createDate = None,
    subcontractorType = None,
    subbieResourceRef = None,
    utr = None,
    partnerUtr = None,
    crn = None,
    nino = None
  )

  private def batchResponseWithEmail(
    email: String,
    subs: Seq[Subcontractor] = Seq.empty
  ): GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = Some(ContractorScheme(accountsOfficeReference = None, emailAddress = Some(email))),
      subcontractors = subs,
      verificationBatch = None,
      verifications = Seq.empty,
      submission = None,
      monthlyReturn = None,
      monthlyReturnSubmission = None
    )

  "VerifyCheckYourAnswersController" - {

    "onPageLoad" - {

      // ─── Scenario 1: single subcontractor, stored email, CurrentEmail ───────────
      "Scenario 1 — single subcontractor with stored email" - {

        "must return OK" in {
          val ua = emptyUserAnswers
            .setOrException(SelectSubcontractorPage, Set(brodyMartin))
            .setOrException(ReverifyExistingSubcontractorsYesNoPage, false)
            .setOrException(NewestVerificationBatchResponsePage, batchResponseWithEmail("agent@example.com"))
            .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
            .setOrException(VerificationBatchReadinessPage, true)

          val application = applicationBuilder(userAnswers = Some(ua)).build()
          running(application) {
            val result = route(application, FakeRequest(GET, onPageLoadRoute)).value
            status(result) mustEqual OK
          }
        }

        "must render the subcontractor as plain text (no bullet)" in {
          val ua = emptyUserAnswers
            .setOrException(SelectSubcontractorPage, Set(brodyMartin))
            .setOrException(ReverifyExistingSubcontractorsYesNoPage, false)
            .setOrException(NewestVerificationBatchResponsePage, batchResponseWithEmail("agent@example.com"))
            .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
            .setOrException(VerificationBatchReadinessPage, true)

          val application = applicationBuilder(userAnswers = Some(ua)).build()
          running(application) {
            val result = route(application, FakeRequest(GET, onPageLoadRoute)).value
            val doc    = Jsoup.parse(contentAsString(result))

            val rows = doc.select(".govuk-summary-list__row")
            rows.size() mustBe 3

            val subRow = rows.get(0)
            subRow.select(".govuk-summary-list__value").text() mustBe "Brody, Martin"
            subRow.select(".govuk-list--bullet").size() mustBe 0
          }
        }

        "must render the stored email inline in bold in the email confirmation row" in {
          val ua = emptyUserAnswers
            .setOrException(SelectSubcontractorPage, Set(brodyMartin))
            .setOrException(ReverifyExistingSubcontractorsYesNoPage, false)
            .setOrException(NewestVerificationBatchResponsePage, batchResponseWithEmail("agent@example.com"))
            .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
            .setOrException(VerificationBatchReadinessPage, true)

          val application = applicationBuilder(userAnswers = Some(ua)).build()
          running(application) {
            val result = route(application, FakeRequest(GET, onPageLoadRoute)).value
            val doc    = Jsoup.parse(contentAsString(result))

            val emailRows = doc.select(".govuk-summary-list__row")
            val emailRow  = emailRows.get(2)
            emailRow.select(".govuk-summary-list__value").text() must include("agent@example.com")
            emailRow.select(".govuk-summary-list__value strong").text() mustBe "agent@example.com"
          }
        }

        "must not render the reverify or email address rows" in {
          val ua = emptyUserAnswers
            .setOrException(SelectSubcontractorPage, Set(brodyMartin))
            .setOrException(ReverifyExistingSubcontractorsYesNoPage, false)
            .setOrException(NewestVerificationBatchResponsePage, batchResponseWithEmail("agent@example.com"))
            .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
            .setOrException(VerificationBatchReadinessPage, true)

          val application = applicationBuilder(userAnswers = Some(ua)).build()
          running(application) {
            val result = route(application, FakeRequest(GET, onPageLoadRoute)).value
            val doc    = Jsoup.parse(contentAsString(result))

            doc.select(".govuk-summary-list__row").size() mustBe 3
          }
        }

        "must render the None Selected text inline in the reverify subcontractor row for none selected" in {
          val ua = emptyUserAnswers
            .setOrException(SelectSubcontractorPage, Set(brodyMartin))
            .setOrException(ReverifyExistingSubcontractorsYesNoPage, true)
            .setOrException(SelectSubcontractorsToReverifyPage, Set())
            .setOrException(NewestVerificationBatchResponsePage, batchResponseWithEmail("agent@example.com"))
            .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
            .setOrException(VerificationBatchReadinessPage, true)

          val application = applicationBuilder(userAnswers = Some(ua)).build()
          running(application) {
            val messagesApi = application.injector.instanceOf[MessagesApi]
            implicit val messages: Messages = messagesApi.preferred(FakeRequest())
            val result = route(application, FakeRequest(GET, onPageLoadRoute)).value
            val doc    = Jsoup.parse(contentAsString(result))
            val allRows     = doc.select(".govuk-summary-list__row")
            val reverifyRow = allRows.get(2)
            reverifyRow.select(".govuk-summary-list__value").text() must include(messages("verify.selectSubcontractor.display.noneSelected"))
          }
        }

        "must render the None selected text inline in the verify subcontractor row for none selected " in {
          val ua = emptyUserAnswers
            .setOrException(SelectSubcontractorPage, Set())
            .setOrException(ReverifyExistingSubcontractorsYesNoPage, true)
            .setOrException(SelectSubcontractorsToReverifyPage, Set(grantAlan, ingenResearch))
            .setOrException(NewestVerificationBatchResponsePage, batchResponseWithEmail("agent@example.com"))
            .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
            .setOrException(VerificationBatchReadinessPage, true)

          val application = applicationBuilder(userAnswers = Some(ua)).build()
          running(application) {
            val messagesApi = application.injector.instanceOf[MessagesApi]
            implicit val messages: Messages = messagesApi.preferred(FakeRequest())

            val result = route(application, FakeRequest(GET, onPageLoadRoute)).value
            val doc    = Jsoup.parse(contentAsString(result))

            val allRows     = doc.select(".govuk-summary-list__row")
            val reverifyRow = allRows.get(0)
            reverifyRow.select(".govuk-summary-list__value").text() mustBe messages("verify.selectSubcontractor.display.noneSelected")
          }
        }
      }

      // ─── Scenario 2: multiple subcontractors, stored email, CurrentEmail ──────
      "Scenario 2/3 — multiple subcontractors with stored email" - {

        "must render subcontractors as a bullet list" in {
          val ua = emptyUserAnswers
            .setOrException(SelectSubcontractorPage, Set(brodyMartin, hooperAndAssociates, quintTransportation))
            .setOrException(ReverifyExistingSubcontractorsYesNoPage, false)
            .setOrException(NewestVerificationBatchResponsePage, batchResponseWithEmail("agent@example.com"))
            .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
            .setOrException(VerificationBatchReadinessPage, true)

          val application = applicationBuilder(userAnswers = Some(ua)).build()
          running(application) {
            val result = route(application, FakeRequest(GET, onPageLoadRoute)).value
            val doc    = Jsoup.parse(contentAsString(result))

            val subRow  = doc.select(".govuk-summary-list__row").get(0)
            val bullets = subRow.select(".govuk-list--bullet li")
            bullets.size() mustBe 3
          }
        }
      }

      // ─── Scenario 4: multiple subcontractors, DifferentEmail, user email ────────
      "Scenario 4 — user selects a different email address" - {

        "must show 'Use a different email address' in the confirmation row and the entered email separately" in {
          val ua = emptyUserAnswers
            .setOrException(SelectSubcontractorPage, Set(brodyMartin, hooperAndAssociates))
            .setOrException(ReverifyExistingSubcontractorsYesNoPage, false)
            .setOrException(NewestVerificationBatchResponsePage, batchResponseWithEmail("scheme@example.com"))
            .setOrException(ContractorEmailConfirmationStoredPage, DifferentEmail)
            .setOrException(EmailAddressPage, "override@example.com")
            .setOrException(VerificationBatchReadinessPage, true)

          val application = applicationBuilder(userAnswers = Some(ua)).build()
          running(application) {
            val result = route(application, FakeRequest(GET, onPageLoadRoute)).value
            val doc    = Jsoup.parse(contentAsString(result))

            val rows = doc.select(".govuk-summary-list__row")
            rows.size() mustBe 4

            val confirmRow = rows.get(2)
            confirmRow.select(".govuk-summary-list__value").text() must include(
              messages(application)("verify.contractorEmailConfirmationStored.differentEmail")
            )

            val emailRow = rows.get(3)
            emailRow.select(".govuk-summary-list__value").text() mustBe "override@example.com"
          }
        }
      }

      // ─── Scenario 5: multiple subcontractors, DoNotSend ─────────────────────────
      "Scenario 5 — user selects do not send an email" - {

        "must show 'Do not send an email confirmation' and no separate email row" in {
          val ua = emptyUserAnswers
            .setOrException(SelectSubcontractorPage, Set(brodyMartin, hooperAndAssociates))
            .setOrException(ReverifyExistingSubcontractorsYesNoPage, false)
            .setOrException(NewestVerificationBatchResponsePage, batchResponseWithEmail("scheme@example.com"))
            .setOrException(ContractorEmailConfirmationStoredPage, DoNotSend)
            .setOrException(VerificationBatchReadinessPage, true)

          val application = applicationBuilder(userAnswers = Some(ua)).build()
          running(application) {
            val result = route(application, FakeRequest(GET, onPageLoadRoute)).value
            val doc    = Jsoup.parse(contentAsString(result))

            val rows = doc.select(".govuk-summary-list__row")
            rows.size() mustBe 3

            val confirmRow = rows.get(2)
            confirmRow.select(".govuk-summary-list__value").text() mustBe
              messages(application)("verify.contractorEmailConfirmationStored.doNotSend")
          }
        }
      }

      // ─── Scenario 6: verify + reverify, stored email ─────────────────────────────
      "Scenario 6 — verification and reverification with stored email" - {

        "must render all four rows in the correct order" in {
          val ua = emptyUserAnswers
            .setOrException(SelectSubcontractorPage, Set(brodyMartin, hooperAndAssociates))
            .setOrException(ReverifyExistingSubcontractorsYesNoPage, true)
            .setOrException(SelectSubcontractorsToReverifyPage, Set(grantAlan, ingenResearch))
            .setOrException(
              NewestVerificationBatchResponsePage,
              batchResponseWithEmail("agent@example.com", Seq(aSubcontractor))
            )
            .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
            .setOrException(VerificationBatchReadinessPage, true)

          val application = applicationBuilder(userAnswers = Some(ua)).build()
          running(application) {
            val result = route(application, FakeRequest(GET, onPageLoadRoute)).value
            val doc    = Jsoup.parse(contentAsString(result))

            val rows = doc.select(".govuk-summary-list__row")
            rows.size() mustBe 4

            rows.get(0).select(".govuk-summary-list__key").text() must include(
              messages(application)("verify.selectSubcontractor.checkYourAnswersLabel")
            )
            rows.get(1).select(".govuk-summary-list__key").text() must include(
              messages(application)("verify.reverifyExistingSubcontractorsYesNo.checkYourAnswersLabel")
            )
            rows.get(2).select(".govuk-summary-list__key").text() must include(
              messages(application)("verify.selectSubcontractorsToReverify.checkYourAnswersLabel")
            )
            rows.get(3).select(".govuk-summary-list__key").text() must include(
              messages(application)("verify.contractorEmailConfirmationStored.checkYourAnswersLabel")
            )
          }
        }

        "must render reverify subcontractors as a bullet list" in {
          val ua = emptyUserAnswers
            .setOrException(SelectSubcontractorPage, Set(brodyMartin, hooperAndAssociates))
            .setOrException(ReverifyExistingSubcontractorsYesNoPage, true)
            .setOrException(SelectSubcontractorsToReverifyPage, Set(grantAlan, ingenResearch))
            .setOrException(
              NewestVerificationBatchResponsePage,
              batchResponseWithEmail("agent@example.com", Seq(aSubcontractor))
            )
            .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
            .setOrException(VerificationBatchReadinessPage, true)

          val application = applicationBuilder(userAnswers = Some(ua)).build()
          running(application) {
            val result = route(application, FakeRequest(GET, onPageLoadRoute)).value
            val doc    = Jsoup.parse(contentAsString(result))

            val reverifyRow = doc.select(".govuk-summary-list__row").get(2)
            val bullets     = reverifyRow.select(".govuk-list--bullet li")
            bullets.size() mustBe 2
          }
        }

        "must render reverify Yes/No row with 'No' but no selection row when answer is false" in {
          val ua = emptyUserAnswers
            .setOrException(SelectSubcontractorPage, Set(brodyMartin, hooperAndAssociates))
            .setOrException(ReverifyExistingSubcontractorsYesNoPage, false)
            .setOrException(
              NewestVerificationBatchResponsePage,
              batchResponseWithEmail("agent@example.com", Seq(aSubcontractor))
            )
            .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
            .setOrException(VerificationBatchReadinessPage, true)

          val application = applicationBuilder(userAnswers = Some(ua)).build()
          running(application) {
            val result = route(application, FakeRequest(GET, onPageLoadRoute)).value
            val doc    = Jsoup.parse(contentAsString(result))

            val rows = doc.select(".govuk-summary-list__row")
            rows.size() mustBe 3

            rows.get(1).select(".govuk-summary-list__key").text() must include(
              messages(application)("verify.reverifyExistingSubcontractorsYesNo.checkYourAnswersLabel")
            )
            rows.get(1).select(".govuk-summary-list__value").text() mustBe messages(application)("site.no")
          }
        }

        "must not render the reverify row when the batch has no existing subcontractors" in {
          val ua = emptyUserAnswers
            .setOrException(SelectSubcontractorPage, Set(brodyMartin, hooperAndAssociates))
            .setOrException(ReverifyExistingSubcontractorsYesNoPage, false)
            .setOrException(NewestVerificationBatchResponsePage, batchResponseWithEmail("agent@example.com"))
            .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
            .setOrException(VerificationBatchReadinessPage, true)

          val application = applicationBuilder(userAnswers = Some(ua)).build()
          running(application) {
            val result = route(application, FakeRequest(GET, onPageLoadRoute)).value
            val doc    = Jsoup.parse(contentAsString(result))

            doc.select(".govuk-summary-list__row").size() mustBe 3
          }
        }
      }

      "must not render the reverify row when the reverify yes/no page wasn't answered" in {
        val ua = emptyUserAnswers
          .setOrException(SelectSubcontractorPage, Set(brodyMartin, hooperAndAssociates))
          .setOrException(NewestVerificationBatchResponsePage, batchResponseWithEmail("agent@example.com"))
          .setOrException(ContractorEmailConfirmationStoredPage, CurrentEmail)
          .setOrException(VerificationBatchReadinessPage, true)

        val application = applicationBuilder(userAnswers = Some(ua)).build()
        running(application) {
          val result = route(application, FakeRequest(GET, onPageLoadRoute)).value
          val doc    = Jsoup.parse(contentAsString(result))

          doc.select(".govuk-summary-list__row").size() mustBe 2
        }
      }

      "must redirect to Journey Recovery when there is no session data" in {
        val application = applicationBuilder(userAnswers = None).build()
        running(application) {
          val result = route(application, FakeRequest(GET, onPageLoadRoute)).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when session data is incomplete" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          val result = route(application, FakeRequest(GET, onPageLoadRoute)).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must redirect to Submission Sending when answers are valid" in {
        val ua = emptyUserAnswers
          .setOrException(SelectSubcontractorPage, Set(brodyMartin))
          .setOrException(ReverifyExistingSubcontractorsYesNoPage, false)
          .setOrException(ContractorEmailConfirmationStoredPage, DoNotSend)
          .setOrException(VerificationBatchReadinessPage, true)

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val result = route(application, FakeRequest(POST, onSubmitRoute)).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.verify.routes.SubmissionSendingController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery when answers fail validation" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val result = route(application, FakeRequest(POST, onSubmitRoute)).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}

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
import controllers.routes
import models.{NormalMode, Subcontractor, UserAnswers}
import models.response.GetNewestVerificationBatchResponse
import models.verify.SelectedSubcontractors
import navigation.{FakeNavigator, Navigator}

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.verify._
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import viewmodels.verify.SubcontractorReverifyRow

import java.time.{Clock, Instant, LocalDateTime, ZoneOffset}
import scala.concurrent.Future

class SelectSubcontractorsToReverifyControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  private val fixedClock: Clock =
    Clock.fixed(Instant.parse("2026-01-25T10:00:00Z"), ZoneOffset.UTC)

  private def newestBatchResponse(subcontractors: Seq[Subcontractor]): GetNewestVerificationBatchResponse =
    GetNewestVerificationBatchResponse(
      scheme = None,
      subcontractors = subcontractors,
      verificationBatch = None,
      verifications = Nil,
      submission = None,
      monthlyReturn = None,
      monthlyReturnSubmission = None
    )

  private def mkSub(
    id: Long,
    verified: Option[String],
    firstName: Option[String] = None,
    surname: Option[String] = None,
    tradingName: Option[String] = None,
    partnershipTradingName: Option[String] = None,
    subcontractorType: Option[String] = None,
    utr: Option[String] = None,
    verificationNumber: Option[String] = None,
    taxTreatment: Option[String] = None,
    verificationDate: Option[LocalDateTime] = None,
    lastMonthlyReturnDate: Option[LocalDateTime] = None,
    createDate: Option[LocalDateTime] = None
  ): Subcontractor =
    Subcontractor(
      subcontractorId = id,
      firstName = firstName,
      secondName = None,
      surname = surname,
      tradingName = tradingName,
      partnershipTradingName = partnershipTradingName,
      verified = verified,
      verificationNumber = verificationNumber,
      taxTreatment = taxTreatment,
      verificationDate = verificationDate,
      lastMonthlyReturnDate = lastMonthlyReturnDate,
      createDate = createDate,
      subcontractorType = subcontractorType,
      subbieResourceRef = None,
      utr = utr,
      partnerUtr = None,
      crn = None,
      nino = None
    )

  private def url(page: Int = 1): String =
    controllers.verify.routes.SelectSubcontractorsToReverifyController
      .onPageLoad(NormalMode, page)
      .url

  private lazy val postUrl: String =
    controllers.verify.routes.SelectSubcontractorsToReverifyController
      .onPageLoad(NormalMode)
      .url

  "SelectSubcontractorsToReverifyController" - {

    "onPageLoad" - {

      "must redirect to JourneyRecovery when NewestVerificationBatchResponsePage is missing" in {
        val mockRepo = mock[SessionRepository]
        when(mockRepo.set(any())) thenReturn Future.successful(true)

        val app =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[Clock].toInstance(fixedClock),
              bind[SessionRepository].toInstance(mockRepo)
            )
            .build()

        running(app) {
          val result = route(app, FakeRequest(GET, url(1))).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

          verify(mockRepo, never()).set(any())
        }
      }

      "must return OK, render rows derived from newest batch and save SubcontractorReverifyRowsPage in session" in {
        val mockRepo = mock[SessionRepository]
        when(mockRepo.set(any())) thenReturn Future.successful(true)

        // Sub 1: VERIFIED=Y, verificationDate missing => reverifyRequired=true => Verified column = No,
        val subNeedsReverify =
          mkSub(
            id = 100L,
            verified = Some("Y"),
            firstName = Some("Martin"),
            surname = Some("Brody"),
            tradingName = Some("Some Trading"),
            subcontractorType = Some("individual"),
            utr = Some("1234567890"),
            verificationDate = None,
            createDate = Some(LocalDateTime.of(2020, 5, 11, 0, 0))
          )

        // Sub 2: VERIFIED=Y, verificationDate recent => reverifyRequired=false => Verified column =Yes,
        val subNoReverify =
          mkSub(
            id = 200L,
            verified = Some("Y"),
            tradingName = Some("Hammond House"),
            subcontractorType = Some("company"),
            utr = Some("2904743750"),
            verificationNumber = Some("V0001217702"),
            taxTreatment = Some("gross"),
            verificationDate = Some(LocalDateTime.of(2025, 10, 1, 0, 0)),
            createDate = Some(LocalDateTime.of(2025, 10, 1, 0, 0))
          )

        val ua =
          emptyUserAnswers
            .set(NewestVerificationBatchResponsePage, newestBatchResponse(Seq(subNeedsReverify, subNoReverify)))
            .success
            .value

        val app =
          applicationBuilder(userAnswers = Some(ua))
            .overrides(
              bind[Clock].toInstance(fixedClock),
              bind[SessionRepository].toInstance(mockRepo)
            )
            .build()

        running(app) {
          val result = route(app, FakeRequest(GET, url(1))).value

          status(result) mustBe OK

          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockRepo).set(uaCaptor.capture())

          uaCaptor.getValue.get(SubcontractorReverifyRowsPage) mustBe defined
          uaCaptor.getValue.get(SubcontractorReverifyRowsPage).value.map(_.id) must contain allOf ("100", "200")

          val body = contentAsString(result)
          body must include("Which subcontractors do you want to reverify?")

          body must include("Brody, Martin")
          body must include("Hammond House")
          body must include("1234567890")
          body must include("2904743750")

          body must include(">No<")
          body must include(">Yes<")

          body must include("V0001217702")
          body must include("Unknown")

          body must include("Gross")

          body must include("11 May 2020")
          body must include("1 Oct 2025")
        }
      }

      "must return OK and save empty SubcontractorReverifyRowsPage when there are no VERIFIED=Y subcontractors" in {
        val mockRepo = mock[SessionRepository]
        when(mockRepo.set(any())) thenReturn Future.successful(true)

        val ua =
          emptyUserAnswers
            .set(
              NewestVerificationBatchResponsePage,
              newestBatchResponse(
                Seq(
                  mkSub(id = 1L, verified = Some("N"), tradingName = Some("Not verified"))
                )
              )
            )
            .success
            .value

        val app =
          applicationBuilder(userAnswers = Some(ua))
            .overrides(
              bind[Clock].toInstance(fixedClock),
              bind[SessionRepository].toInstance(mockRepo)
            )
            .build()

        running(app) {
          val result = route(app, FakeRequest(GET, url(1))).value

          status(result) mustBe OK

          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockRepo).set(uaCaptor.capture())

          uaCaptor.getValue.get(SubcontractorReverifyRowsPage) mustBe Some(Seq.empty)

          contentAsString(result) must include("Which subcontractors do you want to reverify?")
        }
      }
    }

    "onSubmit" - {

      "must return BadRequest and not call repo when SubcontractorReverifyRowsPage is missing (getOrElse Seq.empty path) and selection is required" in {
        val mockRepo = mock[SessionRepository]
        when(mockRepo.set(any())) thenReturn Future.successful(true)

        val ua =
          emptyUserAnswers
            .set(UnverifiedSubcontractorsPage, Seq.empty)
            .success
            .value
            .set(SelectSubcontractorPage, Set.empty)
            .success
            .value

        val app =
          applicationBuilder(userAnswers = Some(ua))
            .overrides(
              bind[Clock].toInstance(fixedClock),
              bind[SessionRepository].toInstance(mockRepo)
            )
            .build()

        running(app) {
          val request =
            FakeRequest(POST, postUrl)
              .withFormUrlEncodedBody("value" -> "")

          val result = route(app, request).value

          status(result) mustBe BAD_REQUEST
          verify(mockRepo, never()).set(any())
        }
      }

      "must redirect to the next page when valid data is submitted (uses rows stored in SubcontractorReverifyRowsPage)" in {
        val mockRepo = mock[SessionRepository]
        when(mockRepo.set(any())) thenReturn Future.successful(true)

        val rows: Seq[SubcontractorReverifyRow] =
          Seq(
            SubcontractorReverifyRow(
              id = "100",
              name = "Brody, Martin",
              utr = "1234567890",
              verified = "Yes",
              verificationNumber = "Unknown",
              taxTreatment = "Unknown",
              dateAdded = "11 May 2020"
            )
          )

        val ua =
          emptyUserAnswers
            .set(SubcontractorReverifyRowsPage, rows)
            .success
            .value

        val app =
          applicationBuilder(userAnswers = Some(ua))
            .overrides(
              bind[SessionRepository].toInstance(mockRepo),
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[Clock].toInstance(fixedClock)
            )
            .build()

        running(app) {
          val request =
            FakeRequest(POST, postUrl)
              .withFormUrlEncodedBody("value[0]" -> "100")

          val result = route(app, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe onwardRoute.url

          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockRepo).set(uaCaptor.capture())
          uaCaptor.getValue.get(SelectSubcontractorsToReverifyPage).value must contain(
            SelectedSubcontractors("100", "Brody, Martin")
          )
        }
      }

      "must redirect to target page when gotoPage is present and persist selections" in {
        val mockRepo = mock[SessionRepository]
        when(mockRepo.set(any())) thenReturn Future.successful(true)

        val rows: Seq[SubcontractorReverifyRow] =
          Seq(
            SubcontractorReverifyRow(
              id = "100",
              name = "Brody, Martin",
              utr = "1234567890",
              verified = "Yes",
              verificationNumber = "Unknown",
              taxTreatment = "Unknown",
              dateAdded = "11 May 2020"
            )
          )

        val ua =
          emptyUserAnswers
            .set(SubcontractorReverifyRowsPage, rows)
            .success
            .value

        val app =
          applicationBuilder(userAnswers = Some(ua))
            .overrides(
              bind[SessionRepository].toInstance(mockRepo),
              bind[Clock].toInstance(fixedClock)
            )
            .build()

        running(app) {
          val request =
            FakeRequest(POST, url(1))
              .withFormUrlEncodedBody(
                "value[0]" -> "100",
                "gotoPage" -> "2"
              )

          val result = route(app, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe url(2)

          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockRepo).set(uaCaptor.capture())
          uaCaptor.getValue.get(SelectSubcontractorsToReverifyPage).value must contain(
            SelectedSubcontractors("100", "Brody, Martin")
          )
        }
      }

      "must redirect to JourneyRecovery when no existing data is found (requireData fails)" in {
        val mockRepo = mock[SessionRepository]
        val app      =
          applicationBuilder(userAnswers = None)
            .overrides(bind[SessionRepository].toInstance(mockRepo))
            .build()

        running(app) {
          val request =
            FakeRequest(POST, url(1))
              .withFormUrlEncodedBody("value[0]" -> "100")

          val result = route(app, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url

          verify(mockRepo, never()).set(any())
        }
      }
    }

    "must show name as No name provided when subcontractorType is missing" in {
      val mockRepo = mock[SessionRepository]
      when(mockRepo.set(any())) thenReturn Future.successful(true)

      val sub =
        mkSub(
          id = 300L,
          verified = Some("Y"),
          firstName = Some("Jane"),
          surname = Some("Doe"),
          tradingName = Some("Doe Trading"),
          subcontractorType = None,
          utr = Some("9999999999"),
          verificationDate = None,
          createDate = Some(LocalDateTime.of(2024, 1, 1, 0, 0))
        )

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestBatchResponse(Seq(sub)))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[Clock].toInstance(fixedClock),
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, url(1))).value

        status(result) mustBe OK
        val body = contentAsString(result)

        body must include("no name provided")
        body must include("9999999999")
      }
    }

    "must show name as surname only when a Individualorsoletrader row whose surname is provided, firstName is blank" in {
      val mockRepo = mock[SessionRepository]
      when(mockRepo.set(any())) thenReturn Future.successful(true)

      val sub =
        mkSub(
          id = 300L,
          verified = Some("Y"),
          firstName = Some(" "),
          surname = Some("Doe"),
          tradingName = Some("Doe Trading"),
          subcontractorType = Some("soletrader"),
          utr = Some("9999999999"),
          verificationDate = None,
          createDate = Some(LocalDateTime.of(2024, 1, 1, 0, 0))
        )

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestBatchResponse(Seq(sub)))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[Clock].toInstance(fixedClock),
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, url(1))).value

        status(result) mustBe OK
        val body = contentAsString(result)

        body must include("Doe")
        body must not include "Doe Trading" // confirm surname wins over tradingName
        body must include("9999999999")
      }
    }

    "must show name as tradingName when a Individualorsoletrader row whose tradingName is provided, firstName and surname are blank" in {
      val mockRepo = mock[SessionRepository]
      when(mockRepo.set(any())) thenReturn Future.successful(true)

      val sub =
        mkSub(
          id = 300L,
          verified = Some("Y"),
          firstName = Some(" "),
          surname = Some(" "),
          tradingName = Some("Doe Trading"),
          subcontractorType = Some("soletrader"),
          utr = Some("9999999999"),
          verificationDate = None,
          createDate = Some(LocalDateTime.of(2024, 1, 1, 0, 0))
        )

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestBatchResponse(Seq(sub)))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[Clock].toInstance(fixedClock),
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, url(1))).value

        status(result) mustBe OK
        val body = contentAsString(result)

        body must include("Doe Trading")
        body must include("9999999999")
      }
    }

    "must show name as tradingName when a Individualorsoletrader row whose tradingName and firstNare are provided, surname are blank" in {
      val mockRepo = mock[SessionRepository]
      when(mockRepo.set(any())) thenReturn Future.successful(true)

      val sub =
        mkSub(
          id = 300L,
          verified = Some("Y"),
          firstName = Some("John"),
          surname = Some(" "),
          tradingName = Some("Doe Trading"),
          subcontractorType = Some("soletrader"),
          utr = Some("9999999999"),
          verificationDate = None,
          createDate = Some(LocalDateTime.of(2024, 1, 1, 0, 0))
        )

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestBatchResponse(Seq(sub)))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[Clock].toInstance(fixedClock),
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, url(1))).value

        status(result) mustBe OK
        val body = contentAsString(result)

        body must include("Doe Trading")
        body must include("9999999999")
      }
    }

    "must show name as partnershipTradingName when a Partnership row whose partnershipTradingName is provided" in {
      val mockRepo = mock[SessionRepository]
      when(mockRepo.set(any())) thenReturn Future.successful(true)

      val sub =
        mkSub(
          id = 300L,
          verified = Some("Y"),
          partnershipTradingName = Some("ABC Partnership"),
          tradingName = Some("Doe Trading"),
          subcontractorType = Some("partnership"),
          utr = Some("9999999999"),
          verificationDate = None,
          createDate = Some(LocalDateTime.of(2024, 1, 1, 0, 0))
        )

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestBatchResponse(Seq(sub)))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[Clock].toInstance(fixedClock),
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, url(1))).value

        status(result) mustBe OK
        val body = contentAsString(result)

        body must include("ABC Partnership")
        body must include("9999999999")
      }
    }

    "must show name as tradingName when a Partnership row whose partnershipTradingName is blank, tradingName is provided" in {
      val mockRepo = mock[SessionRepository]
      when(mockRepo.set(any())) thenReturn Future.successful(true)

      val sub =
        mkSub(
          id = 300L,
          verified = Some("Y"),
          partnershipTradingName = Some("   "),
          tradingName = Some("Doe Trading"),
          subcontractorType = Some("partnership"),
          utr = Some("9999999999"),
          verificationDate = None,
          createDate = Some(LocalDateTime.of(2024, 1, 1, 0, 0))
        )

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestBatchResponse(Seq(sub)))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[Clock].toInstance(fixedClock),
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, url(1))).value

        status(result) mustBe OK
        val body = contentAsString(result)

        body must include("Doe Trading")
        body must include("9999999999")
      }
    }

    "must show empty utr when utr is missing" in {
      val mockRepo = mock[SessionRepository]
      when(mockRepo.set(any())) thenReturn Future.successful(true)

      val sub =
        mkSub(
          id = 400L,
          verified = Some("Y"),
          tradingName = Some("No UTR Ltd"),
          subcontractorType = Some("company"),
          utr = None,
          verificationDate = None,
          createDate = Some(LocalDateTime.of(2024, 1, 1, 0, 0))
        )

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestBatchResponse(Seq(sub)))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[Clock].toInstance(fixedClock),
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, url(1))).value
        status(result) mustBe OK

        val body = contentAsString(result)
        body must include("No UTR Ltd")
        body must not include "No name provided"
      }
    }

    "must render the table rows sorted alphabetically by name" in {
      val mockRepo = mock[SessionRepository]
      when(mockRepo.set(any())) thenReturn Future.successful(true)

      val zulu =
        mkSub(
          id = 300L,
          verified = Some("Y"),
          firstName = Some("Zoe"),
          surname = Some("Zulu"),
          subcontractorType = Some("individual"),
          utr = Some("1111111111"),
          verificationDate = None,
          createDate = Some(LocalDateTime.of(2024, 1, 1, 0, 0))
        )

      val alpha =
        mkSub(
          id = 100L,
          verified = Some("Y"),
          firstName = Some("Amy"),
          surname = Some("Alpha"),
          subcontractorType = Some("individual"),
          utr = Some("2222222222"),
          verificationDate = None,
          createDate = Some(LocalDateTime.of(2024, 1, 1, 0, 0))
        )

      val middle =
        mkSub(
          id = 200L,
          verified = Some("Y"),
          firstName = Some("mike"),
          surname = Some("Middle"),
          subcontractorType = Some("individual"),
          utr = Some("3333333333"),
          verificationDate = None,
          createDate = Some(LocalDateTime.of(2024, 1, 1, 0, 0))
        )

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestBatchResponse(Seq(zulu, alpha, middle)))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[Clock].toInstance(fixedClock),
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, url(1))).value

        status(result) mustBe OK

        val body = contentAsString(result)

        val alphaName  = "Alpha, Amy"
        val middleName = "Middle, mike"
        val zuluName   = "Zulu, Zoe"

        body.indexOf(alphaName)  must be < body.indexOf(middleName)
        body.indexOf(middleName) must be < body.indexOf(zuluName)
      }
    }

    "must map taxTreatment to the correct display value when reverification is NOT required" in {
      val mockRepo = mock[SessionRepository]
      when(mockRepo.set(any())) thenReturn Future.successful(true)

      def verifiedNoReverify(id: Long, tradingName: String, tax: Option[String]) =
        mkSub(
          id = id,
          verified = Some("Y"),
          tradingName = Some(tradingName),
          subcontractorType = Some("company"),
          utr = Some(s"$id$id$id$id$id$id$id$id$id$id"),
          verificationNumber = Some(s"V$id"),
          taxTreatment = tax,
          verificationDate = Some(LocalDateTime.of(2025, 10, 1, 0, 0)),
          createDate = Some(LocalDateTime.of(2025, 10, 1, 0, 0))
        )

      val ua =
        emptyUserAnswers
          .set(
            NewestVerificationBatchResponsePage,
            newestBatchResponse(
              Seq(
                verifiedNoReverify(1L, "Net Ltd", Some("net")),
                verifiedNoReverify(2L, "Unmatched Ltd", Some("unmatched")),
                verifiedNoReverify(3L, "Gross Ltd", Some("gross")),
                verifiedNoReverify(4L, "Unknown Ltd", Some("something-else"))
              )
            )
          )
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[Clock].toInstance(fixedClock),
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, url(1))).value
        status(result) mustBe OK

        val body = contentAsString(result)

        body must include("Standard rate")
        body must include("Higher rate")
        body must include("Gross")
        body must include("Unknown")
      }
    }

    "must use partnershipTradingName (or tradingName fallback) when subcontractorType is partnership" in {
      val mockRepo = mock[SessionRepository]
      when(mockRepo.set(any())) thenReturn Future.successful(true)

      val partnershipWithPTN =
        mkSub(
          id = 500L,
          verified = Some("Y"),
          tradingName = Some("Trading Fallback"),
          partnershipTradingName = Some("Partnership Trading Name"),
          subcontractorType = Some("partnership"),
          utr = Some("5555555555"),
          verificationDate = None,
          createDate = Some(LocalDateTime.of(2024, 1, 1, 0, 0))
        )

      val partnershipWithoutPTN =
        mkSub(
          id = 600L,
          verified = Some("Y"),
          tradingName = Some("Trading Only"),
          partnershipTradingName = None,
          subcontractorType = Some("partnership"),
          utr = Some("6666666666"),
          verificationDate = None,
          createDate = Some(LocalDateTime.of(2024, 1, 1, 0, 0))
        )

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestBatchResponse(Seq(partnershipWithPTN, partnershipWithoutPTN)))
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[Clock].toInstance(fixedClock),
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, url(1))).value
        status(result) mustBe OK

        val body = contentAsString(result)

        body must include("Partnership Trading Name")
        body must include("Trading Only")
      }
    }

    "must pre-populate the form (checked boxes) from SelectSubcontractorsToReverifyPage using ids" in {
      val mockRepo = mock[SessionRepository]
      when(mockRepo.set(any())) thenReturn Future.successful(true)

      val a =
        mkSub(
          id = 700L,
          verified = Some("Y"),
          tradingName = Some("A Ltd"),
          subcontractorType = Some("company"),
          utr = Some("7007007007"),
          verificationDate = None,
          createDate = Some(LocalDateTime.of(2024, 1, 1, 0, 0))
        )

      val b =
        mkSub(
          id = 800L,
          verified = Some("Y"),
          tradingName = Some("B Ltd"),
          subcontractorType = Some("company"),
          utr = Some("8008008008"),
          verificationDate = None,
          createDate = Some(LocalDateTime.of(2024, 1, 1, 0, 0))
        )

      val ua =
        emptyUserAnswers
          .set(NewestVerificationBatchResponsePage, newestBatchResponse(Seq(a, b)))
          .success
          .value
          .set(
            SelectSubcontractorsToReverifyPage,
            Set(
              SelectedSubcontractors("700", "A Ltd")
            )
          )
          .success
          .value

      val app =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[Clock].toInstance(fixedClock),
            bind[SessionRepository].toInstance(mockRepo)
          )
          .build()

      running(app) {
        val result = route(app, FakeRequest(GET, url(1))).value
        status(result) mustBe OK

        val body = contentAsString(result)

        val doc = org.jsoup.Jsoup.parse(body)

        val input700 =
          doc.selectFirst("input[value=700]")

        input700 must not be null
        input700.hasAttr("checked") mustBe true
      }
    }
  }
}

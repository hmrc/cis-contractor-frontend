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

package controllers.amend

import base.SpecBase
import controllers.routes
import models.add.SubcontractorName
import models.address.{Address, Country}
import models.amend.OriginalIndividualAnswers
import models.contact.ContactOptions.NoDetails
import org.jsoup.Jsoup
import pages.add.{SubcontractorNamePage, TradingNameOfSubcontractorPage}
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{CisIdQuery, OriginalIndividualAnswersQuery}

class IndividualAmendedControllerSpec extends SpecBase {

  private lazy val individualAmendedRoute =
    controllers.amend.routes.IndividualAmendedController.onPageLoad().url

  private val subcontractorName =
    SubcontractorName("Martin", None, "Brody")

  private val address =
    Address(
      addressLine1 = "12 Harbor View Road",
      addressLine2 = Some("Amity Island"),
      addressLine3 = Some("Bodmin"),
      addressLine4 = Some("Cornwall"),
      postcode = Some("PL31 2HL"),
      country = Some(Country(code = None, name = Some("England")))
    )

  private val original =
    OriginalIndividualAnswers(
      usesTradingName = Some(false),
      tradingName = None,
      subcontractorName = Some(subcontractorName),
      address = Some(address),
      contactMethod = Some(NoDetails),
      contactValue = None,
      utr = Some("3992651526"),
      nino = Some("QQ123456C"),
      worksReference = Some("XLS345-MM")
    )

  "IndividualAmendedController" - {

    "onPageLoad" - {

      "must return OK when all required answers exist" in {

        val answers =
          emptyUserAnswers
            .set(OriginalIndividualAnswersQuery, original).success.value
            .set(CisIdQuery, "1").success.value
            .set(SubcontractorNamePage, subcontractorName).success.value

        val application =
          applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)
          val request = FakeRequest(GET, individualAmendedRoute)
          val result = route(application, request).value

          status(result) mustEqual OK
          val doc = Jsoup.parse(contentAsString(result))

          doc.title() must include(msgs("individualAmended.panel.heading"))
          val rows = doc.select("tbody tr")
          rows.size() mustBe 3

          val subRow = rows.get(0)
          subRow.select("td").get(0).text() mustBe "Brody, Martin"
        }
      }

      "must redirect to JourneyRecovery when OriginalIndividualAnswersQuery is missing" in {

        val answers =
          emptyUserAnswers
            .set(CisIdQuery, "1").success.value

        val application =
          applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {

          val request = FakeRequest(GET, individualAmendedRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery when CisIdQuery is missing" in {

        val answers =
          emptyUserAnswers
            .set(OriginalIndividualAnswersQuery, original).success.value

        val application =
          applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {

          val request = FakeRequest(GET, individualAmendedRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must display the subcontractor name" in {

        val answers =
          emptyUserAnswers
            .set(OriginalIndividualAnswersQuery, original).success.value
            .set(CisIdQuery, "1").success.value
            .set(SubcontractorNamePage, subcontractorName).success.value

        val application =
          applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {

          val request = FakeRequest(GET, individualAmendedRoute)
          val result = route(application, request).value

          val doc = Jsoup.parse(contentAsString(result))

          doc.text() must include("Brody, Martin")
        }
      }

      "must display the trading name when a subcontractor name is not present" in {

        val answers =
          emptyUserAnswers
            .set(OriginalIndividualAnswersQuery, original).success.value
            .set(CisIdQuery, "1").success.value
            .set(TradingNameOfSubcontractorPage, "ABC Roofing").success.value

        val application =
          applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {

          val request = FakeRequest(GET, individualAmendedRoute)
          val result = route(application, request).value

          val doc = Jsoup.parse(contentAsString(result))

          doc.text() must include("ABC Roofing")
        }
      }
    }
  }
}

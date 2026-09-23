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

package models.finalvalidation

import models.finalvalidation.FinalValidationReadiness.{Complete, Incomplete}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsString, Json}

class FinalValidationDraftSpec extends AnyFreeSpec with Matchers {

  "FinalValidationDraftIssue" - {

    "must round trip through JSON" in {

      val issue =
        FinalValidationDraftIssue(
          fieldKey = "utr",
          value = Some("1234567890")
        )

      Json.toJson(issue).as[FinalValidationDraftIssue] mustBe issue
    }
  }

  "FinalValidationSubcontractorDetails" - {

    "must round trip through JSON" in {

      val details =
        FinalValidationSubcontractorDetails(
          firstName = Some("John"),
          secondName = Some("Paul"),
          surname = Some("Smith"),
          partnershipTradingName = Some("Partnership Trading Name"),
          tradingName = Some("Trading Name"),
          addressLine1 = Some("1 Test Street"),
          addressLine2 = Some("Test Area"),
          addressLine3 = Some("Test Town"),
          addressLine4 = Some("Test County"),
          country = Some("GB"),
          postcode = Some("AA1 1AA"),
          emailAddress = Some("john@example.com"),
          phoneNumber = Some("01234567890"),
          mobilePhoneNumber = Some("07123456789"),
          utr = Some("1234567890"),
          partnerUtr = Some("0987654321"),
          nino = Some("AB123456C"),
          crn = Some("12345678"),
          worksReferenceNumber = Some("WRN123")
        )

      Json.toJson(details).as[FinalValidationSubcontractorDetails] mustBe details
    }

    "must round trip when all values are empty" in {

      val details = FinalValidationSubcontractorDetails()

      Json.toJson(details).as[FinalValidationSubcontractorDetails] mustBe details
    }
  }

  "FinalValidationReadiness" - {

    "must write Incomplete to JSON" in {

      Json.toJson[FinalValidationReadiness](Incomplete) mustBe JsString("Incomplete")
    }

    "must write Complete to JSON" in {

      Json.toJson[FinalValidationReadiness](Complete) mustBe JsString("Complete")
    }

    "must read Incomplete from JSON" in {

      Json
        .fromJson[FinalValidationReadiness](JsString("Incomplete"))
        .get mustBe Incomplete
    }

    "must read Complete from JSON" in {

      Json
        .fromJson[FinalValidationReadiness](JsString("Complete"))
        .get mustBe Complete
    }

    "must fail to read an unknown value" in {

      Json
        .fromJson[FinalValidationReadiness](JsString("Unknown")) mustBe a[JsError]
    }
  }

  "FinalValidationDraftSubcontractor" - {

    "must round trip through JSON" in {

      val subcontractor =
        FinalValidationDraftSubcontractor(
          subcontractorId = 1L,
          subbieResourceRef = 100L,
          baseVersion = Some(1),
          subcontractorType = Some("SoleTrader"),
          displayName = "John Smith",
          base = FinalValidationSubcontractorDetails(
            firstName = Some("John"),
            surname = Some("Smith")
          ),
          proposed = FinalValidationSubcontractorDetails(
            firstName = Some("Jonathan"),
            surname = Some("Smith")
          ),
          changedTargets = Set("subcontractorName"),
          issues = Seq(
            FinalValidationDraftIssue(
              fieldKey = "firstName",
              value = Some("Jonathan")
            )
          ),
          readiness = Incomplete
        )

      Json.toJson(subcontractor).as[FinalValidationDraftSubcontractor] mustBe subcontractor
    }
  }

  "FinalValidationDraft" - {

    "must return a subcontractor matching the supplied id" in {

      val subcontractor =
        FinalValidationDraftSubcontractor(
          subcontractorId = 1L,
          subbieResourceRef = 100L,
          baseVersion = Some(1),
          subcontractorType = Some("SoleTrader"),
          displayName = "John Smith",
          base = FinalValidationSubcontractorDetails(),
          proposed = FinalValidationSubcontractorDetails(),
          changedTargets = Set.empty,
          issues = Seq.empty,
          readiness = Incomplete
        )

      val draft =
        FinalValidationDraft(
          subcontractors = Seq(subcontractor)
        )

      draft.subcontractor(1L) mustBe Some(subcontractor)
    }

    "must return None when the subcontractor does not exist" in {

      val draft =
        FinalValidationDraft(
          subcontractors = Seq.empty
        )

      draft.subcontractor(1L) mustBe None
    }

    "must return true from allComplete when all subcontractors are complete" in {

      val subcontractor1 =
        FinalValidationDraftSubcontractor(
          subcontractorId = 1L,
          subbieResourceRef = 100L,
          baseVersion = Some(1),
          subcontractorType = Some("SoleTrader"),
          displayName = "John Smith",
          base = FinalValidationSubcontractorDetails(),
          proposed = FinalValidationSubcontractorDetails(),
          changedTargets = Set.empty,
          issues = Seq.empty,
          readiness = Complete
        )

      val subcontractor2 =
        FinalValidationDraftSubcontractor(
          subcontractorId = 2L,
          subbieResourceRef = 200L,
          baseVersion = Some(1),
          subcontractorType = Some("Company"),
          displayName = "Test Company",
          base = FinalValidationSubcontractorDetails(),
          proposed = FinalValidationSubcontractorDetails(),
          changedTargets = Set.empty,
          issues = Seq.empty,
          readiness = Complete
        )

      FinalValidationDraft(
        subcontractors = Seq(subcontractor1, subcontractor2)
      ).allComplete mustBe true
    }

    "must return false from allComplete when a subcontractor is incomplete" in {

      val subcontractor1 =
        FinalValidationDraftSubcontractor(
          subcontractorId = 1L,
          subbieResourceRef = 100L,
          baseVersion = Some(1),
          subcontractorType = Some("SoleTrader"),
          displayName = "John Smith",
          base = FinalValidationSubcontractorDetails(),
          proposed = FinalValidationSubcontractorDetails(),
          changedTargets = Set.empty,
          issues = Seq.empty,
          readiness = Complete
        )

      val subcontractor2 =
        FinalValidationDraftSubcontractor(
          subcontractorId = 2L,
          subbieResourceRef = 200L,
          baseVersion = Some(1),
          subcontractorType = Some("Company"),
          displayName = "Test Company",
          base = FinalValidationSubcontractorDetails(),
          proposed = FinalValidationSubcontractorDetails(),
          changedTargets = Set.empty,
          issues = Seq.empty,
          readiness = Incomplete
        )

      FinalValidationDraft(
        subcontractors = Seq(subcontractor1, subcontractor2)
      ).allComplete mustBe false
    }

    "must round trip through JSON" in {

      val draft =
        FinalValidationDraft(
          subcontractors = Seq(
            FinalValidationDraftSubcontractor(
              subcontractorId = 1L,
              subbieResourceRef = 100L,
              baseVersion = Some(1),
              subcontractorType = Some("SoleTrader"),
              displayName = "John Smith",
              base = FinalValidationSubcontractorDetails(),
              proposed = FinalValidationSubcontractorDetails(
                tradingName = Some("Smith Trading")
              ),
              changedTargets = Set("tradingName"),
              issues = Seq(
                FinalValidationDraftIssue(
                  fieldKey = "tradingName",
                  value = Some("Smith Trading")
                )
              ),
              readiness = Incomplete
            )
          )
        )

      Json.toJson(draft).as[FinalValidationDraft] mustBe draft
    }
  }

  "CreateFinalValidationDraftSubcontractor" - {

    "must round trip through JSON" in {

      val subcontractor =
        CreateFinalValidationDraftSubcontractor(
          subcontractorId = 1L,
          subbieResourceRef = 100L,
          baseVersion = Some(1),
          subcontractorType = Some("SoleTrader"),
          displayName = "John Smith",
          details = FinalValidationSubcontractorDetails(
            firstName = Some("John"),
            surname = Some("Smith")
          ),
          issues = Seq(
            FinalValidationDraftIssue(
              fieldKey = "utr",
              value = Some("1234567890")
            )
          )
        )

      Json.toJson(subcontractor).as[CreateFinalValidationDraftSubcontractor] mustBe subcontractor
    }
  }

  "CreateFinalValidationDraftRequest" - {

    "must round trip through JSON" in {

      val request =
        CreateFinalValidationDraftRequest(
          instanceId = "instance-id",
          context = "final-validation",
          subcontractors = Seq(
            CreateFinalValidationDraftSubcontractor(
              subcontractorId = 1L,
              subbieResourceRef = 100L,
              baseVersion = Some(1),
              subcontractorType = Some("SoleTrader"),
              displayName = "John Smith",
              details = FinalValidationSubcontractorDetails(),
              issues = Seq.empty
            )
          )
        )

      Json.toJson(request).as[CreateFinalValidationDraftRequest] mustBe request
    }
  }

  "CreateFinalValidationDraftResponse" - {

    "must round trip through JSON" in {

      val response =
        CreateFinalValidationDraftResponse(
          draftId = "draft-id"
        )

      Json.toJson(response).as[CreateFinalValidationDraftResponse] mustBe response
    }
  }

  "UpdateFinalValidationReadinessRequest" - {

    "must round trip through JSON" in {

      val request =
        UpdateFinalValidationReadinessRequest(
          subcontractorId = 1L,
          issues = Seq(
            FinalValidationDraftIssue(
              fieldKey = "utr",
              value = Some("1234567890")
            )
          )
        )

      Json.toJson(request).as[UpdateFinalValidationReadinessRequest] mustBe request
    }
  }
}

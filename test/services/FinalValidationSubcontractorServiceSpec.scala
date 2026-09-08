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
import models.UserAnswers
import models.TypeOfSubcontractor.Individualorsoletrader
import models.finalvalidation.*
import pages.add.*
import pages.finalvalidation.*
import play.api.libs.json.{JsObject, Json}
import queries.CisIdQuery

class FinalValidationSubcontractorServiceSpec extends SpecBase {

  private val service = new FinalValidationSubcontractorService()

  private def subcontractor(
    proposed: JsObject,
    base: JsObject = Json.obj(),
    subcontractorType: Option[String] = Some("soletrader")
  ): FinalValidationDraftSubcontractor =
    Json
      .obj(
        "subcontractorId"   -> 1L,
        "subbieResourceRef" -> 2L,
        "baseVersion"       -> 1,
        "subcontractorType" -> subcontractorType,
        "displayName"       -> "Test Subcontractor",
        "base"              -> base,
        "proposed"          -> proposed,
        "changedTargets"    -> Json.arr(),
        "issues"            -> Json.arr(),
        "readiness"         -> "Incomplete"
      )
      .as[FinalValidationDraftSubcontractor]

  "populateFinalValidationUserAnswers" - {

    "must populate the common answers, name, base UTR and UTR target" in {

      val draftSubcontractor =
        subcontractor(
          base = Json.obj(
            "utr" -> "1111111111"
          ),
          proposed = Json.obj(
            "firstName"  -> "John",
            "secondName" -> "Paul",
            "surname"    -> "Smith",
            "utr"        -> "2222222222"
          )
        )

      val result =
        service
          .populateFinalValidationUserAnswers(
            userAnswers = UserAnswers("id"),
            instanceId = "CIS-123",
            subcontractor = draftSubcontractor,
            changeTarget = FinalValidationChangeTarget.Utr
          )
          .success
          .value

      result.get(CisIdQuery).value mustBe "CIS-123"

      result
        .get(TypeOfSubcontractorPage)
        .value mustBe Individualorsoletrader

      val name =
        result
          .get(SubcontractorNamePage)
          .value

      name.firstName mustBe "John"
      name.middleName mustBe Some("Paul")
      name.lastName mustBe "Smith"

      result
        .get(FinalValidationChangeTargetPage)
        .value mustBe FinalValidationChangeTarget.Utr

      result
        .get(FinalValidationBaseUtrPage)
        .value mustBe "1111111111"

      result
        .get(UniqueTaxpayerReferenceYesNoPage)
        .value mustBe true

      result
        .get(SubcontractorsUniqueTaxpayerReferencePage)
        .value mustBe "2222222222"
    }

    "must populate the trading name target" in {

      val draftSubcontractor =
        subcontractor(
          proposed = Json.obj(
            "firstName"   -> "John",
            "surname"     -> "Smith",
            "tradingName" -> "Smith Construction"
          )
        )

      val result =
        service
          .populateFinalValidationUserAnswers(
            userAnswers = UserAnswers("id"),
            instanceId = "CIS-123",
            subcontractor = draftSubcontractor,
            changeTarget = FinalValidationChangeTarget.TradingName
          )
          .success
          .value

      result
        .get(TradingNameOfSubcontractorPage)
        .value mustBe "Smith Construction"
    }

    "must remove the base UTR when the draft subcontractor has no base UTR" in {

      val userAnswers =
        UserAnswers("id")
          .set(
            FinalValidationBaseUtrPage,
            "1111111111"
          )
          .success
          .value

      val draftSubcontractor =
        subcontractor(
          proposed = Json.obj(
            "firstName"   -> "John",
            "surname"     -> "Smith",
            "tradingName" -> "Smith Construction"
          )
        )

      val result =
        service
          .populateFinalValidationUserAnswers(
            userAnswers = userAnswers,
            instanceId = "CIS-123",
            subcontractor = draftSubcontractor,
            changeTarget = FinalValidationChangeTarget.TradingName
          )
          .success
          .value

      result.get(FinalValidationBaseUtrPage) mustBe None
    }

    "must populate UTR YesNo as false and remove the UTR when there is no proposed UTR" in {

      val userAnswers =
        UserAnswers("id")
          .set(
            SubcontractorsUniqueTaxpayerReferencePage,
            "1111111111"
          )
          .success
          .value

      val draftSubcontractor =
        subcontractor(
          proposed = Json.obj(
            "firstName" -> "John",
            "surname"   -> "Smith"
          )
        )

      val result =
        service
          .populateFinalValidationUserAnswers(
            userAnswers = userAnswers,
            instanceId = "CIS-123",
            subcontractor = draftSubcontractor,
            changeTarget = FinalValidationChangeTarget.UtrYesNo
          )
          .success
          .value

      result
        .get(UniqueTaxpayerReferenceYesNoPage)
        .value mustBe false

      result.get(SubcontractorsUniqueTaxpayerReferencePage) mustBe None
    }

    "must populate the address target" in {

      val draftSubcontractor =
        subcontractor(
          proposed = Json.obj(
            "firstName"    -> "John",
            "surname"      -> "Smith",
            "addressLine1" -> " 1 High Street ",
            "addressLine2" -> "   ",
            "addressLine3" -> "London",
            "postcode"     -> " SW1A 1AA ",
            "country"      -> " United Kingdom "
          )
        )

      val result =
        service
          .populateFinalValidationUserAnswers(
            userAnswers = UserAnswers("id"),
            instanceId = "CIS-123",
            subcontractor = draftSubcontractor,
            changeTarget = FinalValidationChangeTarget.Address
          )
          .success
          .value

      result
        .get(SubAddressYesNoPage)
        .value mustBe true

      val address =
        result
          .get(AddressOfSubcontractorPage)
          .value

      address.addressLine1 mustBe "1 High Street"
      address.addressLine2 mustBe None
      address.addressLine3 mustBe Some("London")
      address.postcode mustBe Some("SW1A 1AA")
      address.country.value.code mustBe None
      address.country.value.name mustBe Some("United Kingdom")
    }

    "must populate ContactDetailsYesNo as false and remove existing contact details when none are proposed" in {

      val userAnswers =
        UserAnswers("id")
          .set(
            IndividualEmailAddressPage,
            "old@test.com"
          )
          .success
          .value
          .set(
            IndividualPhoneNumberPage,
            "01234567890"
          )
          .success
          .value
          .set(
            IndividualMobileNumberPage,
            "07123456789"
          )
          .success
          .value

      val draftSubcontractor =
        subcontractor(
          proposed = Json.obj(
            "firstName"    -> "John",
            "surname"      -> "Smith",
            "emailAddress" -> "   "
          )
        )

      val result =
        service
          .populateFinalValidationUserAnswers(
            userAnswers = userAnswers,
            instanceId = "CIS-123",
            subcontractor = draftSubcontractor,
            changeTarget = FinalValidationChangeTarget.ContactDetailsYesNo
          )
          .success
          .value

      result
        .get(AddIndividualContactMethodsYesNoPage)
        .value mustBe false

      result.get(IndividualEmailAddressPage) mustBe None
      result.get(IndividualPhoneNumberPage) mustBe None
      result.get(IndividualMobileNumberPage) mustBe None
    }

    "must populate contact details and set the YesNo answer to true for an EmailAddress target" in {

      val draftSubcontractor =
        subcontractor(
          proposed = Json.obj(
            "firstName"    -> "John",
            "surname"      -> "Smith",
            "emailAddress" -> " test@example.com ",
            "phoneNumber"  -> " 01234567890 "
          )
        )

      val result =
        service
          .populateFinalValidationUserAnswers(
            userAnswers = UserAnswers("id"),
            instanceId = "CIS-123",
            subcontractor = draftSubcontractor,
            changeTarget = FinalValidationChangeTarget.EmailAddress
          )
          .success
          .value

      result
        .get(AddIndividualContactMethodsYesNoPage)
        .value mustBe true

      result
        .get(IndividualEmailAddressPage)
        .value mustBe "test@example.com"

      result
        .get(IndividualPhoneNumberPage)
        .value mustBe "01234567890"

      result.get(IndividualMobileNumberPage) mustBe None
    }

    "must fail when the subcontractor type is unsupported" in {

      val draftSubcontractor =
        subcontractor(
          proposed = Json.obj(),
          subcontractorType = Some("invalid")
        )

      val result =
        service.populateFinalValidationUserAnswers(
          userAnswers = UserAnswers("id"),
          instanceId = "CIS-123",
          subcontractor = draftSubcontractor,
          changeTarget = FinalValidationChangeTarget.Utr
        )

      result.failure.exception.getMessage mustBe
        "Unsupported subcontractor type: Some(invalid)"
    }

    "must fail when the subcontractor type is missing" in {

      val draftSubcontractor =
        subcontractor(
          proposed = Json.obj(),
          subcontractorType = None
        )

      val result =
        service.populateFinalValidationUserAnswers(
          userAnswers = UserAnswers("id"),
          instanceId = "CIS-123",
          subcontractor = draftSubcontractor,
          changeTarget = FinalValidationChangeTarget.Utr
        )

      result.failure.exception.getMessage mustBe
        "Unsupported subcontractor type: None"
    }
  }
}

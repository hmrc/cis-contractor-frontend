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

package utils

import base.SpecBase
import models.TypeOfSubcontractor.{Individualorsoletrader, Limitedcompany, Partnership, Trust}
import models.validation.{FieldValidationFailure, SubcontractorValidationField}

class TradingNameValidatorSpec extends SpecBase {

  Seq(
    ("Trust", Trust),
    ("Limited Company", Limitedcompany),
    ("Partnership", Partnership),
    ("Individual/Sole Trader", Individualorsoletrader)
  ).foreach { case (subcontractorType, typeOfSubcontractor) =>
    s"when the type of subcontractor is '$subcontractorType'" - {

      "TradingNameValidator - validate tradingName " - {

        "return failure when the trading name is empty" in {
          val tradingName = ""
          if (typeOfSubcontractor == Individualorsoletrader) {
            TradingNameValidator
              .validate(Some(""), SubcontractorValidationField.TradingName, typeOfSubcontractor) mustBe None
          } else {
            TradingNameValidator
              .validate(Some(""), SubcontractorValidationField.TradingName, typeOfSubcontractor) mustBe Some(
              FieldValidationFailure(
                field = SubcontractorValidationField.TradingName,
                value = Some(tradingName)
              )
            )
          }

        }

        "return failure when the trading name is None" in {
          val tradingName = None
          if (typeOfSubcontractor == Individualorsoletrader) {
            TradingNameValidator
              .validate(Some(""), SubcontractorValidationField.TradingName, typeOfSubcontractor) mustBe None
          } else {
            TradingNameValidator
              .validate(tradingName, SubcontractorValidationField.TradingName, typeOfSubcontractor) mustBe Some(
              FieldValidationFailure(
                field = SubcontractorValidationField.TradingName,
                value = None
              )
            )
          }
        }

        "return no failure for a valid trading name" in {
          TradingNameValidator
            .validate(
              Some("trading Name"),
              SubcontractorValidationField.TradingName,
              typeOfSubcontractor
            ) mustBe None
        }

        "return no failure for a valid trading name - Test Trading Name 1234@" in {
          TradingNameValidator
            .validate(
              Some("Test Trading Name 1234@"),
              SubcontractorValidationField.TradingName,
              typeOfSubcontractor
            ) mustBe None
        }

        "retain the original invalid trading name in the failure" in {
          val tradingName =
            "12345678901234567890123456789012345678901234567890<>"

          TradingNameValidator
            .validate(
              Some(tradingName),
              SubcontractorValidationField.TradingName,
              typeOfSubcontractor
            ) mustBe
            Some(
              FieldValidationFailure(
                field = SubcontractorValidationField.TradingName,
                value = Some(tradingName)
              )
            )
        }

      }
    }
  }

  "return a failure against the requested field" in {
    TradingNameValidator
      .validate(
        None,
        SubcontractorValidationField.PartnershipTradingName,
        Partnership
      ) mustBe Some(
      FieldValidationFailure(
        field = SubcontractorValidationField.PartnershipTradingName,
        value = None
      )
    )
  }
}

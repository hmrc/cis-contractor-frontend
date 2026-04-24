/*
 * Copyright 2025 HM Revenue & Customs
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

package generators

import models.*
import models.contact.ContactOptions
import models.add.{InternationalAddress, TypeOfSubcontractor}
import models.verify.ContractorEmailConfirmationStored
import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.Json

import java.time.{Instant, LocalDateTime, ZoneOffset}

trait ModelGenerators {

  implicit lazy val arbitraryInternationalAddress: Arbitrary[InternationalAddress] =
    Arbitrary {
      for {
        addressLine1 <- Gen.alphaStr.suchThat(_.nonEmpty)
        addressLine2 <- Gen.option(Gen.alphaStr)
        addressLine3 <- Gen.alphaStr.suchThat(_.nonEmpty)
        addressLine4 <- Gen.option(Gen.alphaStr)
        postalCode   <- Gen.alphaStr.suchThat(_.nonEmpty)
        country      <- Gen.alphaStr.suchThat(_.nonEmpty)
      } yield InternationalAddress(
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        addressLine3 = addressLine3,
        addressLine4 = addressLine4,
        postalCode = postalCode,
        country = country
      )
    }

  implicit lazy val arbitraryContactOptions: Arbitrary[ContactOptions] =
    Arbitrary {
      Gen.oneOf(ContactOptions.values)
    }

  implicit lazy val arbitrarySubcontractorTypes: Arbitrary[TypeOfSubcontractor] =
    Arbitrary {
      Gen.oneOf(TypeOfSubcontractor.values)
    }

  implicit lazy val arbitraryContractorEmailConfirmationStored: Arbitrary[ContractorEmailConfirmationStored] =
    Arbitrary {
      Gen.oneOf(ContractorEmailConfirmationStored.values)
    }

  implicit lazy val arbitraryUserAnswers: Arbitrary[UserAnswers] =
    Arbitrary {
      for {
        id <- Gen.uuid.map(_.toString)
      } yield UserAnswers(
        id = id,
        data = Json.obj(),
        lastUpdated = Instant.now()
      )
    }

  implicit lazy val arbitrarySubcontractor: Arbitrary[Subcontractor] =
    Arbitrary {
      for {
        subcontractorId        <- Gen.posNum[Long]
        firstName              <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
        secondName             <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
        surname                <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
        tradingName            <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
        partnershipTradingName <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
        verified               <- Gen.option(Gen.oneOf("Y", "N"))
        verificationNumber     <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
        taxTreatment           <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
        verificationDate       <- Gen.option(genLocalDateTime)
        lastMonthlyReturnDate  <- Gen.option(genLocalDateTime)
        createDate             <- Gen.option(genLocalDateTime)
      } yield Subcontractor(
        subcontractorId = subcontractorId,
        firstName = firstName,
        secondName = secondName,
        surname = surname,
        tradingName = tradingName,
        partnershipTradingName = partnershipTradingName,
        verified = verified,
        verificationNumber = verificationNumber,
        taxTreatment = taxTreatment,
        verificationDate = verificationDate,
        lastMonthlyReturnDate = lastMonthlyReturnDate,
        createDate = createDate
      )
    }
  private val genLocalDateTime: Gen[LocalDateTime]                   =
    Gen
      .choose(0L, System.currentTimeMillis())
      .map(ms => LocalDateTime.ofEpochSecond(ms / 1000, 0, ZoneOffset.UTC))
}

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

package generators

import models.*

import models.contact.ContactOptions
import models.address.{Address, Country}
import models.verify.ContractorEmailConfirmationStored
import models.verify.SelectedSubcontractors
import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.Json

import java.time.{Instant, LocalDateTime, ZoneOffset}

trait ModelGenerators {

//  implicit lazy val arbitraryContactMethodOptions: Arbitrary[ContactMethodOptions] =
//    Arbitrary {
//      Gen.oneOf(ContactMethodOptions.values)
//    }

  implicit lazy val arbitrarySelectSubcontractorsToReverify: Arbitrary[Set[SelectedSubcontractors]] =
    Arbitrary {
      Gen
        .listOf(
          for {
            id   <- Gen.alphaStr.suchThat(_.nonEmpty)
            name <- Gen.alphaStr.suchThat(_.nonEmpty)
          } yield SelectedSubcontractors(id, name)
        )
        .map(_.toSet)
    }

  private val genNonEmptyAlphaStr: Gen[String] =
    Gen.nonEmptyListOf(Gen.alphaChar).map(_.mkString)

  private val genNonEmptyAlphaNumStr: Gen[String] =
    Gen.nonEmptyListOf(Gen.alphaNumChar).map(_.mkString)

  implicit lazy val arbitrarySubcontractorViewModel: Arbitrary[SubcontractorViewModel] =
    Arbitrary {
      for {
        id   <- genNonEmptyAlphaStr
        name <- genNonEmptyAlphaStr
      } yield SubcontractorViewModel(id, name)
    }

  implicit lazy val arbitraryAddress: Arbitrary[Address] =
    Arbitrary {
      for {
        addressLine1 <- genNonEmptyAlphaStr
        addressLine2 <- Gen.option(Gen.alphaStr)
        addressLine3 <- Gen.option(Gen.alphaStr)
        addressLine4 <- Gen.option(Gen.alphaStr)
        addressLine5 <- Gen.option(Gen.alphaStr)
        postcode     <- Gen.option(genNonEmptyAlphaNumStr)
        countryName  <- genNonEmptyAlphaStr
      } yield Address(
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        addressLine3 = addressLine3,
        addressLine4 = addressLine4,
        addressLine5 = addressLine5,
        postcode = postcode,
        country = Some(Country(None, Some(countryName)))
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
        firstName              <- Gen.option(genNonEmptyAlphaStr)
        secondName             <- Gen.option(genNonEmptyAlphaStr)
        surname                <- Gen.option(genNonEmptyAlphaStr)
        tradingName            <- Gen.option(genNonEmptyAlphaStr)
        partnershipTradingName <- Gen.option(genNonEmptyAlphaStr)
        verified               <- Gen.option(Gen.oneOf("Y", "N"))
        verificationNumber     <- Gen.option(genNonEmptyAlphaNumStr)
        taxTreatment           <- Gen.option(genNonEmptyAlphaStr)
        verificationDate       <- Gen.option(genLocalDateTime)
        lastMonthlyReturnDate  <- Gen.option(genLocalDateTime)
        createDate             <- Gen.option(genLocalDateTime)
        subcontractorType      <- Gen.option(genNonEmptyAlphaStr)
        subbieResourceRef      <- Gen.option(Gen.posNum[Long])
        utr                    <- Gen.option(genNonEmptyAlphaStr)
        partnerUtr             <- Gen.option(genNonEmptyAlphaStr)
        crn                    <- Gen.option(genNonEmptyAlphaStr)
        nino                   <- Gen.option(genNonEmptyAlphaStr)
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
        createDate = createDate,
        subcontractorType = subcontractorType,
        subbieResourceRef = subbieResourceRef,
        utr = utr,
        partnerUtr = partnerUtr,
        crn = crn,
        nino = nino
      )
    }
  private val genLocalDateTime: Gen[LocalDateTime]                   =
    Gen
      .choose(0L, System.currentTimeMillis())
      .map(ms => LocalDateTime.ofEpochSecond(ms / 1000, 0, ZoneOffset.UTC))

  implicit lazy val arbitraryIndividualContactMethodOptions: Arbitrary[models.add.IndividualContactMethodOptions] =
    Arbitrary {
      Gen.oneOf(models.add.IndividualContactMethodOptions.values)
    }
}

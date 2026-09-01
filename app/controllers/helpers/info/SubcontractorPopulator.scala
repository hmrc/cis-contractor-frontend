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

package controllers.helpers.info

import controllers.amend.AmendControllerUtils.shouldShowVerificationDetails
import controllers.helpers.SubcontractorPopulatorUtils
import models.TypeOfSubcontractor.{Individualorsoletrader, Limitedcompany, Partnership, Trust}
import models.info.IndividualAnswers
import models.info.company.CompanyAnswers
import models.info.partnership.PartnershipAnswers
import models.info.trust.TrustAnswers
import models.response.SubcontractorResponse
import models.{TypeOfSubcontractor, UserAnswers}
import queries.info.{CompanyAnswersQuery, IndividualAnswersQuery, PartnershipAnswersQuery, TrustAnswersQuery}

import scala.util.Try

object SubcontractorPopulator {

  private object IndividualPopulator {

    def populate(
      userAnswers: UserAnswers,
      subcontractorType: TypeOfSubcontractor,
      subcontractor: SubcontractorResponse
    ): Try[UserAnswers] = {

      val address = SubcontractorPopulatorUtils.toAddress(subcontractor)

      val name =
        SubcontractorPopulatorUtils.individualName(subcontractor)

      val usesTradingName =
        SubcontractorPopulatorUtils.usesTradingName(subcontractor)

      val methods =
        SubcontractorPopulatorUtils.contactMethods(subcontractor)

      val answers =
        IndividualAnswers(
          subcontractorType = subcontractorType,
          showVerificationDetails = shouldShowVerificationDetails(subcontractor),
          usesTradingName = Some(usesTradingName),
          tradingName = subcontractor.tradingName,
          subcontractorName = name,
          addressYesNo = Some(address.isDefined),
          address = address,
          individualContactMethodsYesNo = Some(methods.nonEmpty),
          individualContactMethod = methods,
          email = subcontractor.emailAddress,
          phone = subcontractor.phoneNumber,
          mobile = subcontractor.mobilePhoneNumber,
          utrYesNo = Some(subcontractor.utr.isDefined),
          utr = subcontractor.utr,
          ninoYesNo = Some(subcontractor.nino.isDefined),
          nino = subcontractor.nino,
          worksReferenceYesNo = Some(subcontractor.worksReferenceNumber.isDefined),
          worksReference = subcontractor.worksReferenceNumber,
          verificationNumber = subcontractor.verificationNumber
        )

      userAnswers.set(
        IndividualAnswersQuery,
        answers
      )
    }
  }

  private object CompanyPopulator {

    def populate(
      userAnswers: UserAnswers,
      subcontractorType: TypeOfSubcontractor,
      subcontractor: SubcontractorResponse
    ): Try[UserAnswers] = {

      val address =
        SubcontractorPopulatorUtils.toAddress(subcontractor)

      val methods =
        SubcontractorPopulatorUtils.contactMethods(subcontractor)

      val answers =
        CompanyAnswers(
          subcontractorType = subcontractorType,
          showVerificationDetails = shouldShowVerificationDetails(subcontractor),
          companyName = subcontractor.tradingName,
          addressYesNo = Some(address.isDefined),
          address = address,
          companyContactMethodsYesNo = Some(methods.nonEmpty),
          companyContactMethod = methods,
          email = subcontractor.emailAddress,
          phone = subcontractor.phoneNumber,
          mobile = subcontractor.mobilePhoneNumber,
          crnYesNo = Some(subcontractor.crn.isDefined),
          crn = subcontractor.crn,
          utrYesNo = Some(subcontractor.utr.isDefined),
          utr = subcontractor.utr,
          worksReferenceYesNo = Some(subcontractor.worksReferenceNumber.isDefined),
          worksReference = subcontractor.worksReferenceNumber,
          verificationNumber = subcontractor.verificationNumber
        )

      userAnswers.set(
        CompanyAnswersQuery,
        answers
      )
    }
  }

  private object PartnershipPopulator {

    def populate(
      userAnswers: UserAnswers,
      subcontractorType: TypeOfSubcontractor,
      subcontractor: SubcontractorResponse
    ): Try[UserAnswers] = {

      val address =
        SubcontractorPopulatorUtils.toAddress(subcontractor)

      val methods =
        SubcontractorPopulatorUtils.contactMethods(subcontractor)

      val partnershipName =
        subcontractor.partnershipTradingName

      val nominatedPartnerName =
        subcontractor.tradingName

      val answers =
        PartnershipAnswers(
          subcontractorType = subcontractorType,
          showVerificationDetails = shouldShowVerificationDetails(subcontractor),
          partnershipName = partnershipName,
          addressYesNo = Some(address.isDefined),
          address = address,
          partnershipContactMethodsYesNo = Some(methods.nonEmpty),
          partnershipContactMethodOptions = methods,
          email = subcontractor.emailAddress,
          phone = subcontractor.phoneNumber,
          mobile = subcontractor.mobilePhoneNumber,
          hasUtrYesNo = Some(subcontractor.utr.isDefined),
          utr = subcontractor.utr,
          nominatedPartnerName = nominatedPartnerName,
          nominatedPartnerUtrYesNo = Some(subcontractor.partnerUtr.isDefined),
          nominatedPartnerUtr = subcontractor.partnerUtr,
          nominatedPartnerNinoYesNo = Some(subcontractor.nino.isDefined),
          nominatedPartnerNino = subcontractor.nino,
          nominatedPartnerCrnYesNo = Some(subcontractor.crn.isDefined),
          nominatedPartnerCrn = subcontractor.crn,
          nominatedPartnerWorksReferenceYesNo = Some(subcontractor.worksReferenceNumber.isDefined),
          nominatedPartnerWorksReference = subcontractor.worksReferenceNumber,
          verificationNumber = subcontractor.verificationNumber
        )

      userAnswers.set(
        PartnershipAnswersQuery,
        answers
      )
    }
  }

  private object TrustPopulator {

    def populate(
      userAnswers: UserAnswers,
      subcontractorType: TypeOfSubcontractor,
      subcontractor: SubcontractorResponse
    ): Try[UserAnswers] = {

      val address =
        SubcontractorPopulatorUtils.toAddress(subcontractor)

      val methods =
        SubcontractorPopulatorUtils.contactMethods(subcontractor)

      val trustName =
        subcontractor.tradingName
          .orElse(subcontractor.partnershipTradingName)

      val answers =
        TrustAnswers(
          subcontractorType = subcontractorType,
          showVerificationDetails = shouldShowVerificationDetails(subcontractor),
          trustName = trustName,
          addressYesNo = Some(address.isDefined),
          address = address,
          trustContactMethodsYesNo = Some(methods.nonEmpty),
          trustContactMethod = methods,
          email = subcontractor.emailAddress,
          phone = subcontractor.phoneNumber,
          mobile = subcontractor.mobilePhoneNumber,
          utrYesNo = Some(subcontractor.utr.isDefined),
          utr = subcontractor.utr,
          worksReferenceYesNo = Some(subcontractor.worksReferenceNumber.isDefined),
          worksReference = subcontractor.worksReferenceNumber,
          verificationNumber = subcontractor.verificationNumber
        )

      userAnswers.set(
        TrustAnswersQuery,
        answers
      )
    }
  }

  def populate(
    userAnswers: UserAnswers,
    subcontractorType: TypeOfSubcontractor,
    subcontractor: SubcontractorResponse
  ): Try[UserAnswers] =
    subcontractorType match {

      case Individualorsoletrader =>
        IndividualPopulator.populate(
          userAnswers,
          subcontractorType,
          subcontractor
        )

      case Limitedcompany =>
        CompanyPopulator.populate(
          userAnswers,
          subcontractorType,
          subcontractor
        )

      case Partnership =>
        PartnershipPopulator.populate(
          userAnswers,
          subcontractorType,
          subcontractor
        )

      case Trust =>
        TrustPopulator.populate(
          userAnswers,
          subcontractorType,
          subcontractor
        )
    }
}

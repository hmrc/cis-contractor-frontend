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

import models.{SubcontractorCurrentVerification, UserAnswers}
import models.TypeOfSubcontractor.*
import models.finalvalidation.*
import models.finalvalidation.FinalValidationField.*
import models.finalvalidation.VerifyFinalValidationSource.*
import models.response.SubcontractorResponse
import pages.finalvalidation.VerifyFinalValidationSourcePage
import pages.verify.*
import services.finalvalidation.*
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class VerifyFinalValidationService @Inject() (
  subcontractorService: SubcontractorService,
  subcontractorValidator: SubcontractorValidator
)(implicit ec: ExecutionContext) {

  private final case class SelectedReference(
    subcontractorId: Long,
    subbieResourceRef: Long
  )

  def validate(
    instanceId: String,
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier): Future[VerifyFinalValidationResult] =
    userAnswers.get(VerifyFinalValidationSourcePage) match {

      case Some(source) =>
        Future
          .fromTry(selectedReferences(userAnswers, source))
          .flatMap { references =>
            Future
              .traverse(references) { reference =>
                subcontractorService
                  .getSubcontractor(instanceId, reference.subbieResourceRef)
                  .flatMap { response =>
                    response.subcontractor match {

                      case Some(subcontractor) if subcontractor.subcontractorId == reference.subcontractorId =>
                        Future.successful(subcontractor)

                      case Some(subcontractor) =>
                        Future.failed(
                          new IllegalStateException(
                            s"Expected subcontractorId ${reference.subcontractorId} " +
                              s"but got ${subcontractor.subcontractorId}"
                          )
                        )

                      case None =>
                        Future.failed(
                          new IllegalStateException(
                            s"Subcontractor not found for subbieResourceRef ${reference.subbieResourceRef}"
                          )
                        )
                    }
                  }
              }
              .map(validateSelectedSubcontractors)
          }

      case None =>
        Future.failed(new IllegalStateException("VerifyFinalValidationSourcePage not found"))
    }

  def validateDraftSubcontractor(
    draft: FinalValidationDraft,
    subcontractorId: Long
  ): Try[Seq[FinalValidationDraftIssue]] =
    draft.subcontractor(subcontractorId) match {

      case Some(subcontractor) =>
        Try {
          val proposedSubcontractors =
            draft.subcontractors.map(
              toCurrentVerification
            )

          val fields =
            subcontractorValidator
              .validateFieldsFor(
                subcontractorId = subcontractorId,
                subcontractors = proposedSubcontractors
              )
              .map(
                FinalValidationFieldMapper.fromValidationField
              )
              .distinct

          fields.map { field =>
            FinalValidationDraftIssue(
              fieldKey = field.key,
              value = valueFor(
                field,
                subcontractor.proposed
              )
            )
          }
        }

      case None =>
        Failure(
          new IllegalStateException(
            s"Subcontractor $subcontractorId not found in Final Validation draft"
          )
        )
    }

  private def validateSelectedSubcontractors(
    subcontractors: Seq[SubcontractorResponse]
  ): VerifyFinalValidationResult = {

    val validationSubcontractors =
      subcontractors.map(toCurrentVerification)

    val failedFieldsBySubcontractor =
      subcontractorValidator.validateFields(validationSubcontractors)

    val failures =
      subcontractors.flatMap { subcontractor =>

        val fields =
          failedFieldsBySubcontractor
            .getOrElse(
              subcontractor.subcontractorId,
              Seq.empty
            )
            .map(
              FinalValidationFieldMapper.fromValidationField
            )
            .distinct

        Option.when(fields.nonEmpty) {
          SubcontractorFinalValidationFailure(
            subcontractorId = subcontractor.subcontractorId,
            issues = fields.map { field =>
              FinalValidationIssue(
                field = field,
                value = valueFor(field, subcontractor)
              )
            },
            subbieResourceRef = subcontractor.subbieResourceRef
          )
        }
      }

    VerifyFinalValidationResult(
      subcontractors = subcontractors,
      failures = failures
    )
  }

  private def toCurrentVerification(
    subcontractor: SubcontractorResponse
  ): SubcontractorCurrentVerification =
    SubcontractorCurrentVerification(
      subcontractorId = subcontractor.subcontractorId,
      subbieResourceRef = subcontractor.subbieResourceRef,
      firstName = subcontractor.firstName,
      secondName = subcontractor.secondName,
      surname = subcontractor.surname,
      tradingName = subcontractor.tradingName,
      utr = subcontractor.utr,
      nino = subcontractor.nino,
      crn = subcontractor.crn,
      partnerUtr = subcontractor.partnerUtr,
      partnershipTradingName = subcontractor.partnershipTradingName,
      subcontractorType = subcontractor.subcontractorType,
      addressLine1 = subcontractor.addressLine1,
      addressLine2 = subcontractor.addressLine2,
      addressLine3 = subcontractor.addressLine3,
      addressLine4 = subcontractor.addressLine4,
      country = subcontractor.country,
      postcode = subcontractor.postcode,
      emailAddress = subcontractor.emailAddress,
      phoneNumber = subcontractor.phoneNumber,
      mobilePhoneNumber = subcontractor.mobilePhoneNumber,
      worksReferenceNumber = subcontractor.worksReferenceNumber,
      matched = None,
      autoVerified = None,
      verified = None,
      verificationNumber = None,
      taxTreatment = None,
      verificationDate = None,
      version = None,
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = None
    )

  private def toCurrentVerification(
    subcontractor: FinalValidationDraftSubcontractor
  ): SubcontractorCurrentVerification = {

    val proposed =
      subcontractor.proposed

    SubcontractorCurrentVerification(
      subcontractorId = subcontractor.subcontractorId,
      subbieResourceRef = None,
      firstName = proposed.firstName,
      secondName = proposed.secondName,
      surname = proposed.surname,
      tradingName = proposed.tradingName,
      utr = proposed.utr,
      nino = proposed.nino,
      crn = proposed.crn,
      partnerUtr = proposed.partnerUtr,
      partnershipTradingName = proposed.partnershipTradingName,
      subcontractorType = subcontractor.subcontractorType,
      addressLine1 = proposed.addressLine1,
      addressLine2 = proposed.addressLine2,
      addressLine3 = proposed.addressLine3,
      addressLine4 = proposed.addressLine4,
      country = proposed.country,
      postcode = proposed.postcode,
      emailAddress = proposed.emailAddress,
      phoneNumber = proposed.phoneNumber,
      mobilePhoneNumber = proposed.mobilePhoneNumber,
      worksReferenceNumber = proposed.worksReferenceNumber,
      matched = None,
      autoVerified = None,
      verified = None,
      verificationNumber = None,
      taxTreatment = None,
      verificationDate = None,
      version = None,
      updatedTaxTreatment = None,
      lastMonthlyReturnDate = None,
      pendingVerifications = None
    )
  }

  private def selectedReferences(
    userAnswers: UserAnswers,
    source: VerifyFinalValidationSource
  ): Try[Seq[SelectedReference]] =
    source match {
      case SelectSubcontractor =>
        for {
          selected    <- userAnswers
                           .get(SelectSubcontractorPage)
                           .map(Success(_))
                           .getOrElse(Failure(new IllegalStateException("SelectSubcontractorPage not found")))
          available   <- userAnswers
                           .get(UnverifiedSubcontractorsPage)
                           .map(Success(_))
                           .getOrElse(Failure(new IllegalStateException("UnverifiedSubcontractorsPage not found")))
          selectedIds <- ids(selected.map(_.id))
          references  <- referencesFor(
                           selectedIds,
                           available.map { subcontractor =>
                             (
                               subcontractor.subcontractorId,
                               subcontractor.subbieResourceRef
                             )
                           }
                         )
        } yield references

      case SelectSubcontractorsToReverify =>
        for {
          selected    <- userAnswers
                           .get(SelectSubcontractorsToReverifyPage)
                           .map(Success(_))
                           .getOrElse(Failure(new IllegalStateException("SelectSubcontractorsToReverifyPage not found")))
          response    <- userAnswers
                           .get(NewestVerificationBatchResponsePage)
                           .map(Success(_))
                           .getOrElse(Failure(new IllegalStateException("NewestVerificationBatchResponsePage not found")))
          selectedIds <- ids(selected.map(_.id))
          references  <- referencesFor(
                           selectedIds,
                           response.subcontractors.map { subcontractor =>
                             (
                               subcontractor.subcontractorId,
                               subcontractor.subbieResourceRef
                             )
                           }
                         )
        } yield references

      case ReviewUnmatchedSubcontractors =>
        Failure(
          new UnsupportedOperationException("ReviewUnmatchedSubcontractors FinalValidation is not implemented yet")
        )

      case ReviewInsufficientInfoSubcontractors =>
        Failure(
          new UnsupportedOperationException(
            "ReviewInsufficientInfoSubcontractors FinalValidation is not implemented yet"
          )
        )
    }

  private def ids(values: Set[String]): Try[Set[Long]] =
    Try(
      values.map { value =>
        value.toLongOption.getOrElse(
          throw new IllegalStateException(s"Invalid subcontractorId: $value")
        )
      }
    )

  private def referencesFor(
    selectedIds: Set[Long],
    available: Seq[(Long, Option[Long])]
  ): Try[Seq[SelectedReference]] =
    Try {
      selectedIds.toSeq.map { subcontractorId =>

        val subbieResourceRef =
          available
            .find(_._1 == subcontractorId)
            .flatMap(_._2)
            .getOrElse(
              throw new IllegalStateException(
                s"Subcontractor with id $subcontractorId not found in available subcontractors"
              )
            )

        SelectedReference(
          subcontractorId = subcontractorId,
          subbieResourceRef = subbieResourceRef
        )
      }
    }

  private def valueFor(
    field: FinalValidationField,
    subcontractor: SubcontractorResponse
  ): Option[String] =
    field match {
      case FirstName              => subcontractor.firstName
      case SecondName             => subcontractor.secondName
      case Surname                => subcontractor.surname
      case TradingName            => subcontractor.tradingName
      case PartnershipTradingName => subcontractor.partnershipTradingName
      case Utr                    => subcontractor.utr
      case PartnerUtr             => subcontractor.partnerUtr
      case Nino                   => subcontractor.nino
      case Crn                    => subcontractor.crn
      case AddressLine1           => subcontractor.addressLine1
      case AddressLine2           => subcontractor.addressLine2
      case AddressLine3           => subcontractor.addressLine3
      case AddressLine4           => subcontractor.addressLine4
      case PostCode               => subcontractor.postcode
      case Country                => subcontractor.country
      case EmailAddress           => subcontractor.emailAddress
      case PhoneNumber            => subcontractor.phoneNumber
      case MobilePhoneNumber      => subcontractor.mobilePhoneNumber
      case WorkReferenceNumber    => subcontractor.worksReferenceNumber
      case _                      => None
    }

  private def valueFor(
    field: FinalValidationField,
    details: FinalValidationSubcontractorDetails
  ): Option[String] =
    field match {
      case FirstName              => details.firstName
      case SecondName             => details.secondName
      case Surname                => details.surname
      case TradingName            => details.tradingName
      case PartnershipTradingName => details.partnershipTradingName
      case Utr                    => details.utr
      case PartnerUtr             => details.partnerUtr
      case Nino                   => details.nino
      case Crn                    => details.crn
      case AddressLine1           => details.addressLine1
      case AddressLine2           => details.addressLine2
      case AddressLine3           => details.addressLine3
      case AddressLine4           => details.addressLine4
      case PostCode               => details.postcode
      case Country                => details.country
      case EmailAddress           => details.emailAddress
      case PhoneNumber            => details.phoneNumber
      case MobilePhoneNumber      => details.mobilePhoneNumber
      case WorkReferenceNumber    => details.worksReferenceNumber
      case _                      => None
    }
}

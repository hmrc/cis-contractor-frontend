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

import models.{TypeOfSubcontractor, UserAnswers}
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
individualValidation: IndividualSubcontractorFinalValidation,
companyValidation: CompanySubcontractorFinalValidation,
partnershipValidation: PartnershipSubcontractorFinalValidation,
trustValidation: TrustSubcontractorFinalValidation,
addressDetailsValidation: AddressDetailsFinalValidation
)(implicit ec: ExecutionContext) {
  
  private final case class SelectedReference(
    subcontractorId: Long,
    subbieResourceRef: Long
  )

  def validate(
    instanceId: String,
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier): Future[Seq[SubcontractorFinalValidationFailure]] =
    userAnswers.get(VerifyFinalValidationSourcePage) match {

      case Some(source) =>
        Future.fromTry(selectedReferences(userAnswers, source))
          .flatMap { references =>
            Future
              .traverse(references) { reference =>
                subcontractorService.getSubcontractor(instanceId, reference.subbieResourceRef)
                  .flatMap { response =>
                    response.subcontractor match {

                      case Some(subcontractor)
                        if subcontractor.subcontractorId == reference.subcontractorId =>
                          Future.successful(subcontractor)

                      case Some(subcontractor) =>
                        Future.failed(
                          new IllegalStateException(s"Expected subcontractorId ${reference.subcontractorId} " +
                            s"but got ${subcontractor.subcontractorId}")
                        )

                      case None =>
                        Future.failed(
                          new IllegalStateException(s"Subcontractor not found for subbieResourceRef ${reference.subbieResourceRef}")
                        )
                    }
                  }
              }
              .map(validateSelectedSubcontractors)
          }

      case None =>
        Future.failed(new IllegalStateException("VerifyFinalValidationSourcePage not found"))
    }

  private def validateSelectedSubcontractors(
    subcontractors: Seq[SubcontractorResponse]
  ): Seq[SubcontractorFinalValidationFailure] = {

    // TODO: Revisit this scope when F1 rules land
    val allSubcontractors = subcontractors

    subcontractors.flatMap { subcontractor =>
      val fields = validateFields(subcontractor, allSubcontractors)

      Option.when(fields.nonEmpty) {
        SubcontractorFinalValidationFailure(
          subcontractorId = subcontractor.subcontractorId,
          issues = fields.map { field =>
            FinalValidationIssue(field, valueFor(field, subcontractor))
          },
          subbieResourceRef = subcontractor.subbieResourceRef
        )
      }
    }
  }

  private def validateFields(
    subcontractor: SubcontractorResponse,
    allSubcontractors: Seq[SubcontractorResponse]
  ): Seq[FinalValidationField] = {
    val subcontractorType =
      subcontractor.subcontractorType
        .flatMap(TypeOfSubcontractor.fromString)
        .getOrElse(
          throw new IllegalStateException(s"Invalid subcontractorType: ${subcontractor.subcontractorType}")
        )
    
    val typeSpecificFields =
      subcontractorType match {
          case Individualorsoletrader => individualValidation.validate(subcontractor, allSubcontractors)
          case Limitedcompany         => companyValidation.validate(subcontractor, allSubcontractors)
          case Partnership            => partnershipValidation.validate(subcontractor, allSubcontractors)
          case Trust                  => trustValidation.validate(subcontractor, allSubcontractors)
        }

    val addressFields = addressDetailsValidation.validate(subcontractor)

    (typeSpecificFields ++ addressFields).distinct
  }

  private def selectedReferences(userAnswers: UserAnswers, source: VerifyFinalValidationSource): Try[Seq[SelectedReference]] =
    source match {
      case SelectSubcontractor =>
        for {
          selected  <- userAnswers.get(SelectSubcontractorPage)
                         .map(Success(_))
                         .getOrElse(Failure(new IllegalStateException("SelectSubcontractorPage not found")))
          available <- userAnswers.get(UnverifiedSubcontractorsPage)
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
          selected  <- userAnswers.get(SelectSubcontractorsToReverifyPage)
                         .map(Success(_))
                         .getOrElse(Failure(new IllegalStateException("SelectSubcontractorsToReverifyPage not found")))
          response  <- userAnswers.get(NewestVerificationBatchResponsePage)
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
        Failure(new UnsupportedOperationException("ReviewUnmatchedSubcontractors FinalValidation is not implemented yet"))

      case ReviewInsufficientInfoSubcontractors =>
        Failure(new UnsupportedOperationException("ReviewInsufficientInfoSubcontractors FinalValidation is not implemented yet"))
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
    Try{
      selectedIds.toSeq.map { subcontractorId =>

        val subbieResourceRef =
          available.find(_._1 == subcontractorId)
            .flatMap(_._2)
            .getOrElse(
              throw new IllegalStateException(s"Subcontractor with id $subcontractorId not found in available subcontractors")
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
      case FirstName => subcontractor.firstName
      case SecondName => subcontractor.secondName
      case Surname => subcontractor.surname
      case TradingName => subcontractor.tradingName
      case PartnershipTradingName => subcontractor.partnershipTradingName
      case Utr => subcontractor.utr
      case PartnerUtr => subcontractor.partnerUtr
      case Nino => subcontractor.nino
      case Crn => subcontractor.crn
      case AddressLine1 => subcontractor.addressLine1
      case AddressLine2 => subcontractor.addressLine2
      case AddressLine3 => subcontractor.addressLine3
      case AddressLine4 => subcontractor.addressLine4
      case PostCode => subcontractor.postcode
      case Country => subcontractor.country
      case EmailAddress => subcontractor.emailAddress
      case PhoneNumber => subcontractor.phoneNumber
      case MobilePhoneNumber => subcontractor.mobilePhoneNumber
      case WorkReferenceNumber => subcontractor.worksReferenceNumber
      case _ => None
    }


}

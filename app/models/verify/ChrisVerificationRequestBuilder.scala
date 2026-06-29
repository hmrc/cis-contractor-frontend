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

package models.verify

import connectors.ConstructionIndustrySchemeConnector
import models.{EmployerReference, UserAnswers, VerificationCurrentVerification}
import models.requests.{ChrisVerificationRequest, VerificationDetails}
import queries.CisIdQuery
import uk.gov.hmrc.http.HeaderCarrier
import utils.VerifyEmailResolver

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChrisVerificationRequestBuilder @Inject() (
  cisConnector: ConstructionIndustrySchemeConnector
)(implicit ec: ExecutionContext) {

  def build(
    ua: UserAnswers,
    isAgent: Boolean,
    employerReference: EmployerReference
  )(implicit hc: HeaderCarrier): Future[ChrisVerificationRequest] = {

    val cisIdFut = requireFromSession(
      ua.get(CisIdQuery),
      "CisIdQuery not found in session data"
    )

    for {
      cisId                       <- cisIdFut
      scheme                      <- cisConnector.getScheme(cisId)
      currentVerificationBatch    <- cisConnector.getCurrentVerificationBatch(cisId)
      utr                          = requireValue(scheme.utr, "UTR not found in scheme data")
      verificationBatch            = requireValue(currentVerificationBatch.verificationBatch, "Verification batch not found")
      verificationBatchResourceRef =
        requireValue(verificationBatch.verifBatchResourceRef, "Verification batch resource ref not found")
      aoRef                        = scheme.accountsOfficeReference
    } yield ChrisVerificationRequest(
      instanceId = cisId,
      isAgent = isAgent,
      clientTaxOfficeNumber = employerReference.taxOfficeNumber,
      clientTaxOfficeRef = employerReference.taxOfficeReference,
      contractorUTR = utr,
      contractorAORef = aoRef,
      verificationBatchId = verificationBatch.verificationBatchId.toString,
      verificationBatchResourceRef = verificationBatchResourceRef.toString,
      emailRecipient = VerifyEmailResolver.resolvedEmail(ua),
      subcontractors = currentVerificationBatch.subcontractors,
      verifications = currentVerificationBatch.verifications.map(toVerificationDetails)
    )
  }

  private def requireFromSession[A](valueOpt: Option[A], errorMsg: String): Future[A] =
    valueOpt match {
      case Some(value) => Future.successful(value)
      case None        => Future.failed(new RuntimeException(errorMsg))
    }

  private def requireValue[A](valueOpt: Option[A], errorMsg: String): A =
    valueOpt.getOrElse(throw new RuntimeException(errorMsg))

  private def toVerificationDetails(verification: VerificationCurrentVerification): VerificationDetails =
    VerificationDetails(
      subcontractorName =
        "TBC", // TODO: added as per BE 4931 PR impl, but not really used in BE to build chris submission xml payload
      verificationResourceRef = requireValue(
        verification.verificationResourceRef.map(_.toString),
        "Verification resource ref not found"
      ),
      proceedVerification = true // TODO: how to decide this boolean?
    )

}

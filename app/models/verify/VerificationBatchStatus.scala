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

sealed trait VerificationBatchStatus

object VerificationBatchStatus {

  case object Started extends VerificationBatchStatus
  case object Validated extends VerificationBatchStatus
  case object Pending extends VerificationBatchStatus
  case object Accepted extends VerificationBatchStatus
  case object Submitted extends VerificationBatchStatus
  case object SubmittedNoReceipt extends VerificationBatchStatus
  case object FatalError extends VerificationBatchStatus
  case object DepartmentalError extends VerificationBatchStatus

  def from(value: String): Option[VerificationBatchStatus] =
    value match {
      case "STARTED"              => Some(Started)
      case "VALIDATED"            => Some(Validated)
      case "PENDING"              => Some(Pending)
      case "ACCEPTED"             => Some(Accepted)
      case "SUBMITTED"            => Some(Submitted)
      case "SUBMITTED_NO_RECEIPT" => Some(SubmittedNoReceipt)
      case "FATAL_ERROR"          => Some(FatalError)
      case "DEPARTMENTAL_ERROR"   => Some(DepartmentalError)
      case _                      => None
    }
}

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

package services.finalvalidation

import models.finalvalidation.{FinalValidationDraftSubcontractor, FinalValidationField}
import models.finalvalidation.FinalValidationField.*
import models.response.SubcontractorResponse

import javax.inject.{Inject, Singleton}

@Singleton
class IndividualSubcontractorFinalValidation @Inject() {

  def validate(
    subcontractor: SubcontractorResponse,
    allSubcontractors: Seq[SubcontractorResponse]
  ): Seq[FinalValidationField] =
    // TODO F3 - Individual subcontractor final validation
    
    val _ = allSubcontractors
    
    // Temporary manual failure to unblock FinalValidation journey testing
    if (Set(10903L, 10904L, 10905L).contains(subcontractor.subcontractorId)) {
      Seq(Utr, Nino)
    } else {
      Seq.empty
    }

  def validateDraft(
    subcontractor: FinalValidationDraftSubcontractor,
    allSubcontractors: Seq[FinalValidationDraftSubcontractor]
  ): Seq[FinalValidationField] =
    // TODO F3 - Individual subcontractor final validation

    val details =
      subcontractor.proposed

    // same F2/F3/F4/F5 rule using details

    Seq.empty
}

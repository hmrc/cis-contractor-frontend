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

import models.SubcontractorViewModel

import javax.inject.{Inject, Singleton}

object SubcontractorSource {

  // Placeholder list — TODO will be replaced with a backend call when the connector is implemented
  val subcontractors: Seq[SubcontractorViewModel] = Seq(
    SubcontractorViewModel("100", "Brody Martin"),
    SubcontractorViewModel("95", "Hooper Associates"),
    SubcontractorViewModel("96", "Alpha Plumbing"),
    SubcontractorViewModel("98", "Beta Builders"),
    SubcontractorViewModel("97", "Gamma Construction"),
    SubcontractorViewModel("99", "Delta Electrical"),
    SubcontractorViewModel("101", "Epsilon Carpentry"),
    SubcontractorViewModel("103", "Zeta Roofing"),
    SubcontractorViewModel("102", "Eta Plastering")
  )
}

@Singleton
class SubcontractorSource @Inject() () {
  def get(): Seq[SubcontractorViewModel] = SubcontractorSource.subcontractors
}

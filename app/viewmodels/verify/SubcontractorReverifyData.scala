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

package viewmodels.verify

object SubcontractorReverifyData {

  val rows: Seq[SubcontractorReverifyRow] = Seq(
    SubcontractorReverifyRow(
      id = "Grantalan",
      name = "Grant, Alan",
      utr = "0991272528",
      verified = "No",
      verificationNumber = "V0001256246",
      taxTreatment = "Standard rate",
      dateAdded = "11 May 2020"
    ),
    SubcontractorReverifyRow(
      id = "Hammondhouse",
      name = "Hammond House",
      utr = "2904743750",
      verified = "Yes",
      verificationNumber = "V0001217702",
      taxTreatment = "Gross",
      dateAdded = "1 Oct 2025"
    ),
    SubcontractorReverifyRow(
      id = "Ingenresearch",
      name = "InGen Research",
      utr = "9347488729",
      verified = "No",
      verificationNumber = "V0005617876",
      taxTreatment = "Standard rate",
      dateAdded = "1 Mar 2020"
    ),
    SubcontractorReverifyRow(
      id = "Malcolmandsattler",
      name = "Malcolm And Sattler",
      utr = "0074742762",
      verified = "Yes",
      verificationNumber = "V0004635231",
      taxTreatment = "Higher rate",
      dateAdded = "1 Oct 2025"
    ),
    SubcontractorReverifyRow(
      id = "brightwellPartners",
      name = "Brightwell Partners",
      utr = "1234567890",
      verified = "No",
      verificationNumber = "V0007771001",
      taxTreatment = "Standard rate",
      dateAdded = "23 Apr 2026"
    ),
    SubcontractorReverifyRow(
      id = "carterfieldsLtd",
      name = "Carterfields Ltd",
      utr = "2345678901",
      verified = "Yes",
      verificationNumber = "V0007771002",
      taxTreatment = "Gross",
      dateAdded = "23 Apr 2026"
    ),
    SubcontractorReverifyRow(
      id = "northbridgeBuild",
      name = "Northbridge Build",
      utr = "3456789012",
      verified = "No",
      verificationNumber = "V0007771003",
      taxTreatment = "Standard rate",
      dateAdded = "23 Apr 2026"
    ),
    SubcontractorReverifyRow(
      id = "oakthornServices",
      name = "Oakthorn Services",
      utr = "4567890123",
      verified = "Yes",
      verificationNumber = "V0007771004",
      taxTreatment = "Higher rate",
      dateAdded = "23 Apr 2026"
    )
  ).sortBy(_.name.toLowerCase)
}

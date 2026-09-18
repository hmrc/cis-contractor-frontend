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

final case class LinkViewModel(
  url: String,
  hiddenText: String
)

final case class MissingSubcontractorRow(
  name: String,
  nameLink: LinkViewModel,
  utr: String,
  editLink: LinkViewModel,
  proceedLink: LinkViewModel,
  removeLink: LinkViewModel
)

final case class ReadySubcontractorRow(
  name: String,
  nameLink: LinkViewModel,
  utr: String
)

final case class ReviewInsufficientInfoViewModel(
  missing: Seq[MissingSubcontractorRow],
  ready: Seq[ReadySubcontractorRow]
) {
  val hasMissing: Boolean = missing.nonEmpty
  val hasReady: Boolean   = ready.nonEmpty
  val allReady: Boolean   = missing.isEmpty && ready.nonEmpty
}

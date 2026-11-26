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

package models

// Multi-tab
// Defines the namespaces used to store journey and subjourney answers.
// Subjourneys (e.g. Final Validation) reuse the flowId of their parent journey.
// Additional journey namespaces will be added as multi-tab support is extended.
enum JourneyNamespace(val key: String) {
  case AddSubcontractor extends JourneyNamespace("addSubcontractor")
  case SubcontractorFinalValidation extends JourneyNamespace("subcontractorFinalValidation")
}

object JourneyNamespace {

  // Multi-tab
  // Resolves the namespace for modes used by shared subcontractor controllers.
  // Extend this mapping as additional modes are supported by multi-tab journeys.
  def resolveNamespace(mode: Mode): Option[JourneyNamespace] =
    mode match {
      case NormalMode | CheckMode => Some(AddSubcontractor)
      case FinalValidationMode    => Some(SubcontractorFinalValidation)
      case _                      => None
    }
}

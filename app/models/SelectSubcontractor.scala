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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait SelectSubcontractor

object SelectSubcontractor extends Enumerable.Implicits {

  case object BrodyMartin extends WithName("brodyMartin") with SelectSubcontractor
  case object Hooperassociates extends WithName("hooperAssociates") with SelectSubcontractor

  case object AlphaPlumbing extends WithName("alphaPlumbing") with SelectSubcontractor

  case object BetaBuilders extends WithName("betaBuilders") with SelectSubcontractor

  case object GammaConstruction extends WithName("gammaConstruction") with SelectSubcontractor

  case object DeltaElectrical extends WithName("deltaElectrical") with SelectSubcontractor

  // These will appear on page 2
  case object EpsilonCarpentry extends WithName("epsilonCarpentry") with SelectSubcontractor

  case object ZetaRoofing extends WithName("zetaRoofing") with SelectSubcontractor

  case object EtaPlastering extends WithName("etaPlastering") with SelectSubcontractor

  val values: Seq[SelectSubcontractor] = Seq(
    BrodyMartin,
    Hooperassociates,
    AlphaPlumbing,
    BetaBuilders,
    GammaConstruction,
    DeltaElectrical,
    EpsilonCarpentry,
    ZetaRoofing,
    EtaPlastering
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map { case (value, index) =>
      CheckboxItemViewModel(
        content = Text(messages(s"verify.selectSubcontractor.${value.toString}")),
        fieldId = "value",
        index = index,
        value = value.toString
      )
    }

  implicit val enumerable: Enumerable[SelectSubcontractor] =
    Enumerable(values.map(v => v.toString -> v): _*)
}

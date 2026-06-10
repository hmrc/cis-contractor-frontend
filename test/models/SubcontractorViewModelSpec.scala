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

import base.SpecBase
import org.scalatest.matchers.should.Matchers.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.all.CheckboxItemViewModel

class SubcontractorViewModelSpec extends SpecBase {

  implicit val messages: Messages = play.api.i18n.MessagesImpl(
    play.api.i18n.Lang.defaultLang,
    app.injector.instanceOf[play.api.i18n.MessagesApi]
  )

  private def subcontractor(
    subcontractorId: Long,
    firstName: Option[String] = None,
    surname: Option[String] = None,
    tradingName: Option[String] = None,
    partnershipTradingName: Option[String] = None,
    subcontractorType: Option[String]
  ): Subcontractor =
    Subcontractor(
      subcontractorId = subcontractorId,
      firstName = firstName,
      secondName = None,
      surname = surname,
      tradingName = tradingName,
      partnershipTradingName = partnershipTradingName,
      verified = None,
      verificationNumber = None,
      taxTreatment = None,
      verificationDate = None,
      lastMonthlyReturnDate = None,
      createDate = None,
      subcontractorType = subcontractorType,
      subbieResourceRef = None,
      utr = None,
      partnerUtr = None,
      crn = None,
      nino = None
    )

  private val NoName = "no name provided"

  "SubcontractorViewModel.checkboxItems" - {
    "map SubcontractorViewModel into CheckboxItemViewModel using id / name" in {
      val subcontractors = Seq(
        SubcontractorViewModel(
          id = "10",
          name = "John, Smith"
        ),
        SubcontractorViewModel(
          id = "20",
          name = "ABC Property Services"
        )
      )

      val result = SubcontractorViewModel.checkboxItems(subcontractors)

      result shouldBe Seq(
        CheckboxItemViewModel(
          content = Text("ABC Property Services"),
          fieldId = "value",
          index = 0,
          value = "20"
        ),
        CheckboxItemViewModel(
          content = Text("John, Smith"),
          fieldId = "value",
          index = 1,
          value = "10"
        )
      )
    }
  }

  "SubcontractorViewModel.fromSubcontractors" - {
    "map subcontractors into view models using firstName / surname / tradingName / partnershipTradingName / subcontractorType" in {
      val subcontractors = Seq(
        subcontractor(
          subcontractorId = 10L,
          firstName = Some("John"),
          surname = Some("Smith"),
          tradingName = Some("ABC Property Services"),
          subcontractorType = Some("soletrader")
        ),
        subcontractor(
          subcontractorId = 20L,
          tradingName = Some("ABC Property Services"),
          subcontractorType = Some("soletrader")
        ),
        subcontractor(
          subcontractorId = 30L,
          tradingName = Some("ABC Construction Ltd"),
          subcontractorType = Some("company")
        ),
        subcontractor(
          subcontractorId = 40L,
          partnershipTradingName = Some("ABC Partnership"),
          tradingName = Some("ABC Property Services"),
          subcontractorType = Some("partnership")
        ),
        subcontractor(
          subcontractorId = 50L,
          tradingName = Some("ABC Trust"),
          subcontractorType = Some("trust")
        )
      )

      val viewModels = SubcontractorViewModel.fromSubcontractors(subcontractors)

      viewModels shouldBe Seq(
        SubcontractorViewModel(id = "10", name = "Smith, John"),
        SubcontractorViewModel(id = "20", name = "ABC Property Services"),
        SubcontractorViewModel(id = "30", name = "ABC Construction Ltd"),
        SubcontractorViewModel(id = "40", name = "ABC Partnership"),
        SubcontractorViewModel(id = "50", name = "ABC Trust")
      )
    }

    "map subcontractors into view models with name equal to 'no name provided' when subcontractorType is None" in {
      val subcontractors = List(
        subcontractor(
          subcontractorId = 10L,
          firstName = Some("John"),
          surname = Some("Smith"),
          tradingName = None,
          subcontractorType = None
        )
      )

      val viewModels = SubcontractorViewModel.fromSubcontractors(subcontractors)

      viewModels shouldBe Seq(
        SubcontractorViewModel(id = "10", name = NoName)
      )
    }

    "map subcontractors into view models with name equal to 'no name provided' when subcontractorType is not recognised" in {
      val subcontractors = List(
        subcontractor(
          subcontractorId = 10L,
          firstName = Some("John"),
          surname = Some("Smith"),
          tradingName = None,
          subcontractorType = Some("notValidType")
        )
      )

      val viewModels = SubcontractorViewModel.fromSubcontractors(subcontractors)

      viewModels shouldBe Seq(
        SubcontractorViewModel(id = "10", name = NoName)
      )
    }

    "map subcontractors into view models with name equal to 'no name provided' when a Individualorsoletrader row whose firstName, surname is blank" in {
      val subcontractors = List(
        subcontractor(
          subcontractorId = 10L,
          firstName = Some("   "),
          surname = Some("   "),
          subcontractorType = Some("soletrader")
        )
      )

      val viewModels = SubcontractorViewModel.fromSubcontractors(subcontractors)

      viewModels shouldBe Seq(SubcontractorViewModel(id = "10", name = NoName))
    }

    "map subcontractors into view models with name equal to 'no name provided' when a Individualorsoletrader row whose tradingName is blank" in {
      val subcontractors = List(
        subcontractor(
          subcontractorId = 10L,
          tradingName = Some("   "),
          subcontractorType = Some("soletrader")
        )
      )

      val viewModels = SubcontractorViewModel.fromSubcontractors(subcontractors)

      viewModels shouldBe Seq(SubcontractorViewModel(id = "10", name = NoName))
    }

    "map subcontractors into view models with name equal to surname only when a Individualorsoletrader row whose surname is provided, firstName is blank" in {
      val subcontractors = List(
        subcontractor(
          subcontractorId = 10L,
          firstName = Some("   "),
          surname = Some("Smith"),
          tradingName = Some("ABC Property Services"),
          subcontractorType = Some("soletrader")
        )
      )

      val viewModels = SubcontractorViewModel.fromSubcontractors(subcontractors)

      viewModels shouldBe Seq(SubcontractorViewModel(id = "10", name = "Smith"))
    }

    "map subcontractors into view models with name equal to tradingName when a Individualorsoletrader row whose tradingName is provided, firstName and surname are blank" in {
      val subcontractors = List(
        subcontractor(
          subcontractorId = 10L,
          firstName = Some("   "),
          surname = Some("   "),
          tradingName = Some("ABC Property Services"),
          subcontractorType = Some("soletrader")
        )
      )

      val viewModels = SubcontractorViewModel.fromSubcontractors(subcontractors)

      viewModels shouldBe Seq(SubcontractorViewModel(id = "10", name = "ABC Property Services"))
    }

    "map subcontractors into view models with name equal to tradingName when a Individualorsoletrader row whose tradingName and firstNare are provided, surname are blank" in {
      val subcontractors = List(
        subcontractor(
          subcontractorId = 10L,
          firstName = Some("John"),
          surname = Some("   "),
          tradingName = Some("ABC Property Services"),
          subcontractorType = Some("soletrader")
        )
      )

      val viewModels = SubcontractorViewModel.fromSubcontractors(subcontractors)

      viewModels shouldBe Seq(SubcontractorViewModel(id = "10", name = "ABC Property Services"))
    }

    "map subcontractors into view models with name equal to 'no name provided' when a Limitedcompany row whose tradingName is blank" in {
      val subcontractors = List(
        subcontractor(
          subcontractorId = 10L,
          tradingName = Some("   "),
          subcontractorType = Some("company")
        )
      )

      val viewModels = SubcontractorViewModel.fromSubcontractors(subcontractors)

      viewModels shouldBe Seq(SubcontractorViewModel(id = "10", name = NoName))
    }

    "map subcontractors into view models with name equal to 'no name provided' when a Partnership row whose partnershipTradingName is blank" in {
      val subcontractors = List(
        subcontractor(
          subcontractorId = 10L,
          partnershipTradingName = Some("   "),
          subcontractorType = Some("partnership")
        )
      )

      val viewModels = SubcontractorViewModel.fromSubcontractors(subcontractors)

      viewModels shouldBe Seq(SubcontractorViewModel(id = "10", name = NoName))
    }

    "map subcontractors into view models with name equal to 'no name provided' when a Partnership row whose tradingName is blank" in {
      val subcontractors = List(
        subcontractor(
          subcontractorId = 10L,
          tradingName = Some("   "),
          subcontractorType = Some("partnership")
        )
      )

      val viewModels = SubcontractorViewModel.fromSubcontractors(subcontractors)

      viewModels shouldBe Seq(SubcontractorViewModel(id = "10", name = NoName))
    }

    "map subcontractors into view models with name equal to tradingName when a Partnership row whose partnershipTradingName is blank, tradingName is provided" in {
      val subcontractors = List(
        subcontractor(
          subcontractorId = 10L,
          partnershipTradingName = Some("   "),
          tradingName = Some("ABC Property Services"),
          subcontractorType = Some("partnership")
        )
      )

      val viewModels = SubcontractorViewModel.fromSubcontractors(subcontractors)

      viewModels shouldBe Seq(SubcontractorViewModel(id = "10", name = "ABC Property Services"))
    }

    "map subcontractors into view models with name equal to 'no name provided' when a Trust row whose tradingName is blank" in {
      val subcontractors = List(
        subcontractor(
          subcontractorId = 10L,
          tradingName = Some("   "),
          subcontractorType = Some("trust")
        )
      )

      val viewModels = SubcontractorViewModel.fromSubcontractors(subcontractors)

      viewModels shouldBe Seq(SubcontractorViewModel(id = "10", name = NoName))
    }

  }
}

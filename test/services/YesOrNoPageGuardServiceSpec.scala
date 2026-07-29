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

import base.SpecBase
import generators.ModelGenerators
import models.NormalMode
import org.scalatestplus.mockito.MockitoSugar
import pages.add.{AddIndividualContactMethodsYesNoPage, NationalInsuranceNumberYesNoPage, SubTradingNameYesNoPage, UniqueTaxpayerReferenceYesNoPage, WorksReferenceNumberYesNoPage}
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.mvc
import play.api.mvc.Results
import play.api.test.Helpers.LOCATION


class YesOrNoPageGuardServiceSpec extends SpecBase with MockitoSugar with ModelGenerators {

  private val service = new YesOrNoPageGuardService()
  val continueRoute: mvc.Results.Status = Results.Ok

  "yesOrNoPageRoute" - {
    val mode = NormalMode

    val serviceTestData = Seq((SubTradingNameYesNoPage, controllers.add.routes.SubTradingNameYesNoController.onPageLoad(mode)),
      (AddIndividualContactMethodsYesNoPage, controllers.add.routes.AddIndividualContactMethodsYesNoController.onPageLoad(mode)),
      (UniqueTaxpayerReferenceYesNoPage, controllers.add.routes.UniqueTaxpayerReferenceYesNoController.onPageLoad(mode)),
      (NationalInsuranceNumberYesNoPage, controllers.add.routes.NationalInsuranceNumberYesNoController.onPageLoad(mode)),
      (WorksReferenceNumberYesNoPage, controllers.add.routes.WorksReferenceNumberYesNoController.onPageLoad(mode))
    )

    serviceTestData.foreach { case (page, expectedURL) =>
      s" must return continue route for ${page.toString} when guardCheck is Some(true)" in {

        val result =
          service.yesOrNoPageRoute(
            continueRoute,
            Some(true),
            page,
            mode
          )
        result.header.status mustBe OK

      }
      s" must redirect to ${page.toString} route when guardCheck is Some(false)" in {

        val result =
          service.yesOrNoPageRoute(
            continueRoute,
            Some(false),
            page,
            mode
          )
        result.header.status mustBe SEE_OTHER
        result.header.headers.get(LOCATION).value contains expectedURL.url
      }

      s"redirect to journey recovery for ${page.toString} when guardCheck is None" in {
        val result =
          service.yesOrNoPageRoute(
            continueRoute,
            None,
            page,
            mode
          )
        result.header.status mustBe SEE_OTHER
        result.header.headers.get(LOCATION).value contains expectedURL.url
      }
    }
  }
}

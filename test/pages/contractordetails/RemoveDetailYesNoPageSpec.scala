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

package pages.contractordetails

import pages.behaviours.PageBehaviours
import play.api.libs.json.JsPath

class RemoveDetailYesNoPageSpec extends PageBehaviours {

  "RemoveDetailYesNoPage" - {

    Seq(
      ("email", EnterContractorEmailAddressPage, AddEmailAddressYesNoPage, "email@test.com"),
      ("scheme-name", SchemeNamePage, AddSchemeNameYesNoPage, "Scheme123")
    ).foreach { case (contractorDetail, selectedDetailPage, screenerPage, dummyDetail) =>
      s"when contractorDetail is '$contractorDetail'" - {

        "have the correct path" in {
          RemoveDetailYesNoPage(
            contractorDetail
          ).path mustBe (JsPath \ "contractordetails" \ s"removeDetailYesNo-$contractorDetail")
        }

        "have the correct toString" in {
          RemoveDetailYesNoPage(contractorDetail).toString mustBe s"removeDetailYesNo-$contractorDetail"
        }

        s"'$selectedDetailPage'" - {
          beRetrievable[String](selectedDetailPage)
          beSettable[String](selectedDetailPage)
          beRemovable[String](selectedDetailPage)
        }

        s"'$screenerPage'" - {
          beRetrievable[Boolean](screenerPage)
          beSettable[Boolean](screenerPage)
        }

        s"cleanup: must remove '$selectedDetailPage' userAnswers and set '$screenerPage' to No when Yes is selected" in {
          val userAnswers = emptyUserAnswers
            .set(selectedDetailPage, dummyDetail)
            .success
            .value
            .set(screenerPage, true)
            .success
            .value

          val updatedUserAnswers =
            userAnswers.set(RemoveDetailYesNoPage(contractorDetail), true).success.value

          updatedUserAnswers.get(selectedDetailPage) mustBe None
          updatedUserAnswers.get(screenerPage) mustBe Some(false)
        }

        s"cleanup: must retain '$selectedDetailPage' userAnswers and keep '$screenerPage' as Yes when No is selected" in {
          val userAnswers = emptyUserAnswers
            .set(selectedDetailPage, dummyDetail)
            .success
            .value
            .set(screenerPage, true)
            .success
            .value

          val updatedUserAnswers =
            userAnswers.set(RemoveDetailYesNoPage(contractorDetail), false).success.value

          updatedUserAnswers.get(selectedDetailPage) mustBe Some(dummyDetail)
          updatedUserAnswers.get(screenerPage) mustBe Some(true)
        }
      }
    }
  }
}

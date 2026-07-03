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
    "have the correct path" in {
      RemoveDetailYesNoPage.path mustBe (JsPath \ "contractordetails" \ "removeDetailYesNo")
    }

    "have the correct toString" in {
      RemoveDetailYesNoPage.toString mustBe "removeDetailYesNo"
    }

//    "contractorDetail is set to 'email'" in {
//      contractorDetail match {
//        case "email" =>
//          beRetrievable[String](EnterContractorEmailAddressPage)
//
//          beSettable[String](EnterContractorEmailAddressPage)
//
//          beRemovable[String](EnterContractorEmailAddressPage)
//      }
//    }
//
//    "contractorDetail is set to 'scheme-name'" in {
//      contractorDetail match {
//        case "scheme-name" =>
//          beRetrievable[String](SchemeNamePage)
//
//          beSettable[String](SchemeNamePage)
//
//          beRemovable[String](SchemeNamePage)
//      }
//    }
  }
}

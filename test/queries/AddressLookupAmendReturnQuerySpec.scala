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

package queries

import base.SpecBase
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsPath, Json}

class AddressLookupAmendReturnQuerySpec extends AnyFreeSpec with Matchers with SpecBase {

  "AddressLookupAmendReturnQuery" - {

    "must have the correct JSON path" in {
      AddressLookupAmendReturnQuery.path mustEqual (JsPath \ "addressLookupAmendReturn")
    }

    "must set the flag to true" in {
      val ua = emptyUserAnswers
        .set(AddressLookupAmendReturnQuery, true)
        .success
        .value

      ua.data mustBe Json.obj("addressLookupAmendReturn" -> true)
    }

    "must set the flag to false" in {
      val ua = emptyUserAnswers
        .set(AddressLookupAmendReturnQuery, false)
        .success
        .value

      ua.data mustBe Json.obj("addressLookupAmendReturn" -> false)
    }

    "must get the flag when set to true" in {
      val ua = emptyUserAnswers
        .set(AddressLookupAmendReturnQuery, true)
        .success
        .value

      ua.get(AddressLookupAmendReturnQuery) mustBe Some(true)
    }

    "must get the flag when set to false" in {
      val ua = emptyUserAnswers
        .set(AddressLookupAmendReturnQuery, false)
        .success
        .value

      ua.get(AddressLookupAmendReturnQuery) mustBe Some(false)
    }

    "must return None when the flag has not been set" in {
      emptyUserAnswers.get(AddressLookupAmendReturnQuery) mustBe None
    }

    "must overwrite an existing value" in {
      val ua = emptyUserAnswers
        .set(AddressLookupAmendReturnQuery, true)
        .success
        .value
        .set(AddressLookupAmendReturnQuery, false)
        .success
        .value

      ua.get(AddressLookupAmendReturnQuery) mustBe Some(false)
    }

    "must remove the flag" in {
      val ua = emptyUserAnswers
        .set(AddressLookupAmendReturnQuery, true)
        .success
        .value
        .remove(AddressLookupAmendReturnQuery)
        .success
        .value

      ua.get(AddressLookupAmendReturnQuery) mustBe None
    }
  }
}

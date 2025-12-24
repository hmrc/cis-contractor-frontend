/*
 * Copyright 2025 HM Revenue & Customs
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

package forms.add

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import org.scalatest.matchers.should.Matchers.should
import play.api.data.{Form, FormError}

class WorksReferenceNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "worksReferenceNumber.error.required"
  val invalidKey  = "worksReferenceNumber.error.invalid"
  val lengthKey   = "worksReferenceNumber.error.length"
  val maxLength   = 20

  val form: Form[String] = new WorksReferenceNumberFormProvider()()

  val fieldName = "value"

  val validWorkRefsGen: Gen[String] = Gen.oneOf(
    "ABC Construction LTD",
    "A&B Contractors Ltd",
    "North-East (UK)",
    "Symbols ~!@#$%&'()*+",
    "Symbols ,-./:;=?_{}£",
    "Symbol €",
    "Contains £ and €",
    "Dollar $ sign"
  )

  override def stringsLongerThan(n: Int): Gen[String] =
    Gen.listOfN(n + 1, Gen.numChar).map(_.mkString)

  override def stringsWithMaxLength(n: Int): Gen[String] =
    Gen.listOfN(n, Gen.numChar).map(_.mkString)

  behave like fieldThatBindsValidData(form, fieldName, validWorkRefsGen)

  behave like fieldWithMaxLength(
    form,
    fieldName,
    maxLength = maxLength,
    lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
  )

  behave like mandatoryField(
    form,
    fieldName,
    requiredError = FormError(fieldName, requiredKey)
  )

  "trim leading and trailing spaces" in {
    val result = form.bind(Map(fieldName -> "   ABC Ltd   "))
    result.value.value mustBe "ABC Ltd"
  }

  "reject invalid characters (backtick, pipe)" in {
    val invalidNames = Seq("Backtick ` here", "Pipe | symbol")
    invalidNames.foreach { name =>
      val result = form.bind(Map(fieldName -> name))
      result.errors.map(_.message) must contain(invalidKey)
    }
  }

}

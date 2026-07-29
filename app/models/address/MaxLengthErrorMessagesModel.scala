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

package models.address

import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.{Json, Writes}
import utils.MessageOption

case class MaxLengthErrorMessagesModel(
  en: Option[MaxLengthErrorMessage] = None,
  cy: Option[MaxLengthErrorMessage] = None
)

object MaxLengthErrorMessagesModel {

  implicit val writes: Writes[MaxLengthErrorMessagesModel] =
    Json.writes[MaxLengthErrorMessagesModel]

  def forConfig(
    line1MaxLength: Option[Int],
    line2MaxLength: Option[Int],
    line3MaxLength: Option[Int],
    townMaxLength: Option[Int]
  )(implicit messagesApi: MessagesApi): MaxLengthErrorMessagesModel =
    MaxLengthErrorMessagesModel(
      en = MaxLengthErrorMessage.forLang(
        Lang("en"),
        line1MaxLength,
        line2MaxLength,
        line3MaxLength,
        townMaxLength
      ),
      cy = MaxLengthErrorMessage.forLang(
        Lang("cy"),
        line1MaxLength,
        line2MaxLength,
        line3MaxLength,
        townMaxLength
      )
    )
}

case class MaxLengthErrorMessage(
  addressLine1: String,
  addressLine2: String,
  addressLine3: String,
  town: String
)

object MaxLengthErrorMessage {

  implicit val writes: Writes[MaxLengthErrorMessage] =
    Json.writes[MaxLengthErrorMessage]

  def forLang(
    lang: Lang,
    line1MaxLength: Option[Int],
    line2MaxLength: Option[Int],
    line3MaxLength: Option[Int],
    townMaxLength: Option[Int]
  )(implicit messagesApi: MessagesApi): Option[MaxLengthErrorMessage] =
    for {
      line1 <- line1MaxLength.flatMap(length =>
                 MessageOption(
                   "addressLookup.common.editPage.addressLine1.maxLength",
                   lang,
                   length.toString
                 )
               )
      line2 <- line2MaxLength.flatMap(length =>
                 MessageOption(
                   "addressLookup.common.editPage.addressLine2.maxLength",
                   lang,
                   length.toString
                 )
               )
      line3 <- line3MaxLength.flatMap(length =>
                 MessageOption(
                   "addressLookup.common.editPage.addressLine3.maxLength",
                   lang,
                   length.toString
                 )
               )
      town  <- townMaxLength.flatMap(length =>
                 MessageOption(
                   "addressLookup.common.editPage.town.maxLength",
                   lang,
                   length.toString
                 )
               )
    } yield MaxLengthErrorMessage(
      addressLine1 = line1,
      addressLine2 = line2,
      addressLine3 = line3,
      town = town
    )
}

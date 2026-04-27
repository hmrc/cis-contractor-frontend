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

package controllers.verify

import config.FrontendAppConfig
import controllers.actions.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.verify.VerificationSubmittedViewModel
import views.html.verify.VerificationRequestSubmittedView

import java.time.LocalDateTime
import javax.inject.Inject

class VerificationRequestSubmittedController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: VerificationRequestSubmittedView
)(implicit appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>

      // TODO: Replace this with your real source:
      // TODO: Replace 1. referenceNumber 2. submittedAt 3. verify list 4. Reverify lists 5. confirmationEmail
      val vm = VerificationSubmittedViewModel(
        referenceNumber = "Reference number 12345",
        submittedAt = LocalDateTime.now(),
        subcontractorsToVerify =
          Seq("Brody, Martin", "Hooper And Associates", "Quint Transportation", "The Kintner Group"),
        //  To check the validation of empty reverify list
        //  subcontractorsToReverify = Seq.empty,
        subcontractorsToReverify = Seq("Grant, Alan", "InGen Research"),
        //  To test no email provided scenario
        //  confirmationEmail = None
        confirmationEmail = Some("test@testmail.com")
      )

      Ok(view(vm))
    }
}

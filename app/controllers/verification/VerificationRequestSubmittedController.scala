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

package controllers.verification

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.verification.VerificationSubmittedViewModel
import views.html.verification.VerificationRequestSubmittedView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class VerificationRequestSubmittedController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: VerificationRequestSubmittedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>

      // TODO: replace this with your real source:
      // - referenceNumber ???? not such where to get this value from
      // - submittedAt ?? which session data i can get this
      // - verify/reverify lists ????  where to get the lst for the verify and reVerify
      // - confirmationEmail ??? can I use the value in scheme
      val vm = VerificationSubmittedViewModel(
        referenceNumber = "Reference number",
        submittedAt = java.time.LocalDateTime.now(),
        subcontractorsToVerify =
          Seq("Brody, Martin", "Hooper And Associates", "Quint Transportation", "The Kintner Group"),
        subcontractorsToReverify = Seq.empty,
        confirmationEmail = None
      )

      Ok(view(vm))
    }
}

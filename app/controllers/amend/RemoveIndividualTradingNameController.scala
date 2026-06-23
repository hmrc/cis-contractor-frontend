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

package controllers.amend

import controllers.actions.*
import pages.add.{SubTradingNameYesNoPage, TradingNameOfSubcontractorPage}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveIndividualTradingNameController @Inject() (
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val result = for {
      a <- request.userAnswers.remove(TradingNameOfSubcontractorPage)
      b <- a.set(SubTradingNameYesNoPage, false)
    } yield b

    result.fold(
      _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())),
      updated =>
        sessionRepository
          .set(updated)
          .map(_ => Redirect(controllers.add.routes.SubcontractorNameController.onPageLoad(models.AmendMode)))
    )
  }
}

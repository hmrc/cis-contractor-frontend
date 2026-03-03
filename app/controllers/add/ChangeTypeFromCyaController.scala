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

package controllers.add

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.CheckMode
import pages.add.{ChangingTypeFromCyaPage, HasSwitchedTypeFromCyaPage}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangeTypeFromCyaController @Inject() (
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              sessionRepository: SessionRepository,
                                              val controllerComponents: MessagesControllerComponents
                                            )(implicit ec: ExecutionContext)
  extends FrontendBaseController {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      for {
        ua1 <- Future.fromTry(request.userAnswers.set(ChangingTypeFromCyaPage, true))
        ua2 <- Future.fromTry(ua1.set(HasSwitchedTypeFromCyaPage, false))
        _   <- sessionRepository.set(ua2)
      } yield Redirect(controllers.add.routes.TypeOfSubcontractorController.onPageLoad(CheckMode))
    }
}
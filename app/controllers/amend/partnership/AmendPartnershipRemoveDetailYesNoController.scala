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

package controllers.amend.partnership

import controllers.actions.*
import forms.amend.partnership.AmendPartnershipRemoveDetailYesNoFormProvider
import models.UserAnswers
import pages.add.partnership.*
import pages.amend.partnership.AmendPartnershipRemoveDetailYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.amend.partnership.AmendPartnershipRemoveDetailYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendPartnershipRemoveDetailYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AmendPartnershipRemoveDetailYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AmendPartnershipRemoveDetailYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val detailTitles: Map[String, String] = Map(
    "address"                                       -> "address",
    "contact-details"                               -> "contact details",
    "utr"                                           -> "UTR",
    "works-reference-number"                        -> "works reference number",
    "nominated-partner-utr"                         -> "nominated partner's UTR",
    "nominated-partner-nino"                        -> "nominated partner's National Insurance number",
    "nominated-partner-company-registration-number" ->
      "nominated partner's company registration number"
  )

  private def withValidDetail(
    detail: String
  )(action: => Future[Result]): Future[Result] =
    if (detailTitles.contains(detail)) {
      action
    } else {
      Future.successful(
        Redirect(
          controllers.routes.JourneyRecoveryController.onPageLoad()
        )
      )
    }

  private def getPartnershipName(
    userAnswers: UserAnswers
  ): Option[String] =
    userAnswers.get(PartnershipNamePage)

  private def getNominatedPartnerName(
    userAnswers: UserAnswers
  ): Option[String] =
    userAnswers.get(PartnershipNominatedPartnerNamePage)

  private def getDetailName(
    detail: String,
    userAnswers: UserAnswers
  ): Option[String] =
    detail match {

      case "nominated-partner-utr" | "nominated-partner-nino" | "nominated-partner-company-registration-number" =>
        getNominatedPartnerName(userAnswers)

      case _ =>
        getPartnershipName(userAnswers)
    }

  private def detailIsPresent(
    detail: String,
    userAnswers: UserAnswers
  ): Boolean =
    detail match {

      case "address" =>
        userAnswers
          .get(PartnershipAddressYesNoPage)
          .contains(true)

      case "contact-details" =>
        userAnswers
          .get(AddPartnershipContactMethodsYesNoPage)
          .contains(true)

      case "utr" =>
        userAnswers
          .get(PartnershipHasUtrYesNoPage)
          .contains(true)

      case "works-reference-number" =>
        userAnswers
          .get(PartnershipWorksReferenceNumberYesNoPage)
          .contains(true)

      case "nominated-partner-utr" =>
        userAnswers
          .get(PartnershipNominatedPartnerUtrYesNoPage)
          .contains(true)

      case "nominated-partner-nino" =>
        userAnswers
          .get(PartnershipNominatedPartnerNinoYesNoPage)
          .contains(true)

      case "nominated-partner-company-registration-number" =>
        userAnswers
          .get(PartnershipNominatedPartnerCrnYesNoPage)
          .contains(true)

      case _ =>
        false
    }

  def onPageLoad(
    detail: String
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      withValidDetail(detail) {

        getDetailName(detail, request.userAnswers) match {

          case Some(detailName) if detailIsPresent(detail, request.userAnswers) =>
            Future.successful(
              Ok(
                view(
                  formProvider(),
                  detail,
                  detailTitles(detail),
                  detailName
                )
              )
            )

          case _ =>
            Future.successful(
              Redirect(
                controllers.routes.JourneyRecoveryController.onPageLoad()
              )
            )
        }
      }
    }

  def onSubmit(
    detail: String
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      withValidDetail(detail) {

        getDetailName(detail, request.userAnswers) match {

          case Some(detailName) if detailIsPresent(detail, request.userAnswers) =>
            val detailTitle = detailTitles(detail)

            formProvider()
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(
                        formWithErrors,
                        detail,
                        detailTitle,
                        detailName
                      )
                    )
                  ),
                value => {

                  val page =
                    AmendPartnershipRemoveDetailYesNoPage(detail)

                  val updatedAnswers =
                    request.userAnswers.set(page, value)

                  updatedAnswers match {

                    case scala.util.Success(answers) =>
                      sessionRepository
                        .set(answers)
                        .map { _ =>
                          Redirect(
                            controllers.add.partnership.routes.PartnershipCheckYourAnswersController
                              .onPageLoad()
                          )
                        }

                    case scala.util.Failure(_) =>
                      Future.successful(
                        Redirect(
                          controllers.routes.JourneyRecoveryController
                            .onPageLoad()
                        )
                      )
                  }
                }
              )

          case _ =>
            Future.successful(
              Redirect(
                controllers.routes.JourneyRecoveryController.onPageLoad()
              )
            )
        }
      }
    }
}

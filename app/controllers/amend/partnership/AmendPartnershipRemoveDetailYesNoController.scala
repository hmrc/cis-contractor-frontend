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
import models.amend.partnership.AmendPartnershipRemoveDetail
import pages.add.partnership.*
import pages.amend.partnership.AmendPartnershipRemoveDetailYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import play.api.Logging
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
    with I18nSupport
    with Logging {

  private def withValidDetail(
    detail: String
  )(
    action: AmendPartnershipRemoveDetail => Future[Result]
  ): Future[Result] =
    AmendPartnershipRemoveDetail.fromKey(detail) match {

      case Some(detailType) =>
        action(detailType)

      case None =>
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
    detail: AmendPartnershipRemoveDetail,
    userAnswers: UserAnswers
  ): Option[String] =
    if (detail.isNominatedPartnerDetail) {
      getNominatedPartnerName(userAnswers)
    } else {
      getPartnershipName(userAnswers)
    }

  private def detailIsPresent(
    detail: AmendPartnershipRemoveDetail,
    userAnswers: UserAnswers
  ): Boolean =
    detail match {

      case AmendPartnershipRemoveDetail.Address =>
        userAnswers
          .get(PartnershipAddressYesNoPage)
          .contains(true)

      case AmendPartnershipRemoveDetail.ContactDetails =>
        userAnswers
          .get(AddPartnershipContactMethodsYesNoPage)
          .contains(true)

      case AmendPartnershipRemoveDetail.Utr =>
        userAnswers
          .get(PartnershipHasUtrYesNoPage)
          .contains(true)

      case AmendPartnershipRemoveDetail.WorksReferenceNumber =>
        userAnswers
          .get(PartnershipWorksReferenceNumberYesNoPage)
          .contains(true)

      case AmendPartnershipRemoveDetail.NominatedPartnerUtr =>
        userAnswers
          .get(PartnershipNominatedPartnerUtrYesNoPage)
          .contains(true)

      case AmendPartnershipRemoveDetail.NominatedPartnerNino =>
        userAnswers
          .get(PartnershipNominatedPartnerNinoYesNoPage)
          .contains(true)

      case AmendPartnershipRemoveDetail.NominatedPartnerCompanyRegistrationNumber =>
        userAnswers
          .get(PartnershipNominatedPartnerCrnYesNoPage)
          .contains(true)
    }

  def onPageLoad(
    detail: String
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      withValidDetail(detail) { detailType =>
        if (!detailIsPresent(detailType, request.userAnswers)) {

          Future.successful(
            Redirect(
              controllers.routes.JourneyRecoveryController.onPageLoad()
            )
          )

        } else {

          getDetailName(detailType, request.userAnswers) match {

            case Some(detailName) =>
              val messages =
                messagesApi.preferred(request)

              val detailTitle =
                messages(detailType.messageKey)

              Future.successful(
                Ok(
                  view(
                    formProvider(),
                    detail,
                    detailTitle,
                    detailName
                  )
                )
              )

            case None =>
              Future.successful(
                Redirect(
                  controllers.routes.JourneyRecoveryController.onPageLoad()
                )
              )
          }
        }
      }
    }

  def onSubmit(
    detail: String
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      withValidDetail(detail) { detailType =>
        if (!detailIsPresent(detailType, request.userAnswers)) {

          Future.successful(
            Redirect(
              controllers.routes.JourneyRecoveryController.onPageLoad()
            )
          )

        } else {

          getDetailName(detailType, request.userAnswers) match {

            case Some(detailName) =>
              val messages =
                messagesApi.preferred(request)

              val detailTitle =
                messages(detailType.messageKey)

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

                    request.userAnswers.set(page, value) match {

                      case scala.util.Success(updatedAnswers) =>
                        sessionRepository
                          .set(updatedAnswers)
                          .map { _ =>
                            Redirect(
                              controllers.add.partnership.routes.PartnershipCheckYourAnswersController
                                .onPageLoad()
                            )
                          }

                      case scala.util.Failure(exception) =>
                        logger.error(
                          s"Failed to update user answers for remove detail '$detail'",
                          exception
                        )

                        Future.successful(
                          Redirect(
                            controllers.routes.JourneyRecoveryController
                              .onPageLoad()
                          )
                        )
                    }
                  }
                )

            case None =>
              Future.successful(
                Redirect(
                  controllers.routes.JourneyRecoveryController.onPageLoad()
                )
              )
          }
        }
      }
    }
}

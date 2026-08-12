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

package controllers.amend.company

import controllers.actions.*
import forms.amend.company.AmendCompanyRemoveDetailYesNoFormProvider
import models.UserAnswers
import models.amend.company.AmendCompanyRemoveDetail
import pages.add.company.*
import pages.amend.ShowVerificationDetailsPage
import pages.amend.company.AmendCompanyRemoveDetailYesNoPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.amend.company.AmendCompanyRemoveDetailYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendCompanyRemoveDetailYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AmendCompanyRemoveDetailYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AmendCompanyRemoveDetailYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def withValidDetail(
    detail: String
  )(
    action: AmendCompanyRemoveDetail => Future[Result]
  ): Future[Result] =
    AmendCompanyRemoveDetail.fromKey(detail) match {

      case Some(detailType) =>
        action(detailType)

      case None =>
        Future.successful(
          Redirect(
            controllers.routes.JourneyRecoveryController.onPageLoad()
          )
        )
    }

  private def detailIsPresent(
    detail: AmendCompanyRemoveDetail,
    userAnswers: UserAnswers
  ): Boolean =
    detail match {

      case AmendCompanyRemoveDetail.Address =>
        userAnswers
          .get(CompanyAddressYesNoPage)
          .contains(true)

      case AmendCompanyRemoveDetail.ContactDetails =>
        userAnswers
          .get(AddCompanyContactMethodsYesNoPage)
          .contains(true)

      case AmendCompanyRemoveDetail.Utr =>
        userAnswers
          .get(CompanyUtrYesNoPage)
          .contains(true) &&
          userAnswers
            .get(ShowVerificationDetailsPage)
            .contains(false)

      case AmendCompanyRemoveDetail.CompanyRegistrationNumber =>
        userAnswers
          .get(CompanyCrnYesNoPage)
          .contains(true)

      case AmendCompanyRemoveDetail.WorksReferenceNumber =>
        userAnswers
          .get(CompanyWorksReferenceYesNoPage)
          .contains(true)
    }

  private def journeyRecovery: Result =
    Redirect(
      controllers.routes.JourneyRecoveryController.onPageLoad()
    )

  def onPageLoad(subcontractorDetail: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(CompanyNamePage)
        .map { companyName =>
          withValidDetail(subcontractorDetail) { detailType =>
            if (!detailIsPresent(detailType, request.userAnswers)) {

              Future.successful(journeyRecovery)

            } else {
              val form = formProvider()

              val messages =
                messagesApi.preferred(request)

              val subcontractorDetailTitle =
                messages(detailType.messageKey)

              Future.successful(Ok(view(companyName, subcontractorDetail, subcontractorDetailTitle, form)))
            }
          }
        }
        .getOrElse(Future.successful(journeyRecovery))
    }

  def onSubmit(subcontractorDetail: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(CompanyNamePage)
        .map { companyName =>
          withValidDetail(subcontractorDetail) { detailType =>
            if (!detailIsPresent(detailType, request.userAnswers)) {

              Future.successful(journeyRecovery)

            } else {
              formProvider()
                .bindFromRequest()
                .fold(
                  formWithErrors =>

                    val messages = messagesApi.preferred(request)

                    val subcontractorDetailTitle = messages(detailType.messageKey)

                    Future.successful(
                      BadRequest(view(companyName, subcontractorDetail, subcontractorDetailTitle, formWithErrors))
                    )
                  ,
                  value =>
                    (for {
                      updatedAnswers <-
                        Future.fromTry(
                          request.userAnswers
                            .set(AmendCompanyRemoveDetailYesNoPage(detailType), value)
                            .flatMap(_.remove(AmendCompanyRemoveDetailYesNoPage(detailType)))
                        )
                      _              <- sessionRepository.set(updatedAnswers)
                    } yield Redirect(
                      controllers.amend.company.routes.AmendCompanyCheckYourAnswersController.onPageLoad()
                    )).recover { case ex =>
                      logger.error(
                        s"Failed to save remove detail answer for '$subcontractorDetail'",
                        ex
                      )
                      journeyRecovery
                    }
                )
            }
          }
        }
        .getOrElse(Future.successful(journeyRecovery))
  }
}

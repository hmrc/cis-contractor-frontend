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
import forms.amend.AmendIndividualRemoveDetailYesNoFormProvider
import models.{AmendMode, UserAnswers}
import models.amend.AmendIndividualRemoveDetail
import pages.add.*
import pages.amend.{AmendIndividualRemoveDetailYesNoPage, ShowVerificationDetailsPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.SubcontractorNameExtractor
import views.html.amend.AmendIndividualRemoveDetailYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendIndividualRemoveDetailYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AmendIndividualRemoveDetailYesNoFormProvider,
  subcontractorNameExtractor: SubcontractorNameExtractor,
  val controllerComponents: MessagesControllerComponents,
  view: AmendIndividualRemoveDetailYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def withValidDetail(
    detail: String
  )(
    action: AmendIndividualRemoveDetail => Future[Result]
  ): Future[Result] =
    AmendIndividualRemoveDetail.fromKey(detail) match {

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
    detail: AmendIndividualRemoveDetail,
    userAnswers: UserAnswers
  ): Boolean =
    detail match {

      case AmendIndividualRemoveDetail.TradingName =>
        userAnswers
          .get(SubTradingNameYesNoPage)
          .contains(false) &&
        userAnswers
          .get(TradingNameOfSubcontractorPage)
          .isDefined &&
        userAnswers
          .get(ShowVerificationDetailsPage)
          .contains(false)

      case AmendIndividualRemoveDetail.SubcontractorName =>
        userAnswers
          .get(SubTradingNameYesNoPage)
          .contains(true) &&
        userAnswers
          .get(SubcontractorNamePage)
          .isDefined &&
        userAnswers
          .get(ShowVerificationDetailsPage)
          .contains(false)

      case AmendIndividualRemoveDetail.Address =>
        userAnswers
          .get(SubAddressYesNoPage)
          .contains(true)

      case AmendIndividualRemoveDetail.ContactDetails =>
        userAnswers
          .get(AddIndividualContactMethodsYesNoPage)
          .contains(true)

      case AmendIndividualRemoveDetail.Utr =>
        userAnswers
          .get(UniqueTaxpayerReferenceYesNoPage)
          .contains(true) &&
        userAnswers
          .get(ShowVerificationDetailsPage)
          .contains(false)

      case AmendIndividualRemoveDetail.NationalInsuranceNumber =>
        userAnswers
          .get(NationalInsuranceNumberYesNoPage)
          .contains(true)

      case AmendIndividualRemoveDetail.WorksReferenceNumber =>
        userAnswers
          .get(WorksReferenceNumberYesNoPage)
          .contains(true)
    }

  private def journeyRecovery: Result =
    Redirect(
      controllers.routes.JourneyRecoveryController.onPageLoad()
    )

  def onPageLoad(subcontractorDetail: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers)
        .map { subcontractorName =>
          withValidDetail(subcontractorDetail) { detailType =>
            if (!detailIsPresent(detailType, request.userAnswers)) {

              Future.successful(journeyRecovery)

            } else {
              val form = formProvider()

              val messages =
                messagesApi.preferred(request)

              val subcontractorDetailTitle =
                messages(detailType.messageKey)

              Future.successful(Ok(view(subcontractorName, subcontractorDetail, subcontractorDetailTitle, form)))
            }
          }
        }
        .getOrElse(Future.successful(journeyRecovery))
    }

  def onSubmit(subcontractorDetail: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers)
        .map { subcontractorName =>
          withValidDetail(subcontractorDetail) { detailType =>
            if (!detailIsPresent(detailType, request.userAnswers)) {

              Future.successful(journeyRecovery)

            } else {
              formProvider()
                .bindFromRequest()
                .fold(
                  formWithErrors =>
                    val messages =
                      messagesApi.preferred(request)

                    val subcontractorDetailTitle =
                      messages(detailType.messageKey)

                    Future.successful(
                      BadRequest(view(subcontractorName, subcontractorDetail, subcontractorDetailTitle, formWithErrors))
                    )
                  ,
                  value =>
                    (for {
                      updatedAnswers <-
                        Future.fromTry(
                          request.userAnswers
                            .set(AmendIndividualRemoveDetailYesNoPage(detailType), value)
                            .flatMap(_.remove(AmendIndividualRemoveDetailYesNoPage(detailType)))
                        )
                      _              <- sessionRepository.set(updatedAnswers)
                    } yield
                      if (value && subcontractorDetail == "trading-name") {
                        Redirect(
                          controllers.add.routes.SubcontractorNameController.onPageLoad(AmendMode)
                        )
                      } else if (value && subcontractorDetail == "subcontractor-name") {
                        Redirect(
                          controllers.add.routes.TradingNameOfSubcontractorController.onPageLoad(AmendMode)
                        )
                      } else {
                        Redirect(
                          controllers.amend.routes.AmendIndividualCheckYourAnswersController.onPageLoad()
                        )
                      }).recover { case ex =>
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

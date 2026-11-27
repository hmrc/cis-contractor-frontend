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
import models.add.IndividualNamesOptions.{SubcontractorName, TradingName}
import models.amend.AmendIndividualRemoveDetail
import models.{AmendMode, UserAnswers}
import pages.add.*
import pages.amend.AmendIndividualRemoveDetailYesNoPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.SubcontractorNameExtractor
import views.html.amend.AmendIndividualRemoveDetailYesNoView

import javax.inject.Inject
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AmendIndividualRemoveDetailYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  multiTabAction: MultiTabAction,
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
        logger.error(s"[AmendIndividualRemoveDetailYesNoController][withValidDetail] - invalid detail=$detail")
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
          .get(IndividualNamesOptionsPage)
          .exists(_.contains(SubcontractorName)) &&
        userAnswers
          .get(SubcontractorNamePage)
          .isDefined &&
        userAnswers
          .get(TradingNameOfSubcontractorPage)
          .isDefined &&
        !AmendControllerUtils.isVerifiedForAmendJourney(userAnswers)

      case AmendIndividualRemoveDetail.SubcontractorName =>
        userAnswers
          .get(IndividualNamesOptionsPage)
          .exists(_.contains(TradingName)) &&
        userAnswers
          .get(SubcontractorNamePage)
          .isDefined &&
        userAnswers
          .get(TradingNameOfSubcontractorPage)
          .isDefined &&
        !AmendControllerUtils.isVerifiedForAmendJourney(userAnswers)

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
        !AmendControllerUtils.isVerifiedForAmendJourney(userAnswers)

      case AmendIndividualRemoveDetail.NationalInsuranceNumber =>
        userAnswers
          .get(NationalInsuranceNumberYesNoPage)
          .contains(true)

      case AmendIndividualRemoveDetail.WorksReferenceNumber =>
        userAnswers
          .get(WorksReferenceNumberYesNoPage)
          .contains(true)
    }

  private def detailIsPresentWithId(
    flowId: UUID,
    detail: AmendIndividualRemoveDetail,
    userAnswers: UserAnswers
  ): Boolean = {
    val subJourneyId = flowId.toString

    detail match {
      case AmendIndividualRemoveDetail.TradingName =>
        userAnswers
          .get(IndividualNamesOptionsPage(subJourneyId))
          .exists(_.contains(SubcontractorName)) &&
        userAnswers
          .get(SubcontractorNamePage(subJourneyId))
          .isDefined &&
        userAnswers
          .get(TradingNameOfSubcontractorPage(subJourneyId))
          .isDefined &&
        !AmendControllerUtils.isVerifiedForAmendJourney(userAnswers)

      case AmendIndividualRemoveDetail.SubcontractorName =>
        userAnswers
          .get(IndividualNamesOptionsPage(subJourneyId))
          .exists(_.contains(TradingName)) &&
        userAnswers
          .get(SubcontractorNamePage(subJourneyId))
          .isDefined &&
        userAnswers
          .get(TradingNameOfSubcontractorPage(subJourneyId))
          .isDefined &&
        !AmendControllerUtils.isVerifiedForAmendJourney(userAnswers)

      case AmendIndividualRemoveDetail.Address =>
        userAnswers
          .get(SubAddressYesNoPage(subJourneyId))
          .contains(true)

      case AmendIndividualRemoveDetail.ContactDetails =>
        userAnswers
          .get(AddIndividualContactMethodsYesNoPage(subJourneyId))
          .contains(true)

      case AmendIndividualRemoveDetail.Utr =>
        userAnswers
          .get(UniqueTaxpayerReferenceYesNoPage(subJourneyId))
          .contains(true) &&
        !AmendControllerUtils.isVerifiedForAmendJourney(userAnswers)

      case AmendIndividualRemoveDetail.NationalInsuranceNumber =>
        userAnswers
          .get(NationalInsuranceNumberYesNoPage(subJourneyId))
          .contains(true)

      case AmendIndividualRemoveDetail.WorksReferenceNumber =>
        userAnswers
          .get(WorksReferenceNumberYesNoPage(subJourneyId))
          .contains(true)
    }
  }

  private def journeyRecovery: Result =
    Redirect(
      controllers.routes.JourneyRecoveryController.onPageLoad()
    )

  def onPageLoad(subcontractorDetail: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers, AmendMode)
        .map { subcontractorName =>
          withValidDetail(subcontractorDetail) { detailType =>
            if (!detailIsPresent(detailType, request.userAnswers)) {
              logger.error(
                s"[AmendIndividualRemoveDetailYesNoController][onPageLoad] - detail missing: $subcontractorDetail"
              )

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
        .getOrElse {
          logger.error("[AmendIndividualRemoveDetailYesNoController][onPageLoad] - name missing from userAnswers")
          Future.successful(journeyRecovery)
        }
    }

  def onPageLoadWithId(flowId: UUID, subcontractorDetail: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val formAction = Some(
        controllers.amend.routes.AmendIndividualRemoveDetailYesNoController.onSubmitWithId(flowId, subcontractorDetail)
      )

      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers, AmendMode)
        .map { subcontractorName =>
          withValidDetail(subcontractorDetail) { detailType =>
            if (!detailIsPresentWithId(flowId, detailType, request.userAnswers)) {
              logger.error(
                s"[AmendIndividualRemoveDetailYesNoController][onPageLoadWithId] - detail missing: $subcontractorDetail"
              )

              Future.successful(journeyRecovery)

            } else {
              val form = formProvider()

              val messages =
                messagesApi.preferred(request)

              val subcontractorDetailTitle =
                messages(detailType.messageKey)

              Future.successful(
                Ok(view(subcontractorName, subcontractorDetail, subcontractorDetailTitle, form, formAction))
              )
            }
          }
        }
        .getOrElse {
          logger.error("[AmendIndividualRemoveDetailYesNoController][onPageLoadWithId] - name missing from userAnswers")
          Future.successful(journeyRecovery)
        }
    }

  def onSubmit(subcontractorDetail: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers, AmendMode)
        .map { subcontractorName =>
          withValidDetail(subcontractorDetail) { detailType =>
            if (!detailIsPresent(detailType, request.userAnswers)) {
              logger.error(
                s"[AmendIndividualRemoveDetailYesNoController][onSubmit] - detail missing: $subcontractorDetail"
              )

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
                    } yield Redirect(controllers.amend.routes.AmendIndividualCheckYourAnswersController.onPageLoad()))
                      .recover { case ex =>
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
        .getOrElse {
          logger.error("[AmendIndividualRemoveDetailYesNoController][onSubmit] - name missing from userAnswers")
          Future.successful(journeyRecovery)
        }
  }

  def onSubmitWithId(flowId: UUID, subcontractorDetail: String): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val formAction = Some(
        controllers.amend.routes.AmendIndividualRemoveDetailYesNoController.onSubmitWithId(flowId, subcontractorDetail)
      )

      subcontractorNameExtractor
        .getSubcontractorName(request.userAnswers, AmendMode)
        .map { subcontractorName =>
          withValidDetail(subcontractorDetail) { detailType =>
            if (!detailIsPresentWithId(flowId, detailType, request.userAnswers)) {
              logger.error(
                s"[AmendIndividualRemoveDetailYesNoController][onSubmitWithId] - detail missing: $subcontractorDetail"
              )

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
                      BadRequest(
                        view(
                          subcontractorName,
                          subcontractorDetail,
                          subcontractorDetailTitle,
                          formWithErrors,
                          formAction
                        )
                      )
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
                    } yield Redirect(
                      controllers.amend.routes.AmendIndividualCheckYourAnswersController.onPageLoadWithId(flowId)
                    ))
                      .recover { case ex =>
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
        .getOrElse {
          logger.error("[AmendIndividualRemoveDetailYesNoController][onSubmitWithId] - name missing from userAnswers")
          Future.successful(journeyRecovery)
        }
    }
}

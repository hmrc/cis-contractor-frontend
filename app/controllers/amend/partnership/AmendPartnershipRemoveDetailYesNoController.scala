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
import controllers.amend.AmendControllerUtils
import controllers.helpers.SubcontractorNameDisplayHelper
import forms.amend.partnership.AmendPartnershipRemoveDetailYesNoFormProvider
import models.amend.partnership.AmendPartnershipRemoveDetail
import models.requests.DataRequest
import models.{AmendMode, UserAnswers}
import pages.add.partnership.*
import pages.amend.partnership.AmendPartnershipRemoveDetailYesNoPage
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.amend.partnership.AmendPartnershipRemoveDetailYesNoView

import javax.inject.Inject
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AmendPartnershipRemoveDetailYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  multiTabAction: MultiTabAction,
  requireData: DataRequiredAction,
  formProvider: AmendPartnershipRemoveDetailYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AmendPartnershipRemoveDetailYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def journeyRecovery: Result =
    Redirect(
      controllers.routes.JourneyRecoveryController.onPageLoad()
    )

  private def withValidDetail(
    subcontractorDetail: String
  )(
    action: AmendPartnershipRemoveDetail => Future[Result]
  ): Future[Result] =
    AmendPartnershipRemoveDetail.fromKey(subcontractorDetail) match {

      case Some(detailType) =>
        action(detailType)

      case None =>
        logger.error(
          s"[AmendPartnershipRemoveDetailYesNoController][withValidDetail] - invalid detail=$subcontractorDetail"
        )
        Future.successful(journeyRecovery)
    }

  private def getPartnershipName(
    userAnswers: UserAnswers,
    subJourneyId: Option[String] = None
  )(implicit messages: Messages): Option[String] =
    subJourneyId match {
      case Some(id) => SubcontractorNameDisplayHelper.getPartnershipDisplayName(userAnswers, AmendMode, id)
      case None     => SubcontractorNameDisplayHelper.getPartnershipDisplayName(userAnswers, AmendMode)
    }

  private def getNominatedPartnerName(
    userAnswers: UserAnswers,
    subJourneyId: Option[String] = None
  )(implicit messages: Messages): Option[String] =
    subJourneyId match {
      case Some(id) => SubcontractorNameDisplayHelper.getPartnerDisplayName(userAnswers, AmendMode, id)
      case None     => SubcontractorNameDisplayHelper.getPartnerDisplayName(userAnswers, AmendMode)
    }

  private def getDetailName(
    subcontractorDetail: AmendPartnershipRemoveDetail,
    userAnswers: UserAnswers,
    subJourneyId: Option[String] = None
  )(implicit messages: Messages): Option[String] =
    subJourneyId match {
      case Some(id) =>
        if (subcontractorDetail.isNominatedPartnerDetail) {
          getNominatedPartnerName(userAnswers, subJourneyId)
        } else {
          getPartnershipName(userAnswers, subJourneyId)
        }
      case None     =>
        if (subcontractorDetail.isNominatedPartnerDetail) {
          getNominatedPartnerName(userAnswers)
        } else {
          getPartnershipName(userAnswers)
        }
    }

  private def detailIsPresent(
    subcontractorDetail: AmendPartnershipRemoveDetail,
    userAnswers: UserAnswers
  ): Boolean =
    subcontractorDetail match {

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
          .contains(true) &&
        !AmendControllerUtils.isVerifiedForAmendJourney(userAnswers)

      case AmendPartnershipRemoveDetail.WorksReferenceNumber =>
        userAnswers
          .get(PartnershipWorksReferenceNumberYesNoPage)
          .contains(true)

      case AmendPartnershipRemoveDetail.NominatedPartnerUtr =>
        userAnswers
          .get(PartnershipNominatedPartnerUtrYesNoPage)
          .contains(true) &&
        !AmendControllerUtils.isVerifiedForAmendJourney(userAnswers)

      case AmendPartnershipRemoveDetail.NominatedPartnerNino =>
        userAnswers
          .get(PartnershipNominatedPartnerNinoYesNoPage)
          .contains(true)

      case AmendPartnershipRemoveDetail.NominatedPartnerCompanyRegistrationNumber =>
        userAnswers
          .get(PartnershipNominatedPartnerCrnYesNoPage)
          .contains(true)
    }

  private def detailIsPresentWithId(
    flowId: UUID,
    subcontractorDetail: AmendPartnershipRemoveDetail,
    userAnswers: UserAnswers
  ): Boolean = {
    val subJourneyId = flowId.toString

    subcontractorDetail match {
      case AmendPartnershipRemoveDetail.Address =>
        userAnswers
          .get(PartnershipAddressYesNoPage(subJourneyId))
          .contains(true)

      case AmendPartnershipRemoveDetail.ContactDetails =>
        userAnswers
          .get(AddPartnershipContactMethodsYesNoPage(subJourneyId))
          .contains(true)

      case AmendPartnershipRemoveDetail.Utr =>
        userAnswers
          .get(PartnershipHasUtrYesNoPage(subJourneyId))
          .contains(true) &&
        !AmendControllerUtils.isVerifiedForAmendJourney(userAnswers, subJourneyId)

      case AmendPartnershipRemoveDetail.WorksReferenceNumber =>
        userAnswers
          .get(PartnershipWorksReferenceNumberYesNoPage(subJourneyId))
          .contains(true)

      case AmendPartnershipRemoveDetail.NominatedPartnerUtr =>
        userAnswers
          .get(PartnershipNominatedPartnerUtrYesNoPage(subJourneyId))
          .contains(true) &&
        !AmendControllerUtils.isVerifiedForAmendJourney(userAnswers, subJourneyId)

      case AmendPartnershipRemoveDetail.NominatedPartnerNino =>
        userAnswers
          .get(PartnershipNominatedPartnerNinoYesNoPage(subJourneyId))
          .contains(true)

      case AmendPartnershipRemoveDetail.NominatedPartnerCompanyRegistrationNumber =>
        userAnswers
          .get(PartnershipNominatedPartnerCrnYesNoPage(subJourneyId))
          .contains(true)
    }
  }

  private def withDetailContext(
    subcontractorDetail: String,
    subJourneyId: Option[String] = None
  )(
    block: (String, String) => Future[Result]
  )(implicit request: DataRequest[_], messages: Messages): Future[Result] =
    withValidDetail(subcontractorDetail) { detailType =>
      if (!detailIsPresent(detailType, request.userAnswers)) {
        logger.error(
          s"[AmendPartnershipRemoveDetailYesNoController][withDetailContext] - detail missing: $subcontractorDetail"
        )
        Future.successful(journeyRecovery)
      } else {

        getDetailName(detailType, request.userAnswers, subJourneyId) match {

          case Some(detailName) =>
            block(
              messagesApi.preferred(request)(detailType.messageKey),
              detailName
            )

          case None =>
            logger.error(
              "[AmendPartnershipRemoveDetailYesNoController][withDetailContext] - name missing from userAnswers"
            )
            Future.successful(journeyRecovery)
        }
      }
    }

  def onPageLoad(
    subcontractorDetail: String
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      withDetailContext(subcontractorDetail) { (detailTitle, detailName) =>
        Future.successful(
          Ok(
            view(
              formProvider(),
              subcontractorDetail,
              detailTitle,
              detailName
            )
          )
        )
      }
    }

  def onPageLoadWithId(
    flowId: UUID,
    subcontractorDetail: String
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      val subJourneyId = flowId.toString
      val formAction   = Some(
        controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
          .onSubmitWithId(flowId, subcontractorDetail)
      )

      withDetailContext(subcontractorDetail, Some(subJourneyId)) { (detailTitle, detailName) =>
        Future.successful(
          Ok(
            view(
              formProvider(),
              subcontractorDetail,
              detailTitle,
              detailName,
              formAction
            )
          )
        )
      }
    }

  def onSubmit(
    subcontractorDetail: String
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      withDetailContext(subcontractorDetail) { (detailTitle, detailName) =>
        formProvider()
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(
                  view(
                    formWithErrors,
                    subcontractorDetail,
                    detailTitle,
                    detailName
                  )
                )
              ),
            value =>
              (for {
                updatedAnswers <-
                  Future.fromTry(
                    request.userAnswers.set(
                      AmendPartnershipRemoveDetailYesNoPage(subcontractorDetail),
                      value
                    )
                  )

                _ <- sessionRepository.set(updatedAnswers)

              } yield Redirect(
                controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onPageLoad()
              )).recover { case ex =>
                logger.error(
                  s"Failed to save remove subcontractorDetail answer for '$subcontractorDetail'",
                  ex
                )

                Redirect(
                  controllers.routes.JourneyRecoveryController
                    .onPageLoad()
                )
              }
          )
      }
    }

  def onSubmitWithId(
    flowId: UUID,
    subcontractorDetail: String
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen multiTabAction.forJourney(flowId)).async { implicit request =>
      withDetailContext(subcontractorDetail) { (detailTitle, detailName) =>
        val subJourneyId = flowId.toString
        val formAction   = Some(
          controllers.amend.partnership.routes.AmendPartnershipRemoveDetailYesNoController
            .onSubmitWithId(flowId, subcontractorDetail)
        )

        formProvider()
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(
                  view(
                    formWithErrors,
                    subcontractorDetail,
                    detailTitle,
                    detailName,
                    formAction
                  )
                )
              ),
            value =>
              (for {
                updatedAnswers <-
                  Future.fromTry(
                    request.userAnswers.set(
                      AmendPartnershipRemoveDetailYesNoPage(subcontractorDetail, Some(subJourneyId)),
                      value
                    )
                  )

                _ <- sessionRepository.set(updatedAnswers)

              } yield Redirect(
                controllers.amend.partnership.routes.AmendPartnershipCheckYourAnswersController.onPageLoadWithId(flowId)
              )).recover { case ex =>
                logger.error(
                  s"Failed to save remove subcontractorDetail answer for '$subcontractorDetail'",
                  ex
                )

                Redirect(
                  controllers.routes.JourneyRecoveryController
                    .onPageLoad()
                )
              }
          )
      }
    }
}

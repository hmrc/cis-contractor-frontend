package controllers

import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import pages.CisIdPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.*
import uk.gov.hmrc.play.bootstrap.binders.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{JourneyRecoveryContinueView, JourneyRecoveryStartAgainView}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class JourneyRecoveryController @Inject() (
                                            val controllerComponents: MessagesControllerComponents,
                                            identify: IdentifierAction,
                                            sessionRepository: SessionRepository,
                                            continueView: JourneyRecoveryContinueView,
                                            startAgainView: JourneyRecoveryStartAgainView
                                          )(implicit appConfig: FrontendAppConfig, ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(continueUrl: Option[RedirectUrl] = None): Action[AnyContent] = identify.async {
    implicit request =>

      sessionRepository
        .get(request.userId)
        .recover { case _ => None }
        .map { maybeAnswers =>

          val cisAccountUrl =
            if (!request.isAgent) {
              appConfig.constructionIndustryOrgAccountUrl
            } else {
              maybeAnswers
                .flatMap(_.get(CisIdPage))
                .fold(appConfig.constructionIndustryAgentAccountUrl)(cisId =>
                  s"${appConfig.constructionIndustryAgentAccountUrl}$cisId"
                )
            }

          val safeUrl: Option[String] = continueUrl.flatMap { unsafeUrl =>
            unsafeUrl.getEither(OnlyRelative) match {
              case Right(safeUrl) =>
                Some(safeUrl.url)
              case Left(message)  =>
                logger.info(message)
                None
            }
          }

          safeUrl
            .map(url => Ok(continueView(url)))
            .getOrElse(Ok(startAgainView(cisAccountUrl)))
        }
  }
}
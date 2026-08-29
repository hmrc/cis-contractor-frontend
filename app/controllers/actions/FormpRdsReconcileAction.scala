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

package controllers.actions

import connectors.ConstructionIndustrySchemeConnector
import models.requests.DataRequest
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, PRECONDITION_FAILED}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import queries.CisIdQuery
import services.CisManageService
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

// F1 - runs the FORMP vs RDS DataCache comparison/update before a submission is sent to ChRIS.
class FormpRdsReconcileActionImpl @Inject() (
  cisConnector: ConstructionIndustrySchemeConnector,
  cisManageService: CisManageService
)(implicit val executionContext: ExecutionContext)
    extends FormpRdsReconcileAction
    with Logging {

  override protected def filter[A](request: DataRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    request.userAnswers.get(CisIdQuery) match {
      case None =>
        logger.warn("[FormpRdsReconcileAction] Missing cisId; cannot run FORMP-RDS comparison")
        Future.successful(Some(unauthorisedRedirect(request.isAgent)))

      case Some(cisId) =>
        resolveTaxOffice(request).flatMap {
          case None =>
            logger.warn(s"[FormpRdsReconcileAction] Missing tax office details for cisId=$cisId")
            Future.successful(Some(unauthorisedRedirect(request.isAgent)))

          case Some((taxOfficeNumber, taxOfficeReference)) =>
            cisConnector
              .prepopulateContractorKnownFacts(cisId, taxOfficeNumber, taxOfficeReference)
              .map(_ => None)
              .recover {
                case u: UpstreamErrorResponse if u.statusCode == PRECONDITION_FAILED || u.statusCode == NOT_FOUND =>
                  logger.warn(
                    s"[FormpRdsReconcileAction] Contractor data missing for cisId=$cisId (status=${u.statusCode})"
                  )
                  Some(unauthorisedRedirect(request.isAgent))

                case NonFatal(e) =>
                  logger.error(s"[FormpRdsReconcileAction] FORMP-RDS comparison failed for cisId=$cisId", e)
                  Some(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
              }
        }
    }
  }

  private def resolveTaxOffice[A](
    request: DataRequest[A]
  )(implicit hc: HeaderCarrier): Future[Option[(String, String)]] =
    request.employerReference match {
      case Some(ref)               =>
        Future.successful(Some((ref.taxOfficeNumber, ref.taxOfficeReference)))
      case None if request.isAgent =>
        cisManageService.getAgentClient(request.userId).map(_.map(c => (c.taxOfficeNumber, c.taxOfficeReference)))
      case None                    =>
        Future.successful(None)
    }

  private def unauthorisedRedirect(isAgent: Boolean): Result =
    if (isAgent) Redirect(controllers.routes.UnauthorisedAgentAffinityController.onPageLoad())
    else Redirect(controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad())
}

trait FormpRdsReconcileAction extends ActionFilter[DataRequest]

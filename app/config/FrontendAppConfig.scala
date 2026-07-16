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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import scala.io.Source
import play.api.libs.json.Json

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  lazy val host: String       = configuration.get[String]("host")
  lazy val appName: String    = configuration.get[String]("appName")
  lazy val cisStubUrl: String = baseUrl("construction-industry-scheme-external-stub")

  private lazy val contactHost             = configuration.get[String]("contact-frontend.host")
  val contactFormServiceIdentifier: String = configuration.get[String]("contact-frontend.serviceId")

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  protected lazy val rootServices = "microservice.services"

  protected lazy val defaultProtocol: String =
    configuration
      .getOptional[String](s"$rootServices.protocol")
      .getOrElse("http")

  private def throwConfigNotFoundError(key: String): Nothing =
    throw new RuntimeException(s"Could not find config key '$key'")

  def getConfString(confKey: String, defString: => String): String =
    configuration
      .getOptional[String](s"$rootServices.$confKey")
      .getOrElse(defString)

  def getConfInt(confKey: String, defInt: => Int): Int =
    configuration
      .getOptional[Int](s"$rootServices.$confKey")
      .getOrElse(defInt)

  def baseUrl(serviceName: String): String = {
    val protocol = getConfString(s"$serviceName.protocol", defaultProtocol)
    val host     = getConfString(s"$serviceName.host", throwConfigNotFoundError(s"$serviceName.host"))
    val port     = getConfInt(s"$serviceName.port", throwConfigNotFoundError(s"$serviceName.port"))
    s"$protocol://$host:$port"
  }

  def manageYourSubcontractorsUrl(cisId: String): String =
    s"${configuration.get[String]("urls.manageBaseUrl")}/subcontractors/$cisId/your-subcontractors"

  lazy val loginUrl: String                      = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String              = configuration.get[String]("urls.loginContinue")
  lazy val signOutUrl: String                    = configuration.get[String]("urls.signOut")
  lazy val govUkCISGuidanceUrl: String           = configuration.get[String]("urls.govUkCISGuidance")
  lazy val manageSubcontractorsUrl: String       = configuration.get[String]("urls.manageSubcontractors")
  lazy val hmrcOnlineServiceDeskUrl: String      = configuration.get[String]("urls.hmrcOnlineServiceDesk")
  lazy val cisGeneralEnquiries: String           = configuration.get[String]("urls.cisGeneralEnquiries")
  lazy val payeCisForAgentsOnlineService: String = configuration.get[String]("urls.payeCisForAgentsOnlineService")
  lazy val cisReturnDashboardUrl: String         = configuration.get[String]("urls.cisReturnDashboard")
  lazy val findUtr: String                       = configuration.get[String]("urls.findUtr")
  lazy val managefrontendBaseUrl: String         = configuration.get[String]("urls.manageFrontendBaseUrl")
  lazy val verificationHistoryUrl: String        = s"$managefrontendBaseUrl/verification-history/retrieve"

  private val exitSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  lazy val exitSurveyUrl: String        = s"$exitSurveyBaseUrl/feedback/cis-contractor-frontend"

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  lazy val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  lazy val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  lazy val cacheTtl: Long = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  lazy val addressLookupFrontendUrl: String = baseUrl("address-lookup-frontend")

  def addressLookupRetrievalUrl(id: String): String = s"$addressLookupFrontendUrl/api/v2/confirmed?id=$id"
  def addressLookupJourneyUrl: String               = s"$addressLookupFrontendUrl/api/v2/init"

  lazy val submissionPollTimeoutSeconds: Int         = configuration.get[Int]("submission-poll-timeout-seconds")
  lazy val submissionPollDefaultIntervalSeconds: Int =
    configuration.get[Int]("submission-poll-default-interval-seconds")

  lazy val locationCanonicalList: Seq[(String, String)] = {
    val source     = Source.fromResource("location-autocomplete-canonical-list.json")
    val jsonString =
      try source.mkString
      finally source.close()
    val json       = Json.parse(jsonString)

    json.as[Seq[Seq[String]]].map {
      case Seq(name, code) => (name, code)
      case other           => throw new RuntimeException(s"Unexpected format in JSON: $other")
    }
  }
}

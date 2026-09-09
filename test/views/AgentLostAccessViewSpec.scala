package views

import base.SpecBase
import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.AgentLostAccessView

class AgentLostAccessViewSpec extends SpecBase {

  "AgentLostAccessView" - {

    "must render the page with the expected content and links" in {

      val application: Application =
        applicationBuilder().build()

      running(application) {

        implicit val request   = FakeRequest()
        implicit val appConfig = applicationConfig
        implicit val msgs      = messages(application)

        val view =
          application.injector.instanceOf[AgentLostAccessView]

        val authoriseClientRequestUrl =
          "https://example.com/authorise-client"

        val html =
          view(authoriseClientRequestUrl).toString

        html must include(msgs("agent.agentLostAccess.heading"))
        html must include(msgs("agent.agentLostAccess.p1"))
        html must include(msgs("agent.agentLostAccess.h2"))

        html must include(authoriseClientRequestUrl)
        html must include(appConfig.taxAgentsAndAdvisorsAuthorisationFormsUrl)
        html must include(appConfig.clientListSearchUrl)
      }
    }
  }
}

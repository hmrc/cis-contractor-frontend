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

package views.finalvalidations

import base.SpecBase
import models.finalvalidation.{ReviewSubcontractorDetailsPageModel, ReviewSubcontractorDetailsRow}
import play.api.test.CSRFTokenHelper
import play.api.test.FakeRequest
import views.html.finalvalidations.ReviewSubcontractorDetailsView

class ReviewSubcontractorDetailsViewSpec extends SpecBase {

  "ReviewSubcontractorDetailsView" - {

    "must render subcontractors and the verify action when the user can continue" in {

      val view =
        app.injector.instanceOf[ReviewSubcontractorDetailsView]

      val model =
        ReviewSubcontractorDetailsPageModel(
          subcontractors = Seq(
            ReviewSubcontractorDetailsRow(
              subcontractorId = 1L,
              name = "John Smith",
              hasErrors = true
            ),
            ReviewSubcontractorDetailsRow(
              subcontractorId = 2L,
              name = "Smith Ltd",
              hasErrors = false
            )
          ),
          canContinue = true,
          backUrl = "/back"
        )

      val request =
        CSRFTokenHelper.addCSRFToken(
          FakeRequest()
        )

      val result =
        view(model)(
          request,
          messages(app)
        ).toString

      result must include("John Smith")
      result must include("Smith Ltd")

      result must include(
        controllers.finalvalidations.routes.UpdateSubcontractorDetailsController
          .onPageLoad(1L)
          .url
      )

      result must include(
        controllers.finalvalidations.routes.ReviewSubcontractorDetailsController.onSubmit.url
      )

      result must include("""href="/back"""")
      result must include("app-task-list-row-submit__button")
    }

    "must render the cannot continue status when the user cannot continue" in {

      val view =
        app.injector.instanceOf[ReviewSubcontractorDetailsView]

      val model =
        ReviewSubcontractorDetailsPageModel(
          subcontractors = Seq(
            ReviewSubcontractorDetailsRow(
              subcontractorId = 1L,
              name = "John Smith",
              hasErrors = true
            )
          ),
          canContinue = false,
          backUrl = "/back"
        )

      val request =
        CSRFTokenHelper.addCSRFToken(
          FakeRequest()
        )

      val result =
        view(model)(
          request,
          messages(app)
        ).toString

      result must include("John Smith")
      result must include("govuk-task-list__status--cannot-start-yet")
      result must not include "app-task-list-row-submit__button"
    }
  }
}

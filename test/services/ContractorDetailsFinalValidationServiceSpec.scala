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

package services

import base.SpecBase
import forms.contractordetails.{ContractorUtrFormProvider, EnterContractorEmailAddressFormProvider, SchemeNameFormProvider}
import models.Scheme
import models.contractordetails.ContractorDetailsValidationTarget
import models.requests.{UpdateSchemeRequest, UpdateSchemeVersionRequest}
import models.response.UpdateSchemeVersionResponse
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.contractordetails.{AddEmailAddressYesNoPage, AddSchemeNameYesNoPage, ContractorSchemePage, ContractorUtrPage, EnterContractorEmailAddressPage, SchemeNamePage}
import queries.CisIdQuery
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class ContractorDetailsFinalValidationServiceSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.global

  private val scheme =
    Scheme(
      schemeId = 123,
      instanceId = "cisId",
      accountsOfficeReference = "123 PA 87654321",
      taxOfficeNumber = "123",
      taxOfficeReference = "45678",
      utr = Some("5860920998"),
      name = Some("Test Scheme"),
      emailAddress = Some("test@example.com"),
      prePopCount = Some(7),
      prePopSuccessful = Some("Y"),
      version = Some(3)
    )

  private def service(
    contractorDetailsService: ContractorDetailsService,
    sessionRepository: SessionRepository
  ) =
    new ContractorDetailsFinalValidationService(
      contractorDetailsService,
      sessionRepository,
      new ContractorUtrFormProvider(),
      new SchemeNameFormProvider(),
      new EnterContractorEmailAddressFormProvider()
    )

  "refreshAndValidate" - {

    "must get the scheme, store contractor details and return complete validation when details are valid" in {
      val mockContractorDetailsService = mock[ContractorDetailsService]
      val mockSessionRepository        = mock[SessionRepository]

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, "cisId")
          .success
          .value

      when(mockContractorDetailsService.getScheme(eqTo("cisId"))(any[HeaderCarrier]))
        .thenReturn(Future.successful(scheme))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val (updatedAnswers, validation) =
        service(mockContractorDetailsService, mockSessionRepository)
          .refreshAndValidate(userAnswers, ContractorDetailsValidationTarget.FileMonthlyReturn)
          .futureValue

      updatedAnswers.get(ContractorSchemePage) mustBe Some(scheme)
      updatedAnswers.get(ContractorUtrPage) mustBe Some("5860920998")
      updatedAnswers.get(SchemeNamePage) mustBe Some("Test Scheme")
      updatedAnswers.get(EnterContractorEmailAddressPage) mustBe Some("test@example.com")
      validation.allComplete mustBe true
    }

    "must mark missing UTR and invalid optional details as incomplete" in {
      val mockContractorDetailsService = mock[ContractorDetailsService]
      val mockSessionRepository        = mock[SessionRepository]

      val invalidScheme =
        scheme.copy(
          utr = None,
          name = Some("Invalid<>Scheme"),
          emailAddress = Some("not-an-email")
        )

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, "cisId")
          .success
          .value

      when(mockContractorDetailsService.getScheme(eqTo("cisId"))(any[HeaderCarrier]))
        .thenReturn(Future.successful(invalidScheme))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val (_, validation) =
        service(mockContractorDetailsService, mockSessionRepository)
          .refreshAndValidate(userAnswers, ContractorDetailsValidationTarget.VerifySubcontractors)
          .futureValue

      validation.utrComplete mustBe false
      validation.schemeNameComplete mustBe false
      validation.emailComplete mustBe false
      validation.allComplete mustBe false
    }

    "must remove stale contractor detail answers when GetScheme returns empty values" in {
      val mockContractorDetailsService = mock[ContractorDetailsService]
      val mockSessionRepository        = mock[SessionRepository]

      val schemeWithoutOptionalDetails =
        scheme.copy(
          utr = None,
          name = None,
          emailAddress = None
        )

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery, "cisId")
          .success
          .value
          .set(ContractorUtrPage, "5860920998")
          .success
          .value
          .set(SchemeNamePage, "Stale Scheme")
          .success
          .value
          .set(EnterContractorEmailAddressPage, "stale@example.com")
          .success
          .value

      when(mockContractorDetailsService.getScheme(eqTo("cisId"))(any[HeaderCarrier]))
        .thenReturn(Future.successful(schemeWithoutOptionalDetails))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val (updatedAnswers, validation) =
        service(mockContractorDetailsService, mockSessionRepository)
          .refreshAndValidate(userAnswers, ContractorDetailsValidationTarget.ReviewUnmatchedSubcontractors)
          .futureValue

      updatedAnswers.get(ContractorUtrPage) mustBe None
      updatedAnswers.get(SchemeNamePage) mustBe None
      updatedAnswers.get(EnterContractorEmailAddressPage) mustBe None
      validation.utrComplete mustBe false
      validation.schemeNameComplete mustBe true
      validation.emailComplete mustBe true
    }
  }

  "refreshAndValidateWithId" - {

    "must get the scheme using the scoped CisId, store scoped contractor details and return complete validation" in {
      val mockContractorDetailsService = mock[ContractorDetailsService]
      val mockSessionRepository        = mock[SessionRepository]

      val flowId =
        UUID.fromString("11111111-1111-1111-1111-111111111111")

      val id = flowId.toString

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery(id), "cisId")
          .success
          .value

      when(
        mockContractorDetailsService
          .getScheme(eqTo("cisId"))(any[HeaderCarrier])
      ).thenReturn(Future.successful(scheme))

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val (updatedAnswers, validation) =
        service(mockContractorDetailsService, mockSessionRepository)
          .refreshAndValidateWithId(
            userAnswers,
            ContractorDetailsValidationTarget.FileMonthlyReturn,
            flowId
          )
          .futureValue

      updatedAnswers.get(ContractorSchemePage(id)) mustBe Some(scheme)
      updatedAnswers.get(ContractorUtrPage(id)) mustBe Some("5860920998")
      updatedAnswers.get(SchemeNamePage(id)) mustBe Some("Test Scheme")

      updatedAnswers.get(EnterContractorEmailAddressPage(id)) mustBe
        Some("test@example.com")

      updatedAnswers.get(AddSchemeNameYesNoPage(id)) mustBe Some(true)
      updatedAnswers.get(AddEmailAddressYesNoPage(id)) mustBe Some(true)

      validation.allComplete mustBe true

      verify(mockSessionRepository).set(updatedAnswers)
    }

    "must remove stale scoped contractor detail answers when GetScheme returns empty values" in {
      val mockContractorDetailsService = mock[ContractorDetailsService]
      val mockSessionRepository        = mock[SessionRepository]

      val flowId =
        UUID.fromString("11111111-1111-1111-1111-111111111111")

      val id = flowId.toString

      val schemeWithoutOptionalDetails =
        scheme.copy(
          utr = None,
          name = None,
          emailAddress = None
        )

      val userAnswers =
        emptyUserAnswers
          .set(CisIdQuery(id), "cisId")
          .success
          .value
          .set(ContractorUtrPage(id), "5860920998")
          .success
          .value
          .set(SchemeNamePage(id), "Stale Scheme")
          .success
          .value
          .set(
            EnterContractorEmailAddressPage(id),
            "stale@example.com"
          )
          .success
          .value

      when(
        mockContractorDetailsService
          .getScheme(eqTo("cisId"))(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(schemeWithoutOptionalDetails)
      )

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val (updatedAnswers, validation) =
        service(mockContractorDetailsService, mockSessionRepository)
          .refreshAndValidateWithId(
            userAnswers,
            ContractorDetailsValidationTarget.ReviewUnmatchedSubcontractors,
            flowId
          )
          .futureValue

      updatedAnswers.get(ContractorUtrPage(id)) mustBe None
      updatedAnswers.get(SchemeNamePage(id)) mustBe None
      updatedAnswers.get(EnterContractorEmailAddressPage(id)) mustBe None

      updatedAnswers.get(AddSchemeNameYesNoPage(id)) mustBe Some(false)
      updatedAnswers.get(AddEmailAddressYesNoPage(id)) mustBe Some(false)

      validation.utrComplete mustBe false
      validation.schemeNameComplete mustBe true
      validation.emailComplete mustBe true
    }

    "must fail when the scoped CisIdQuery is missing" in {
      val mockContractorDetailsService = mock[ContractorDetailsService]
      val mockSessionRepository        = mock[SessionRepository]

      val flowId =
        UUID.fromString("11111111-1111-1111-1111-111111111111")

      val exception =
        service(mockContractorDetailsService, mockSessionRepository)
          .refreshAndValidateWithId(
            emptyUserAnswers,
            ContractorDetailsValidationTarget.FileMonthlyReturn,
            flowId
          )
          .failed
          .futureValue

      exception.getMessage mustBe
        "CisIdQuery not found in contractor details tab"
    }
  }

  "validateWithId" - {

    "must validate contractor details belonging to the supplied flow id" in {
      val mockContractorDetailsService = mock[ContractorDetailsService]
      val mockSessionRepository        = mock[SessionRepository]

      val flowId =
        UUID.fromString("11111111-1111-1111-1111-111111111111")

      val id = flowId.toString

      val userAnswers =
        emptyUserAnswers
          .set(ContractorUtrPage(id), "5860920998")
          .success
          .value
          .set(SchemeNamePage(id), "Test Scheme")
          .success
          .value
          .set(
            EnterContractorEmailAddressPage(id),
            "test@example.com"
          )
          .success
          .value

      val validation =
        service(mockContractorDetailsService, mockSessionRepository)
          .validateWithId(userAnswers, flowId)

      validation.utrComplete mustBe true
      validation.schemeNameComplete mustBe true
      validation.emailComplete mustBe true
      validation.allComplete mustBe true
    }

    "must not use valid root answers when scoped answers are missing" in {
      val mockContractorDetailsService = mock[ContractorDetailsService]
      val mockSessionRepository        = mock[SessionRepository]

      val flowId =
        UUID.fromString("11111111-1111-1111-1111-111111111111")

      val userAnswers =
        emptyUserAnswers
          .set(ContractorUtrPage, "5860920998")
          .success
          .value
          .set(SchemeNamePage, "Root Scheme")
          .success
          .value
          .set(
            EnterContractorEmailAddressPage,
            "root@example.com"
          )
          .success
          .value

      val validation =
        service(mockContractorDetailsService, mockSessionRepository)
          .validateWithId(userAnswers, flowId)

      validation.utrComplete mustBe false

      validation.schemeNameComplete mustBe true
      validation.emailComplete mustBe true

      validation.allComplete mustBe false
    }
  }

  "updateSchemeFromAnswers" - {

    "must call update version number then update scheme using answers and new version" in {
      val mockContractorDetailsService = mock[ContractorDetailsService]
      val mockSessionRepository        = mock[SessionRepository]

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage, scheme)
          .success
          .value
          .set(ContractorUtrPage, "5860920998")
          .success
          .value
          .set(SchemeNamePage, "Updated Scheme")
          .success
          .value
          .set(EnterContractorEmailAddressPage, "updated@example.com")
          .success
          .value

      when(mockContractorDetailsService.updateSchemeVersion(any[UpdateSchemeVersionRequest])(any[HeaderCarrier]))
        .thenReturn(Future.successful(UpdateSchemeVersionResponse(newVersion = 4)))
      when(mockContractorDetailsService.updateScheme(any[UpdateSchemeRequest])(any[HeaderCarrier]))
        .thenReturn(Future.successful(()))

      service(mockContractorDetailsService, mockSessionRepository)
        .updateSchemeFromAnswers(userAnswers)
        .futureValue mustBe (())

      verify(mockContractorDetailsService)
        .updateSchemeVersion(eqTo(UpdateSchemeVersionRequest(currentVersion = 3, instanceId = "cisId")))(
          any[HeaderCarrier]
        )

      val requestCaptor =
        ArgumentCaptor.forClass(classOf[UpdateSchemeRequest])

      verify(mockContractorDetailsService)
        .updateScheme(requestCaptor.capture())(any[HeaderCarrier])

      requestCaptor.getValue mustBe
        UpdateSchemeRequest(
          schemeId = 123,
          instanceId = "cisId",
          taxOfficeNumber = "123",
          taxOfficeReference = "45678",
          accountsOfficeReference = "123 PA 87654321",
          prePopCount = 7,
          prePopSuccessful = "Y",
          utr = "5860920998",
          name = "Updated Scheme",
          emailAddress = "updated@example.com",
          version = 4
        )
    }
  }

  "updateSchemeFromAnswersWithId" - {

    "must update the scheme using answers belonging to the supplied flow id" in {
      val mockContractorDetailsService = mock[ContractorDetailsService]
      val mockSessionRepository        = mock[SessionRepository]

      val flowId =
        UUID.fromString("11111111-1111-1111-1111-111111111111")

      val id = flowId.toString

      val userAnswers =
        emptyUserAnswers
          .set(ContractorSchemePage(id), scheme)
          .success
          .value
          .set(ContractorUtrPage(id), "5860920998")
          .success
          .value
          .set(SchemeNamePage(id), "Updated Scheme")
          .success
          .value
          .set(
            EnterContractorEmailAddressPage(id),
            "updated@example.com"
          )
          .success
          .value

      when(
        mockContractorDetailsService.updateSchemeVersion(
          any[UpdateSchemeVersionRequest]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(
          UpdateSchemeVersionResponse(newVersion = 4)
        )
      )

      when(
        mockContractorDetailsService.updateScheme(
          any[UpdateSchemeRequest]
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(()))

      service(mockContractorDetailsService, mockSessionRepository)
        .updateSchemeFromAnswersWithId(userAnswers, flowId)
        .futureValue mustBe (())

      verify(mockContractorDetailsService)
        .updateSchemeVersion(
          eqTo(
            UpdateSchemeVersionRequest(
              currentVersion = 3,
              instanceId = "cisId"
            )
          )
        )(any[HeaderCarrier])

      val requestCaptor =
        ArgumentCaptor.forClass(classOf[UpdateSchemeRequest])

      verify(mockContractorDetailsService)
        .updateScheme(requestCaptor.capture())(
          any[HeaderCarrier]
        )

      requestCaptor.getValue mustBe
        UpdateSchemeRequest(
          schemeId = 123,
          instanceId = "cisId",
          taxOfficeNumber = "123",
          taxOfficeReference = "45678",
          accountsOfficeReference = "123 PA 87654321",
          prePopCount = 7,
          prePopSuccessful = "Y",
          utr = "5860920998",
          name = "Updated Scheme",
          emailAddress = "updated@example.com",
          version = 4
        )
    }

    "must use scoped answers instead of root answers" in {
      val mockContractorDetailsService = mock[ContractorDetailsService]
      val mockSessionRepository        = mock[SessionRepository]

      val flowId =
        UUID.fromString("11111111-1111-1111-1111-111111111111")

      val id = flowId.toString

      val userAnswers =
        emptyUserAnswers
          .set(ContractorUtrPage, "1111111111")
          .success
          .value
          .set(SchemeNamePage, "Root Scheme")
          .success
          .value
          .set(
            EnterContractorEmailAddressPage,
            "root@example.com"
          )
          .success
          .value
          .set(ContractorSchemePage(id), scheme)
          .success
          .value
          .set(ContractorUtrPage(id), "5860920998")
          .success
          .value
          .set(SchemeNamePage(id), "Scoped Scheme")
          .success
          .value
          .set(
            EnterContractorEmailAddressPage(id),
            "scoped@example.com"
          )
          .success
          .value

      when(
        mockContractorDetailsService.updateSchemeVersion(
          any[UpdateSchemeVersionRequest]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(
          UpdateSchemeVersionResponse(newVersion = 4)
        )
      )

      when(
        mockContractorDetailsService.updateScheme(
          any[UpdateSchemeRequest]
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(()))

      service(mockContractorDetailsService, mockSessionRepository)
        .updateSchemeFromAnswersWithId(userAnswers, flowId)
        .futureValue

      val requestCaptor =
        ArgumentCaptor.forClass(classOf[UpdateSchemeRequest])

      verify(mockContractorDetailsService)
        .updateScheme(requestCaptor.capture())(
          any[HeaderCarrier]
        )

      requestCaptor.getValue.utr mustBe "5860920998"
      requestCaptor.getValue.name mustBe "Scoped Scheme"

      requestCaptor.getValue.emailAddress mustBe
        "scoped@example.com"
    }
  }
}

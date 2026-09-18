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
import pages.contractordetails.{ContractorSchemePage, ContractorUtrPage, EnterContractorEmailAddressPage, SchemeNamePage}
import queries.CisIdQuery
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

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
          uniqueTaxReference = "5860920998",
          name = "Updated Scheme",
          emailAddress = "updated@example.com",
          version = 4
        )
    }
  }
}

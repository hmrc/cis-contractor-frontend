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

import forms.contractordetails.{ContractorUtrFormProvider, EnterContractorEmailAddressFormProvider, SchemeNameFormProvider}
import models.UserAnswers
import models.contractordetails.{ContractorDetailsFinalValidation, ContractorDetailsValidationTarget}
import models.requests.{UpdateSchemeRequest, UpdateSchemeVersionRequest}
import pages.QuestionPage
import pages.contractordetails.*
import queries.CisIdQuery
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ContractorDetailsFinalValidationService @Inject() (
  contractorDetailsService: ContractorDetailsService,
  sessionRepository: SessionRepository,
  contractorUtrFormProvider: ContractorUtrFormProvider,
  schemeNameFormProvider: SchemeNameFormProvider,
  emailFormProvider: EnterContractorEmailAddressFormProvider
)(implicit ec: ExecutionContext) {

  def refreshAndValidate(
    userAnswers: UserAnswers,
    target: ContractorDetailsValidationTarget
  )(implicit hc: HeaderCarrier): Future[(UserAnswers, ContractorDetailsFinalValidation)] =
    for {
      instanceId <- required(userAnswers.get(CisIdQuery), "CisIdQuery not found in session data")
      scheme     <- contractorDetailsService.getScheme(instanceId)
      updated    <- Future.fromTry(populate(userAnswers, target, scheme))
      _          <- sessionRepository.set(updated)
    } yield (updated, validate(updated))

  def validate(userAnswers: UserAnswers): ContractorDetailsFinalValidation =
    ContractorDetailsFinalValidation(
      utrComplete = userAnswers.get(ContractorUtrPage).exists(isValidContractorUtr),
      schemeNameComplete = userAnswers.get(SchemeNamePage).forall(isValidSchemeName),
      emailComplete = userAnswers.get(EnterContractorEmailAddressPage).forall(isValidEmail)
    )

  def updateSchemeFromAnswers(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Unit] =
    for {
      scheme <- required(userAnswers.get(ContractorSchemePage), "ContractorSchemePage not found in session data")
      _      <- if (validate(userAnswers).allComplete) Future.successful(())
                else Future.failed(new RuntimeException("Contractor details final validations are incomplete"))
      versionResponse <- contractorDetailsService.updateSchemeVersion(
                           UpdateSchemeVersionRequest(
                             currentVersion = scheme.version.getOrElse(0),
                             instanceId = scheme.instanceId
                           )
                         )
      _ <- contractorDetailsService.updateScheme(
             UpdateSchemeRequest(
               schemeId = scheme.schemeId,
               instanceId = scheme.instanceId,
               taxOfficeNumber = scheme.taxOfficeNumber,
               taxOfficeReference = scheme.taxOfficeReference,
               accountsOfficeReference = scheme.accountsOfficeReference,
               prePopCount = scheme.prePopCount.getOrElse(0),
               prePopSuccessful = scheme.prePopSuccessful.getOrElse(""),
               uniqueTaxReference = userAnswers.get(ContractorUtrPage).getOrElse(""),
               name = userAnswers.get(SchemeNamePage).getOrElse(""),
               emailAddress = userAnswers.get(EnterContractorEmailAddressPage).getOrElse(""),
               version = versionResponse.newVersion
             )
           )
    } yield ()

  private def populate(
    userAnswers: UserAnswers,
    target: ContractorDetailsValidationTarget,
    scheme: models.Scheme
  ) =
    for {
      ua1 <- userAnswers.set(ContractorSchemePage, scheme)
      ua2 <- ua1.set(ContractorDetailsValidationTargetPage, target)
      ua3 <- setOptionalString(ua2, ContractorUtrPage, scheme.utr)
      ua4 <- setOptionalString(ua3, SchemeNamePage, scheme.name)
      ua5 <- ua4.set(AddSchemeNameYesNoPage, hasValue(scheme.name))
      ua6 <- setOptionalString(ua5, EnterContractorEmailAddressPage, scheme.emailAddress)
      ua7 <- ua6.set(AddEmailAddressYesNoPage, hasValue(scheme.emailAddress))
    } yield ua7

  private def setOptionalString(
    userAnswers: UserAnswers,
    page: QuestionPage[String],
    value: Option[String]
  ) =
    value.map(_.trim).filter(_.nonEmpty) match {
      case Some(answer) => userAnswers.set(page, answer)
      case None         => userAnswers.remove(page)
    }

  private def hasValue(value: Option[String]): Boolean =
    value.exists(_.trim.nonEmpty)

  private def isValidContractorUtr(value: String): Boolean =
    !contractorUtrFormProvider().bind(Map("value" -> value)).hasErrors

  private def isValidSchemeName(value: String): Boolean =
    !schemeNameFormProvider().bind(Map("value" -> value)).hasErrors

  private def isValidEmail(value: String): Boolean =
    !emailFormProvider().bind(Map("value" -> value)).hasErrors

  private def required[A](value: Option[A], message: String): Future[A] =
    value.map(Future.successful).getOrElse(Future.failed(new RuntimeException(message)))
}

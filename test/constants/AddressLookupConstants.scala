/*
 * Copyright 2025 HM Revenue & Customs
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

package constants

import models.address.*

object AddressLookupConstants {

  val testContinueUrl        = "continueUrl"
  val testSignOutHref        = "signOutHref"
  val testPhaseFeedbackLink  = "phaseFeedbackLink"
  val testAccessibilityUrl   = "accessibilityUrl"
  val testDeskProServiceName = "VATREG"
  val testTimeoutUrl         = "timeoutUrl"

  val testAppLevelMessages: AppLevelMessagesModel = AppLevelMessagesModel(
    navTitle = "test nav title"
  )

  val testLookupMessages: LookupPageMessagesModel = LookupPageMessagesModel(
    title = Some("test lookup page title"),
    heading = Some("test lookup page heading"),
    filterLabel = None,
    postcodeLabel = None,
    submitLabel = None,
    noResultsFoundMessage = None,
    resultLimitExceededMessage = None,
    manualAddressLinkText = None
  )

  val testEditMessages: EditPageMessagesModel = EditPageMessagesModel(
    title = Some("test edit page title"),
    heading = Some("test edit page heading"),
    line1Label = None,
    line2Label = None,
    line3Label = None,
    townLabel = None,
    countryLabel = None,
    postcodeLabel = None,
    submitLabel = None
  )

  val testMandatoryFieldsConfigModel: MandatoryFieldsConfigModel = MandatoryFieldsConfigModel(
    addressLine1 = Some(true),
    addressLine2 = Some(true),
    addressLine3 = Some(true),
    town = Some(true),
    postcode = Some(true)
  )

  val testSelectMessages: SelectPageMessagesModel = SelectPageMessagesModel(
    title = Some("test select page title"),
    heading = Some("test select page heading"),
    headingWithPostcode = None,
    proposalListLabel = None,
    submitLabel = None,
    searchAgainLinkText = None,
    editAddressLinkText = None
  )

  val testConfirmMessages: ConfirmPageMessagesModel = ConfirmPageMessagesModel(
    title = Some("test confirm page title"),
    heading = Some("test confirm page heading"),
    infoMessage = None,
    infoSubheading = None,
    submitLabel = None,
    searchAgainLinkText = None,
    changeLinkText = None,
    confirmChangeText = None
  )

  val internationalMessagesModel: InternationalAddressMessagesModel = InternationalAddressMessagesModel(
    appLevelLabels = testAppLevelMessages,
    lookupPageLabels = testLookupMessages,
    selectPageLabels = testSelectMessages,
    editPageLabels = testEditMessages,
    confirmPageLabels = testConfirmMessages
  )

  val testMaxLengthErrorMessage =
    MaxLengthErrorMessage(
      addressLine1 = "Address line 1 max length error",
      addressLine2 = "Address line 2 max length error",
      addressLine3 = "Address line 3 max length error",
      town = "Town max length error"
    )

  val testMaxLengthErrorMessagesModel =
    MaxLengthErrorMessagesModel(
      en = Some(testMaxLengthErrorMessage),
      cy = Some(testMaxLengthErrorMessage)
    )

  val testAlfConfig: AddressLookupConfigurationModel = AddressLookupConfigurationModel(
    version = 2,
    options = AddressLookupOptionsModel(
      continueUrl = testContinueUrl,
      signOutHref = Some(testSignOutHref),
      phaseFeedbackLink = Some(testPhaseFeedbackLink),
      accessibilityFooterUrl = Some(testAccessibilityUrl),
      deskProServiceName = Some(testDeskProServiceName),
      showPhaseBanner = Some(true),
      showBackButtons = Some(true),
      disableTranslations = Some(true),
      includeHMRCBranding = Some(true),
      ukMode = Some(true),
      selectPageConfig = AddressLookupSelectConfigModel(
        showSearchAgainLink = Some(true)
      ),
      confirmPageConfig = AddressLookupConfirmConfigModel(
        showChangeLinkcontinueUrl = Some(true),
        showSubHeadingAndInfo = Some(false),
        showSearchAgainLink = Some(true),
        showConfirmChangeText = Some(false)
      ),
      manualAddressEntryConfig = ManualAddressEntryConfig(
        line1MaxLength = Some(35),
        line2MaxLength = Some(35),
        line3MaxLength = Some(35),
        townMaxLength = Some(35),
        mandatoryFields = testMandatoryFieldsConfigModel,
        maxLengthErrorMessages = testMaxLengthErrorMessagesModel,
        showOrganisationName = false
      ),
      pageHeadingStyle = "govuk-heading-l"
    ),
    labels = AddressMessageLanguageModel(
      en = AddressMessagesModel(
        appLevelLabels = testAppLevelMessages,
        lookupPageLabels = testLookupMessages,
        selectPageLabels = testSelectMessages,
        editPageLabels = testEditMessages,
        confirmPageLabels = testConfirmMessages,
        international = internationalMessagesModel
      ),
      cy = AddressMessagesModel(
        appLevelLabels = testAppLevelMessages,
        lookupPageLabels = testLookupMessages,
        selectPageLabels = testSelectMessages,
        editPageLabels = testEditMessages,
        confirmPageLabels = testConfirmMessages,
        international = internationalMessagesModel
      )
    )
  )
}

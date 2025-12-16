package pages.add

import pages.behaviours.PageBehaviours

class SubcontractorContactDetailsYesNoPageSpec extends PageBehaviours {

  "SubcontractorContactDetailsYesNoPage" - {

    beRetrievable[Boolean](SubcontractorContactDetailsYesNoPage)

    beSettable[Boolean](SubcontractorContactDetailsYesNoPage)

    beRemovable[Boolean](SubcontractorContactDetailsYesNoPage)
  }
}

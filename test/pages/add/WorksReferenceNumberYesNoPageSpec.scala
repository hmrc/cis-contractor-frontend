package pages.add

import pages.behaviours.PageBehaviours

class WorksReferenceNumberYesNoPageSpec extends PageBehaviours {

  "WorksReferenceNumberYesNoPage" - {
    beRetrievable[Boolean](WorksReferenceNumberYesNoPage)

    beSettable[Boolean](WorksReferenceNumberYesNoPage)

    beRemovable[Boolean](WorksReferenceNumberYesNoPage)
  }
}
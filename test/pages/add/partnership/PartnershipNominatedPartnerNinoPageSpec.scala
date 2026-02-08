package pages.add.partnership

import pages.behaviours.PageBehaviours

class PartnershipNominatedPartnerNinoPageSpec extends PageBehaviours {

  "PartnershipNominatedPartnerNinoPage" - {

    beRetrievable[String](PartnershipNominatedPartnerNinoPage)

    beSettable[String](PartnershipNominatedPartnerNinoPage)

    beRemovable[String](PartnershipNominatedPartnerNinoPage)
  }
}

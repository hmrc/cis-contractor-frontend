package queries

import play.api.libs.json.JsPath

case object SubContractorVerifiedQuery extends Gettable[Boolean] with Settable[Boolean] {
  override def path: JsPath = JsPath \ "subContractorVerified"
}

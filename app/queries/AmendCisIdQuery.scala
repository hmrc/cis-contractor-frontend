package queries

import play.api.libs.json.JsPath

case class AmendCisIdQuery(id: String) extends Gettable[String] with Settable[String] {
  override def path: JsPath = JsPath \ "amendSubcontractor" \ id \ "cisId"
}

package json.reads

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Reads.minLength
import play.api.libs.json.{JsPath, Reads}

/*
  Todo更新API用Inputクラス
 */
case class TodoUpdateInput(
  categoryId: Option[Long],
  title:      String,
  body:       String,
  stateCode:  Short
)

object TodoUpdateInput {
  implicit val todoUpdateInputReads: Reads[TodoUpdateInput] = (
    (JsPath \ "categoryId").readNullable[Long] and
      (JsPath \ "title").read[String](minLength[String](1)) and
      (JsPath \ "body").read[String](minLength[String](1)) and
      (JsPath \ "stateCode").read[Short]
    ) (apply _)
}
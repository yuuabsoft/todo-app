package model

import play.api.libs.functional.syntax.toFunctionalBuilderOps
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
  // TODO: バリデーション詳細
  implicit val todoUpdateInputReads: Reads[TodoUpdateInput] = (
    (JsPath \ "categoryId").readNullable[Long] and
      (JsPath \ "title").read[String] and
      (JsPath \ "body").read[String] and
      (JsPath \ "stateCode").read[Short]
    ) (apply _)
}
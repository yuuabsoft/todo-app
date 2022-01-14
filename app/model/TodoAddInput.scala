package model

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}

/*
  Todo追加API用Inputクラス
 */
case class TodoAddInput(
  categoryId: Option[Long],
  title:      String,
  body:       String
)

object TodoAddInput {
  // TODO: バリデーション詳細
  implicit val todoAddInputReads: Reads[TodoAddInput] = (
    (JsPath \ "categoryId").readNullable[Long] and
      (JsPath \ "title").read[String] and
      (JsPath \ "body").read[String]
  )(apply _)
}
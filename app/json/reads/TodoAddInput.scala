package json.reads

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Reads.minLength
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
  implicit val todoAddInputReads: Reads[TodoAddInput] = (
    (JsPath \ "categoryId").readNullable[Long] and
      (JsPath \ "title").read[String](minLength[String](1)) and
      (JsPath \ "body").read[String](minLength[String](1))
  )(apply _)
}
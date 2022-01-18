package json.reads

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Reads.{minLength, pattern}
import play.api.libs.json.{JsPath, Reads}

/*
  カテゴリ追加API用Inputクラス
 */
case class CategoryUpdateInput(
  name:      String,
  slug:      String,
  colorCode: Short
)

object CategoryUpdateInput {
  implicit val categoryUpdateInputReads: Reads[CategoryUpdateInput] = (
    (JsPath \ "name").read[String](minLength[String](1)) and
      (JsPath \ "slug").read[String](pattern("^[A-Za-z0-9]+$".r)) and
      (JsPath \ "colorCode").read[Short]
    ) (apply _)
}

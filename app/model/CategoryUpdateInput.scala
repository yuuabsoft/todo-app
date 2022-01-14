package model

import play.api.libs.functional.syntax.toFunctionalBuilderOps
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
  // TODO: バリデーション詳細
  implicit val categoryUpdateInputReads: Reads[CategoryUpdateInput] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "slug").read[String] and
      (JsPath \ "colorCode").read[Short]
    ) (apply _)
}

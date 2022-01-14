package model

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}

/*
  カテゴリ追加API用Inputクラス
 */
case class CategoryAddInput(
  name:      String,
  slug:      String,
  colorCode: Short
)

object CategoryAddInput {
  // TODO: バリデーション詳細
  implicit val categoryAddInputReads: Reads[CategoryAddInput] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "slug").read[String] and
      (JsPath \ "colorCode").read[Short]
    ) (apply _)
}

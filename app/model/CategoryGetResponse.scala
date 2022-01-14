package model

import lib.model.Category
import play.api.libs.json.Json

/*
  カテゴリ取得API用Responseクラス
 */
case class CategoryGetResponse(
  id:        Long,
  name:      String,
  slug:      String,
  colorCode: Short,
  colorName: String,
)

object CategoryGetResponse {
  def apply(category: Category.EmbeddedId): CategoryGetResponse = {
    CategoryGetResponse(
      id = category.id,
      name = category.v.name,
      slug = category.v.slug,
      colorCode = category.v.color.code,
      colorName = category.v.color.name
      )
  }
  implicit val categoryGetResponseWrites = Json.writes[CategoryGetResponse]
}
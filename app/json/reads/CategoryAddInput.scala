package json.reads

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Reads.{minLength, pattern}
import play.api.libs.json.{Reads, __}

/*
  カテゴリ追加API用Inputクラス
 */
case class CategoryAddInput(
  name:      String,
  slug:      String,
  colorCode: Short
)

object CategoryAddInput {
  implicit val categoryAddInputReads: Reads[CategoryAddInput] = (
    (__ \ "name").read[String](minLength[String](1)) and
      (__ \ "slug").read[String](pattern("^[A-Za-z0-9]+$".r)) and
      (__ \ "colorCode").read[Short]
    ) (apply _)
}

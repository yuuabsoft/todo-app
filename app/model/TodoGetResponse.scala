package model

import lib.model.{Category, Todo}
import play.api.libs.json.Json

/*
  Todo取得API用Responseクラス
 */
// TODO: categoryやstateをネストで表現したい
case class TodoGetResponse(
  id:            Long,
  categoryId:    Option[Long],
  categoryName:  Option[String],
  categoryColor: Option[String],
  title:         String,
  body:          String,
  stateCode:     Short,
  stateName:     String,
)

object TodoGetResponse {
  def apply(todo: Todo.EmbeddedId, category: Option[Category.EmbeddedId]): TodoGetResponse = {
    TodoGetResponse(
      id = todo.id,
      categoryId = category.map(_.id),
      categoryName = category.map(_.v.name),
      categoryColor = category.map(_.v.color.name),
      title = todo.v.title,
      body = todo.v.body,
      stateCode = todo.v.state.code,
      stateName = todo.v.state.name
      )
  }
  implicit val todoAllGetResponseWrites = Json.writes[TodoGetResponse]
}
package json.writes

import lib.model.{Category, Todo}
import play.api.libs.json.Json

/*
  Todo取得API用Responseクラス
 */
case class TodoGetResponse(
  id:        Long,
  category:  Option[CategoryGetResponse],
  title:     String,
  body:      String,
  stateCode: Short,
  stateName: String,
)

object TodoGetResponse {
  def apply(todo: Todo.EmbeddedId, category: Option[Category.EmbeddedId]): TodoGetResponse = {
    TodoGetResponse(
      id = todo.id,
      category = category.map(CategoryGetResponse(_)),
      title = todo.v.title,
      body = todo.v.body,
      stateCode = todo.v.state.code,
      stateName = todo.v.state.name
      )
  }
  implicit val todoGetResponseWrites = Json.writes[TodoGetResponse]
}
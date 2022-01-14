package controllers

import lib.model.{Category, Todo}
import lib.persistence.onMySQL
import model.{TodoAddForm, TodoAddInput, TodoGetResponse, TodoInput, TodoUpdateInput}
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.{AnyContent, MessagesAbstractController, MessagesControllerComponents, MessagesRequest, Request}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TodoApiController @Inject()(val mcc: MessagesControllerComponents) extends MessagesAbstractController(mcc) {

  /*
    Todoリスト取得
   */
  def getAll() = Action.async { implicit req: MessagesRequest[AnyContent] =>
    val todoListFuture = onMySQL.TodoRepository.all()
    val categoryFuture = onMySQL.CategoryRepository.all()
    for {
      todoList <- todoListFuture
      categoryList <- categoryFuture
    } yield {
      val todoViewList = todoList.map(todo => {
        val category = categoryList.find(_.id == todo.v.categoryId.getOrElse())
        TodoGetResponse(todo, category)
      })
      Ok(Json.toJson(todoViewList))
    }
  }

  /*
    Todo取得
   */
  def get(id: String) = Action.async { implicit req: MessagesRequest[AnyContent] =>
    val todoFuture     = onMySQL.TodoRepository.get(Todo.Id(id.toLong))
    val categoryFuture = onMySQL.CategoryRepository.all()
    for {
      todo <- todoFuture
      categoryList <- categoryFuture
    } yield {
      todo match {
        case Some(t) => {
          val category = categoryList.find(_.id == t.v.categoryId.getOrElse())
          Ok(Json.toJson(TodoGetResponse(t, category)))
        }
        // TODO: 準正常エラーをフォーマット揃えてjsonで返却する（他APIも同様）
        case _ => NotFound("todo not found")
      }
    }
  }

  /*
    Todo追加
   */
  def add() = Action.async(parse.json) { implicit req: Request[JsValue] =>
    val inputResult: JsResult[TodoAddInput] = req.body.validate[TodoAddInput]
    inputResult match {
      // バリデーションOK
      case JsSuccess(input: TodoAddInput, _: JsPath) =>
        val todo: Todo#WithNoId = Todo(
          input.categoryId.map(Category.Id(_)),
          input.title,
          input.body,
          Todo.Status.TODO
          )
        for {
          _ <- onMySQL.TodoRepository.add(todo)
        } yield {
          // NOTE: 追加したIDぐらいは返した方が良い気がするが一旦積み
          Created("todo created")
        }
      // バリデーションエラー
      case JsError(errors: Seq[(JsPath, Seq[JsonValidationError])]) =>
        Future.successful(BadRequest(JsError.toJson(errors)))
    }
  }

  /*
    Todo更新
   */
  def update(id: String) = Action.async(parse.json) { implicit req: Request[JsValue] =>
    val inputResult: JsResult[TodoUpdateInput] = req.body.validate[TodoUpdateInput]
    inputResult match {
      // バリデーションOK
      case JsSuccess(input: TodoUpdateInput, _: JsPath) =>
        onMySQL.TodoRepository.get(Todo.Id(id.toLong)).flatMap { todo => {
          val updatedTodo = todo.get.map(_.copy(
            categoryId = input.categoryId.map(Category.Id(_)),
            title = input.title,
            body = input.body,
            state = Todo.Status(input.stateCode)))
          for {
            _ <- onMySQL.TodoRepository.update(updatedTodo)
          } yield {
            Ok("todo updated")
          }
          }
        }
      // バリデーションエラー
      case JsError(errors: Seq[(JsPath, Seq[JsonValidationError])]) =>
        Future.successful(BadRequest(JsError.toJson(errors)))
    }
  }

  /*
    Todo削除
   */
  def delete(id: String) = Action.async(parse.json) { implicit req: Request[JsValue] =>
    for {
      _ <- onMySQL.TodoRepository.remove(Todo.Id(id.toLong))
    } yield {
      Ok("todo deleted")
    }
  }
}


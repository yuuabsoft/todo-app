package controllers

import lib.model.Todo
import lib.persistence.onMySQL
import model.{TodoAddForm, TodoInput, ViewValueTodoAdd, ViewValueTodoList}
import play.api.data.Form
import play.api.mvc.{AnyContent, MessagesAbstractController, MessagesControllerComponents, MessagesRequest}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Singleton
class TodoController @Inject()(val mcc: MessagesControllerComponents) extends MessagesAbstractController(mcc) {

  /*
    Todoリストページ表示
   */
  def index() = Action { implicit req =>

    val todoList = Await.result(onMySQL.TodoRepository.all(), Duration.Inf)
    val categoryList = Await.result(onMySQL.CategoryRepository.all(), Duration.Inf)

    val todoViewList = for {
      todo <- todoList
      category <- categoryList.find(_.id == todo.v.categoryId)
    } yield {
      (todo, category)
    }

    val vv = ViewValueTodoList(
      title  = "Todoリスト",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js"),
      todoList = todoViewList
    )
    Ok(views.html.todo.TodoList(vv))
  }

  /*
    Todo追加ページ表示
   */
  def addView() = Action { implicit req: MessagesRequest[AnyContent] =>
    val vv = this.createViewValueTodoAdd()
    Ok(views.html.todo.TodoAdd(vv, TodoAddForm()))
  }

  /*
    Todo追加実行
   */
  def addSubmit() = Action { implicit req =>

    TodoAddForm().bindFromRequest.fold(
      // バリデーションエラーがあった場合
      (formWithErrors: Form[TodoInput]) => {
        val vv = this.createViewValueTodoAdd()
        BadRequest(views.html.todo.TodoAdd(vv, formWithErrors))
      },
      // 値が正常だった場合
      (input: TodoInput) => {
        val todo: Todo#WithNoId = Todo(
          input.categoryId,
          input.title,
          input.body,
          input.state
          )
        Await.result(onMySQL.TodoRepository.add(todo), Duration.Inf)
        Redirect(routes.TodoController.index()).flashing("notice" -> "todoを作成しました。")
      }
    )
  }

  /*
    Todo追加ページ共通処理
   */
  private def createViewValueTodoAdd() = {
    val categoryList = Await.result(onMySQL.CategoryRepository.all(), Duration.Inf)
                            .map(c => (c.id.toString, c.v.name))
    ViewValueTodoAdd(
      title  = "Todoリスト",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js"),
      categoryList = categoryList
      )
  }
}

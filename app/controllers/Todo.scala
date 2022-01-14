package controllers

import lib.model.Todo
import lib.persistence.onMySQL
import model.{TodoAddForm, TodoInput, ViewValueTodoAdd, ViewValueTodoList}
import play.api.data.Form
import play.api.mvc.{AnyContent, MessagesAbstractController, MessagesControllerComponents, MessagesRequest}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TodoController @Inject()(val mcc: MessagesControllerComponents) extends MessagesAbstractController(mcc) {

  /*
    Todoリストページ表示
   */
  def index() = Action.async { implicit req =>

    for {
      todoList <- onMySQL.TodoRepository.all()
      categoryList <- onMySQL.CategoryRepository.all()
    } yield {
      val todoViewList = for {
        todo <- todoList
        category <- categoryList.find(_.id == todo.v.categoryId)
      } yield {
        (todo, category)
      }

      val vv = ViewValueTodoList(
        title = "Todoリスト",
        cssSrc = Seq("main.css"),
        jsSrc = Seq("main.js"),
        todoList = todoViewList
        )
      Ok(views.html.todo.TodoList(vv))
    }
  }

  /*
    Todo追加ページ表示
   */
  def addView() = Action.async { implicit req: MessagesRequest[AnyContent] =>
    for {
      vv <- this.createViewValueTodoAdd()
    } yield {
      Ok(views.html.todo.TodoAdd(vv, TodoAddForm.f))
    }
  }

  /*
    Todo追加実行
   */
  def addSubmit() = Action.async { implicit req =>

    TodoAddForm.f.bindFromRequest.fold(
      // バリデーションエラーがあった場合
      (formWithErrors: Form[TodoInput]) => {
        for {
          vv <- this.createViewValueTodoAdd()
        } yield {
          BadRequest(views.html.todo.TodoAdd(vv, formWithErrors))
        }
      },
      // 値が正常だった場合
      (input: TodoInput) => {
        val todo: Todo#WithNoId = Todo(
          input.categoryId,
          input.title,
          input.body,
          input.state
          )
        for {
          _ <- onMySQL.TodoRepository.add(todo)
        } yield {
          Redirect(routes.TodoController.index()).flashing("notice" -> "todoを作成しました。")
        }
      }
    )
  }

  /*
    Todo追加ページ共通処理
   */
  private def createViewValueTodoAdd() = {
    for {
      categoryList <- onMySQL.CategoryRepository.all()
    } yield {
      ViewValueTodoAdd(
        title  = "Todoリスト",
        cssSrc = Seq("main.css"),
        jsSrc  = Seq("main.js"),
        categoryList = categoryList.map(c => (c.id.toString, c.v.name))
        )
    }
  }
}

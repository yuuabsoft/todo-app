package controllers

import lib.model.{Category, Todo}
import lib.persistence.onMySQL
import model.{TodoAddForm, TodoInput, TodoUpdateForm, ViewValueTodoAdd, ViewValueTodoList, ViewValueTodoUpdate}
import play.api.data.Form
import play.api.data.Forms.{default, ignored, longNumber, mapping, nonEmptyText, number, text}
import play.api.mvc.{AnyContent, MessagesAbstractController, MessagesControllerComponents, MessagesRequest}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration

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
      Ok(views.html.todo.TodoAdd(vv, TodoAddForm()))
    }
  }

  /*
    Todo追加実行
   */
  def addSubmit() = Action.async { implicit req =>

    TodoAddForm().bindFromRequest.fold(
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
    Todo編集ページ表示
   */
  def updateView(id: String) = Action.async { implicit req: MessagesRequest[AnyContent] =>
    for {
      todo <- onMySQL.TodoRepository.get(Todo.Id(id.toLong))
      vv <- this.createViewValueTodoUpdate(id)
    } yield {
      val filledForm = TodoUpdateForm().fill(
        TodoInput(
          todo.get.v.categoryId,
          todo.get.v.title,
          todo.get.v.body,
          todo.get.v.state
          ))
      Ok(views.html.todo.TodoUpdate(vv, filledForm))
    }
  }

  /*
    Todo編集実行
   */
  def updateSubmit(id: String) = Action.async { implicit req =>

    TodoUpdateForm().bindFromRequest.fold(
      // バリデーションエラーがあった場合
      (formWithErrors: Form[TodoInput]) => {
        for {
          vv <- this.createViewValueTodoUpdate(id)
        } yield {
          BadRequest(views.html.todo.TodoUpdate(vv, formWithErrors))
        }
      },
      // 値が正常だった場合
      (input: TodoInput) => {
        onMySQL.TodoRepository.get(Todo.Id(id.toLong)).flatMap { todo => {
          val updatedTodo = todo.get.map(_.copy(
            categoryId = input.categoryId,
            title = input.title,
            body = input.body,
            state = input.state))
          for {
            _ <- onMySQL.TodoRepository.update(updatedTodo)
          } yield {
            Redirect(routes.TodoController.index()).flashing("notice" -> "todoを更新しました。")
          }
        }
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

  /*
    Todo編集ページ共通処理
   */
  private def createViewValueTodoUpdate(id: String) = {
    for {
      categoryList <- onMySQL.CategoryRepository.all()
    } yield {
      val stateList    = Todo.Status.values.map(s => (s.code.toString, s.name))
      ViewValueTodoUpdate(
        title = "Todoリスト",
        cssSrc = Seq("main.css"),
        jsSrc = Seq("main.js"),
        todoId = id,
        categoryList = categoryList.map(c => (c.id.toString, c.v.name)),
        stateList = stateList
        )
    }
  }
}

package controllers

import lib.model.{Category, Todo}
import lib.persistence.onMySQL
import model.{TodoInput, ViewValueTodoAdd, ViewValueTodoList, ViewValueTodoUpdate}
import play.api.data.Form
import play.api.data.Forms.{default, ignored, longNumber, mapping, nonEmptyText, number, text}
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

    val todoList     = Await.result(onMySQL.TodoRepository.all(), Duration.Inf)
    val categoryList = Await.result(onMySQL.CategoryRepository.all(), Duration.Inf)

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

  /*
    Todo追加ページ表示
   */
  def addView() = Action { implicit req: MessagesRequest[AnyContent] =>
    val vv = this.createViewValueTodoAdd()
    Ok(views.html.todo.TodoAdd(vv, this.todoAddForm))
  }

  /*
    Todo追加実行
   */
  def addSubmit() = Action { implicit req =>

    this.todoAddForm.bindFromRequest.fold(
      // バリデーションエラーがあった場合
      (formWithErrors: Form[TodoInput]) => {
        val vv = this.createViewValueTodoAdd()
        BadRequest(views.html.todo.TodoAdd(vv, formWithErrors))
      },
      // 値が正常だった場合
      (input: TodoInput) => {
        val todo: Todo#WithNoId = Todo(
          Category.Id(input.categoryId),
          input.title,
          input.body,
          Todo.Status(input.state.toShort)
          )
        Await.result(onMySQL.TodoRepository.add(todo), Duration.Inf)
        Redirect(routes.TodoController.index()).flashing("notice" -> "todoを作成しました。")
      }
      )
  }

  /*
    Todo編集ページ表示
   */
  def updateView(id: String) = Action { implicit req: MessagesRequest[AnyContent] =>
    val todo       = Await.result(onMySQL.TodoRepository.get(Todo.Id(id.toLong)), Duration.Inf).get
    val filledForm = this.todoUpdateForm.fill(
      TodoInput(
        todo.v.categoryId,
        todo.v.title,
        todo.v.body,
        todo.v.state.code
        ))
    val vv         = this.createViewValueTodoUpdate(id)
    Ok(views.html.todo.TodoUpdate(vv, filledForm))
  }

  /*
    Todo編集実行
   */
  def updateSubmit(id: String) = Action { implicit req =>

    this.todoUpdateForm.bindFromRequest.fold(
      // バリデーションエラーがあった場合
      (formWithErrors: Form[TodoInput]) => {
        val vv = this.createViewValueTodoUpdate(id)
        BadRequest(views.html.todo.TodoUpdate(vv, formWithErrors))
      },
      // 値が正常だった場合
      (input: TodoInput) => {
        val todo =
          Await.result(onMySQL.TodoRepository.get(Todo.Id(id.toLong)), Duration.Inf)
            .get.map(_.copy(
            categoryId = Category.Id(input.categoryId),
            title = input.title,
            body = input.body,
            state = Todo.Status(input.state.toShort)))
        Await.result(onMySQL.TodoRepository.update(todo), Duration.Inf)
        Redirect(routes.TodoController.index()).flashing("notice" -> "todoを更新しました。")
      }
      )
  }

  // Todo追加フォーム
  val todoAddForm: Form[TodoInput] = Form(
    mapping(
      "categoryId" -> longNumber,
      "title" -> nonEmptyText,
      "body" -> text,
      "state" -> ignored(0)
      )(TodoInput.apply)(TodoInput.unapply)
    )

  // Todo編集フォーム
  val todoUpdateForm: Form[TodoInput] = Form(
    mapping(
      "categoryId" -> longNumber,
      "title" -> nonEmptyText,
      "body" -> text,
      "state" -> number
      )(TodoInput.apply)(TodoInput.unapply)
    )

  /*
    Todo追加ページ共通処理
   */
  private def createViewValueTodoAdd() = {
    val categoryList = Await.result(onMySQL.CategoryRepository.all(), Duration.Inf)
      .map(c => (c.id.toString, c.v.name))
    ViewValueTodoAdd(
      title = "Todoリスト",
      cssSrc = Seq("main.css"),
      jsSrc = Seq("main.js"),
      categoryList = categoryList
      )
  }

  /*
    Todo編集ページ共通処理
   */
  private def createViewValueTodoUpdate(id: String) = {
    val categoryList = Await.result(onMySQL.CategoryRepository.all(), Duration.Inf)
      .map(c => (c.id.toString, c.v.name))
    val stateList    = Todo.Status.values.map(s => (s.code.toString, s.name))
    ViewValueTodoUpdate(
      title = "Todoリスト",
      cssSrc = Seq("main.css"),
      jsSrc = Seq("main.js"),
      todoId = id,
      categoryList = categoryList,
      stateList = stateList
      )
  }
}

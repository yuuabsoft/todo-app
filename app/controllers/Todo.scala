package controllers

import lib.persistence.onMySQL
import model.ViewValueTodoList
import play.api.mvc.{BaseController, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def index() = Action.async { implicit req =>

    val todoListFuture = onMySQL.TodoRepository.all()
    val categoryFuture = onMySQL.CategoryRepository.all()
    for {
      todoList <- todoListFuture
      categoryList <- categoryFuture
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
}

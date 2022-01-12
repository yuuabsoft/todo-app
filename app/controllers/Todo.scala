package controllers

import lib.persistence.onMySQL
import model.ViewValueTodoList
import play.api.mvc.{BaseController, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

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
}

package model

import lib.model.{Category, Todo}
import play.api.data.Form
import play.api.data.Forms.{ignored, longNumber, mapping, nonEmptyText, number, optional, text}

// Todo追加・更新用のinput
case class TodoInput(
  categoryId: Option[Category.Id],
  title:      String,
  body:       String,
  state:      Todo.Status
)

object TodoAddForm {
  def apply(): Form[TodoInput] = {
    Form(
      mapping(
        "categoryId" -> optional(longNumber.transform[Category.Id](v => Category.Id(v), _.longValue())),
        "title" -> nonEmptyText,
        "body" -> text,
        "state" -> ignored(0).transform[Todo.Status](v => Todo.Status(v.toShort), _.code)
        )(TodoInput.apply)(TodoInput.unapply)
      )
  }
}

object TodoUpdateForm {
  def apply(): Form[TodoInput] = {
    Form(
      mapping(
        "categoryId" -> optional(longNumber.transform[Category.Id](v => Category.Id(v), _.longValue())),
        "title" -> nonEmptyText,
        "body" -> text,
        "state" -> number.transform[Todo.Status](v => Todo.Status(v.toShort), _.code)
        )(TodoInput.apply)(TodoInput.unapply)
      )
  }
}

package model

import play.api.data.Form
import play.api.data.Forms.{ignored, longNumber, mapping, nonEmptyText, text}

// Todo追加・更新用のinput
case class TodoInput(
  categoryId: Long,
  title: String,
  body: String,
  state: Int
)

object TodoAddForm {
  def apply(): Form[TodoInput] = {
    Form(
      mapping(
        "categoryId" -> longNumber,
        "title" -> nonEmptyText,
        "body" -> text,
        "state" -> ignored(0)
        )(TodoInput.apply)(TodoInput.unapply)
      )
  }
}

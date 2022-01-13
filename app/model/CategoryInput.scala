package model

import lib.model.Category
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, number, text}
import play.api.data.validation.Constraints.pattern

// カテゴリ追加・更新用のinput
case class CategoryInput(
  name:  String,
  slug:  String,
  color: Category.Color
)

object CategoryAddForm {
  def apply(): Form[CategoryInput] = {
    Form(
      mapping(
        "name" -> nonEmptyText,
        "slug" -> text.verifying(pattern("/^[0-9a-zA-Z]*$/".r, "hankaku", "半角英数字で入力してください")),
        "color" -> number.transform[Category.Color](v => Category.Color(v.toShort), _.code)
        )(CategoryInput.apply)(CategoryInput.unapply)
      )
  }
}

object CategoryUpdateForm {
  def apply(): Form[CategoryInput] = {
    Form(
      mapping(
        "name" -> nonEmptyText,
        "slug" -> text.verifying(pattern("/^[0-9a-zA-Z]*$/".r, "hankaku", "半角英数字で入力してください")),
        "color" -> number.transform[Category.Color](v => Category.Color(v.toShort), _.code)
        )(CategoryInput.apply)(CategoryInput.unapply)
      )
  }
}
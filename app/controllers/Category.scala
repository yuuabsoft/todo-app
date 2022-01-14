package controllers

import lib.model.Category
import lib.persistence.onMySQL
import model.{CategoryAddForm, CategoryInput, CategoryUpdateForm, ViewValueCategoryAdd, ViewValueCategoryList, ViewValueCategoryUpdate, ViewValueHome}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, MessagesAbstractController, MessagesControllerComponents, MessagesRequest}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CategoryController @Inject()(val mcc: MessagesControllerComponents) extends MessagesAbstractController(mcc) {

  /*
    カテゴリリストページ表示
   */
  def index() = Action.async { implicit req: MessagesRequest[AnyContent] =>
    for {
      categoryList <- onMySQL.CategoryRepository.all()
    } yield {
      val vv = ViewValueCategoryList(
        title = "カテゴリリスト",
        cssSrc = Seq("main.css"),
        jsSrc = Seq("main.js"),
        categoryList = categoryList
        )
      Ok(views.html.category.CategoryList(vv))
    }
  }

  def addView() = Action { implicit req: MessagesRequest[AnyContent] =>
    val vv = this.createViewValueCategoryAdd()
    Ok(views.html.category.CategoryAdd(vv, CategoryAddForm()))
  }

  def addSubmit() = Action.async { implicit req: MessagesRequest[AnyContent] =>
    CategoryAddForm().bindFromRequest.fold(
      // バリデーションエラーがあった場合
      (formWithErrors: Form[CategoryInput]) => {
        val vv = this.createViewValueCategoryAdd()
        Future.successful(BadRequest(views.html.category.CategoryAdd(vv, formWithErrors)))
      },
      // 値が正常だった場合
      (input: CategoryInput) => {
        val category: Category#WithNoId = Category(
          input.name,
          input.slug,
          input.color
          )
        for {
          _ <- onMySQL.CategoryRepository.add(category)
        } yield {
          Redirect(routes.CategoryController.index()).flashing("notice" -> Messages("message.category.add"))
        }
      }
      )
  }

  def updateView(id: String) = Action.async { implicit req: MessagesRequest[AnyContent] =>
    val categoryId = Category.Id(id.toLong)
    val vv         = this.createViewValueCategoryUpdate(categoryId)
    for {
      category <- onMySQL.CategoryRepository.get(categoryId)
    } yield {
      val filledForm = CategoryUpdateForm().fill(
        CategoryInput(
          category.get.v.name,
          category.get.v.slug,
          category.get.v.color
          ))
      Ok(views.html.category.CategoryUpdate(vv, filledForm))
    }
  }

  /*
    カテゴリ編集実行
   */
  def updateSubmit(id: String) = Action.async { implicit req =>
    val categoryId = Category.Id(id.toLong)
    CategoryUpdateForm().bindFromRequest.fold(
      // バリデーションエラーがあった場合
      (formWithErrors: Form[CategoryInput]) => {
        val vv = this.createViewValueCategoryUpdate(categoryId)
        Future.successful(BadRequest(views.html.category.CategoryUpdate(vv, formWithErrors)))
      },
      // 値が正常だった場合
      (input: CategoryInput) => {
        onMySQL.CategoryRepository.get(categoryId).flatMap { category => {
          val updatedCategory = category.get.map(_.copy(
            name = input.name,
            slug = input.slug,
            color = input.color))
          for {
            _ <- onMySQL.CategoryRepository.update(updatedCategory)
          } yield {
            Redirect(routes.CategoryController.index()).flashing("notice" -> Messages("message.category.update"))
          }
        }
        }
      }
      )
  }

  /*
    カテゴリ削除実行
   */
  def delete(id: String) = Action.async { implicit req: MessagesRequest[AnyContent] =>
    for {
      _ <- onMySQL.CategoryRepository.remove(Category.Id(id.toLong))
    } yield {
      Redirect(routes.CategoryController.index()).flashing("notice" -> Messages("message.category.delete"))
    }
  }

  /*
    カテゴリ追加ページ共通処理
   */
  private def createViewValueCategoryAdd() = {
    ViewValueCategoryAdd(
      title = "カテゴリ追加",
      cssSrc = Seq("main.css"),
      jsSrc = Seq("main.js"),
      Category.Color.values.map(c => (c.code.toString, c.name))
      )
  }

  /*
    カテゴリ編集ページ共通処理
   */
  private def createViewValueCategoryUpdate(id: Category.Id) = {
    ViewValueCategoryUpdate(
      title = "カテゴリ編集",
      cssSrc = Seq("main.css"),
      jsSrc = Seq("main.js"),
      categoryId = Category.Id(id.toLong),
      Category.Color.values.map(c => (c.code.toString, c.name))
      )
  }
}

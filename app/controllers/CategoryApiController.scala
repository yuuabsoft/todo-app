package controllers

import json.reads.{CategoryAddInput, CategoryUpdateInput, TodoUpdateInput}
import json.writes.CategoryGetResponse
import lib.model.{Category, Todo}
import lib.persistence.onMySQL
import play.api.libs.json.{JsError, JsPath, JsResult, JsSuccess, JsValue, Json, JsonValidationError}
import play.api.mvc.{AnyContent, MessagesAbstractController, MessagesControllerComponents, MessagesRequest, Request}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CategoryApiController @Inject()(val mcc: MessagesControllerComponents) extends MessagesAbstractController(mcc) {
  /*
    Categoryリスト取得
   */
  def getAll() = Action.async { implicit req: MessagesRequest[AnyContent] =>
    val categoryFuture = onMySQL.CategoryRepository.all()
    for {
      categoryList <- categoryFuture
    } yield {
      Ok(Json.toJson(categoryList.map(CategoryGetResponse(_))))
    }
  }

  /*
    Category取得
   */
  def get(id: String) = Action.async { implicit req: MessagesRequest[AnyContent] =>
    val categoryFuture = onMySQL.CategoryRepository.get(Category.Id(id.toLong))
    for {
      category <- categoryFuture
    } yield {
      category match {
        case Some(c) => Ok(Json.toJson(CategoryGetResponse(c)))
        // TODO: 準正常エラーをフォーマット揃えてjsonで返却する（他APIも同様）
        case _ => NotFound("category not found")
      }
    }
  }

  /*
    Category追加
   */
  def add() = Action.async(parse.json) { implicit req: Request[JsValue] =>
    val inputResult: JsResult[CategoryAddInput] = req.body.validate[CategoryAddInput]
    inputResult match {
      // バリデーションOK
      case JsSuccess(input: CategoryAddInput, _: JsPath) =>
        val category: Category#WithNoId = Category(
          input.name,
          input.slug,
          Category.Color(input.colorCode)
          )
        for {
          id <- onMySQL.CategoryRepository.add(category)
        } yield {
          Created(Json.obj("id" -> id.toLong))
        }
      // バリデーションエラー
      case JsError(errors: Seq[(JsPath, Seq[JsonValidationError])]) =>
        Future.successful(BadRequest(JsError.toJson(errors)))
    }
  }

  /*
    Category更新
   */
  def update(id: String) = Action.async(parse.json) { implicit req: Request[JsValue] =>
    val inputResult: JsResult[CategoryUpdateInput] = req.body.validate[CategoryUpdateInput]
    inputResult match {
      // バリデーションOK
      case JsSuccess(input: CategoryUpdateInput, _: JsPath) =>
        onMySQL.CategoryRepository.get(Category.Id(id.toLong)).flatMap { category => {
          val updatedCategory = category.get.map(_.copy(
            name = input.name,
            slug = input.slug,
            color = Category.Color(input.colorCode)))
          for {
            _ <- onMySQL.CategoryRepository.update(updatedCategory)
          } yield {
            Ok(Json.obj("id" -> id.toLong))
          }
        }
        }
      // バリデーションエラー
      case JsError(errors: Seq[(JsPath, Seq[JsonValidationError])]) =>
        Future.successful(BadRequest(JsError.toJson(errors)))
    }
  }

  /*
    Category削除
   */
  def delete(id: String) = Action.async(parse.json) { implicit req: Request[JsValue] =>
    for {
      _ <- onMySQL.CategoryRepository.remove(Category.Id(id.toLong))
    } yield {
      Ok(Json.obj("id" -> id.toLong))
    }
  }
}

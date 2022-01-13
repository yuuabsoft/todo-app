package model

import lib.model.Category

// カテゴリ編集ページのviewvalue
case class ViewValueCategoryUpdate(
  title:      String,
  cssSrc:     Seq[String],
  jsSrc:      Seq[String],
  categoryId: Category.Id,
  colorList:  Seq[(String, String)]
) extends ViewValueCommon

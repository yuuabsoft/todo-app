package model

import lib.model.Category

// Todoリストページのviewvalue
case class ViewValueCategoryList(
  title:        String,
  cssSrc:       Seq[String],
  jsSrc:        Seq[String],
  categoryList: Seq[Category.EmbeddedId]
) extends ViewValueCommon

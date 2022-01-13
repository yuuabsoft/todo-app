package model

// カテゴリ追加ページのviewvalue
case class ViewValueCategoryAdd(
  title:     String,
  cssSrc:    Seq[String],
  jsSrc:     Seq[String],
  colorList: Seq[(String, String)]
) extends ViewValueCommon

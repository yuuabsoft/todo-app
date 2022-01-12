package model

// Todo追加ページのviewvalue
case class ViewValueTodoAdd(
  title:    String,
  cssSrc:   Seq[String],
  jsSrc:    Seq[String],
  categoryList: Seq[(String, String)]
) extends ViewValueCommon

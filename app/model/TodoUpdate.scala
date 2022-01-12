package model

// Todo編集ページのviewvalue
case class ViewValueTodoUpdate(
  title:        String,
  cssSrc:       Seq[String],
  jsSrc:        Seq[String],
  todoId:       String,
  categoryList: Seq[(String, String)],
  stateList:    Seq[(String, String)]
) extends ViewValueCommon

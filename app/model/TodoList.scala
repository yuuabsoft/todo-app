package model

import lib.model.{Category, Todo}

// Todoリストページのviewvalue
case class ViewValueTodoList(
  title:    String,
  cssSrc:   Seq[String],
  jsSrc:    Seq[String],
  todoList: Seq[(Todo.EmbeddedId,Option[Category.EmbeddedId])]
) extends ViewValueCommon

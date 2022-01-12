package model

// Todo追加・更新用のinput
case class TodoInput(
  categoryId: Long,
  title: String,
  body: String,
  state: Int
)


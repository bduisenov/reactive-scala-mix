package org.scalalang.boot.reactive.core.usecase

import org.scalalang.boot.reactive.core.document.{HasUser, HasUserId}
import org.scalalang.boot.reactive.service.UserService

class GetUserUseCase[T <: HasUserId with HasUser](private val userService: UserService) extends UseCase[T] {
  override def apply(doc: T): Either[String, T] =
    doc.userId.flatMap(id => userService.getUser(id)) match {
      case Some(user) => Right(doc.user(user))
      case _ => Left("user not found")
    }
}

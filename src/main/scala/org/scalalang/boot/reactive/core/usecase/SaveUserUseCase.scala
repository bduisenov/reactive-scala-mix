package org.scalalang.boot.reactive.core.usecase

import org.scalalang.boot.reactive.core.document.HasUser
import org.scalalang.boot.reactive.service.UserService

class SaveUserUseCase[T <: HasUser](private val userService: UserService) extends UseCase[T] {
  override def apply(doc: T): Either[String, T] =
    doc.user.map(user => userService.saveUser(user)) match {
      case Some(user) => Right(doc.user(user))
      case _ => Left("failed to save user")
    }
}

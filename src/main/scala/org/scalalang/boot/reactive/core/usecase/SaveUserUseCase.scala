package org.scalalang.boot.reactive.core.usecase

import cats.data.EitherT
import cats.effect.IO
import org.scalalang.boot.reactive.core.document.HasUser
import org.scalalang.boot.reactive.service.UserService

class SaveUserUseCase[T <: HasUser](private val userService: UserService) extends UseCase[T] {
  override def apply(doc: T): EitherT[IO, String, T] = {
    val result: Option[IO[T]] = doc.user.map { user =>
      userService.saveUser(user)
        .map(user => doc.user(user))
    }

    result match {
      case Some(doc) => EitherT.right(doc)
      case _ => EitherT.leftT("empty arg")
    }
  }
}

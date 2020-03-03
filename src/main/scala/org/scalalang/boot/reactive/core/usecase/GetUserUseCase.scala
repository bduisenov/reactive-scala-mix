package org.scalalang.boot.reactive.core.usecase

import cats.data.EitherT
import cats.effect.IO
import org.scalalang.boot.reactive.core.document.{HasUser, HasUserId}
import org.scalalang.boot.reactive.service.UserService

class GetUserUseCase[T <: HasUserId with HasUser](private val userService: UserService) extends UseCase[T] {
  override def apply(doc: T): EitherT[IO, String, T] = {
    val result: Option[IO[Either[String, T]]] = doc.userId.map { id =>
      userService.getUser(id).map {
        case Some(user) => Right(doc.user(user))
        case _ => Left("user not found")
      }
    }

    result match {
      case Some(doc) => EitherT(doc)
      case _ => EitherT.leftT("empty arg")
    }
  }
}

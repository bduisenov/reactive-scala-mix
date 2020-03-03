package org.scalalang.boot.reactive.core.usecase

import cats.data.EitherT
import cats.effect.IO
import org.scalalang.boot.reactive.core.document.HasUser
import org.scalalang.boot.reactive.repository.UserEntity

class ValidateUserUseCase[T <: HasUser] extends UseCase[T] {
  override def apply(doc: T): EitherT[IO, String, T] = doc.user match {
    case Some(UserEntity(_, _, _)) => EitherT.pure(doc)
    case _ => EitherT.leftT("not valid")
  }
}

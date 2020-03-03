package org.scalalang.boot.reactive.core.usecase

import cats.data.EitherT
import cats.effect.IO
import org.scalalang.boot.reactive.core.document.HasUser
import org.scalalang.boot.reactive.service.PasswordService

import scala.util.{Failure, Success}

class HashUserPasswordUseCase[T <: HasUser](private val passwordService: PasswordService) extends UseCase[T] {
  override def apply(doc: T): EitherT[IO, String, T] = doc.user match {
    case Some(user) => passwordService.encode(user.password) match {
      case Success(encodedPwd) => EitherT.pure(doc.user(user.copy(password = encodedPwd)))
      case Failure(e) => EitherT.leftT(e.getMessage)
    }
    case _ => EitherT.leftT("not valid")
  }
}

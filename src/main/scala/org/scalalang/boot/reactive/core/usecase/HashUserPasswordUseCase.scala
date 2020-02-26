package org.scalalang.boot.reactive.core.usecase

import org.scalalang.boot.reactive.core.document.HasUser
import org.scalalang.boot.reactive.service.PasswordService

import scala.util.{Failure, Success}

class HashUserPasswordUseCase[T <: HasUser](private val passwordService: PasswordService) extends UseCase[T] {
  override def apply(doc: T): Either[String, T] = doc.user match {
    case Some(user) => passwordService.encode(user.password) match {
      case Success(encodedPwd) => Right(doc.user(user.copy(password = encodedPwd)))
      case Failure(e) => Left(e.getMessage)
    }
    case _ => Left("not valid")
  }
}

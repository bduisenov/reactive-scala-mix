package org.scalalang.boot.reactive.core.usecase

import org.scalalang.boot.reactive.core.document.HasUser
import org.scalalang.boot.reactive.repository.UserEntity

class ValidateUserUseCase[T <: HasUser] extends UseCase[T] {
  override def apply(doc: T): Either[String, T] = doc.user match {
    case Some(UserEntity(_, _, _)) => Right(doc)
    case _ => Left("not valid")
  }
}

package org.scalalang.boot.reactive.controller

import org.scalalang.boot.reactive.core.document.Attrs.{user, userId}
import org.scalalang.boot.reactive.core.document.Document
import org.scalalang.boot.reactive.core.usecase.UseCase
import org.scalalang.boot.reactive.repository.UserEntity
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

trait UserController {
  def getUser(id: Long): Mono[ServerResponse]

  def saveUser(json: Map[String, String]): Mono[ServerResponse]
}

class UserControllerImpl(private val getUserUseCase: UseCase[Document],
                         private val saveUserUseCase: UseCase[Document]) extends UserController {

  override def getUser(id: Long): Mono[ServerResponse] =
    getUserUseCase(Document(userId -> id)) match {
      case Right(user) => ServerResponse.ok().bodyValue(user)
      case Left(error) => ServerResponse.badRequest().body(BodyInserters.fromValue(error))
    }

  override def saveUser(json: Map[String, String]): Mono[ServerResponse] =
    saveUserUseCase(Document(user -> (() => UserEntity(name = json("name"))))) match {
      case Right(user) => ServerResponse.ok().bodyValue(user)
      case Left(error) => ServerResponse.badRequest().body(BodyInserters.fromValue(error))
    }
}

package org.scalalang.boot.reactive.controller

import java.net.URI

import org.scalalang.boot.reactive.core.document.Document
import org.scalalang.boot.reactive.core.document.Document._
import org.scalalang.boot.reactive.core.router.{RouteContext, Router}
import org.scalalang.boot.reactive.repository.UserEntity
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

trait UserController {
  def getUser(id: Long): Mono[ServerResponse]

  def saveUser(json: Map[String, String]): Mono[ServerResponse]
}

class UserControllerImpl(private val getUserUseCase: Document => Either[String, Document],
                         private val validateUserUseCase: Document => Either[String, Document],
                         private val hashUserPasswordUseCase: Document => Either[String, Document],
                         private val saveUserUseCase: Document => Either[String, Document]) extends UserController {

  val getUserRoute: Document => Either[String, Document] = Router[Document, String](route => route
    .flatMap(getUserUseCase))()

  val createUserRoute: Document => Either[String, Document] = Router[Document, String](route => route
    .flatMap(validateUserUseCase)
    .flatMap(hashUserPasswordUseCase)
    .recover((lastState, _) => Right(lastState)) // if something happens, try to save anyway
    .flatMap(saveUserUseCase)) {
    (routeContext: RouteContext[Document, String]) => routeContext.historyRecords.foreach(println)
  }

  override def getUser(id: Long): Mono[ServerResponse] =
    getUserRoute(Document(userId -> id)) match {
      case Right(doc) => doc.user.fold(ServerResponse.badRequest().bodyValue("not found")) {
        user => ServerResponse.ok().bodyValue(user)
      }
      case Left(error) => ServerResponse.badRequest().bodyValue(error)
    }

  override def saveUser(json: Map[String, String]): Mono[ServerResponse] =
    createUserRoute(Document(user -> (() => UserEntity(name = json("name"), password = json("password"))))) match {
      case Right(doc) => doc.user.fold(ServerResponse.badRequest().bodyValue("not found")) {
        user => ServerResponse.created(URI.create(user.id.get.toString)).bodyValue(user)
      }
      case Left(error) => ServerResponse.badRequest().bodyValue(error)
    }
}

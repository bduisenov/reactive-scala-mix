package org.scalalang.boot.reactive.controller

import java.net.URI

import cats.data.EitherT
import cats.effect.IO
import org.scalalang.boot.reactive.core.document.Document
import org.scalalang.boot.reactive.core.document.Document._
import org.scalalang.boot.reactive.core.router.{RouteContext, Router}
import org.scalalang.boot.reactive.core.usecase.UseCase
import org.scalalang.boot.reactive.repository.UserEntity
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.{badRequest, created, ok}
import reactor.core.publisher.Mono
import reactor.core.scala.publisher.SMono.fromFuture

trait UserController {
  def getUser(id: Long): Mono[ServerResponse]

  def saveUser(json: Map[String, String]): Mono[ServerResponse]
}

class UserControllerImpl(private val getUserUseCase: UseCase[Document],
                         private val validateUserUseCase: UseCase[Document],
                         private val hashUserPasswordUseCase: UseCase[Document],
                         private val saveUserUseCase: UseCase[Document]) extends UserController {
  val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.parasitic

  val getUserRoute: Document => EitherT[IO, String, Document] = Router[String, Document](route => route
    .flatMap(getUserUseCase))()

  val createUserRoute: Document => EitherT[IO, String, Document] = Router[String, Document](route => route
    .flatMap(validateUserUseCase)
    .flatMap(hashUserPasswordUseCase)
    .recover((_, lastState) => EitherT.rightT(lastState)) // if something happens, try to save anyway
    .nest((nestedRoute, either) => either match {
      case Right(_) => nestedRoute.flatMap(hashUserPasswordUseCase) // revert the changes to password
      case _ => nestedRoute.flatMap(_ => EitherT.leftT("failed"))
    })
    .flatMap(saveUserUseCase)) {
    (routeContext: RouteContext[String, Document]) => routeContext.historyRecords.foreach(println)
  }

  override def getUser(id: Long): Mono[ServerResponse] = {
    val document = Document(userId -> id)
    val result = getUserRoute(document).value.unsafeToFuture()

    fromFuture(result)(ec).asJava().flatMap {
      case Right(doc) => doc.user.fold(badRequest().bodyValue("not found")) {
        user => ok().bodyValue(user)
      }
      case Left(error) => badRequest().bodyValue(error)
    }
  }

  override def saveUser(json: Map[String, String]): Mono[ServerResponse] = {
    val document = Document(user -> (() => UserEntity(name = json("name"), password = json("password"))))
    val result = createUserRoute(document).value.unsafeToFuture()


    fromFuture(result)(ec).asJava().flatMap {
      case Right(doc) => doc.user.fold(badRequest().bodyValue("not found")) {
        user => created(URI.create(user.id.get.toString)).bodyValue(user)
      }
      case Left(error) => badRequest().bodyValue(error)
    }
  }
}

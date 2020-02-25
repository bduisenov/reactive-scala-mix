package org.scalalang.boot.reactive.controller

import org.scalalang.boot.reactive.repository.UserEntity
import org.scalalang.boot.reactive.service.UserService
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

trait UserController {
  def getUser(id: Long): Mono[ServerResponse]

  def saveUser(json: Map[String, String]): Mono[ServerResponse]
}

class UserControllerImpl(private val userService: UserService) extends UserController {

  override def getUser(id: Long): Mono[ServerResponse] =
    userService.getUser(id) match {
      case Some(user) => ServerResponse.ok().bodyValue(user)
      case None => ServerResponse.badRequest().build()
    }

  override def saveUser(json: Map[String, String]): Mono[ServerResponse] =
    json.get("name").map(x => UserEntity(name = x)).map(userService.saveUser) match {
      case Some(user) => ServerResponse.ok().bodyValue(user)
      case None => ServerResponse.badRequest().build()
    }
}

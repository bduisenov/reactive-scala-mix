package org.scalalang.boot.reactive

import org.scalalang.boot.reactive.controller.UserController
import org.springframework.web.reactive.function.server.RequestPredicates.{GET, POST}
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.{RouterFunction, ServerRequest, ServerResponse}

object Routes {

  def userRouterFunction(userController: UserController): RouterFunction[ServerResponse] =
    route(GET("/{id}"), (request: ServerRequest) =>
      userController.getUser(request.pathVariable("id").toLong))
      .andRoute(POST("/"), (request: ServerRequest) =>
        request.bodyToMono(classOf[Map[String, String]]).flatMap(userController.saveUser))
}

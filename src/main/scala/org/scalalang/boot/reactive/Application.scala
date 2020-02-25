package org.scalalang.boot.reactive

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.scalalang.boot.reactive.controller.UserControllerImpl
import org.scalalang.boot.reactive.repository.UserRepositoryImpl
import org.scalalang.boot.reactive.service.UserServiceImpl
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.RequestPredicates.{GET, POST}
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.{RouterFunction, ServerRequest, ServerResponse}

@SpringBootApplication
class Application {

  @Bean
  def userRepository = new UserRepositoryImpl

  @Bean
  def userService = new UserServiceImpl(userRepository)

  @Bean
  def userController = new UserControllerImpl(userService)

  @Bean
  def routerFunction: RouterFunction[ServerResponse] =
    route(GET("/{id}"), (request: ServerRequest) =>
      userController.getUser(request.pathVariable("id").toLong))
      .andRoute(POST("/"), (request: ServerRequest) =>
        request.bodyToMono(classOf[Map[String, String]])
          .flatMap(json => userController.saveUser(json)))

  @Bean
  def objectMapperScalaModule: Jackson2ObjectMapperBuilderCustomizer =
    builder => builder.modules(DefaultScalaModule)
}

object Application {
  def main(args: Array[String]): Unit = SpringApplication.run(classOf[Application], args: _*)
}
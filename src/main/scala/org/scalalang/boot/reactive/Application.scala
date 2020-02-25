package org.scalalang.boot.reactive

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.scalalang.boot.reactive.controller.{UserController, UserControllerImpl}
import org.scalalang.boot.reactive.repository.UserRepositoryImpl
import org.scalalang.boot.reactive.service.UserServiceImpl
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType.REACTIVE
import org.springframework.boot.autoconfigure.jackson.{Jackson2ObjectMapperBuilderCustomizer, JacksonInitializer, JacksonProperties}
import org.springframework.boot.autoconfigure.web.reactive._
import org.springframework.boot.autoconfigure.web.{ResourceProperties, ServerProperties}
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.{ApplicationContext, ApplicationContextInitializer}
import org.springframework.web.reactive.function.server.RequestPredicates.{GET, POST}
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.{RouterFunction, ServerRequest, ServerResponse}

class Application

object UserRouterFunction extends (UserController => RouterFunction[ServerResponse]) {
  override def apply(userController: UserController): RouterFunction[ServerResponse] =
    route(GET("/{id}"), (request: ServerRequest) =>
      userController.getUser(request.pathVariable("id").toLong))
      .andRoute(POST("/"), (request: ServerRequest) =>
        request.bodyToMono(classOf[Map[String, String]]).flatMap(userController.saveUser))
}

object Application {
  def main(args: Array[String]): Unit = {
    val appInitializer = new ApplicationContextInitializer[GenericApplicationContext] {
      override def initialize(context: GenericApplicationContext): Unit = {
        context.registerBean(classOf[UserRepositoryImpl])
        context.registerBean(classOf[UserServiceImpl])
        context.registerBean(classOf[UserControllerImpl])

        context.registerBean(classOf[RouterFunction[ServerResponse]], () => UserRouterFunction(context.getBean(classOf[UserController])))
        context.registerBean(classOf[Jackson2ObjectMapperBuilderCustomizer], () => (builder => builder.modules(DefaultScalaModule)): Jackson2ObjectMapperBuilderCustomizer)
      }
    }

    val serverProperties = new ServerProperties()
    val resourceProperties = new ResourceProperties()
    val webFluxProperties = new WebFluxProperties()
    val jacksonProperties = new JacksonProperties()

    val engine = new NettyReactiveWebServerFactory
    engine.setPort(8080)

    val app = new SpringApplication(classOf[Application]) {
      override def load(context: ApplicationContext, sources: Array[AnyRef]): Unit = {
        // We don't want the annotation bean definition reader
      }
    }

    app.setWebApplicationType(REACTIVE)
    app.setApplicationContextClass(classOf[ReactiveWebServerApplicationContext])
    app.addInitializers(
      new JacksonInitializer(jacksonProperties),
      new JacksonJsonCodecInitializer(false),
      new ReactiveWebServerInitializer(serverProperties, resourceProperties, webFluxProperties, engine),
      appInitializer)

    System.setProperty("spring.backgroundpreinitializer.ignore", "true")
    System.setProperty("spring.main.lazy-initialization", "true")

    app.run(args: _*)
  }
}
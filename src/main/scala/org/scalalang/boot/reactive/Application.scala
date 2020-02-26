package org.scalalang.boot.reactive

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.scalalang.boot.reactive.controller.{UserController, UserControllerImpl}
import org.scalalang.boot.reactive.core.document.Document
import org.scalalang.boot.reactive.core.usecase.{GetUserUseCase, SaveUserUseCase}
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
import org.springframework.web.reactive.function.server.{RouterFunction, ServerResponse}

class Application

object Application {

  val app: SpringApplication = new SpringApplication(classOf[Application]) {
    setWebApplicationType(REACTIVE)
    setApplicationContextClass(classOf[ReactiveWebServerApplicationContext])
    addInitializers(
      new JacksonInitializer(new JacksonProperties()),
      new JacksonJsonCodecInitializer(false),
      new ReactiveWebServerInitializer(new ServerProperties(), new ResourceProperties(), new WebFluxProperties(), new NettyReactiveWebServerFactory(8080)),
      (context => {
        context.registerBean("getUserUseCase", classOf[GetUserUseCase[Document]])
        context.registerBean("saveUserUseCase", classOf[SaveUserUseCase[Document]])

        context.registerBean(classOf[UserRepositoryImpl])
        context.registerBean(classOf[UserServiceImpl])
        context.registerBean(classOf[UserControllerImpl])

        context.registerBean(classOf[RouterFunction[ServerResponse]], () => Routes.userRouterFunction(context.getBean(classOf[UserController])))
        context.registerBean(classOf[Jackson2ObjectMapperBuilderCustomizer], () => (builder => builder.modules(DefaultScalaModule)): Jackson2ObjectMapperBuilderCustomizer)
      }): ApplicationContextInitializer[GenericApplicationContext])

    override def load(context: ApplicationContext, sources: Array[AnyRef]): Unit = {
      // We don't want the annotation bean definition reader
    }
  }

  def main(args: Array[String]): Unit = {
    System.setProperty("spring.backgroundpreinitializer.ignore", "true")
    System.setProperty("spring.main.lazy-initialization", "true")

    app.run(args: _*)
  }
}
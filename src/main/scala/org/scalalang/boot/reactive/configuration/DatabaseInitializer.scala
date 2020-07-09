package org.scalalang.boot.reactive.configuration

import io.r2dbc.postgresql.{PostgresqlConnectionConfiguration, PostgresqlConnectionFactory}
import org.scalalang.boot.reactive.repository.UserRepositoryImpl
import org.springframework.boot.autoconfigure.data.r2dbc.PostgresqlR2dbcProperties
import org.springframework.boot.context.properties.FunctionalConfigurationPropertiesBinder
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator

class DatabaseInitializer extends ApplicationContextInitializer[GenericApplicationContext] {
  override def initialize(context: GenericApplicationContext): Unit = {
    val bindedProperties = new FunctionalConfigurationPropertiesBinder(context)
      .bind("postgres", Bindable.of(classOf[PostgresqlR2dbcProperties])).get()
    context.registerBean(s"${classOf[PostgresqlR2dbcProperties].getSimpleName}ConfigurationProperties", classOf[PostgresqlR2dbcProperties], () => bindedProperties)
    context.registerBean(classOf[PostgresqlConnectionFactory], () => {
      val properties = context.getBean(classOf[PostgresqlR2dbcProperties])
      new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
        .host(properties.getHost)
        .port(properties.getPort)
        .database(properties.getDatabase)
        .username(properties.getUsername)
        .password(properties.getPassword)
        .build())
    })
    context.registerBean(classOf[DatabaseClient], () => DatabaseClient.builder().connectionFactory(context.getBean(classOf[PostgresqlConnectionFactory])).build())
    context.registerBean(classOf[TransactionalOperator], () => TransactionalOperator.create(context.getBean(classOf[ReactiveTransactionManager])))
    context.registerBean(classOf[R2dbcTransactionManager])
    context.registerBean(classOf[UserRepositoryImpl])
  }
}

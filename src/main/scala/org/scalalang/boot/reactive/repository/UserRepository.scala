package org.scalalang.boot.reactive.repository

import java.util.Optional
import java.util.concurrent.{CancellationException, CompletableFuture, CompletionException}

import cats.effect.IO
import org.springframework.data.r2dbc.core.DatabaseClient

case class UserEntity(id: Optional[java.lang.Integer] = Optional.empty(), name: String, password: String)

trait UserRepository {
  def save(userEntity: UserEntity): IO[UserEntity]

  def findById(id: Long): IO[Option[UserEntity]]

  def deleteAll(): IO[Unit]
}

class UserRepositoryImpl(private val client: DatabaseClient) extends UserRepository {

  // language=SQL
  private val insertStatement = "INSERT INTO users (name) VALUES(:name) RETURNING *"

  // language=SQL
  private val selectByIdStatement = "SELECT * FROM users WHERE id = :id"

  // language=SQL
  private val deleteAllStatement = "DELETE FROM users"

  override def save(userEntity: UserEntity): IO[UserEntity] = {
    val promise = client.execute(insertStatement)
      .bind("name", userEntity.name)
      .as(classOf[UserEntity])
      .fetch()
      .one()
      .toFuture

    promiseToIo(promise)
  }

  override def findById(id: Long): IO[Option[UserEntity]] = {
    val promise = client.execute(selectByIdStatement)
      .bind("id", id)
      .as(classOf[UserEntity])
      .fetch()
      .one()
      .map(e => Option(e))
      .toFuture

    promiseToIo(promise)
  }

  private def promiseToIo[T](promise: CompletableFuture[T]): IO[T] = {
    IO.cancelable(cb => {
      promise.handle[Unit]((result: T, err: Throwable) => {
        err match {
          case null => cb(Right(result))
          case _: CancellationException => ()
          case ex: CompletionException if ex.getCause ne null => cb(Left(ex.getCause))
          case ex => cb(Left(ex))
        }
      })
      IO(promise.cancel(true))
    })
  }

  override def deleteAll(): IO[Unit] = {
    val promise: CompletableFuture[Unit] = client.execute(deleteAllStatement)
      .`then`()
      .map(_ => null: Unit)
      .toFuture

    promiseToIo(promise)
  }
}
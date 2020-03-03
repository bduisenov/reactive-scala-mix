package org.scalalang.boot.reactive.repository

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

import cats.effect.IO

import scala.jdk.OptionConverters._

case class UserEntity(id: Option[Long] = None, name: String, password: String)

trait UserRepository {
  def save(userEntity: UserEntity): IO[UserEntity]

  def findById(id: Long): IO[Option[UserEntity]]

  def deleteAll(): IO[Unit]
}

class UserRepositoryImpl extends UserRepository {

  private val cache: java.util.List[UserEntity] = new CopyOnWriteArrayList
  private val seq: AtomicLong = new AtomicLong

  override def save(userEntity: UserEntity): IO[UserEntity] = {
    val x = userEntity.copy(id = Option(seq.incrementAndGet))
    cache.add(x)
    IO.pure(x)
  }

  override def findById(id: Long): IO[Option[UserEntity]] = {
    val result = cache.stream().filter(_.id contains id).findFirst().toScala
    IO.pure(result)
  }

  override def deleteAll(): IO[Unit] =
    IO.pure(cache.clear())
}
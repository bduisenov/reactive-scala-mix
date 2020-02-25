package org.scalalang.boot.reactive.repository

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

import scala.jdk.OptionConverters._

case class UserEntity(id: Option[Long] = None, name: String)

trait UserRepository {
  def save(userEntity: UserEntity): UserEntity

  def findById(id: Long): Option[UserEntity]

  def deleteAll()
}

class UserRepositoryImpl extends UserRepository {

  private val cache: java.util.List[UserEntity] = new CopyOnWriteArrayList
  private val seq: AtomicLong = new AtomicLong

  override def save(userEntity: UserEntity): UserEntity = {
    val x = userEntity.copy(id = Option(seq.incrementAndGet))
    cache.add(x)
    x
  }

  override def findById(id: Long): Option[UserEntity] =
    cache.stream().filter(_.id contains id).findFirst().toScala

  override def deleteAll(): Unit = cache.clear()
}
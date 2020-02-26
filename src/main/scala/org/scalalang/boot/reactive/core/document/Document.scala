package org.scalalang.boot.reactive.core.document

import org.scalalang.boot.reactive.repository.UserEntity

import scala.reflect.ClassTag
import scala.util.{Success, Try}

sealed trait LiftedThunkDocument {
  def properties: Map[String, Any]

  def creator(xs: Map[String, Any]): this.type

  def apply[T: ClassTag](key: String): Option[T] = properties.get(key) match {
    case Some(x) => x match {
      case y: (() => Any) => Try(y()) match {
        case Success(z: T) => Some(z)
        case _ => None
      }
      case y: T => Some(y)
      case _ => None
    }
    case _ => None
  }
}

sealed trait HasUserId extends LiftedThunkDocument {
  self =>
  def userId: Option[Long] = self[Long](Document.userId)
}

sealed trait HasUser extends LiftedThunkDocument {
  self =>
  def user: Option[UserEntity] = self[UserEntity](Document.user)

  def user(userEntity: UserEntity): this.type = creator(properties + (Document.user -> userEntity))
}

sealed class Document private(private val xs: Map[String, Any])
  extends HasUserId
    with HasUser {

  override def properties: Map[String, Any] = xs

  override def creator(xs: Map[String, Any]): this.type = new Document(xs).asInstanceOf[this.type]

  override def toString: String = xs.toString
}

object Document {

  val userId = "userId"
  val user = "user"

  def apply(elems: (String, Any)*) = new Document(Map.from(elems))
}


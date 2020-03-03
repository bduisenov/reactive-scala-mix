package org.scalalang.boot.reactive.core.document

import cats.Eval
import org.scalalang.boot.reactive.repository.UserEntity

import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.util.{Success, Try}

sealed trait LiftedThunkDocument {
  def properties: Map[String, Any]

  def creator(xs: Map[String, Any]): this.type

  def apply[T: ClassTag](key: String): Option[T] = {
    @tailrec def apply0(x: Any): Option[T] = x match {
      case y: Eval[Any] => apply0(Try(y.value))
      case y: (() => Any) => apply0(Try(y()))
      case Success(y) => apply0(y)
      case Some(y) => apply0(y)
      case y: T => Some(y)
      case _ => None
    }

    apply0(properties.get(key))
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

  def apply(elems: (String, Any)*) = new Document(Map.from(elems).view.mapValues {
    case f: (() => Any) => Eval.later(f())
    case x => x
  }.toMap)
}


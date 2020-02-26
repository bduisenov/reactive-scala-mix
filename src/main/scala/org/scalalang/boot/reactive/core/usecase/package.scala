package org.scalalang.boot.reactive.core

package object usecase {

  trait UseCase[T] extends (T => Either[String, T])

}

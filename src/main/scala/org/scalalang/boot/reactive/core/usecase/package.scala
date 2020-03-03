package org.scalalang.boot.reactive.core

import cats.data.EitherT
import cats.effect.IO

package object usecase {

  trait UseCase[T] extends (T => EitherT[IO, String, T])

}

package org.scalalang.boot.reactive.core.router

import cats.data.EitherT
import cats.effect.IO

abstract private[core] class RouterFunctions {

  def apply[T, P](route: RouterBuilder[T, P] => RouterBuilder[T, P])(routeContextConsumer: RouteContext[T, P] => Unit = (_: RouteContext[T, P]) => ()): Router[T, P] =
    route(new RouterBuilder[T, P](routeContextConsumer)).build()
}

object Router extends RouterFunctions

sealed class Router[T, P](val route: T => IO[(RouteContext[T, P], Either[P, T])],
                          val routeContextConsumer: RouteContext[T, P] => Unit = (_: RouteContext[T, P]) => ()) extends (T => EitherT[IO, P, T]) {

  override def apply(initialState: T): EitherT[IO, P, T] = {
    EitherT(route(initialState).map { case (routeContext, result) =>
      routeContextConsumer(routeContext)
      result
    })
  }
}

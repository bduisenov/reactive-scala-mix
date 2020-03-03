package org.scalalang.boot.reactive.core.router

import cats.data.EitherT
import cats.effect.IO

abstract private[core] class RouterFunctions {

  def apply[P, T](route: RouterBuilder[P, T] => RouterBuilder[P, T])(routeContextConsumer: RouteContext[P, T] => Unit = (_: RouteContext[P, T]) => ()): Router[P, T] =
    route(new RouterBuilder[P, T](routeContextConsumer)).build()
}

object Router extends RouterFunctions

sealed class Router[P, T](val route: T => IO[(RouteContext[P, T], Either[P, T])],
                          val routeContextConsumer: RouteContext[P, T] => Unit = (_: RouteContext[P, T]) => ()) extends (T => EitherT[IO, P, T]) {

  override def apply(initialState: T): EitherT[IO, P, T] = {
    EitherT(route(initialState).map { case (routeContext, result) =>
      routeContextConsumer(routeContext)
      result
    })
  }
}

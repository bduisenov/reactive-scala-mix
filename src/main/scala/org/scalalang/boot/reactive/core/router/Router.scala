package org.scalalang.boot.reactive.core.router

abstract private[core] class RouterFunctions {

  def apply[T, P](route: RouterBuilder[T, P] => RouterBuilder[T, P])(routeContextConsumer: RouteContext[T, P] => Unit = (_: RouteContext[T, P]) => ()): Router[T, P] =
    route(new RouterBuilder[T, P](routeContextConsumer)).build()
}

object Router extends RouterFunctions

sealed class Router[T, P](val route: T => (RouteContext[T, P], Either[P, T]),
                          val routeContextConsumer: RouteContext[T, P] => Unit = (_: RouteContext[T, P]) => ()) extends (T => Either[P, T]) {

  override def apply(initialState: T): Either[P, T] = {
    val (routeContext, result) = route(initialState)
    routeContextConsumer(routeContext)
    result
  }
}

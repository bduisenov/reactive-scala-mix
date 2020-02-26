package org.scalalang.boot.reactive.core

import java.time.Duration

import cats.data.State

package object router {

  sealed case class RouteHistoryRecord[T, P](private val in: T, private val out: Either[P, T], private val timeTakenNanos: Int, private val functionName: String)

  sealed case class RouteContext[T, P](state: T, historyRecords: List[RouteHistoryRecord[T, P]] = List())

  type ExecutionContext[T, P] = (List[RouteHistoryRecord[T, P]], Either[P, T])

  sealed trait RouterFunction[T, P] {
    def apply(state: T, either: Either[P, T]): ExecutionContext[T, P] =
      if (either.isRight) internalApply(state, either) else (List(), either)

    def internalApply(state: T, either: Either[P, T]): ExecutionContext[T, P] = ???

    def execute(function: Either[P, T] => Either[P, T], either: Either[P, T]): (Either[P, T], Duration) = { // ???
      val startTime = System.nanoTime()
      val result = function.apply(either)
      val elapsed = Duration.ofNanos(System.nanoTime() - startTime)

      (result, elapsed)
    }
  }

  sealed class RouterBuilder[T, P](val route: State[RouteContext[T, P], Either[P, T]],
                                   val routeContextConsumer: RouteContext[T, P] => Unit) {

    def this(routeContextConsumer: RouteContext[T, P] => Unit) =
      this(State(context => (context, Right(context.state))), routeContextConsumer)

    def flatMap(fun: T => Either[P, T]): RouterBuilder[T, P] = {
      val name = fun.getClass.getSimpleName
      new RouterBuilder[T, P](route.flatMap(thunk(State.pure(simple(either => either.flatMap(fun), name)))), routeContextConsumer)
    }

    def simple(function: Either[P, T] => Either[P, T], name: String): RouterFunction[T, P] =
      new RouterFunction[T, P] {
        override def internalApply(state: T, either: Either[P, T]): ExecutionContext[T, P] = {
          val (result, elapsed) = execute(function, either)
          val rhr = new RouteHistoryRecord[T, P](either.right.get, result, elapsed.getNano, name)

          (List(rhr), result)
        }
      }

    def thunk(state: State[RouteContext[T, P], RouterFunction[T, P]]): Either[P, T] => State[RouteContext[T, P], Either[P, T]] =
      either =>
        state.flatMap(function => State(context => {
          val (newHistoryRecords, result) = function(context.state, either)

          val updatedContext = new RouteContext[T, P](result.getOrElse(context.state), context.historyRecords ++ newHistoryRecords)

          (updatedContext, result)
        }))

    def build(): Router[T, P] =
      new Router[T, P](initialState => route.run(new RouteContext[T, P](initialState)).value, routeContextConsumer)
  }

}

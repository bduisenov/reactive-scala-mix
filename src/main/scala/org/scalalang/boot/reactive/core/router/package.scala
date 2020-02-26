package org.scalalang.boot.reactive.core

import java.time.Duration

import cats.data.State

package object router {

  sealed case class RouteHistoryRecord[T, P](private val in: T, private val out: Either[P, T], private val timeTakenNanos: Int)

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

    def createRouteHistoryRecord(inArg: T, executionResult: (Either[P, T], Duration)): RouteHistoryRecord[T, P] = {
      val (result, elapsed) = executionResult
      new RouteHistoryRecord[T, P](inArg, result, elapsed.getNano)
    }
  }

  sealed class RouterBuilder[T, P](val route: State[RouteContext[T, P], Either[P, T]],
                                   val routeContextConsumer: RouteContext[T, P] => Unit) {

    def this(routeContextConsumer: RouteContext[T, P] => Unit) =
      this(State(context => (context, Right(context.state))), routeContextConsumer)

    def flatMap(fun: T => Either[P, T]): RouterBuilder[T, P] =
      new RouterBuilder[T, P](route.flatMap(thunk(State.pure(simple(either => either.flatMap(fun))))), routeContextConsumer)

    def simple(function: Either[P, T] => Either[P, T]): RouterFunction[T, P] =
      new RouterFunction[T, P] {
        override def internalApply(state: T, either: Either[P, T]): ExecutionContext[T, P] = {
          val executionResult = execute(function, either)
          val rhr = createRouteHistoryRecord(either.right.get, executionResult)

          (List(rhr), executionResult._1)
        }
      }

    def thunk(stateM: State[RouteContext[T, P], RouterFunction[T, P]]): Either[P, T] => State[RouteContext[T, P], Either[P, T]] = either =>
      stateM.flatMap(fun => State(context => {
        val (historyRecords, result) = fun(context.state, either)

        val updatedContext = new RouteContext[T, P](result.getOrElse(context.state), context.historyRecords ++ historyRecords)

        (updatedContext, result)
      }))

    def build(): Router[T, P] =
      new Router[T, P](initialState => route.run(new RouteContext[T, P](initialState)).value, routeContextConsumer)
  }

}

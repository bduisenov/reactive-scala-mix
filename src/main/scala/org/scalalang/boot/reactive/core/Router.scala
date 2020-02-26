package org.scalalang.boot.reactive.core

import java.time.Duration

import cats.data.State

sealed class RouteHistoryRecord[T, P](private val in: T, private val out: Either[P, T], private val timeTakenNanos: Int)

sealed class RouteContext[T, P](val state: T, val historyRecords: List[RouteHistoryRecord[T, P]] = List())

sealed trait RouterFunction[T, P] {
  def apply(state: T, either: Either[P, T]): (List[RouteHistoryRecord[T, P]], Either[P, T]) =
    if (either.isRight) internalApply(state, either) else (List(), either)

  def internalApply(state: T, either: Either[P, T]): (List[RouteHistoryRecord[T, P]], Either[P, T]) = ???

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

sealed class RouterBuilder[T, P](val route: State[RouteContext[T, P], Either[P, T]]) {

  def this() =
    this(State(context => (context, Right(context.state))))

  def flatMap(fun: T => Either[P, T]): RouterBuilder[T, P] =
    new RouterBuilder[T, P](route.flatMap(thunk(State.pure(simple(either => either.flatMap(fun))))))

  def simple(function: Either[P, T] => Either[P, T]): RouterFunction[T, P] =
    new RouterFunction[T, P] {
      override def internalApply(state: T, either: Either[P, T]): (List[RouteHistoryRecord[T, P]], Either[P, T]) = {
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
    new Router[T, P](initialState => route.run(new RouteContext[T, P](initialState)).value)
}

abstract private[core] class RouterFunctions {

  def apply[T, P](route: RouterBuilder[T, P] => RouterBuilder[T, P]): Router[T, P] =
    route(new RouterBuilder[T, P]()).build()
}

object Router extends RouterFunctions

sealed class Router[T, P](val route: T => (RouteContext[T, P], Either[P, T])) extends (T => Either[P, T]) {

  override def apply(initialState: T): Either[P, T] = {
    val executionResult = route(initialState)
    executionResult._2
  }
}

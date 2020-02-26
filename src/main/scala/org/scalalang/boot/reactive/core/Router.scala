package org.scalalang.boot.reactive.core

import java.time.Duration

import cats.data.State

sealed class RouteHistoryRecord[T, P](private val in: T, private val out: Either[P, T], private val timeTakenNanos: Int)

sealed class RouteContext[T, P](val state: T, val historyRecords: List[RouteHistoryRecord[T, P]] = List())

sealed case class ExecutionContext[T, P](historyRecords: List[RouteHistoryRecord[T, P]], result: Either[P, T])

sealed trait RouterFunction[T, P] {
  def apply(state: T, either: Either[P, T]): ExecutionContext[T, P] = {
    if (either.isRight) internalApply(state, either) else new ExecutionContext[T, P](List(), either)
  }

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

sealed class RouterBuilder[T, P]  {
  self =>

  def apply(route1: RouterBuilder[T, P] => RouterBuilder[T, P]): Router[T, P] = {
    val builder = new RouterBuilder[T, P]()
    route1(builder).build()
  }
  
  var route: State[RouteContext[T, P], Either[P, T]] = State(context => (context, Right(context.state)))

  def flatMap(fun: T => Either[P, T]): self.type = {
    route = route.flatMap(thunk(State.pure(simple(either => either.flatMap(fun)))))
    this
  }

  def simple(function: Either[P, T] => Either[P, T]): RouterFunction[T, P] = {
    new RouterFunction[T, P] {
      override def internalApply(state: T, either: Either[P, T]): ExecutionContext[T, P] = {
        val executionResult = execute(function, either)
        val rhr = createRouteHistoryRecord(either.right.get, executionResult)

        new ExecutionContext[T, P](List(rhr), executionResult._1)
      }
    }
  }

  def thunk(stateM: State[RouteContext[T, P], RouterFunction[T, P]]): Either[P, T] => State[RouteContext[T, P], Either[P, T]] = either =>
    stateM.flatMap(fun => State(context => {
      val execContext = fun(context.state, either)

      val historyRecords = execContext.historyRecords
      val result = execContext.result

      val updatedHistoryRecords = context.historyRecords ++ historyRecords
      val updatedContext = new RouteContext[T, P](result.getOrElse(context.state), updatedHistoryRecords)

      (updatedContext, result)
    }))

  def build(): Router[T, P] = {
    new Router[T, P](initialState => route.run(new RouteContext[T, P](initialState)).value)
  }
}

sealed class Router[T, P](val route: T => (RouteContext[T, P], Either[P, T])) extends (T => Either[P, T]) {

  override def apply(initialState: T): Either[P, T] = {
    val executionResult = route(initialState)
    executionResult._2
  }
}

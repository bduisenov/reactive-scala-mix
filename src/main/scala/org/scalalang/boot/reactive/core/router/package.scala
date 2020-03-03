package org.scalalang.boot.reactive.core

import java.lang.System.nanoTime
import java.time.Duration

import cats.arrow.FunctionK.lift
import cats.data.{EitherT, State, StateT}
import cats.effect.IO

package object router {

  type ExecutionContext[P, T] = (List[RouteHistoryRecord[P, T]], Either[P, T])

  sealed trait RouterFunction[P, T] {
    def apply(state: T, either: Either[P, T]): IO[ExecutionContext[P, T]] =
      if (either.isRight) internalApply(state, either) else IO.pure((List(), either))

    def internalApply(state: T, either: Either[P, T]): IO[ExecutionContext[P, T]] = ???

    def execute(function: Either[P, T] => IO[Either[P, T]], either: Either[P, T]): IO[(Either[P, T], Duration)] = for {
      start <- IO(nanoTime())
      result <- function.apply(either)
      finish <- IO(nanoTime())
    } yield (result, Duration.ofNanos(finish - start))
  }

  sealed case class RouteHistoryRecord[P, T](private val in: T, private val out: Either[P, T], private val timeTakenNanos: Int, private val functionName: String)

  sealed case class RouteContext[P, T](state: T, historyRecords: List[RouteHistoryRecord[P, T]] = List())

  sealed class RouterBuilder[P, T](private val route: StateT[IO, RouteContext[P, T], Either[P, T]],
                                   private val routeContextConsumer: RouteContext[P, T] => Unit) {

    def flatMap(fun: T => EitherT[IO, P, T]): RouterBuilder[P, T] = {
      val name = fun.getClass.getSimpleName
      val routerFun = simple({
        case Right(value) => fun(value).value
        case left => IO.pure(left)
      }, name)

      val newRoute = route.flatMap(thunk(StateT.pure(routerFun)))

      new RouterBuilder[P, T](newRoute, routeContextConsumer)
    }

    def simple(function: Either[P, T] => IO[Either[P, T]], name: String): RouterFunction[P, T] =
      new RouterFunction[P, T] {
        override def internalApply(state: T, either: Either[P, T]): IO[ExecutionContext[P, T]] =
          execute(function, either).map { case (result, elapsed) =>
            (List(new RouteHistoryRecord[P, T](either.right.get, result, elapsed.getNano, name)), result)
          }
      }

    def thunk(stateT: StateT[IO, RouteContext[P, T], RouterFunction[P, T]]): Either[P, T] => StateT[IO, RouteContext[P, T], Either[P, T]] =
      either => stateT.flatMap(routerFun => StateT(context =>
        routerFun(context.state, either).map { case (newHistoryRecords, result) =>
          (new RouteContext[P, T](result.getOrElse(context.state), context.historyRecords ++ newHistoryRecords), result)
        }))

    def recover(recoverFun: (P, T) => EitherT[IO, P, T]): RouterBuilder[P, T] = {
      val newRoute = route.flatMap(either => StateT(context =>
        either.fold(problem => recoverFun(problem, context.state).value, value => IO.pure(Right(value)))
          .map((context, _))))

      new RouterBuilder[P, T](newRoute, routeContextConsumer)
    }

    def nest(nestedRoute: (RouterBuilder[P, T], Either[P, T]) => RouterBuilder[P, T]): RouterBuilder[P, T] = {
      val newRoute = route.flatMap(either =>
        nestedRoute(new RouterBuilder[P, T](routeContextConsumer), either).route)

      new RouterBuilder[P, T](newRoute, routeContextConsumer)
    }

    def this(routeContextConsumer: RouteContext[P, T] => Unit) =
      this(State[RouteContext[P, T], Either[P, T]](context => (context, Right(context.state))).mapK(lift(IO.eval)), routeContextConsumer)

    def build(): Router[P, T] =
      new Router[P, T](initialState => route.run(new RouteContext[P, T](initialState)), routeContextConsumer)
  }

}

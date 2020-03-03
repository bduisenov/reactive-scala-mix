package org.scalalang.boot.reactive.core

import java.lang.System.nanoTime
import java.time.Duration

import cats.arrow.FunctionK.lift
import cats.data.{EitherT, State, StateT}
import cats.effect.IO

package object router {

  sealed case class RouteHistoryRecord[T, P](private val in: T, private val out: Either[P, T], private val timeTakenNanos: Int, private val functionName: String)

  sealed case class RouteContext[T, P](state: T, historyRecords: List[RouteHistoryRecord[T, P]] = List())

  type ExecutionContext[T, P] = (List[RouteHistoryRecord[T, P]], Either[P, T])

  sealed trait RouterFunction[T, P] {
    def apply(state: T, either: Either[P, T]): IO[ExecutionContext[T, P]] =
      if (either.isRight) internalApply(state, either) else IO.pure((List(), either))

    def internalApply(state: T, either: Either[P, T]): IO[ExecutionContext[T, P]] = ???

    def execute(function: Either[P, T] => IO[Either[P, T]], either: Either[P, T]): IO[(Either[P, T], Duration)] = for {
      start <- IO(nanoTime())
      result <- function.apply(either)
      finish <- IO(nanoTime())
    } yield (result, Duration.ofNanos(finish - start))
  }

  sealed class RouterBuilder[T, P](private val route: StateT[IO, RouteContext[T, P], Either[P, T]],
                                   private val routeContextConsumer: RouteContext[T, P] => Unit) {

    def this(routeContextConsumer: RouteContext[T, P] => Unit) =
      this(State[RouteContext[T, P], Either[P, T]](context => (context, Right(context.state))).mapK(lift(IO.eval)), routeContextConsumer)

    def flatMap(fun: T => EitherT[IO, P, T]): RouterBuilder[T, P] = {
      val name = fun.getClass.getSimpleName
      val routerFun = simple({
        case Right(value) => fun(value).value
        case left => IO.pure(left)
      }, name)

      val newRoute = route.flatMap(thunk(StateT.pure(routerFun)))

      new RouterBuilder[T, P](newRoute, routeContextConsumer)
    }

    def recover(recoverFun: (T, P) => EitherT[IO, P, T]): RouterBuilder[T, P] = {
      val newRoute = route.flatMap(either => StateT(context =>
        either.fold(problem => recoverFun(context.state, problem).value, value => IO.pure(Right(value)))
          .map((context, _))))

      new RouterBuilder[T, P](newRoute, routeContextConsumer)
    }

    def nest(nestedRoute: (RouterBuilder[T, P], Either[P, T]) => RouterBuilder[T, P]): RouterBuilder[T, P] = {
      val newRoute = route.flatMap(either =>
        nestedRoute(new RouterBuilder[T, P](routeContextConsumer), either).route)

      new RouterBuilder[T, P](newRoute, routeContextConsumer)
    }

    def simple(function: Either[P, T] => IO[Either[P, T]], name: String): RouterFunction[T, P] =
      new RouterFunction[T, P] {
        override def internalApply(state: T, either: Either[P, T]): IO[ExecutionContext[T, P]] =
          execute(function, either).map { case (result, elapsed) =>
            (List(new RouteHistoryRecord[T, P](either.right.get, result, elapsed.getNano, name)), result)
          }
      }

    def thunk(stateT: StateT[IO, RouteContext[T, P], RouterFunction[T, P]]): Either[P, T] => StateT[IO, RouteContext[T, P], Either[P, T]] =
      either => stateT.flatMap(routerFun => StateT(context =>
        routerFun(context.state, either).map { case (newHistoryRecords, result) =>
          (new RouteContext[T, P](result.getOrElse(context.state), context.historyRecords ++ newHistoryRecords), result)
        }))

    def build(): Router[T, P] =
      new Router[T, P](initialState => route.run(new RouteContext[T, P](initialState)), routeContextConsumer)
  }

}

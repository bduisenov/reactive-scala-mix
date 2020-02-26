# reactive-scala-mix
Bootstrap template for spring-boot-webflux + scala + java + cca + abstract doc + router

Router in action
```scala
  val createUserRoute: Document => Either[String, Document] = Router[Document, String](route => route
    .flatMap(validateUserUseCase)
    .flatMap(hashUserPasswordUseCase)
    .recover((lastState, _) => Right(lastState)) // if something happens, try to save anyway
    .subRoute((subRoute, either) => either match {
      case Right(_) => subRoute.flatMap(hashUserPasswordUseCase) // revert the changes to password
      case _ => subRoute.flatMap(_ => Left("failed"))
    })
    .flatMap(saveUserUseCase)) {
    (routeContext: RouteContext[Document, String]) => routeContext.historyRecords.foreach(println)
  }
```

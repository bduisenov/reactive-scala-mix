# reactive-scala-mix
Bootstrap template for spring-boot-webflux + scala + java + cca + abstract doc + router

## Router in action

### code

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
### println of execution

```shell
RouteHistoryRecord(Map(user -> cats.Later@730e7f65),Right(Map(user -> cats.Later@730e7f65)),709704,ValidateUserUseCase)
RouteHistoryRecord(Map(user -> cats.Later@730e7f65),Right(Map(user -> UserEntity(None,Monika,dwp.emos))),349226,HashUserPasswordUseCase)
RouteHistoryRecord(Map(user -> UserEntity(None,Monika,dwp.emos)),Right(Map(user -> UserEntity(None,Monika,some.pwd))),22227,HashUserPasswordUseCase)
RouteHistoryRecord(Map(user -> UserEntity(None,Monika,some.pwd)),Right(Map(user -> UserEntity(Some(1),Monika,some.pwd))),618274,SaveUserUseCase)


```

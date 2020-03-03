package org.scalalang.boot.reactive.service;

import cats.effect.IO;
import org.scalalang.boot.reactive.repository.UserEntity;
import scala.Option;

public interface UserService {

    IO<UserEntity> saveUser(UserEntity user);

    IO<Option<UserEntity>> getUser(Long id);
}

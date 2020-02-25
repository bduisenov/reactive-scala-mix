package org.scalalang.boot.reactive.service;

import org.scalalang.boot.reactive.repository.UserEntity;
import scala.Option;

public interface UserService {

    UserEntity saveUser(UserEntity user);

    Option<UserEntity> getUser(Long id);
}

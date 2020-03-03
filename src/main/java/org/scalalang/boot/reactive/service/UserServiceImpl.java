package org.scalalang.boot.reactive.service;

import cats.effect.IO;
import org.scalalang.boot.reactive.repository.UserEntity;
import org.scalalang.boot.reactive.repository.UserRepository;
import scala.Option;

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public IO<UserEntity> saveUser(UserEntity user) {
        return userRepository.save(user);
    }

    @Override
    public IO<Option<UserEntity>> getUser(Long id) {
        return userRepository.findById(id);
    }
}

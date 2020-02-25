package org.scalalang.boot.reactive.service;

import org.scalalang.boot.reactive.repository.UserEntity;
import org.scalalang.boot.reactive.repository.UserRepository;
import scala.Option;

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }

    @Override
    public Option<UserEntity> getUser(Long id) {
        return userRepository.findById(id);
    }
}

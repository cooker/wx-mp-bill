package com.github.cooker.bill.domain;

import java.util.Optional;

/** 用户仓储接口。 */
public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByOpenId(String openId);

    User save(User user);
}

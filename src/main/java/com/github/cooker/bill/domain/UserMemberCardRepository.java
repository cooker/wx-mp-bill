package com.github.cooker.bill.domain;

import java.util.List;
import java.util.Optional;

/** 用户会员卡仓储接口。 */
public interface UserMemberCardRepository {

    Optional<UserMemberCard> findById(Long id);

    List<UserMemberCard> findByUserId(Long userId);

    Optional<UserMemberCard> findByUserIdAndCardNo(Long userId, String cardNo);

    UserMemberCard save(UserMemberCard card);
}

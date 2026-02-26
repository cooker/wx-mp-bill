package com.github.cooker.bill.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.cooker.bill.domain.User;
import com.github.cooker.bill.domain.UserRepository;
import com.github.cooker.bill.infrastructure.persistence.mapper.UserMapper;
import com.github.cooker.bill.infrastructure.persistence.po.UserPO;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper mapper;

    public UserRepositoryImpl(UserMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public Optional<User> findByOpenId(String openId) {
        if (openId == null || openId.isBlank()) return Optional.empty();
        LambdaQueryWrapper<UserPO> w = new LambdaQueryWrapper<UserPO>().eq(UserPO::getOpenId, openId);
        return Optional.ofNullable(mapper.selectOne(w)).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserPO po = toPO(user);
        if (user.id() == null) {
            mapper.insert(po);
            return toDomain(po);
        }
        mapper.updateById(po);
        return findById(po.getId()).orElseThrow();
    }

    private User toDomain(UserPO po) {
        return new User(po.getId(), po.getOpenId(), po.getMobile(), po.getNickname(), po.getRegisterSource());
    }

    private UserPO toPO(User u) {
        UserPO po = new UserPO();
        po.setId(u.id());
        po.setOpenId(u.openId());
        po.setMobile(u.mobile());
        po.setNickname(u.nickname());
        po.setRegisterSource(u.registerSource());
        return po;
    }
}

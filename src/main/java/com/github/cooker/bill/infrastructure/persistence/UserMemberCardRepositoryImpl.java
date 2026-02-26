package com.github.cooker.bill.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.cooker.bill.domain.UserMemberCard;
import com.github.cooker.bill.domain.UserMemberCardRepository;
import com.github.cooker.bill.infrastructure.persistence.mapper.UserMemberCardMapper;
import com.github.cooker.bill.infrastructure.persistence.po.UserMemberCardPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserMemberCardRepositoryImpl implements UserMemberCardRepository {

    private final UserMemberCardMapper mapper;

    public UserMemberCardRepositoryImpl(UserMemberCardMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<UserMemberCard> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<UserMemberCard> findByUserId(Long userId) {
        LambdaQueryWrapper<UserMemberCardPO> w = new LambdaQueryWrapper<UserMemberCardPO>().eq(UserMemberCardPO::getUserId, userId);
        return mapper.selectList(w).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<UserMemberCard> findByUserIdAndCardNo(Long userId, String cardNo) {
        LambdaQueryWrapper<UserMemberCardPO> w = new LambdaQueryWrapper<UserMemberCardPO>()
            .eq(UserMemberCardPO::getUserId, userId)
            .eq(UserMemberCardPO::getCardNo, cardNo);
        return Optional.ofNullable(mapper.selectOne(w)).map(this::toDomain);
    }

    @Override
    public UserMemberCard save(UserMemberCard card) {
        UserMemberCardPO po = toPO(card);
        if (card.id() == null) {
            mapper.insert(po);
            return toDomain(po);
        }
        mapper.updateById(po);
        return findById(po.getId()).orElseThrow();
    }

    private UserMemberCard toDomain(UserMemberCardPO po) {
        return new UserMemberCard(
            po.getId(), po.getUserId(), po.getCardNo(),
            po.getCardType() != null ? po.getCardType() : "DEBIT",
            po.getAmount(),
            po.getStatus() != null ? po.getStatus() : "ACTIVE"
        );
    }

    private UserMemberCardPO toPO(UserMemberCard c) {
        UserMemberCardPO po = new UserMemberCardPO();
        po.setId(c.id());
        po.setUserId(c.userId());
        po.setCardNo(c.cardNo());
        po.setCardType(c.cardType());
        po.setAmount(c.amount());
        po.setStatus(c.status());
        return po;
    }
}

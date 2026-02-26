package com.github.cooker.bill.domain;

import java.math.BigDecimal;

/**
 * 用户会员卡聚合根。
 * 对应表 user_member_card。卡类型 CREDIT 表示信用卡，支持先用后付；amount 借记卡为余额、信用卡为授信额度。
 *
 * @param id      主键
 * @param userId  关联 user.id
 * @param cardNo  会员卡号/交易卡号
 * @param cardType 卡类型 DEBIT-借记卡，CREDIT-信用卡(先用后付)
 * @param amount  借记卡为账户余额，信用卡为授信额度
 * @param status  ACTIVE/INACTIVE
 */
public record UserMemberCard(Long id, Long userId, String cardNo, String cardType, BigDecimal amount, String status) {}

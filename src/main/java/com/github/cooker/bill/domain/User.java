package com.github.cooker.bill.domain;

/**
 * 用户聚合根。
 * 对应表 user。
 *
 * @param id             主键
 * @param openId         微信 open_id / 第三方唯一标识
 * @param mobile         手机号
 * @param nickname       昵称
 * @param registerSource 注册来源，如 WECHAT_MINI/APP/H5
 */
public record User(Long id, String openId, String mobile, String nickname, String registerSource) {}

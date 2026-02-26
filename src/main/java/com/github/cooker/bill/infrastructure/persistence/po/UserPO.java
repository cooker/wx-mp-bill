package com.github.cooker.bill.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("user")
public class UserPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String openId;
    private String mobile;
    private String nickname;
    private String registerSource;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getRegisterSource() { return registerSource; }
    public void setRegisterSource(String registerSource) { this.registerSource = registerSource; }
}

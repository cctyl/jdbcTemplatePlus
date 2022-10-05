package com.github.cctyl.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
@Data
public class BaseBean  {

    /**
     * 微信openid
     */
    @Column(name = "username", nullable = false)
    private String username;

    /**
     * 密码
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 昵称
     */
    @Column(name = "nick_name", nullable = true)
    private String nickName;
}

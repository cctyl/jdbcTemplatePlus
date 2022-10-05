package com.github.cctyl.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @Description
 * @Author  xwd
 * @Date 2022-09-30 15:36:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "acl_user")
public class AclUser extends BaseBean implements Serializable {

	/**
	 * 会员id
	 */
	@Id
	@GeneratedValue(generator="system-uuid")

	@Column(name = "id", nullable = false)
	private String id;

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

	/**
	 * 用户头像
	 */
	@Column(name = "salt", nullable = true)
	private String salt;

	/**
	 * 用户签名
	 */
	@Column(name = "token", nullable = true)
	private String token;

	/**
	 * 逻辑删除 1（true）已删除， 0（false）未删除
	 */
	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted;




	@Transient
	private List<AclRole> roleList;


	/**
	 * 仅用于演示，无实际功能
	 */
	@Transient
	private AclRole role;

}

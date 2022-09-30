package com.github.cccctyl.domain;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import org.hibernate.annotations.GenericGenerator;

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
public class AclUser implements Serializable {

	/**
	 * 会员id
	 */
	@Id
	@GeneratedValue(generator="system-uuid")
	@GenericGenerator(name="system-uuid", strategy = "uuid")
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

	/**
	 * 创建时间
	 */
	@Column(name = "gmt_create", nullable = false)
	private LocalDateTime gmtCreate;

	/**
	 * 更新时间
	 */
	@Column(name = "gmt_modified", nullable = false)
	private LocalDateTime gmtModified;
}

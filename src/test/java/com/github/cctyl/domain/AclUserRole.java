package com.github.cctyl.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import javax.persistence.*;
import java.io.Serializable;

/**
 * @Description
 * @Author  xwd
 * @Date 2022-09-30 15:36:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "acl_user_role")
public class AclUserRole implements Serializable {

	/**
	 * 主键id
	 */
	@Id
	@GeneratedValue(generator="system-uuid")

	@Column(name = "id", nullable = false)
	private String id;

	/**
	 * 角色id
	 */
	@Column(name = "role_id", nullable = false)
	private String roleId;

	/**
	 * 用户id
	 */
	@Column(name = "user_id", nullable = false)
	private String userId;

	/**
	 * 逻辑删除 1（true）已删除， 0（false）未删除
	 */
	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted;


}

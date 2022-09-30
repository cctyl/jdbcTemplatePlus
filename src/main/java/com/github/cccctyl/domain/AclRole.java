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
@Table(name = "acl_role")
public class AclRole implements Serializable {

	/**
	 * 角色id
	 */
	@Id
	@GeneratedValue(generator="system-uuid")
	@GenericGenerator(name="system-uuid", strategy = "uuid")
	@Column(name = "id", nullable = false)
	private String id;

	/**
	 * 角色名称
	 */
	@Column(name = "role_name", nullable = false)
	private String roleName;

	/**
	 * 角色编码
	 */
	@Column(name = "role_code", nullable = true)
	private String roleCode;

	/**
	 * 备注
	 */
	@Column(name = "remark", nullable = true)
	private String remark;

	/**
	 * 逻辑删除 1（true）已删除， 0（false）未删除
	 */
	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted;


}

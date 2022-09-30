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
@Table(name = "acl_role_permission")
public class AclRolePermission implements Serializable {

	/**
	 * null
	 */
	@Id
	@GeneratedValue(generator="system-uuid")
	@GenericGenerator(name="system-uuid", strategy = "uuid")
	@Column(name = "id", nullable = false)
	private String id;

	/**
	 * null
	 */
	@Column(name = "role_id", nullable = false)
	private String roleId;

	/**
	 * null
	 */
	@Column(name = "permission_id", nullable = false)
	private String permissionId;

	/**
	 * 逻辑删除 1（true）已删除， 0（false）未删除
	 */
	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted;


}

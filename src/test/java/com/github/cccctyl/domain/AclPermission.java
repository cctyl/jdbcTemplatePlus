package com.github.cccctyl.domain;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import javax.persistence.*;
import java.time.LocalDateTime;

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
@Table(name = "acl_permission")
public class AclPermission implements Serializable {

	/**
	 * 编号
	 */
	@Id
	@GeneratedValue(generator="system-uuid")
	@GenericGenerator(name="system-uuid", strategy = "uuid")
	@Column(name = "id", nullable = false)
	private String id;

	/**
	 * 所属上级
	 */
	@Column(name = "pid", nullable = false)
	private String pid;

	/**
	 * 名称
	 */
	@Column(name = "name", nullable = false)
	private String name;

	/**
	 * 类型(1:菜单,2:按钮)
	 */
	@Column(name = "type", nullable = false)
	private Boolean type;

	/**
	 * 权限值
	 */
	@Column(name = "permission_value", nullable = true)
	private String permissionValue;

	/**
	 * 访问路径
	 */
	@Column(name = "path", nullable = true)
	private String path;

	/**
	 * 组件路径
	 */
	@Column(name = "component", nullable = true)
	private String component;

	/**
	 * 图标
	 */
	@Column(name = "icon", nullable = true)
	private String icon;

	/**
	 * 状态(0:禁止,1:正常)
	 */
	@Column(name = "status", nullable = true)
	private Boolean status;

	/**
	 * 逻辑删除 1（true）已删除， 0（false）未删除
	 */
	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted;

	/**
	 * 创建时间
	 */
	@Column(name = "gmt_create", nullable = true)
	private LocalDateTime gmtCreate;

	/**
	 * 更新时间
	 */
	@Column(name = "gmt_modified", nullable = true)
	private LocalDateTime gmtModified;
}

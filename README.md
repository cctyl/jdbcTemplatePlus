### 简介

jdbcTemplatePlus，针对jdbcTemplate 进行了封装，用于弥补JPA 等全自动框架灵活性不足的问题。
todo: 本质只使用了jpa的Id 注解和 @Column注解，后续会逐步创建独立注解，并且支持灵活切换
### 特点

- 针对多表查询嵌套对象的封装，达到类似JPA 的oneToMany、ManyToMany 等注解的作用。
- 相较于JPA，自定义程度高，灵活性强
- 无侵入，耦合低，仅为了兼容以往jpa项目，使用jpaTable\Id\Column 注解。jdbcTemplate为外部传入
- 支持注解：@MappedSuperclass、@Transient


### 涉及到一些问题

- 获得的结果是Integer类型，而实体类的类型是Long。涉及到的类型转换器

- 枚举值转换枚举对象

- 成员对象是集合接口类型，但是接口类型不确定的情况下（List，Set，Collection），如何生成对应的实现类？如果是Set接口，应该生成什么实现类？如果是具体的实现类例如HashSet，怎么进行处理？

- 实体类属性与表中字段的映射，通过注解以及反射的方式

- 在简化重复步骤的同时，又需要插入定制化的代码，怎么实现？依靠类似函数式接口，抽象方法、回调方法等来实现

- lambda 表达式来传递需要封装的子对象

  此处涉及到的是 lambda表达式中的writeReplace方法

- 通过反射获取集合的泛型



### 引入方式


- maven依赖

  ```xml
        <dependency>
            <groupId>io.github.cctyl</groupId>
            <artifactId>jdbcTemplatePlus</artifactId>
            <version>1.0.3</version>
        </dependency>
  ```

### 使用方法

设有 User、Role、UserRole 三张表。其中User表中含有一个成员`List<Role> roleList`，并且还有一个Role role（无实际意义，仅做演示）。

此时希望查询User时，直接将roleList 封装好，以及Role封装好。并且不需要过多的代码。

那么示例如下：

```java
/*
            目标sql为
                SELECT
                  r.*
                FROM
                  acl_user u
                  LEFT JOIN acl_user_role ur
                    ON u.`id` = ur.`user_id`
                  LEFT JOIN acl_role roleList
                    ON ur.`role_id` = roleList.id
                  Left join acl_role r
                  	ON ur.`role_id` = r.id
                WHERE u.id = 1 ;

*/   

        @Test
        public void test01LambdaPlus() {
    
            // =========================参数准备========================
            SqlGenrator sqlGen = new SqlGenrator();
            TargetTable<String, AclUser> tUser = sqlGen.targetTable(AclUser.class);
            TargetTable<String, AclRole> tRoleList = sqlGen.targetTable(AclUser::getRoleList);
            TargetTable<String, AclRole> tRole = sqlGen.targetTable(AclUser::getRole);
            TargetTable<String, AclUserRole> tUserRole = sqlGen.targetTable(AclUserRole.class);
    
            // ======================查询部分===========================
            //如果你只需要一部分字段，则显式的声明这部分字段
            String column = sqlGen.genColumn(tUser.columns(AclUser::getId,AclUser::getUsername),
                    tRoleList.columns(AclRole::getRoleName,AclRole::getId),
                    tRole.columns(AclRole::getRoleName));
    
            //如果你需要全部字段
    //        String column = sqlGen.genColumn(tUser, tRoleList, tRole);
    
    
            sqlGen.select(column)
                            .from(tUser)
                            .lJoin(tUserRole)
                            .on(tUser.id() + "=" + tUserRole.col("user_id"))
            
                            .lJoin(tRoleList)
                            .on(tUserRole.col(AclUserRole::getRoleId) + "=" + tRoleList.id())
            
                            .lJoin(tRole)
                            .on(tUserRole.col(AclUserRole::getRoleId) + "=" + tRole.id())
            
                            .where(tUser.id() + "=:userId")
                            .and(tUser.col(AclUser::getToken) + "=:token")
                            .and(tUser.col(AclUser::getPassword) + "=:password")
                            .or(tUser.col(AclUser::getPassword) + "=:password")
                            .addParam("userId", 1)
                            .addParam("token", "sss")
                            .addParam("password", "e10adc3949ba59abbe56e057f20f883e");
    
            // ===================封装部分==============================
            List<AclUser> genrator = new MapToTable<AclUser, String>() {
                @Override
                public void mapToChildObj(List<Map<String, Object>> tempList, AclUser mainObj) {
                    //在这里进行子对象封装
                    //roleList
                    mapMany(mainObj, tRoleList, AclUser::getRoleList);
    
                    //role
                    mapOne(mainObj, tRole, AclUser::getRole);
    
                }
            }.genrator(sqlGen.queryForList(namedParameterJdbcTemplate), tUser);
    
            System.out.println("end");
        }





```

sqlGen 直接直接传入String sql（但是要求表名从targetTable 中取出），也可以通过调用方法拼接的方式。




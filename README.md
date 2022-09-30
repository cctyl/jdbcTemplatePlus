### 简介

jdbcTemplatePlus，针对jdbcTemplate 进行了封装，用于弥补JPA 等全自动框架灵活性不足的问题。
todo: 本质只使用了jpa的Id 注解和 @Column注解，后续会逐步创建独立注解，并且支持灵活切换
### 特点

- 针对多表查询嵌套对象的封装，达到类似JPA 的oneToMany、ManyToMany 等注解的作用。
- 相较于JPA，自定义程度高，灵活性强



### 框架编写中涉及到的技术问题

- 获得的结果是Integer类型，而实体类的类型是Long。涉及到的类型转换器

- 枚举值转换枚举对象

- 成员对象是集合接口类型，但是接口类型不确定的情况下（List，Set，Collection），如何生成对应的实现类？如果是Set接口，应该生成什么实现类？如果是具体的实现类例如HashSet，怎么进行处理？

- 实体类属性与表中字段的映射，通过注解以及反射的方式

- 在简化重复步骤的同时，又需要插入定制化的代码，怎么实现？依靠类似函数式接口，抽象方法、回调方法等来实现

- lambda 表达式来传递需要封装的子对象

  此处涉及到的是 lambda表达式中的writeReplace方法

### 使用方法

设有 User、Role、UserRole 三张表。其中User表中含有一个成员`List<Role> roleList`，

此时希望查询User时，直接将roleList 封装好。并且不需要过多的代码。

那么示例如下：

```java
    @Test
    public void test01(){

        // =========================参数准备========================
        SqlGenrator sqlGen = new SqlGenrator();
        TargetTable<String> tUser = sqlGen.targetTable(AclUser.class, null);

        TargetTable<String> tRole = sqlGen.targetTable(AclRole.class, "roleList");
        TargetTable<String> tUserRole = sqlGen.targetTable(AclUserRole.class, null);

        // ======================查询部分===========================
        /*
            目标sql为
                SELECT
                  r.*
                FROM
                  acl_user u
                  LEFT JOIN acl_user_role ur
                    ON u.`id` = ur.`user_id`
                  LEFT JOIN acl_role r
                    ON ur.`role_id` = r.id
                WHERE u.id = 1 ;



         */
        String column = sqlGen.getEntityColumn(tUser) + ",\n" + sqlGen.getEntityColumn(tRole);
        sqlGen.select(column)
                .from(tUser)
                .lJoin(tUserRole)
                .on(tUser.col(tUser.getIdColumnName()) + "=" + tUserRole.col("user_id"))
                .lJoin(tRole)
                .on(tUserRole.col("role_id") + "=" + tRole.col(tRole.getIdColumnName()))
                .where(tUser.col(tUser.getIdColumnName()) + "=:userId ")
        ;
        sqlGen.addParam("userId", 1);

        List<Map<String, Object>> mapList = sqlGen.queryForList(namedParameterJdbcTemplate);
        List<AclUser> genrator = new MapToTable<AclUser,String>() {
            @Override
            public void mapToChildObj(List<Map<String, Object>> tempList, AclUser mainObj) {
                //在这里进行子对象封装
                //roleList
                mapMany(mainObj, tRole, "roleList");
            }
        }.genrator(mapList, tUser);

        System.out.println("end");

    }
```

sqlGen 直接直接传入String sql（但是要求表名从targetTable 中取出），也可以通过调用方法拼接的方式。




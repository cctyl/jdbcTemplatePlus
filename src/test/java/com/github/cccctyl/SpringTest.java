package com.github.cccctyl;

import cn.hutool.core.lang.func.Func;
import com.github.cccctyl.domain.*;
import com.github.cccctyl.handler.SimpleTypeHandler;
import com.github.cccctyl.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.*;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {Application.class})
@Slf4j
public class SpringTest {


    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    @Test
    public void test01LambdaPlus() {

        // =========================参数准备========================
        SqlGenrator sqlGen = new SqlGenrator();
        TargetTable<String, AclUser> tUser = sqlGen.targetTable(AclUser.class);
        TargetTable<String, AclRole> tRoleList = sqlGen.targetTable(AclUser::getRoleList);
        TargetTable<String, AclRole> tRole = sqlGen.targetTable(AclUser::getRole);
        TargetTable<String, AclUserRole> tUserRole = sqlGen.targetTable(AclUserRole.class);

        // ======================查询部分===========================
        String column = sqlGen.genColumn(tUser, tRoleList, tRole);
        sqlGen.select(column)
                .from(tUser)
                .lJoin(tUserRole)
                .on(tUser.id() + "=" + tUserRole.col("user_id"))

                .lJoin(tRoleList)
                .on(tUserRole.col("role_id") + "=" + tRoleList.id())

                .lJoin(tRole)
                .on(tUserRole.col("role_id") + "=" + tRole.id())

                .where(tUser.id() + "=:userId")
                .and(tUser.col("token") + "=:token")
                .and(tUser.col("password") + "=:password")
        ;
        sqlGen.addParam("userId", 1);
        sqlGen.addParam("token", "sss");
        sqlGen.addParam("password", "e10adc3949ba59abbe56e057f20f883e");

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


    @Test
    public void test02() {

        Field field = LambdaUtil.extractColum(AclUser::getId);
        System.out.println(field);

    }


}

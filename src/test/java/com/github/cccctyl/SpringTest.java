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
    public void test01LambdaPlus(){

        // =========================参数准备========================
        SqlGenrator sqlGen = new SqlGenrator();
        TargetTable<String> tUser = sqlGen.targetTable(AclUser.class, null);

        TargetTable<String> tRole = sqlGen.targetTable(AclUser::getRoleList);
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
                mapMany(mainObj, tRole, AclUser::getRoleList);
            }
        }.genrator(mapList, tUser);

        System.out.println("end");

    }



    @Test
    public void test02(){

        Field field = LambdaUtil.extractColum(AclUser::getId);
        System.out.println(field);

    }




}

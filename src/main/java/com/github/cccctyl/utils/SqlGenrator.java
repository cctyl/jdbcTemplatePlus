package com.github.cccctyl.utils;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class SqlGenrator {


    private StringBuilder finalSql;
    private HashMap<String, Object> paramMap;
    private HashMap<String,String> tableMap;

    public SqlGenrator() {
        finalSql = new StringBuilder();
        paramMap = new HashMap<>();
        tableMap = new HashMap<>();
    }

    public SqlGenrator addParam(String key, Object value) {
        paramMap.put(key, value);
        return this;
    }

    public SqlGenrator select(String column) {
        finalSql.append("select \n")
                .append(column)
                .append("\n");
        return this;
    }

    public SqlGenrator from(String mainTable) {
        finalSql.append("from ")
                .append(mainTable)
                .append("\n");
        return this;
    }


    public SqlGenrator from(TargetTable mainTable) {

        finalSql.append("from ")
                .append(mainTable.getOriginName())
                .append(" as ")
                .append(mainTable.getTableAlias())
                .append("\n");
        return this;
    }


    public SqlGenrator rJoin(String table) {

        finalSql.append("right join ")
                .append(table)
                .append(" ");
        return this;
    }



    public SqlGenrator rJoin(TargetTable table) {
        finalSql.append("right join ")
                .append(table.getOriginName())
                .append(" ")
                .append(table.getTableAlias())
                .append(" ");
        return this;
    }

    public SqlGenrator lJoin(String table) {
        finalSql.append("left join ")
                .append(table)
                .append(" ");
        return this;
    }

    public SqlGenrator lJoin(TargetTable table) {
        finalSql.append("left join ")
                .append(table.getOriginName())
                .append(" ")
                .append(table.getTableAlias())
                .append(" ");
        return this;
    }



    public SqlGenrator join(TargetTable table) {
        finalSql.append(" join ")
                .append(table.getOriginName())
                .append(" ")
                .append(table.getTableAlias())
                .append(" ");
        return this;
    }

    public SqlGenrator join(String table) {
        finalSql.append(" join ")
                .append(table)
                .append(" ");
        return this;
    }


    public SqlGenrator on(String connectCondition) {
        finalSql.append("on ")
                .append(connectCondition)
                .append("\n");
        return this;
    }


    public SqlGenrator where(String condition) {
        finalSql.append("where ")
                .append(condition)
                .append("\n");
        return this;
    }


    public SqlGenrator groupBy(String column) {
        finalSql.append("group by  ")
                .append(column)
                .append("\n");
        return this;
    }

    public SqlGenrator having(String condition) {
        finalSql.append("having ")
                .append(condition)
                .append("\n");
        return this;
    }

    public SqlGenrator orderBy(String column) {
        finalSql.append("order by ")
                .append(column)
                .append("\n");
        return this;
    }


    public String endSql() {
        return finalSql.toString();
    }

    public String getClassFullNameWithOutDot(Class clazz) {

        String name = clazz.getName();

        String[] split = name.split("\\.");
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : split) {
            stringBuilder.append(s);
        }
        return stringBuilder.toString().toLowerCase();

    }

    public String getTableNameFromAnnotation(Class table) {

        Table annotation = (Table) table.getAnnotation(Table.class);
        String originalTableName = "";
        if (annotation != null) {
            originalTableName = annotation.name();
        } else {
            throw new RuntimeException("该类上未标明表名");
        }

        return originalTableName;
    }

    public List<Map<String, Object>> queryForList(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return namedParameterJdbcTemplate.queryForList(this.endSql(), paramMap);
    }

    public List<Map<String, Object>> queryForList(NamedParameterJdbcTemplate namedParameterJdbcTemplate,String sql) {
        return namedParameterJdbcTemplate.queryForList(sql, paramMap);
    }

    public  TargetTable targetTable(Class tableClass,String propertyName) {
        String originName = getTableNameFromAnnotation(tableClass);
        String aliasName = getClassFullNameWithOutDot(tableClass);
        Field[] declaredFields = tableClass.getDeclaredFields();
        if (tableMap.containsKey(aliasName)){
            aliasName+= propertyName;
        }
        tableMap.put(aliasName,originName);
        return new TargetTable(originName, aliasName,declaredFields,tableClass);
    }

    /**
     * lambda表达式，根据lambda生成对应的TargetTable
     * @param column
     * @param <T>
     * @return
     */
    public <T> TargetTable targetTable(Function<T,?> column) {

        //todo 解析lambda 从而获得 属性的class以及 属性的名称

        throw new RuntimeException();

    }

    public String getEntityColumn(TargetTable targetTable) {
        StringBuilder stringBuilder = new StringBuilder();
        Field[] declaredFields = targetTable.getDeclaredFields();

        String classFullNameWithOutDot = targetTable.getTableAlias();

        for (Field declaredField : declaredFields) {
            Column annotation = declaredField.getAnnotation(Column.class);
            Id idAnnotation = declaredField.getAnnotation(Id.class);
            if (annotation != null) {

                String tableColunmAlias = classFullNameWithOutDot + annotation.name();

                stringBuilder
                        .append(classFullNameWithOutDot)
                        .append('.')
                        .append(annotation.name())
                        .append(" ")
                        .append(tableColunmAlias)
                        .append(',')
                        .append("\n");

            } else if (idAnnotation != null) {
                String tableColunmAlias = classFullNameWithOutDot + declaredField.getName();
                stringBuilder
                        .append(classFullNameWithOutDot)
                        .append('.')
                        .append(declaredField.getName())
                        .append(" ")
                        .append(tableColunmAlias)
                        .append(',')
                        .append("\n");
            }
        }

        String result = stringBuilder.toString();
        String substring = result.substring(0, result.length() - 2);

        return substring;
    }

}

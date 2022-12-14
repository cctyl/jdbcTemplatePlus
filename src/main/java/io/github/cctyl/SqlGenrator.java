package io.github.cctyl;

import io.github.cctyl.utils.LambdaUtil;
import io.github.cctyl.utils.SFunction;
import io.github.cctyl.utils.TargetTable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.persistence.*;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class SqlGenrator {


    private StringBuilder finalSql;
    private HashMap<String, Object> paramMap;
    private HashMap<String, String> tableMap;


    public HashMap<String, Object> getParamMap() {
        return paramMap;
    }

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
        finalSql.append(" where ")
                .append(condition)
                .append(" \n");
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

    /**
     * @param clazz
     * @param <T>   ???????????????
     * @return
     */
    public <T> String getClassFullNameWithOutDot(Class<T> clazz) {

        String name = clazz.getName();

        String[] split = name.split("\\.");
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : split) {
            stringBuilder.append(s);
        }
        return stringBuilder.toString().toLowerCase();

    }

    /**
     * @param table
     * @param <T>   ???????????????
     * @return
     */
    public <T> String getTableNameFromAnnotation(Class<T> table) {

        Table annotation = (Table) table.getAnnotation(Table.class);
        String originalTableName = "";
        if (annotation != null) {
            originalTableName = annotation.name();
        } else {
            throw new RuntimeException("????????????????????????");
        }

        return originalTableName;
    }

    public List<Map<String, Object>> queryForList(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return namedParameterJdbcTemplate.queryForList(this.endSql(), paramMap);
    }

    public List<Map<String, Object>> queryForList(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String sql) {
        return namedParameterJdbcTemplate.queryForList(sql, paramMap);
    }

    /**
     * @param tableClass
     * @param <R>        ????????????????????????
     * @param <T>        ???????????????
     * @return
     */
    public <R, T> TargetTable<R, T> targetTable(Class<T> tableClass) {
        return targetTable(tableClass, null);
    }

    /**
     * @param tableClass
     * @param propertyName
     * @param <R>          ????????????????????????
     * @param <T>          ???????????????
     * @return
     */
    public <R, T> TargetTable<R, T> targetTable(Class<T> tableClass, String propertyName) {
        String originName = getTableNameFromAnnotation(tableClass);
        String aliasName = getClassFullNameWithOutDot(tableClass);
        Field[] declaredFields = tableClass.getDeclaredFields();
        if (tableMap.containsKey(aliasName)) {
            aliasName += propertyName;
        }
        tableMap.put(aliasName, originName);
        return new TargetTable<>(originName, aliasName, declaredFields, tableClass);
    }

    /**
     * @param column
     * @param <T>    ????????????
     * @param <R>    ??????????????????
     * @param <C>    ????????????
     * @return
     */
    public <T, R, C> TargetTable<R, C> targetTable(SFunction<T, ?> column) {

        SerializedLambda serializedLambda = LambdaUtil.getSerializedLambda(column);
        Field field = LambdaUtil.extractColum(serializedLambda);

        Class<C> tableClass = null;
        if (Collection.class.isAssignableFrom(field.getType())) {
            // ?????????????????????????????????Generic?????????
            Type genericType = field.getGenericType();
            if (genericType == null) {
                throw new RuntimeException("genericType is null");
            }
            // ??????????????????????????????
            if (genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                //??????????????????class????????????
                tableClass = (Class<C>) pt.getActualTypeArguments()[0];

            }
        } else {
            //?????????????????????
            tableClass = (Class<C>) field.getType();
        }

        assert tableClass != null;
        String originName = getTableNameFromAnnotation(tableClass);
        String aliasName = getClassFullNameWithOutDot(tableClass);
        Field[] declaredFields = tableClass.getDeclaredFields();
        if (tableMap.containsKey(aliasName)) {
            aliasName += field.getName();
        }
        tableMap.put(aliasName, originName);
        return new TargetTable<R, C>(originName, aliasName, declaredFields, tableClass);
    }

    /**
     * @param tableList
     * @return
     */
    @SafeVarargs
    public final String genColumn(TargetTable... tableList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tableList.length; i++) {
            sb.append(getEntityColumn(tableList[i]));
            if (i != tableList.length - 1) {
                sb.append(",\n");
            }
        }
        return sb.toString();
    }

    public final String genColumn(String... columnList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columnList.length; i++) {
            sb.append(columnList[i])
                    .append(",\n");
        }
        String result = sb.toString();
        return result.substring(0, result.length() - 2);
    }


    /**
     * @param targetTable
     * @param <R>         ????????????????????????
     * @param <C>         ???????????????
     * @return
     */
    public <R, C> String getEntityColumn(TargetTable<R, C> targetTable) {
        StringBuilder stringBuilder = new StringBuilder();
        Field[] declaredFields = targetTable.getDeclaredFields();

        String classFullNameWithOutDot = targetTable.getTableAlias();

        for (Field declaredField : declaredFields) {
            Transient transientAnno = declaredField.getAnnotation(Transient.class);
            if (transientAnno!=null){
                continue;
            }
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

        //????????????????????????
        String parentColumns = getParentColumn(targetTable.getOriginClass().getSuperclass(),
                classFullNameWithOutDot);

        stringBuilder.append(parentColumns);
        String result = stringBuilder.toString();
        return result.substring(0, result.length() - 2);
    }

    private <C> String getParentColumn(Class<? super C> superclass, String classFullNameWithOutDot) {
        StringBuilder stringBuilder = new StringBuilder();
        MappedSuperclass mappedSuperclassAnno = superclass.getAnnotation(MappedSuperclass.class);
        if (mappedSuperclassAnno!=null){
            //???????????????
            stringBuilder.append(getParentColumn(  superclass.getSuperclass(), classFullNameWithOutDot));
            //???????????????
            Field[] declaredFields = superclass.getDeclaredFields();


            for (Field declaredField : declaredFields) {
                Transient transientAnno = declaredField.getAnnotation(Transient.class);
                if (transientAnno!=null){
                    continue;
                }
                Column columnAnnotation = declaredField.getAnnotation(Column.class);
                Id idAnnotation = declaredField.getAnnotation(Id.class);
                if (columnAnnotation != null) {

                    String tableColunmAlias = classFullNameWithOutDot + columnAnnotation.name();

                    stringBuilder
                            .append(classFullNameWithOutDot)
                            .append('.')
                            .append(columnAnnotation.name())
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


            return stringBuilder.toString();
        }else {
            //???????????????????????????
            return "";
        }


    }

    public SqlGenrator and(String condition) {

        finalSql.append(" and ")
                .append(condition)
                .append(" ")
        ;
        return this;
    }

    public SqlGenrator or(String condition) {
        finalSql.append(" or ")
                .append(condition)
                .append(" ")
        ;
        return this;
    }
}

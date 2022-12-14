package io.github.cctyl.utils;

import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @param <R> 主键类型
 * @param <T> 对应实体类类型
 */

public class TargetTable<R, T> {

    private String originName;
    private String tableAlias;
    private Field[] declaredFields;
    private Class<T> originClass;
    private String idColumnName;
    private String idPropertyName;
    private HashMap<String, String> nameFieldNameMap = new HashMap<>();


    public String getOriginName() {
        return originName;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public Field[] getDeclaredFields() {
        return declaredFields;
    }

    public Class<T> getOriginClass() {
        return originClass;
    }


    public String getIdPropertyName() {
        return idPropertyName;
    }

    public HashMap<String, String> getNameFieldNameMap() {
        return nameFieldNameMap;
    }

    public TargetTable(String originName, String tableAlias, Field[] declaredFields, Class<T> originClass) {
        this.originName = originName;
        this.tableAlias = tableAlias;
        this.declaredFields = declaredFields;
        this.originClass = originClass;

        for (Field field : declaredFields) {
            if (field.getAnnotation(Id.class) != null) {
                //此时这个字段就是主键字段，拿到column并且拿到column 的名字
                Column annotation = field.getAnnotation(Column.class);
                if (annotation != null) {
                    this.idColumnName = annotation.name();
                } else {
                    //没有column 注解，那么字段名就是列名
                    this.idColumnName = field.getName();
                    //fixme 是否需要对字段名进行驼峰转换？
                }

                this.idPropertyName = field.getName();
                break;
            }
        }
        if (StringUtils.isEmpty(idColumnName)) {
            throw new RuntimeException("该表无主键");
        }
        genratorColumnNameFieldNameMap();

    }

    public String id() {
        return col(idColumnName);
    }

    public String col(String column) {

        return new StringBuilder()
                .append(tableAlias)
                .append(".")
                .append(column)
                .append(" ")
                .toString()
                ;
    }

    public String col(SFunction<T,?> column) {

        return new StringBuilder()
                .append(tableAlias)
                .append(".")
                .append(ColumnUtil.getTableFieldName(column))
                .append(" ")
                .toString()
                ;
    }

    public String getId(boolean alias) {
        if (alias) {
            return this.tableAlias + this.idColumnName;
        } else {
            return this.idColumnName;
        }

    }

    public Map<R, List<Map<String, Object>>> groupById(List<Map<String, Object>> mapList) {
        return mapList.stream()
                .collect(Collectors.groupingBy(map -> (R) map.get(this.getId(true))
                ));
    }

    /**
     * 获得该字节码上 成员与对应列名的map
     * 通过扫描TableField注解
     *
     * @return
     */
    public void genratorColumnNameFieldNameMap() {

        //父类字段加入map
        genratorParentColumnNameFieldNameMap(this.originClass.getSuperclass());


        Field[] declaredFields = this.declaredFields;

        for (Field declaredField : declaredFields) {
            Transient transientAnno = declaredField.getAnnotation(Transient.class);
            if (transientAnno!=null){
                continue;
            }
            Column annotation = declaredField.getAnnotation(Column.class);
            Id idAnnotation = declaredField.getAnnotation(Id.class);
            if (annotation != null) {
                //说明该字段上添加了注解
                //直接获取注解上的值

                //获取该字段对应的表中列名
                String tableColunmName = this.tableAlias + annotation.name();

                if (tableColunmName.length() > 0) {
                    //获取该字段的名字
                    String fieldName = declaredField.getName();
                    nameFieldNameMap.put(tableColunmName, fieldName);
                }
            } else if (idAnnotation != null) {
                String tableColunmName = this.tableAlias + declaredField.getName();
                if (tableColunmName.length() > 0) {
                    //获取该字段的名字
                    String fieldName = declaredField.getName();
                    nameFieldNameMap.put(tableColunmName, fieldName);
                }
            }
        }

    }

    private void genratorParentColumnNameFieldNameMap(Class<? super T> superclass) {
        MappedSuperclass mappedSuperclassAnno = superclass.getAnnotation(MappedSuperclass.class);
        if (mappedSuperclassAnno!=null){
            genratorParentColumnNameFieldNameMap(superclass.getSuperclass());
            Field[] declaredFields = superclass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                Transient transientAnno = declaredField.getAnnotation(Transient.class);
                if (transientAnno!=null){
                    continue;
                }
                Column annotation = declaredField.getAnnotation(Column.class);
                Id idAnnotation = declaredField.getAnnotation(Id.class);
                if (annotation != null) {
                    //说明该字段上添加了注解
                    //直接获取注解上的值

                    //获取该字段对应的表中列名
                    String tableColunmName = this.tableAlias + annotation.name();

                    if (tableColunmName.length() > 0) {
                        //获取该字段的名字
                        String fieldName = declaredField.getName();
                        nameFieldNameMap.put(tableColunmName, fieldName);
                    }
                } else if (idAnnotation != null) {
                    String tableColunmName = this.tableAlias + declaredField.getName();
                    if (tableColunmName.length() > 0) {
                        //获取该字段的名字
                        String fieldName = declaredField.getName();
                        nameFieldNameMap.put(tableColunmName, fieldName);
                    }
                }
            }
        }



    }


    public String columns(SFunction<T, ?>... columns) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {

            SFunction<T, ?> column = columns[i];
            SerializedLambda serializedLambda = LambdaUtil.getSerializedLambda(column);
            Field field = LambdaUtil.extractColum(serializedLambda);

            String classFullNameWithOutDot = this.getTableAlias();
            Column annotation = field.getAnnotation(Column.class);
            Id idAnnotation = field.getAnnotation(Id.class);
            if (annotation != null) {

                String tableColunmAlias = classFullNameWithOutDot + annotation.name();

                sb
                        .append(classFullNameWithOutDot)
                        .append('.')
                        .append(annotation.name())
                        .append(" ")
                        .append(tableColunmAlias)
                        .append(',')
                        .append("\n");

            } else if (idAnnotation != null) {
                String tableColunmAlias = classFullNameWithOutDot + field.getName();
                sb
                        .append(classFullNameWithOutDot)
                        .append('.')
                        .append(field.getName())
                        .append(" ")
                        .append(tableColunmAlias)
                        .append(',')
                        .append("\n");
            }
        }

        String result = sb.toString();
        return result.substring(0, result.length() - 2);

    }
}

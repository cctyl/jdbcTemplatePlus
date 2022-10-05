package io.github.cctyl.utils;

import javax.persistence.Column;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;

public class ColumnUtil {

    public static <T> String getTableFieldName(SFunction<T, ?> column) {

        SerializedLambda serializedLambda = LambdaUtil.getSerializedLambda(column);
        Field field = LambdaUtil.extractColum(serializedLambda);

        Column annotation = field.getAnnotation(Column.class);
        String columnName = "";
        if (annotation != null) {
            columnName = annotation.name();
        } else {
            //没有column 注解，那么字段名就是列名
            columnName = field.getName();
            //fixme 是否需要对字段名进行驼峰转换？
        }
        return columnName;

    }
}

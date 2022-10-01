package com.github.cctyl.handler;


import java.sql.Timestamp;

/**
 * 简单的类型转换器
 */
public class SimpleTypeHandler {

    /**
     * 将输入的类型进行转换
     * 主要是将数据库中的类似转换为指定的类型
     * @param source
     * @return
     */
    public static Object convert(Object source,Class targetClass){

        if (source==null || targetClass==null){
            return source;
        }

        try {
            //如果是枚举
            if (targetClass.isEnum()){

                //字符串枚举
                return stringToEnum((String) source,targetClass);
                //todo 数字枚举

            }

            switch (source.getClass().getName()){

                case "java.sql.Timestamp":
                   return sqlTimestampToOthers((Timestamp) source,targetClass);

                case "java.lang.Integer":
                    return integerToOthers((Integer)source,targetClass);
                default:
                    return source;
            }
        } catch (Exception e) {
            return null;
        }

    }

    private static Object integerToOthers(Integer source, Class targetClass) {
        switch (targetClass.getName()){
            case "java.lang.Long":
                return source.longValue();
            default:
                return source;
        }
    }

    private static Object sqlTimestampToOthers(Timestamp source, Class targetClass){

        switch (targetClass.getName()){
            case "java.time.Instant":
                return source.toInstant();
            default:
               return source;
        }
    }

    /**
     * 字符串转换枚举对象
     * @param source
     * @param targetClass
     * @return
     */
    private static Object stringToEnum(String source,Class targetClass){
        Enum anEnum = Enum.valueOf(targetClass, source);
        return anEnum;
    }
}

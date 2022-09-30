package com.github.cccctyl.utils;

import com.github.cccctyl.handler.SimpleTypeHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class MapToTable<T,R> {

    private List<Map<String, Object>> mapList = null;

    public void mapMany(T mainObj, TargetTable childTable, SFunction<T,?> column) {
        try {
            Class<?> mainObjClass = mainObj.getClass();
            Field declaredField = LambdaUtil.extractColum(column);
            declaredField.setAccessible(true);

            Class<?> collectionType = declaredField.getType();

            Collection childCollection = null;
            if (Collection.class.isAssignableFrom(collectionType)) {
                //是集合，判断是接口、抽象类 还是实现类，
                if (collectionType.isInterface() || Modifier.isAbstract(collectionType.getModifiers())) {
                    //如果是接口则判断是哪个接口
                    if (List.class.isAssignableFrom(collectionType) || Collection.class.equals(collectionType)) {
                        //如果是List接口，则创建arrayList接口
                        //如果就是collection本身，则创建arrayList。
                        childCollection = new ArrayList();
                    } else if (Set.class.isAssignableFrom(collectionType)) {
                        childCollection = new HashSet();
                    } else {
                        //如果是list和set以外的接口，那么不予封装
                        throw new RuntimeException("将要封装的目标类型:" + collectionType.getName() + "不被支持");
                    }

                } else {
                    //如果是实现类则直接创建对象
                    childCollection = (Collection) collectionType.newInstance();
                }

            } else if (collectionType.isArray()) {
                //是数组
                throw new RuntimeException("将要封装的目标类型不应为数组，应是可变长度的集合");
            } else {
                //不是数组也不是集合，无法封装，抛出异常
                throw new RuntimeException("将要封装的目标类型错误，请检查propertyName=" + declaredField.getName() + "是否为集合或数组类型");
            }


            declaredField.set(mainObj, childCollection);

            //去重
            HashSet<Object> deduplicationSet = new HashSet<>();

            for (Map<String, Object> stringObjectMap : this.mapList) {
                Class childOriginClass = childTable.getOriginClass();

                Object child = childOriginClass.newInstance();

                MapToTable.columnMapToObj(stringObjectMap, child, childTable);


                Field idField = childOriginClass.getDeclaredField(childTable.getIdPropertyName());
                idField.setAccessible(true);
                if (idField.get(child) != null) {
                    deduplicationSet.add(child);
                }
            }
            childCollection.addAll(deduplicationSet);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }


    public void mapOne(T mainObj, TargetTable childTable,SFunction<T,?> column) {
        try {
            Class<?> mainObjClass = mainObj.getClass();
            Field declaredField = LambdaUtil.extractColum(column);
            declaredField.setAccessible(true);


            Class childOriginClass = childTable.getOriginClass();
            Object childObject = null;

            Map<String, Object> stringObjectMapForParent = mapList.get(0);
            if (stringObjectMapForParent.containsKey(childTable.getId(true))) {
                childObject = childOriginClass.newInstance();

                MapToTable.columnMapToObj(stringObjectMapForParent, childObject, childTable);

                Field idField = childOriginClass.getDeclaredField(childTable.getIdPropertyName());
                idField.setAccessible(true);

                if (idField.get(childObject) != null) {
                    declaredField.set(mainObj, childObject);
                }
            }


        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }


    public List<T> genrator(List<Map<String, Object>> mapList,
                            TargetTable<R,T> mainTable
    ) {

        if (mapList.size()<1){
            return new ArrayList<>();
        }
        this.mapList = mapList;
        Map<R, List<Map<String, Object>>> mainTableIdMap = mainTable.groupById(mapList);

        ArrayList<T> finalList = new ArrayList<>();

        for (R idResult : mainTableIdMap.keySet()) {
            Class originClass = mainTable.getOriginClass();
            T mainObj = null;
            try {
                mainObj = (T) originClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            finalList.add(mainObj);

            List<Map<String, Object>> tempList = mainTableIdMap.get(idResult);

            //封装主表的数据
            mapToMainTable(tempList.get(0), mainObj, mainTable);

            //封装子表数据
            mapToChildObj(tempList, mainObj);
        }

        return finalList;
    }


    public abstract void mapToChildObj(List<Map<String, Object>> tempList, T mainObj);


    public void mapToMainTable(Map<String, Object> zhubiaoMap, T obj, TargetTable mainTable) {
        columnMapToObj(zhubiaoMap, obj, mainTable);
    }

    public static void columnMapToObj(Map<String, Object> valueMap, Object tagetObj, TargetTable targetTable) {
        HashMap<String, String> nameFieldNameMap = targetTable.getNameFieldNameMap();
        Class targetClass = targetTable.getOriginClass();
        for (String tableColumnName : nameFieldNameMap.keySet()) {
            String fieldName = nameFieldNameMap.get(tableColumnName);

            Field declaredField;
            try {
                declaredField = targetClass.getDeclaredField(fieldName);
                declaredField.setAccessible(true);

                Object realValue = valueMap.get(tableColumnName);

                //目标字段类型
                Class<?> targetFieldType = declaredField.getType();
                realValue = SimpleTypeHandler.convert(realValue, targetFieldType);

                declaredField.set(tagetObj, realValue);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


}

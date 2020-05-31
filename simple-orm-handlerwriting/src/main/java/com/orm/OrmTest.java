package com.orm;

import com.orm.entity.User;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OrmTest {

    private static final String CONDITION = " where 1=1 ";

    public static void main(String[] args) {
        User user = new User();
        user.setAddress("广东");
        List<Object> select = select(user);
        select.forEach(System.out::println);
    }


    public static List<Object> select(Object entityObject) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql:///day17?serverTimezone=GMT%2B8", "root", "123456");
            String sql = "select * from ";
            StringBuilder sb = new StringBuilder(sql);
            Class<?> entityObjectClass = entityObject.getClass();

            Table table = entityObjectClass.getAnnotation(Table.class);
            String tableName = table.name();
            sb.append(tableName).append(CONDITION);

            // 获取类中所有的属性
            Field[] fields = entityObjectClass.getDeclaredFields();
            Map<String, String> fieldMapper = new HashMap<String, String>();
            for (Field field : fields) {
                String fieldName = field.getName();
                String columnName = fieldName;
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    columnName = column.name();
                    fieldMapper.put(fieldName, columnName);
                } else {
                    fieldMapper.put(fieldName, columnName);
                }


                // 如果有值
                field.setAccessible(true);      // 暴力反射
                Object value = field.get(entityObject);
                value = convertType(value, field.getType());
                if (value != null) {
                    sb.append(" and "+ columnName + " = '" + value+"' ");
                }
            }

            PreparedStatement preparedStatement = connection.prepareStatement(sb.toString());

            ResultSet resultSet = preparedStatement.executeQuery();
            List<Object> result = new ArrayList();
            while (resultSet.next()) {
                Object instance = entityObjectClass.newInstance();
                for (Map.Entry<String, String> entry : fieldMapper.entrySet()) {
                    String fieldName = entry.getKey();
                    String columnName = entry.getValue();
                    Field field = entityObjectClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(instance, resultSet.getObject(columnName));
                }
                result.add(instance);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Object convertType(Object value, Class<?> type) {
        if (String.class == type) {
            return (String) value;
        } else if (Integer.class == type) {
            return (Integer) value;
        }
        return null;
    }
}

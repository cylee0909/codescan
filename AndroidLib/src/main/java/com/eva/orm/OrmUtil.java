package com.eva.orm;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by cylee on 15/9/1.
 */
public class OrmUtil {
  private static final String TAG = OrmUtil.class.getSimpleName();
  private static final String _ID = "_id";

  public static String getTableName(Class<?> table) {
    if (table.isAnnotationPresent(Table.class)) {
      Table annotation = table.getAnnotation(Table.class);
      if (!TextUtils.isEmpty(annotation.name())) {
        return annotation.name();
      }
    }
    return table.getSimpleName();
  }

  public static String getColumnName(Field field) {
    if (field.isAnnotationPresent(Column.class)) {
      Column annotation = field.getAnnotation(Column.class);
      return annotation.name();
    }
    return getDefaultName(field.getName());
  }

  public static boolean isIdColumn(String fieldName) {
    if (fieldName == null) {
      return false;
    }
    return fieldName.equalsIgnoreCase(_ID);
  }

  public static boolean isIdColumn(Field field) {
    if (field == null) {
      return false;
    }
    String columnName = getColumnName(field);
    return isIdColumn(columnName);
  }

  public static String getDefaultName(String camelCased) {
    if (camelCased.equalsIgnoreCase(_ID)) {
      return _ID;
    }

    StringBuilder sb = new StringBuilder();
    char[] buf = camelCased.toCharArray();
    for (int i = 0; i < buf.length; i++) {
      char prevChar = (i > 0) ? buf[i - 1] : 0;
      char c = buf[i];
      char nextChar = (i < buf.length - 1) ? buf[i + 1] : 0;
      boolean isFirstChar = (i == 0);
      if (isFirstChar || Character.isLowerCase(c) || Character.isDigit(c)) {
        sb.append(Character.toUpperCase(c));
      } else if (Character.isUpperCase(c)) {
        if (Character.isLetterOrDigit(prevChar)) {
          if (Character.isLowerCase(prevChar)) {
            sb.append('_').append(c);
          } else if (nextChar > 0 && Character.isLowerCase(nextChar)) {
            sb.append('_').append(c);
          } else {
            sb.append(c);
          }
        } else {
          sb.append(c);
        }
      }
    }
    return sb.toString();
  }


  public static String getColumnType(Class<?> type) {
    if ((type.equals(Boolean.class)) ||
        (type.equals(Boolean.TYPE)) ||
        (type.equals(Integer.class)) ||
        (type.equals(Integer.TYPE)) ||
        (type.equals(Long.class)) ||
        (type.equals(Long.TYPE)))  {
      return "INTEGER";
    }

    if ((type.equals(Date.class)) ||
        (type.equals(java.sql.Date.class)) ||
        (type.equals(Calendar.class))) {
      return "INTEGER NULL";
    }

    if (type.getName().equals("[B")) {
      return "BLOB";
    }

    if ((type.equals(Double.class)) || (type.equals(Double.TYPE)) || (type.equals(Float.class)) ||
        (type.equals(Float.TYPE))) {
      return "FLOAT";
    }

    if ((type.equals(String.class)) || (type.equals(Character.TYPE)) ||
        (type.equals(BigDecimal.class))) {
      return "TEXT";
    }

    return "";
  }



  public static List<Field> getTableFields(Class table) {
    List<Field> typeFields = new ArrayList<Field>();
    getAllFields(typeFields, table);
    List<Field> toStore = new ArrayList<Field>();
    for (Field field : typeFields) {
      if (!field.isAnnotationPresent(Ignore.class) && !Modifier.isStatic(
          field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
        toStore.add(field);
      }
    }
    return toStore;
  }

  private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
    Collections.addAll(fields, type.getDeclaredFields());
    if (type.getSuperclass() != null) {
      fields = getAllFields(fields, type.getSuperclass());
    }
    return fields;
  }



  public static void addFieldValueToColumn(ContentValues values, Field column, Object object) {
    column.setAccessible(true);
    Class<?> columnType = column.getType();
    try {
      String columnName = getColumnName(column);
      Object columnValue = column.get(object);

      if (isIdColumn(columnName)) {
        values.put("_ID", (Long) columnValue);
        return;
      }

      if (columnType.equals(Short.class) || columnType.equals(short.class)) {
        values.put(columnName, (Short) columnValue);
      } else if (columnType.equals(Integer.class) || columnType.equals(int.class)) {
        values.put(columnName, (Integer) columnValue);
      } else if (columnType.equals(Long.class) || columnType.equals(long.class)) {
        values.put(columnName, (Long) columnValue);
      } else if (columnType.equals(Float.class) || columnType.equals(float.class)) {
        values.put(columnName, (Float) columnValue);
      } else if (columnType.equals(Double.class) || columnType.equals(double.class)) {
        values.put(columnName, (Double) columnValue);
      } else if (columnType.equals(Boolean.class) || columnType.equals(boolean.class)) {
        values.put(columnName, (Boolean) columnValue);
      } else if (columnType.equals(BigDecimal.class)) {
        try {
          values.put(columnName, column.get(object).toString());
        } catch (NullPointerException e) {
          values.putNull(columnName);
        }
      } else if (Timestamp.class.equals(columnType)) {
        try {
          values.put(columnName, ((Timestamp) column.get(object)).getTime());
        } catch (NullPointerException e) {
          values.put(columnName, (Long) null);
        }
      } else if (Date.class.equals(columnType)) {
        try {
          values.put(columnName, ((Date) column.get(object)).getTime());
        } catch (NullPointerException e) {
          values.put(columnName, (Long) null);
        }
      } else if (Calendar.class.equals(columnType)) {
        try {
          values.put(columnName, ((Calendar) column.get(object)).getTimeInMillis());
        } catch (NullPointerException e) {
          values.put(columnName, (Long) null);
        }
      } else if (columnType.equals(byte[].class)) {
        if (columnValue == null) {
          values.put(columnName, "".getBytes());
        } else {
          values.put(columnName, (byte[]) columnValue);
        }
      } else {
        if (columnValue == null) {
          values.putNull(columnName);
        } else if (columnType.isEnum()) {
          values.put(columnName, ((Enum) columnValue).name());
        } else {
          values.put(columnName, String.valueOf(columnValue));
        }
      }

    } catch (IllegalAccessException e) {
      Log.e(TAG, e.getMessage());
    }
  }

  public static void setFieldValueFromCursor(Cursor cursor, Field field, Object object) {
    field.setAccessible(true);
    try {
      Class fieldType = field.getType();
      String colName = OrmUtil.getColumnName(field);

      int columnIndex = cursor.getColumnIndex(colName);

      if (cursor.isNull(columnIndex)) {
        return;
      }

      if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
        field.set(object, cursor.getLong(columnIndex));
      } else if (fieldType.equals(String.class)) {
        String val = cursor.getString(columnIndex);
        field.set(object, val != null && val.equals("null") ? null : val);
      } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
        field.set(object, cursor.getDouble(columnIndex));
      } else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
        field.set(object, cursor.getString(columnIndex).equals("1"));
      } else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
        field.set(object, cursor.getInt(columnIndex));
      } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
        field.set(object,
            cursor.getFloat(columnIndex));
      } else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
        field.set(object,
            cursor.getShort(columnIndex));
      } else if (fieldType.equals(BigDecimal.class)) {
        String val = cursor.getString(columnIndex);
        field.set(object, val != null && val.equals("null") ? null : new BigDecimal(val));
      } else if (fieldType.equals(Timestamp.class)) {
        long l = cursor.getLong(columnIndex);
        field.set(object, new Timestamp(l));
      } else if (fieldType.equals(Date.class)) {
        long l = cursor.getLong(columnIndex);
        field.set(object, new Date(l));
      } else if (fieldType.equals(Calendar.class)) {
        long l = cursor.getLong(columnIndex);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(l);
        field.set(object, c);
      } else if (fieldType.equals(byte[].class)) {
        byte[] bytes = cursor.getBlob(columnIndex);
        if (bytes == null) {
          field.set(object, "".getBytes());
        } else {
          field.set(object, cursor.getBlob(columnIndex));
        }
      } else if (Enum.class.isAssignableFrom(fieldType)) {
        try {
          Method valueOf = field.getType().getMethod("valueOf", String.class);
          String strVal = cursor.getString(columnIndex);
          Object enumVal = valueOf.invoke(field.getType(), strVal);
          field.set(object, enumVal);
        } catch (Exception e) {
          Log.e(TAG,
              "Enum cannot be read from Sqlite3 database. Please check the type of field "
                  + field.getName());
        }
      } else
        Log.e(TAG, "Class cannot be read from Sqlite3 database. Please check the type of field "
            + field.getName()
            + "("
            + field.getType().getName()
            + ")");
    } catch (IllegalArgumentException e) {
      Log.e("field set error", e.getMessage());
    } catch (IllegalAccessException e) {
      Log.e("field set error", e.getMessage());
    }
  }

}

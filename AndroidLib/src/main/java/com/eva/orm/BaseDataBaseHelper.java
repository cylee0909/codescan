package com.eva.orm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.text.TextUtils;
import android.util.Log;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cylee on 15/9/1.
 */
abstract public class BaseDataBaseHelper extends SQLiteOpenHelper {
  private static final String TAG = BaseDataBaseHelper.class.getSimpleName();
  protected List<Class<? extends BaseTable>> mTableClasses;

  public BaseDataBaseHelper(Context context, String dbName, int version) {
    super(context, dbName, new InnerCursorFactory(), version);
    mTableClasses = getTableClasses();
  }

  public abstract List<Class<? extends BaseTable>> getTableClasses();

  @Override public void onCreate(SQLiteDatabase db) {
    for (Class<? extends BaseTable> domain : mTableClasses) {
      createTable(domain, db);
    }
  }

  private void createTable(Class<? extends BaseTable> table, SQLiteDatabase sqLiteDatabase) {
    Log.i(TAG, "Create table");
    List<Field> fields = OrmUtil.getTableFields(table);
    String tableName = OrmUtil.getTableName(table);
    StringBuilder sb = new StringBuilder("CREATE TABLE ");
    sb.append(tableName).append(" (_ID INTEGER PRIMARY KEY AUTOINCREMENT ");

    for (Field column : fields) {
      String columnName = OrmUtil.getColumnName(column);
      String columnType = OrmUtil.getColumnType(column.getType());
      if (columnType != null) {
        if (OrmUtil.isIdColumn(columnName)) {
          continue;
        }
        if (column.isAnnotationPresent(Column.class)) {
          Column columnAnnotation = column.getAnnotation(Column.class);
          sb.append(", ").append(columnName).append(" ").append(columnType);
          if (columnAnnotation.notNull()) {
            if (columnType.endsWith(" NULL")) {
              sb.delete(sb.length() - 5, sb.length());
            }
            sb.append(" NOT NULL");
          }

          if (columnAnnotation.unique()) {
            sb.append(" UNIQUE");
          }
        } else {
          sb.append(", ").append(columnName).append(" ").append(columnType);
        }
      }
    }
    sb.append(" ) ");
    String createSql = sb.toString();
    Log.i(TAG, "Creating table " + tableName + " , createSql = " + createSql);
    if (!TextUtils.isEmpty(createSql)) {
      try {
        sqLiteDatabase.execSQL(sb.toString());
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //TODO
  }

  private static class InnerCursorFactory implements SQLiteDatabase.CursorFactory {
    @Override
    public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable,
        SQLiteQuery query) {
      Log.d(TAG, query.toString());
      return new SQLiteCursor(db, masterQuery, editTable, query);
    }
  }

  public <T extends BaseTable> void save(List<T> objects) {
    if (objects != null && objects.size() > 0) {
      long current = System.currentTimeMillis();
      SQLiteDatabase db = getWritableDatabase();
      try {
        db.beginTransaction();
        for (T t : objects) {
          save(t);
        }
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
      Log.d(TAG, "time = "+String.valueOf(System.currentTimeMillis() - current));
    }
  }

  public <T extends BaseTable> long save(T object) {
    Class iClass = object.getClass();
    if (!mTableClasses.contains(iClass)) {
      throw new OrmException("No such table error class = " + iClass.getSimpleName());
    }
    String tableName = OrmUtil.getTableName(iClass);
    List<Field> columns = OrmUtil.getTableFields(iClass);
    ContentValues values = new ContentValues(columns.size());
    for (Field column : columns) {
      OrmUtil.addFieldValueToColumn(values, column, object);
    }
    long id = getWritableDatabase().insertWithOnConflict(tableName, null, values,
        SQLiteDatabase.CONFLICT_REPLACE);
    object._setDataBaseId(id);
    return id;
  }

  public <T extends BaseTable> boolean delete(T object) {
    Class iClass = object.getClass();
    if (!mTableClasses.contains(iClass)) {
      throw new OrmException("No such table error class = " + iClass.getSimpleName());
    }
    try {
      long id = object._getDataBaseId();
      SQLiteDatabase db = getWritableDatabase();
      boolean deleted =
          db.delete(OrmUtil.getTableName(iClass), "_ID = ?", new String[] { String.valueOf(id) })
              == 1;
      Log.i(TAG, iClass.getSimpleName() + " deleted : " + deleted + " item = " + object);
      return deleted;
    } catch (Exception e) {
    }
    return false;
  }

  public <T extends BaseTable> List<T> findWithQuery(Class<T> type, String query,
      String... arguments) {
    SQLiteDatabase sqLiteDatabase = getReadableDatabase();
    T entity;
    List<T> toRet = new ArrayList<T>();
    Cursor c = sqLiteDatabase.rawQuery(query, arguments);
    try {
      while (c.moveToNext()) {
        entity = type.getDeclaredConstructor().newInstance();
        inflate(c, entity);
        toRet.add(entity);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      c.close();
    }
    return toRet;
  }

  public <T extends BaseTable> List<T> find(Class<T> type, String whereClause,
      String[] whereArgs) {
    return find(type, whereClause, whereArgs, null, null, null);
  }

  public <T extends BaseTable> List<T> find(Class<T> type, String whereClause, String[] whereArgs,
      String groupBy, String orderBy, String limit) {
    SQLiteDatabase sqLiteDatabase = getReadableDatabase();
    T entity;
    List<T> toRet = new ArrayList<T>();
    Cursor c =
        sqLiteDatabase.query(OrmUtil.getTableName(type), null, whereClause, whereArgs, groupBy,
            null, orderBy, limit);
    try {
      while (c.moveToNext()) {
        entity = type.getDeclaredConstructor().newInstance();
        inflate(c, entity);
        toRet.add(entity);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      c.close();
    }
    return toRet;
  }

  private <T extends BaseTable> void inflate(Cursor cursor, T object) {
    List<Field> columns = OrmUtil.getTableFields(object.getClass());
    for (Field field : columns) {
      field.setAccessible(true);
      Class<?> fieldType = field.getType();
      if (OrmUtil.isIdColumn(field)) {
        long id = cursor.getLong(cursor.getColumnIndex("_ID"));
        object._setDataBaseId(id);
      } else {
        OrmUtil.setFieldValueFromCursor(cursor, field, object);
      }
    }
  }
}

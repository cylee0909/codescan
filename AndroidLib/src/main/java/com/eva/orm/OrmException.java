package com.eva.orm;

/**
 * Created by cylee on 15/9/1.
 */
public class OrmException extends RuntimeException {
  private String mErrorMsg;
  public OrmException(String e) {
    mErrorMsg = e;
  }
  @Override public String toString() {
    return mErrorMsg;
  }
}

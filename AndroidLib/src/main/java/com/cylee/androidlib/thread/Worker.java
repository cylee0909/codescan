package com.cylee.androidlib.thread;

import android.os.Process;

/**
 * Created by cylee on 15/3/10.
 */
public abstract class Worker implements Runnable{
  private int mPriority = Process.THREAD_PRIORITY_BACKGROUND;

  @Override public void run() {
    Process.setThreadPriority(mPriority);
    work();
  }

  public void setPriority(int prority) {
    mPriority = prority;
  }

  public abstract void work();

}

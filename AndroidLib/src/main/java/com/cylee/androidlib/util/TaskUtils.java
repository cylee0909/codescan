package com.cylee.androidlib.util;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.cylee.androidlib.thread.Worker;

import java.util.concurrent.Executor;

public class TaskUtils {

    private static Executor LIMITED_TASK_EXECUTOR = AsyncTask.THREAD_POOL_EXECUTOR;;
    private static Handler mMainHandler = new Handler(Looper.getMainLooper());

    /**
     * run a rapid task on background.
     *
     * @param background the background Work
     */
    public static void doRapidWork(Worker background) {
        LIMITED_TASK_EXECUTOR.execute(background);
    }

    /**
     * run a rapid task on the background and then post the "post" work on the ui thread
     *
     * @param background the background Work
     * @param post       the post work
     */
    public static void doRapidWorkAndPost(final Worker background, final Worker post) {
        doRapidWorkAndPost(background, post, 0);
    }

    public static void doRapidWork(AsyncWorker<?> asyncWorker) {
        asyncWorker.start(LIMITED_TASK_EXECUTOR, mMainHandler);
    }

    /**
     * run a rapid task on the background and then post the "post" work on the ui thread after
     * the delay time.
     *
     * @param background the background Work
     * @param post       the post work
     * @param delay      time delayed befor post
     */
    public static void doRapidWorkAndPost(final Worker background, final Worker post, final int delay) {
        LIMITED_TASK_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                background.work();
                mMainHandler.postDelayed(post, delay);
            }
        });
    }

    /**
     * post the "post" work on the ui thread after
     * the delay time.
     *
     * @param post  the post work
     * @param delay time delayed befor post
     */
    public static void postOnMain(Worker post, final int delay) {
        mMainHandler.postDelayed(post, delay);
    }

    public static void postOnMain(Worker post) {
        mMainHandler.postDelayed(post, 0);
    }

    public static void removePostedWork(Worker post) {
        mMainHandler.removeCallbacks(post);
    }

    public static abstract class AsyncWorker<Result> {
        private final void start(Executor service, Handler mainHandler) {
            service.execute(new Worker() {
                @Override
                public void work() {
                    final Result result = AsyncWorker.this.work();
                    mMainHandler.post(new Worker() {
                        @Override
                        public void work() {
                            AsyncWorker.this.post(result);
                        }
                    });
                }
            });
        }

        public abstract Result work();

        public abstract void post(Result result);
    }
}

package com.Sumeru.WearBus.utils;

import android.os.AsyncTask;

/**
 * 数据库异步任务工具类
 * 用于在后台线程执行数据库操作，避免主线程阻塞
 */
public class DatabaseTask<T> extends AsyncTask<Void, Void, T> {

    private final DatabaseOperation<T> operation;
    private final DatabaseCallback<T> callback;

    public interface DatabaseOperation<T> {
        T execute();
    }

    public interface DatabaseCallback<T> {
        void onComplete(T result);
    }

    public DatabaseTask(DatabaseOperation<T> operation, DatabaseCallback<T> callback) {
        this.operation = operation;
        this.callback = callback;
    }

    @Override
    protected T doInBackground(Void... voids) {
        return operation.execute();
    }

    @Override
    protected void onPostExecute(T result) {
        if (callback != null) {
            callback.onComplete(result);
        }
    }

    /**
     * 便捷方法：执行数据库操作
     */
    public static <T> void execute(DatabaseOperation<T> operation, DatabaseCallback<T> callback) {
        new DatabaseTask<>(operation, callback).execute();
    }
}

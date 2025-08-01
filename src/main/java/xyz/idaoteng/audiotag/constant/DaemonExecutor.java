package xyz.idaoteng.audiotag.constant;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DaemonExecutor {
    public static final ExecutorService TIME_CONSUMING_TASK_EXECUTOR;

    static {
        TIME_CONSUMING_TASK_EXECUTOR = Executors.newFixedThreadPool(4, new ThreadFactory() {
            private int counter = 0;

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "TimeConsumingTaskThread-" + counter++);
                thread.setDaemon(true);
                return thread;
            }
        });
    }
}

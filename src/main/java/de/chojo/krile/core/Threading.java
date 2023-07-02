package de.chojo.krile.core;

import de.chojo.logutil.marker.LogNotify;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import static org.slf4j.LoggerFactory.getLogger;

public class Threading {
    private static final Logger log = getLogger(Threading.class);

    private final ThreadGroup hikariGroup = new ThreadGroup("Hikari Worker");
    private final ThreadGroup jdaGroup = new ThreadGroup("JDA Worker");
    private final ThreadGroup workerGroup = new ThreadGroup("Bot Worker");

    private final ExecutorService jdaWorker = Executors.newCachedThreadPool(createThreadFactory(jdaGroup));
    private final ScheduledExecutorService botWorker = Executors.newScheduledThreadPool(3, createThreadFactory(workerGroup));

    private static final Thread.UncaughtExceptionHandler EXCEPTION_HANDLER =
            (t, e) -> log.error(LogNotify.NOTIFY_ADMIN, "An uncaught exception occurred in " + t.getName() + "-" + t.threadId() + ".", e);

    public static ThreadFactory createThreadFactory(ThreadGroup group) {
        return r -> {
            var thread = new Thread(group, r, group.getName());
            thread.setUncaughtExceptionHandler(EXCEPTION_HANDLER);
            return thread;
        };
    }

    public ExecutorService jdaWorker() {
        return jdaWorker;
    }

    public ScheduledExecutorService botWorker() {
        return botWorker;
    }

    public ThreadGroup hikariGroup() {
        return hikariGroup;
    }

    public ThreadGroup jdaGroup() {
        return jdaGroup;
    }
}

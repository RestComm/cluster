package org.restcomm.cache;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

public class CacheDataExecutorService {

    private static final Logger logger = Logger.getLogger(CacheDataExecutorService.class);

    private ExecutorService service;
    private LinkedBlockingQueue<Command<?>> queue = new LinkedBlockingQueue<Command<?>>();
    private AtomicBoolean isRunning = new AtomicBoolean(true);

    private long terminateTimeout;
    private long commandTimeout;

    public CacheDataExecutorService(CacheExecutorConfiguration config) {
        this.terminateTimeout = config.getTerminationTimeout();
        this.commandTimeout = config.getCommandTimeout();
        this.service = Executors.newFixedThreadPool(config.getNumberOfThreads(), getFactory());
        for (int i = 0; i < config.getNumberOfThreads(); i++) {
            service.submit(new Thread() {
                @Override
                public void run() {
                    while (isRunning.get()) {
                        try {
                            Command<?> command = queue.poll(100, TimeUnit.MILLISECONDS);
                            if (command != null && isRunning.get()) {
                                command.execute();
                            }
                        } catch (InterruptedException e) {
                            logger.error(this.getName() + " was interrupted", e);
                        }
                    }

                }
            });
        }
    }

    private ThreadFactory getFactory() {
        return new ThreadFactory() {
            private final String PREFIX = CacheDataExecutorService.class.getSimpleName() + "-thread-";
            private AtomicLong count = new AtomicLong(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(PREFIX + count.getAndIncrement());
                return thread;
            }
        };
    }

    public void terminate() {
        isRunning.set(false);
        service.shutdown();
        try {
            service.awaitTermination(terminateTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error(this.getClass().getSimpleName() + " was interrupted while terminating", e);
        }
        service.shutdownNow();
    }

    public <T> T get(MobicentsCache cache, Object key) {
        BlockingCallback<T> callback = new BlockingCallback<T>();
        Command<T> command = new GetCommand<T>(cache, key, callback);
        callback.setCommand(command);
        queue.offer(command);
        return callback.awaitResult();
    }

    public boolean exists(MobicentsCache cache, Object key) {
        BlockingCallback<Boolean> callback = new BlockingCallback<Boolean>();
        Command<Boolean> command = new ExistsCommand(cache, key, callback);
        callback.setCommand(command);
        queue.offer(command);
        return callback.awaitResult();
    }

    public <T> T put(MobicentsCache cache, Object key, T value) {
        BlockingCallback<T> callback = new BlockingCallback<T>();
        Command<T> command = new PutCommand<T>(cache, key, value, callback);
        callback.setCommand(command);
        queue.offer(command);
        return callback.awaitResult();
    }

    public <T> T remove(MobicentsCache cache, Object key) {
        BlockingCallback<T> callback = new BlockingCallback<T>();
        Command<T> command = new RemoveCommand<T>(cache, key, callback);
        callback.setCommand(command);
        queue.offer(command);
        return callback.awaitResult();
    }

    private abstract class Command<V> {

        protected MobicentsCache cache;
        protected Object key;
        protected BlockingCallback<V> callback;

        protected boolean isCanceled;

        public Command(MobicentsCache cache, Object key, BlockingCallback<V> callback) {
            this.cache = cache;
            this.key = key;
            this.callback = callback;
        }

        public abstract void execute();

        public void cancel() {
            this.isCanceled = true;
        }
    }

    private class GetCommand<V> extends Command<V> {

        GetCommand(MobicentsCache cache, Object key, BlockingCallback<V> callback) {
            super(cache, key, callback);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void execute() {
            if (!isCanceled) {
                V result = (V) cache.getJBossCache().get(key);
                callback.receiveResult(result);
            }
        }
    }

    private class ExistsCommand extends Command<Boolean> {

        ExistsCommand(MobicentsCache cache, Object key, BlockingCallback<Boolean> callback) {
            super(cache, key, callback);
        }

        @Override
        public void execute() {
            if (!isCanceled) {
                boolean result = cache.getJBossCache().containsKey(key);
                callback.receiveResult(result);
            }
        }
    }

    private class PutCommand<V> extends Command<V> {
        private V value;

        PutCommand(MobicentsCache cache, Object key, V value, BlockingCallback<V> callback) {
            super(cache, key, callback);
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void execute() {
            V result = (V) cache.getJBossCache().put(key, value);
            callback.receiveResult(result);
        }
    }

    private class RemoveCommand<V> extends Command<V> {

        RemoveCommand(MobicentsCache cache, Object key, BlockingCallback<V> callback) {
            super(cache, key, callback);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void execute() {
            V result = (V) cache.getJBossCache().remove(key);
            callback.receiveResult(result);
        }
    }

    private class BlockingCallback<R> {

        private CountDownLatch latch = new CountDownLatch(1);

        private R result;
        private Command<R> command;

        public void receiveResult(R result) {
            this.result = result;
            latch.countDown();
        }

        public R awaitResult() {
            try {
                boolean wasReleased = latch.await(commandTimeout, TimeUnit.MILLISECONDS);
                if (!wasReleased) {
                    logger.warn(command.getClass().getSimpleName() + " didn't execute due to timeout");
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            } finally {
                command.cancel();
            }
            return this.result;
        }

        public void setCommand(Command<R> command) {
            this.command = command;
        }
    }
}

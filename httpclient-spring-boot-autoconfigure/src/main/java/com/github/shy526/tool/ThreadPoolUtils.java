package com.github.shy526.tool;


import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 创建线程池帮助类
 *
 * @author shy526
 */
public class ThreadPoolUtils {

    /**
     * 线程池参数
     */
    public static class ThreadPoolConfig {
        /**
         * 线程池的基本大小
         */
        private int corePoolSize = 10;
        /**
         * 线程池最大数量
         */
        private int maximumPoolSizeSize = 100;
        /**
         * 线程活动保持时间
         */
        private long keepAliveTime = 1;
        /**
         * 任务队列
         */
        private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(10);


        /**
         * 拒绝策略
         * AbortPolicy
         * DiscardPolicy
         * DiscardOldestPolicy
         * CallerRunsPolicy
         */
        private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaximumPoolSizeSize() {
            return maximumPoolSizeSize;
        }

        public void setMaximumPoolSizeSize(int maximumPoolSizeSize) {
            this.maximumPoolSizeSize = maximumPoolSizeSize;
        }

        public long getKeepAliveTime() {
            return keepAliveTime;
        }

        public void setKeepAliveTime(long keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }

        public BlockingQueue<Runnable> getWorkQueue() {
            return workQueue;
        }

        public void setWorkQueue(BlockingQueue<Runnable> workQueue) {
            this.workQueue = workQueue;
        }

        public RejectedExecutionHandler getRejectedExecutionHandler() {
            return rejectedExecutionHandler;
        }

        public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
            this.rejectedExecutionHandler = rejectedExecutionHandler;
        }
    }

    /**
     * 创建线程池
     *
     * @return ThreadPoolExecutor
     */
    public static ThreadPoolExecutor getThreadPool() {
        return ThreadPoolUtils.getThreadPool(null, null);
    }

    /**
     * 自定义线程池属性
     *
     * @param threadPoolConfig 属性
     * @return ThreadPoolExecutor
     */
    public static ThreadPoolExecutor getThreadPool(ThreadPoolConfig threadPoolConfig) {
        return ThreadPoolUtils.getThreadPool(null, threadPoolConfig);
    }

    /**
     * 自定义任务前戳
     *
     * @param beforeName 前戳
     * @return ThreadPoolExecutor
     */
    public static ThreadPoolExecutor getThreadPool(String beforeName) {
        return ThreadPoolUtils.getThreadPool(beforeName, null);
    }


    /**
     * 创建指定前戳的任务名称和配置
     *
     * @param beforeName       前戳名称
     * @param threadPoolConfig 线程池配置 为null时自动创建
     * @return ThreadPoolExecutor
     */
    public static ThreadPoolExecutor getThreadPool(String beforeName, ThreadPoolConfig threadPoolConfig) {
        NamedThreadFactory namedThreadFactory = getNamedThreadFactory(beforeName, false);
        if (threadPoolConfig == null) {
            threadPoolConfig = new ThreadPoolConfig();
        }
        return new ThreadPoolExecutor(threadPoolConfig.getCorePoolSize(), threadPoolConfig.getMaximumPoolSizeSize(), threadPoolConfig.getKeepAliveTime(),
                TimeUnit.SECONDS,
                threadPoolConfig.getWorkQueue(), namedThreadFactory, threadPoolConfig.getRejectedExecutionHandler());
    }

    private static NamedThreadFactory getNamedThreadFactory(String beforeName, boolean daemon) {
        NamedThreadFactory namedThreadFactory = null;
        if ("".equals(beforeName) || beforeName == null) {
            namedThreadFactory = new NamedThreadFactory();
        } else {
            namedThreadFactory = new NamedThreadFactory(beforeName, daemon);
        }
        return namedThreadFactory;
    }


    public static ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor(String beforeName, int corePoolSize) {
        NamedThreadFactory namedThreadFactory = getNamedThreadFactory(beforeName, false);
        return new ScheduledThreadPoolExecutor(corePoolSize, namedThreadFactory);
    }

    public static ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor(String beforeName, int corePoolSize, boolean daemon) {
        NamedThreadFactory namedThreadFactory = getNamedThreadFactory(beforeName, daemon);
        return new ScheduledThreadPoolExecutor(corePoolSize, namedThreadFactory);
    }


    private static class NamedThreadFactory implements ThreadFactory {
        /**
         * 任务名称前戳
         */
        private String beforeName = "task";
        private final AtomicInteger threadNumberAtomicInteger = new AtomicInteger(1);
        private boolean daemon;

        public NamedThreadFactory(String beforeName, boolean daemon) {
            this.beforeName = beforeName;
            this.daemon = daemon;
        }

        public NamedThreadFactory() {

        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, String.format(Locale.CHINA, "%s-%d", this.beforeName, threadNumberAtomicInteger.getAndIncrement()));
            //是否是守护线程
            thread.setDaemon(daemon);
            //设置优先级 1~10 有3个常量 默认 Thread.MIN_PRIORITY*/
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }

    /**
     * 获取一个默认的线程池配置
     *
     * @return ThreadPoolConfig
     */
    public static ThreadPoolConfig threadPoolConfigBuild() {
        return new ThreadPoolConfig();
    }

    /**
     * 获取一个SingleThreadExecutor
     *
     * @param beforeName 任务前戳
     * @return ThreadPoolExecutor
     */
    public static ThreadPoolExecutor getSingleThreadExecutor(String beforeName) {
        ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig();
        threadPoolConfig.setCorePoolSize(1);
        threadPoolConfig.setMaximumPoolSizeSize(1);
        threadPoolConfig.setKeepAliveTime(0L);
        threadPoolConfig.setWorkQueue(new LinkedBlockingQueue<Runnable>());
        return getThreadPool(beforeName);
    }
}
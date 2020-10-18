package com.chm.headlines;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class MyThread extends Thread {

    int tid;

    public MyThread(int tid) {
        this.tid = tid;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(1000);     //每隔1秒执行一次下面的打印
                System.out.println(String.format("T%d:%d", tid, i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Producer implements Runnable {        //Producer
    private BlockingQueue<String> q;

    public Producer(BlockingQueue<String> q) {
        this.q = q;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 100; i++) {
//                Thread.sleep(1000);
                q.put(String.valueOf(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Consumer implements Runnable {        //Consumer
    private BlockingQueue<String> q;

    public Consumer(BlockingQueue<String> q) {
        this.q = q;
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println(Thread.currentThread().getName() + ":" + q.take());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class MultiThread {
    public static void testThread() {
        for (int i = 0; i < 10; i++) {
//            new MyThread(i).start();
        }

        for (int i = 0; i < 10; i++) {
            final int tid = i;
            new Thread(new Runnable() {     //匿名类
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < 10; i++) {
                            Thread.sleep(1000);     //每隔1秒执行一次下面的打印
                            System.out.println(String.format("T2-%d:%d", tid, i));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private static Object obj = new Object();

    public static void testSynchronized1() {
        synchronized (obj) {
            try {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1000);     //每隔1秒执行一次下面的打印
                    System.out.println(String.format("T3:%d", i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void testSynchronized2() {
        synchronized (new Object()) {
            try {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1000);     //每隔1秒执行一次下面的打印
                    System.out.println(String.format("T4:%d", i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void testSynchronized() {
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    testSynchronized1();
                    testSynchronized2();
                }
            }).start();
        }
    }

    public static void testBlockingQueue() {
        BlockingQueue<String> q = new ArrayBlockingQueue<String>(10);
        new Thread(new Producer(q)).start();
        new Thread(new Consumer(q), "Consumer1").start();
        new Thread(new Consumer(q), "Comsumer2").start();
        new Thread(new Consumer(q), "Consumer3").start();
        new Thread(new Consumer(q), "Consumer4").start();

    }


    public static void sleep(int mile) {
        try {
//            Thread.sleep(new Random().nextInt(mile ));
            Thread.sleep(mile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int count = 0;
    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    public static void testWithAtomic() {
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sleep(1000);
                    for (int j = 0; j < 10; j++) {
                        System.out.println(atomicInteger.incrementAndGet());
                    }
                }
            }).start();
        }
    }

    public static void testWithoutAtomic() {
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sleep(1000);
                    for (int j = 0; j < 10; j++) {
                        System.out.println(count++);
                    }
                }
            }).start();
        }
    }

    public static void testAtomic() {       //testWithAtomic()的最后结果始终是100，而testWithoutAtomic()的最后结果很可能小于100
//        testWithAtomic();         //对atomicIntegerd的累加满足原子性
        testWithoutAtomic();        //对count的累加不满足原子性，线程不安全（比如可能两个线程同时获取到初值80，同时递增1后同时返回81，则相当于少累加一次）
    }

    private static ThreadLocal<Integer> threadLocalUserIds = new ThreadLocal<>();
    private static int userId;

    public static void testThreadLocal() {
        for (int i = 0; i < 10; i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    threadLocalUserIds.set(finalI);
                    sleep(1000);
                    System.out.println("ThreadLocals: " + threadLocalUserIds.get());
                }
            }).start();
        }

        for (int i = 0; i < 10; i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    userId = finalI;        //最后一次循环中，将10个线程的userId全部设为9，睡眠1秒后，一起打印出来
                    sleep(1000);
                    System.out.println("NonThreadLocal: " + userId);
                }
            }).start();
        }
    }

    public static void testExecutor() {
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    sleep(1000);
                    System.out.println("Executor1: " + i);
                }
            }
        });

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    sleep(1000);
                    System.out.println("Executor2: " + i);
                }
            }
        });

        executorService.shutdown();
        while (!executorService.isTerminated()) {        //这里是isTerminated, 而不是isShutdown不同（可ctrl+q查看帮助文档）
            sleep(1000);
            System.out.println("Waiting for termination...");
        }
    }

    public static void testFuture() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Integer> future = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                sleep(1000);
//                return 1;
                throw new IllegalArgumentException("异常");
            }
        });

        executorService.shutdown();

        try {
            //future.get(), Waits if necessary for the computation to complete,
            //and then retrieves its result.
            System.out.println(future.get());
            //下面是100ms内为未收到返回值则直接报超时错误
//            System.out.println(future.get(100, TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] argv) {
//        testThread();
//        testSynchronized();
//        testBlockingQueue();
//        testAtomic();
//        testThreadLocal();
//        testExecutor();
        testFuture();
    }

}

package com.zzr.springboot.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 开发公司：山东海豚数据技术有限公司
 * 版权：山东海豚数据技术有限公司
 * <p>
 * CountDownLatchUtil
 *
 * @author zzr
 * @created Create Time: 2019/4/10
 */
public class CountDownLatchUtil {

    private CountDownLatch start;
    private CountDownLatch end;
    private int pollSize = 10;

    public CountDownLatchUtil(){
        this(10);
    }

    public CountDownLatchUtil(int pollSize){
        this.pollSize = pollSize;
        start = new CountDownLatch(1);
        end = new CountDownLatch(pollSize);
    }

    public void latch(MyFunctionalInterface functionalInterface) throws InterruptedException{
        ExecutorService executorService = Executors.newFixedThreadPool(pollSize);
        for (int i = 0; i < pollSize ; i++){
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        start.await();
                        functionalInterface.run();
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }finally {
                        //使计数器减一
                        end.countDown();
                    }
                }
            };
            executorService.submit(run);
        }
        //使计数器减一
        start.countDown();
        end.await();
        executorService.shutdown();
    }

    @FunctionalInterface
    public interface MyFunctionalInterface{
        void run();
    }
}

package com.zzr.springboot.controller;

import com.zzr.springboot.util.CountDownLatchUtil;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 开发公司：山东海豚数据技术有限公司
 * 版权：山东海豚数据技术有限公司
 * <p>
 * ConcurrentController
 * 方便测试并发
 * @author zzr
 * @created Create Time: 2019/5/24
 */
@RestController
@RequestMapping("/concurrent")
public class ConcurrentController {
    private Lock lock = new ReentrantLock();
    private static Logger log = LoggerFactory.getLogger(ConcurrentController.class);
    @Autowired
    public RestTemplate restTemplate;

    private int i = 0;

    @RequestMapping("/testConcurrent")
    public String testConcurrent(String number) throws InterruptedException {
        CountDownLatchUtil countDownLatchUtil = new CountDownLatchUtil(Integer.parseInt(number));
        countDownLatchUtil.latch(()->{
            addNum2();
//            String allUrl = "http://47.95.117.206:8667/rabbitmq/send?message="+i;
//            ResponseEntity<String> results = restTemplate.exchange(allUrl, HttpMethod.GET, null, String.class);
//            log.info(results.toString());
        });
        return  "成功";
    }

    public void addNum(){
        if(lock.tryLock()){
            try{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i = i + 1;
                log.info(String.valueOf(System.currentTimeMillis())+"=="+ i);
            }finally {
                lock.unlock();
            }
        }else {
            log.info(String.valueOf(System.currentTimeMillis())+"没有获取");
        }

    }

    public void addNum2(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://47.95.117.206:6379").setClientName("clientName").setDatabase(1);
        RedissonClient redisson = Redisson.create(config);
        RLock redLock = redisson.getLock("REDLOCK_KEY");

        boolean isLock;
        try {
            isLock = redLock.tryLock();
            // 500ms拿不到锁, 就认为获取锁失败。10000ms即10s是锁失效时间。
            //isLock = redLock.tryLock(500, 10000, TimeUnit.MILLISECONDS);
            if(isLock){
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i = i + 1;
                log.info(String.valueOf(System.currentTimeMillis())+"=="+ i);
            }else {
                log.info(String.valueOf(System.currentTimeMillis())+"没有获取");
            }
        }catch (Exception e){

        }finally {
            if(redLock.isHeldByCurrentThread()) {
                redLock.unlock();
                log.info(String.valueOf(System.currentTimeMillis())+"关闭锁");
            }
        }
    }
}

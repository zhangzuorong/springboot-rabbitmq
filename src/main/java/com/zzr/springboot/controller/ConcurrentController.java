package com.zzr.springboot.controller;

import com.zzr.springboot.util.CountDownLatchUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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
            addNum();
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
}

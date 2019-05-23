package com.zzr.springboot.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * 开发公司：山东海豚数据技术有限公司
 * 版权：山东海豚数据技术有限公司
 * <p>
 * QueueConfig
 *
 * @author zzr
 * @created Create Time: 2019/5/21
 */

@Configuration
public class QueueConfig {
    @Bean
    public Queue firstQueue() {
        /**
         durable="true" 持久化 rabbitmq重启的时候不需要创建新的队列
         auto-delete 表示消息队列没有在使用时将被自动删除 默认是false
         exclusive  表示该消息队列是否只在当前connection生效,默认是false
         */
        Map<String,Object> args = new HashMap<>();
        args.put("x-expires",18000);
        return new Queue("springboot-queue-one",false,false,false,args);
    }

    @Bean
    public Queue secondQueue() {
        return new Queue("springboot-queue-two",true,false,false);
    }

    /**
     * 设置springboot-queue-dlx 为死信队列
     * @return
     */
    @Bean
    public Queue dlxQueue(){
        Map<String,Object> args = new HashMap<>();
        args.put("x-message-ttl",10000);
        args.put("x-dead-letter-exchange","springbootDixExchange");//为这个队列添加DLX
        args.put("x-dead-letter-routing-key","springbootmqkey3");
        return new Queue("springboot-queue-dlx",true,false,false,args);
    }

    @Bean
    public Queue thrQueue() {
        return new Queue("springboot-queue-thr",true,false,false);
    }
}

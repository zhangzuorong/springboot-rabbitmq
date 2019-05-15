package com.zzr.springboot.config;


import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 开发公司：山东海豚数据技术有限公司
 * 版权：山东海豚数据技术有限公司
 * <p>
 * ExchangeConfig
 * 交换器
 * @author zzr
 * @created Create Time: 2019/5/10
 */
@Configuration
public class ExchangeConfig {
    //配置路由交换机 根据路由匹配转发消息给队列
    //durable 是否持久化
    //autoDelete：自动删除，如果该队列没有任何订阅的消费者的话，该队列会被自动删除。这种队列适用于临时队列。
    @Bean(name = "exchangeOne")
    public TopicExchange exchangeOne() {
        return new TopicExchange("exchange.one");
    }

    //配置路由交换机
    @Bean(name = "exchangeTwo")
    public TopicExchange exchangeTwo() {
        return new TopicExchange("exchange.two");
    }

    //配置交换机(广播) 转发消息给旗下所有队列
    @Bean(name = "fanoutExchange")
    FanoutExchange fanoutExchange() {
        return new FanoutExchange("fanoutExchange");
    }
}

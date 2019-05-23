package com.zzr.springboot.controller;

import com.zzr.springboot.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 开发公司：山东海豚数据技术有限公司
 * 版权：山东海豚数据技术有限公司
 * <p>
 * SpringBootMqController
 * 测试spring-boot-starter-amqp
 * @author zzr
 * @created Create Time: 2019/5/23
 */
@RestController
@RequestMapping("/rabbitmq")
public class SpringBootMqController {

    @Autowired
    public RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     * @param message  消息
     */
    @GetMapping("/send")
    public void send(String message) {
        String uuid = UUID.randomUUID().toString();
        CorrelationData correlationId = new CorrelationData(uuid);
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.ROUTINGKEY2,
                message, correlationId);
    }

    @RabbitListener(queues = {"springboot-queue-one","springboot-queue-two"}, containerFactory = "rabbitListenerContainerFactory")
    public void handleMessage(String message) throws Exception {
        // 处理消息
        System.out.println("FirstConsumer {} handleMessage :"+message);
    }
}

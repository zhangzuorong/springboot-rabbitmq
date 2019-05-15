package com.zzr.springboot;

import com.rabbitmq.client.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 开发公司：山东海豚数据技术有限公司
 * 版权：山东海豚数据技术有限公司
 * <p>
 * ServiceApplication
 *
 * @author zzr
 * @created Create Time: 2019/5/10
 */
@SpringBootApplication
public class ServiceApplication implements CommandLineRunner {
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        SpringApplication.run(ServiceApplication.class, args);

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("47.95.117.206");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");

        Connection connection = connectionFactory.newConnection();//创建连接
        Channel channel = connection.createChannel();//创建信道

        channel.exchangeDeclare("testExchange","direct",true,false,null);
        channel.queueDeclare("testQueue",true,false,false,null);

        channel.queueBind("testQueue","testExchange","routingkey_demo");

        String msg = "Hello World";
        channel.basicPublish("testExchange","routingkey_demo",
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                msg.getBytes());

        //===================消费================
        Address[] addresses = new Address[]{new Address("47.95.117.206",5672)};
        Connection connectionXiaoFei = connectionFactory.newConnection(addresses);
        final Channel channelXiaoFei = connectionXiaoFei.createChannel();
        channelXiaoFei.basicQos(64);//设置客户端最多接收未被ack的消息个数
        Consumer consumer = new DefaultConsumer(channelXiaoFei){
            @Override
            public void handleDelivery(String consumerTag,Envelope envelope,AMQP.BasicProperties properties,byte[] body) throws IOException {
                System.out.println("messges===="+ new String(body));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                channelXiaoFei.basicAck(envelope.getDeliveryTag(),false);
            }
        };
        channelXiaoFei.basicConsume("testQueue",consumer);

        //关闭资源
        channel.close();
        connection.close();

        TimeUnit.SECONDS.sleep(5);
        channelXiaoFei.close();
        connectionXiaoFei.close();
    }

    @Override
    public void run(String... strings) throws Exception {

    }
}

package com.zzr.springboot.controller;

import com.rabbitmq.client.*;
import com.zzr.springboot.config.ConnectionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * 开发公司：山东海豚数据技术有限公司
 * 版权：山东海豚数据技术有限公司
 * <p>
 * ConnectionController
 *
 * @author zzr
 * @created Create Time: 2019/5/15
 */
@RestController
@RequestMapping("/rabbitmq")
public class ConnectionController {

    @Autowired
    public ConnectionConfig connectionConfig;

    @RequestMapping("/sendMsg")
    public String sendMsg(String str) throws IOException, TimeoutException {

        Connection connection = connectionConfig.getConnectionFactory().newConnection();//创建连接
        Channel channel = connection.createChannel();//创建信道
        channel.exchangeDeclare("testExchange","direct",true,false,null);
        channel.queueDeclare("testQueue",true,false,false,null);

        channel.queueBind("testQueue","testExchange","routingkey_demo");

        String msg = str;
        channel.basicPublish("testExchange","routingkey_demo",
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                msg.getBytes());

        //关闭资源
        channel.close();
        connection.close();
        return "发送成功,交换机为testExchange，队列为testQueue，内容为："+str;
    }

    /**
     * 消费消息
     * @return
     * @throws IOException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    @RequestMapping("/getMsg")
    public String getMsg() throws IOException, TimeoutException, InterruptedException {
        Address[] addresses = new Address[]{new Address("47.95.117.206",5672)};
        Connection connectionXiaoFei = connectionConfig.getConnectionFactory().newConnection(addresses);
        final Channel channelXiaoFei = connectionXiaoFei.createChannel();
        channelXiaoFei.basicQos(64);//设置客户端最多接收未被ack的消息个数
        final String[] result = {""};
        Consumer consumer = new DefaultConsumer(channelXiaoFei){
            @Override
            public void handleDelivery(String consumerTag,Envelope envelope,AMQP.BasicProperties properties,byte[] body) throws IOException {
                result[0] = result[0] + new String(body)+ " ";
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
        TimeUnit.SECONDS.sleep(5);
        channelXiaoFei.close();
        connectionXiaoFei.close();
        return StringUtils.isEmpty(result[0]) ? "无订阅消息" : "消息内容："+result[0];
    }

}

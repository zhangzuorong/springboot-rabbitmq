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
        //创建一个持久化的，非自动删除的，绑定类型为direct的交换器
        /**
         * 参数说明
         * exchange: 交换器的名称
         * type: 交换器类型，eg:fanout direct topic
         * durable: 设置是否持久化 true表示持久化，繁殖为非持久化
         * autoDelete: 是否自动删除
         * internal: 设置是否是内置的。
         * argument: 其它一些结构化参数
         */
        channel.exchangeDeclare("testExchange","direct",true,false,null);
        /**
         * 参数说明
         * queue: 队列的名称
         * durable: 设置是否持久化。 true则设置队列为持久化
         * exclusive: 设置是否排他。 true为排他队列
         * autoDelete: 设置是否自动删除。 true为自动删除
         * arguments: 设置队列的其它一些参数
         */
        //创建一个持久化，非排他，非自动删除的队列
        channel.queueDeclare("testQueue",true,false,false,null);

        /**
         * 参数说明
         * queue: 队列名称
         * exchange: 交换器的名称
         * routingKey: 用来绑定队列和交换器的路由键
         * argument: 定义绑定的一些参数
         */
        //使用路由键将交换器和队列绑定
        channel.queueBind("testQueue","testExchange","routingkey_demo");

        String msg = str;

        /**
         * 参数说明
         * routingKey: 路由键，交换器根据路由键将消息存储到相应的队列中
         * props: 消息的基本属性集
         * byte[] body: 消息体，真正需要发送的消息
         */
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
        boolean autoAck = false;
        channelXiaoFei.basicQos(64);//设置客户端最多接收未被ack的消息个数
        final String[] result = {""};
        /**
         * 回调方法
         */
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
        /**
         * 上面代码中显示地设置autoAck为false,然后再接收到消息之后进行显示ack操作（channel.basicAck）,对于消费者来说这个设置是非常必要的，
         * 可以防止消息不必要的丢失
         *
         * 参数说明
         * queue: 队列的名称
         * autoAck: 设置是否自动确认，建议设置成false,即不自动确认
         * consumerTag: 消费者标签，用来区分多个消费者
         * noLocal: true则表示不能将同一个Connection中生产者发送的消息传递给这个Connection中的消费者
         * exclusive: 设置消费者的其他参数
         * callback: 设置消费者的回调函数，用来处理RabbitMQ推送过来的消息，比如DefaultConsumer
         */
        channelXiaoFei.basicConsume("testQueue",autoAck,consumer);
        TimeUnit.SECONDS.sleep(5);
        channelXiaoFei.close();
        connectionXiaoFei.close();
        return StringUtils.isEmpty(result[0]) ? "无订阅消息" : "消息内容："+result[0];
    }

    /**
     * 删除交换机
     */
    @RequestMapping("/exchangeDelete")
    public String exchangeDelete(String exchangeName) throws IOException, TimeoutException {
        Connection connection = connectionConfig.getConnectionFactory().newConnection();//创建连接
        Channel channel = connection.createChannel();//创建信道
        channel.exchangeDelete(exchangeName,false);
        //关闭资源
        channel.close();
        connection.close();
        return "删除交换机"+ exchangeName +"成功";
    }

    /**
     * 删除队列
     */
    @RequestMapping("/queueDelete")
    public String queueDelete(String queueName) throws IOException, TimeoutException {
        Connection connection = connectionConfig.getConnectionFactory().newConnection();//创建连接
        Channel channel = connection.createChannel();//创建信道
        channel.queueDelete(queueName,false,false);
        //关闭资源
        channel.close();
        connection.close();
        return "删除"+queueName+"成功";
    }

    /**
     * 将已绑定的队列和交换器解绑
     */
    @RequestMapping("/queueUnbind")
    public String queueUnbind(String queueName,String exchange) throws IOException, TimeoutException {
        Connection connection = connectionConfig.getConnectionFactory().newConnection();//创建连接
        Channel channel = connection.createChannel();//创建信道
        channel.queueUnbind(queueName,exchange,"routingkey_demo");
        //关闭资源
        channel.close();
        connection.close();
        return "解除绑定成功";
    }


    /**
     * 测试交换器与交换器之间的绑定
     * @throws IOException
     * @throws TimeoutException
     */
    @RequestMapping("/sendMsgTwo")
    public String sendMsgTwo() throws IOException, TimeoutException {

        Connection connection = connectionConfig.getConnectionFactory().newConnection();//创建连接
        Channel channel = connection.createChannel();//创建信道
        //创建一个持久化的，非自动删除的，绑定类型为direct的交换器
        channel.exchangeDeclare("testExchangeTwo","direct",true,false,null);
        //将testExchangeTwo交换器与testExchange交换器绑定，消息从testExchangeTwo交换器转发到testExchange交换器
        channel.exchangeBind("testExchange","testExchangeTwo","routingkey_demo");
        //创建一个持久化，非排他，非自动删除的队列
        channel.queueDeclare("testQueue",true,false,false,null);
        //使用路由键将交换器和队列绑定
        channel.queueBind("testQueue","testExchange","routingkey_demo");

        /**
         * * arguments: 设置队列的其它一些参数
         * 此处设置了消息的过期时间
         */
        String msg = "Hello World";
        channel.basicPublish("testExchangeTwo","routingkey_demo",
                new AMQP.BasicProperties.Builder().expiration("6000").build(),
                msg.getBytes());

        //关闭资源
        channel.close();
        connection.close();
        return "发送成功,交换机为testExchange，队列为testQueue，内容为："+msg;
    }

}
